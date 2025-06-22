package com.hcmus.ecommerce_backend.order.service.impl;

import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.category.repository.CategoryRepository;
import com.hcmus.ecommerce_backend.common.service.EmailService;
import com.hcmus.ecommerce_backend.order.exception.InsufficientInventoryException;
import com.hcmus.ecommerce_backend.order.exception.OrderNotFoundException;
import com.hcmus.ecommerce_backend.order.model.InventoryItem;
import com.hcmus.ecommerce_backend.order.model.dto.request.*;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderDetailWithProductResponse;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderResponse;
import com.hcmus.ecommerce_backend.order.model.entity.Order;
import com.hcmus.ecommerce_backend.order.model.enums.PaymentMethod;
import com.hcmus.ecommerce_backend.order.model.enums.Status;
import com.hcmus.ecommerce_backend.order.model.mapper.OrderMapper;
import com.hcmus.ecommerce_backend.order.repository.OrderRepository;
import com.hcmus.ecommerce_backend.order.service.OrderDetailService;
import com.hcmus.ecommerce_backend.order.service.OrderService;
import com.hcmus.ecommerce_backend.order.service.OrderTrackService;
import com.hcmus.ecommerce_backend.product.exception.ProductColorSizeNotFoundException;
import com.hcmus.ecommerce_backend.product.model.entity.Product;
import com.hcmus.ecommerce_backend.product.model.entity.ProductColorSize;
import com.hcmus.ecommerce_backend.product.repository.CartItemRepository;
import com.hcmus.ecommerce_backend.product.repository.ProductColorSizeRepository;
import com.hcmus.ecommerce_backend.product.repository.ProductRepository;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderDetailService orderDetailService;
    private final OrderTrackService orderTrackService;
    private final ProductColorSizeRepository productColorSizeRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        log.info("OrderServiceImpl | checkout | Processing checkout for customer: {}", request.getCustomerId());
        try {
            // 1. Validate inventory and prepare data before any modifications
            List<InventoryItem> inventoryItems = validateInventory(request.getOrderDetails());

            // 2. Create the order using mapper
            Order order = orderMapper.toEntity(CreateOrderRequest.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phoneNumber(request.getPhoneNumber())
                    .paymentMethod(request.getPaymentMethod())
                    .customerId(request.getCustomerId())
                    .address(request.getAddress())
                    .shippingCost(10.0) // Default shipping cost
                    .build());

            // Set fields not handled by mapper
            order.setStatus(Status.NEW);
            order.setIsPaid(request.getPaymentMethod() != PaymentMethod.COD);

            // Save the order to get an ID
            Order savedOrder = orderRepository.save(order);
            log.info("OrderServiceImpl | checkout | Created order with id: {}", savedOrder.getId());

            // 3. Process order details and update inventory
            List<CreateOrderDetailRequest> orderDetailRequests =
                    processOrderDetailsAndUpdateInventory(inventoryItems, savedOrder.getId());

            // Calculate totals from processed details
            double productCost = orderDetailRequests.stream().mapToDouble(CreateOrderDetailRequest::getProductCost).sum();
            double subTotal = orderDetailRequests.stream().mapToDouble(CreateOrderDetailRequest::getTotal).sum();
            double total = subTotal + savedOrder.getShippingCost();

            // Add order details
            orderDetailService.addOrderDetails(orderDetailRequests);

            // 4. Update order with calculated totals
            savedOrder.setProductCost(productCost);
            savedOrder.setSubTotal(subTotal);
            savedOrder.setTotal(total);
            Order updatedOrder = orderRepository.save(savedOrder);

            // 5. Create initial order track
            orderTrackService.createOrderTrack(CreateOrderTrackRequest.builder()
                    .notes("Order placed")
                    .status(Status.NEW)
                    .orderId(savedOrder.getId())
                    .build());

            // 6. Remove items from customer's cart
            removeItemsFromCart(request.getCustomerId(),
                    request.getOrderDetails().stream()
                          .map(CheckoutOrderDetailRequest::getItemId)
                          .collect(Collectors.toList()));

            // 7. Send order confirmation email
            sendOrderConfirmationEmailAfterCheckout(updatedOrder);

            log.info("OrderServiceImpl | checkout | Completed checkout for order: {}", savedOrder.getId());

            return orderMapper.toResponse(updatedOrder);
        } catch (Exception e) {
            log.error("OrderServiceImpl | checkout | Error during checkout: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Sends order confirmation email after successful checkout
     */
    private void sendOrderConfirmationEmailAfterCheckout(Order order) {
        try {
            Optional<User> userOptional = userRepository.findById(order.getCustomerId());
            if (userOptional.isEmpty()) {
                log.warn("OrderServiceImpl | sendOrderConfirmationEmailAfterCheckout | User not found for order: {}", order.getId());
                return;
            }

            User user = userOptional.get();

            // Get order details information for email
            List<OrderDetailWithProductResponse> orderDetails = orderDetailService.getOrderDetailsWithProductByOrderId(order.getId());

            // Send order confirmation email
            emailService.sendOrderConfirmationEmail(
                    user.getEmail(),
                    user.getFirstName() + " " + user.getLastName(),
                    order.getId(),
                    order.getTotal(),
                    orderDetails,
                    order.getAddress(),
                    order.getSubTotal(),
                    order.getShippingCost(),
                    order.getPaymentMethod(),
                    order.getCreatedAt()
            );

            log.info("OrderServiceImpl | sendOrderConfirmationEmailAfterCheckout | Order confirmation email sent for order: {}", order.getId());
        } catch (Exception e) {
            // Log error but don't fail the checkout process if email sending fails
            log.error("OrderServiceImpl | sendOrderConfirmationEmailAfterCheckout | Error sending confirmation email: {}", e.getMessage(), e);
        }
    }

    /**
     * Remove items from the customer's cart after successful checkout
     */
    private void removeItemsFromCart(String customerId, List<String> itemIds) {
        if (customerId == null || itemIds == null || itemIds.isEmpty()) {
            return;
        }

        log.info("OrderServiceImpl | removeItemsFromCart | Removing {} items from cart for customer: {}",
                itemIds.size(), customerId);

        try {
            for (String itemId : itemIds) {
                cartItemRepository.deleteByUserIdAndItemId(customerId, itemId);
            }
            log.info("OrderServiceImpl | removeItemsFromCart | Successfully removed {} items from cart", itemIds.size());
        } catch (Exception e) {
            // Log error but don't fail the checkout process if cart cleanup fails
            log.error("OrderServiceImpl | removeItemsFromCart | Error removing items from cart: {}", e.getMessage(), e);
        }
    }

    private List<InventoryItem> validateInventory(List<CheckoutOrderDetailRequest> detailRequests) {
        log.info("OrderServiceImpl | validateInventory | Validating inventory for {} items", detailRequests.size());
        List<InventoryItem> items = new ArrayList<>();

        for (CheckoutOrderDetailRequest detailRequest : detailRequests) {
            // Find product item with pessimistic lock to prevent concurrent modifications
            ProductColorSize productItem = productColorSizeRepository.findByIdWithLock(detailRequest.getItemId())
                .orElseThrow(() -> new ProductColorSizeNotFoundException(detailRequest.getItemId()));

            Product product = productItem.getProduct();

            // Check if we have enough stock
            if (productItem.getQuantity() < detailRequest.getQuantity()) {
                log.error("OrderServiceImpl | validateInventory | Insufficient inventory for item: {}, requested: {}, available: {}",
                        detailRequest.getItemId(), detailRequest.getQuantity(), productItem.getQuantity());
                throw new InsufficientInventoryException(
                        "Insufficient inventory for product: " + product.getName() +
                        " (requested: " + detailRequest.getQuantity() +
                        ", available: " + productItem.getQuantity() + ")");
            }

            items.add(new InventoryItem(
                    productItem,
                    product,
                    product.getCategory(),
                    detailRequest.getQuantity(),
                    product.getPrice(),
                    product.getCost()
            ));
        }

        return items;
    }

    /**
     * Processes order details and updates inventory levels across the system
     */
    private List<CreateOrderDetailRequest> processOrderDetailsAndUpdateInventory(
            List<InventoryItem> inventoryItems, String orderId) {
        log.info("OrderServiceImpl | processOrderDetailsAndUpdateInventory | Processing {} items for order {}",
                inventoryItems.size(), orderId);

        List<CreateOrderDetailRequest> orderDetailRequests = new ArrayList<>();

        for (InventoryItem item : inventoryItems) {
            ProductColorSize productItem = item.getProductItem();
            Product product = item.getProduct();
            Category category = item.getCategory();
            int quantity = item.getQuantity();

            // 1. Update ProductColorSize inventory
            int newQuantity = productItem.getQuantity() - quantity;
            productItem.setQuantity(newQuantity);
            productColorSizeRepository.save(productItem);
            log.debug("OrderServiceImpl | processOrderDetailsAndUpdateInventory | Updated item {} quantity to {}",
                    productItem.getId(), newQuantity);

            // 2. Update Product total and inStock flag
            int newTotal = product.getTotal() - quantity;
            product.setTotal(newTotal);
            if (newTotal <= 0) {
                product.setInStock(false);
                product.setTotal(0); // Ensure total is never negative
            }
            productRepository.save(product);
            log.debug("OrderServiceImpl | processOrderDetailsAndUpdateInventory | Updated product {} total to {}",
                    product.getId(), newTotal);

            // 3. Update Category stock
            int newCategoryStock = category.getStock() - quantity;
            category.setStock(Math.max(0, newCategoryStock)); // Ensure stock is never negative
            categoryRepository.save(category);
            log.debug("OrderServiceImpl | processOrderDetailsAndUpdateInventory | Updated category {} stock to {}",
                    category.getId(), newCategoryStock);

            // Calculate costs for the order detail
            double itemProductCost = item.getCost() * quantity;
            double itemTotal = item.getUnitPrice() * quantity;

            // Create order detail request
            orderDetailRequests.add(CreateOrderDetailRequest.builder()
                    .productCost(itemProductCost)
                    .quantity(quantity)
                    .unitPrice(item.getUnitPrice())
                    .total(itemTotal)
                    .itemId(productItem.getId())
                    .orderId(orderId)
                    .build());
        }

        return orderDetailRequests;
    }

    @Override
    public Page<OrderResponse> getAllOrders(String keyword, Status status, PaymentMethod paymentMethod, 
                                        String customerId, Pageable pageable) {
        log.info("OrderServiceImpl | getAllOrders | Retrieving orders with filters - keyword: {}, status: {}, " +
                "paymentMethod: {}, customerId: {}, page: {}, size: {}, sort: {}",
                keyword, status, paymentMethod, customerId, pageable.getPageNumber(), pageable.getPageSize(), 
                pageable.getSort());
        
        try {
            // Nếu tất cả các tham số filter đều null, sử dụng findAll
            Page<Order> orderPage;
            if ((keyword == null || keyword.trim().isEmpty()) && 
                    status == null && 
                    paymentMethod == null && 
                    customerId == null) {
                orderPage = orderRepository.findAll(pageable);
            } else {
                orderPage = orderRepository.findOrdersWithFilters(
                        keyword == null || keyword.trim().isEmpty() ? null : keyword.trim(),
                        status,
                        paymentMethod,
                        customerId,
                        pageable);
            }

            Page<OrderResponse> orderResponsePage = orderPage.map(orderMapper::toResponse);

            log.info("OrderServiceImpl | getAllOrders | Found {} orders on page {} of {}",
                    orderResponsePage.getNumberOfElements(),
                    orderResponsePage.getNumber() + 1,
                    orderResponsePage.getTotalPages());

            return orderResponsePage;
        } catch (DataAccessException e) {
            log.error("OrderServiceImpl | getAllOrders | Database error retrieving paginated orders: {}",
                    e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderServiceImpl | getAllOrders | Unexpected error retrieving paginated orders: {}",
                    e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Page<OrderResponse> searchOrders(String keyword, Status status, Pageable pageable) {
        log.info("OrderServiceImpl | searchOrders | keyword: {}, status: {}, page: {}, size: {}, sort: {}",
                keyword, status, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            // If both search parameters are null, use the standard findAll method
            Page<Order> orderPage;
            if ((keyword == null || keyword.trim()
                    .isEmpty()) && status == null) {
                orderPage = orderRepository.findAll(pageable);
            } else {
                orderPage = orderRepository.searchOrders(
                        keyword == null || keyword.trim()
                                .isEmpty() ? null : keyword.trim(),
                        status,
                        pageable);
            }

            Page<OrderResponse> orderResponsePage = orderPage.map(orderMapper::toResponse);

            log.info("OrderServiceImpl | searchOrders | Found {} orders on page {} of {}",
                    orderResponsePage.getNumberOfElements(),
                    orderResponsePage.getNumber() + 1,
                    orderResponsePage.getTotalPages());

            return orderResponsePage;
        } catch (DataAccessException e) {
            log.error("OrderServiceImpl | searchOrders | Database error searching orders: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderServiceImpl | searchOrders | Unexpected error searching orders: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public OrderResponse getOrderById(String id) {
        log.info("OrderServiceImpl | getOrderById | id: {}", id);
        try {
            Order order = findOrderById(id);
            log.info("OrderServiceImpl | getOrderById | Order found with id: {}", order.getId());
            return orderMapper.toResponse(order);
        } catch (OrderNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("OrderServiceImpl | getOrderById | Database error for id {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderServiceImpl | getOrderById | Unexpected error for id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<OrderResponse> getOrdersByCustomerId(String customerId) {
        log.info("OrderServiceImpl | getOrdersByCustomerId | customerId: {}", customerId);
        try {
            List<OrderResponse> orders = orderRepository.findByCustomerId(customerId)
                    .stream()
                    .map(orderMapper::toResponse)
                    .collect(Collectors.toList());
            log.info("OrderServiceImpl | getOrdersByCustomerId | Found {} orders for customer {}", orders.size(),
                    customerId);
            return orders;
        } catch (DataAccessException e) {
            log.error("OrderServiceImpl | getOrdersByCustomerId | Database error for customerId {}: {}", customerId,
                    e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("OrderServiceImpl | getOrdersByCustomerId | Unexpected error for customerId {}: {}", customerId,
                    e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("OrderServiceImpl | createOrder | Creating order for customer: {}", request.getCustomerId());
        try {
            Order order = orderMapper.toEntity(request);

            Order savedOrder = orderRepository.save(order);
            log.info("OrderServiceImpl | createOrder | Created order with id: {}", savedOrder.getId());
            return orderMapper.toResponse(savedOrder);
        } catch (DataAccessException e) {
            log.error("OrderServiceImpl | createOrder | Database error creating order for customer {}: {}",
                    request.getCustomerId(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderServiceImpl | createOrder | Unexpected error creating order for customer {}: {}",
                    request.getCustomerId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(String id, UpdateOrderRequest request) {
        log.info("OrderServiceImpl | updateOrder | Updating order with id: {}", id);
        try {
            Order order = findOrderById(id);

            orderMapper.updateEntity(request, order);
            Order updatedOrder = orderRepository.save(order);
            log.info("OrderServiceImpl | updateOrder | Updated order with id: {}", updatedOrder.getId());
            return orderMapper.toResponse(updatedOrder);
        } catch (OrderNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("OrderServiceImpl | updateOrder | Database error updating order with id '{}': {}",
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderServiceImpl | updateOrder | Unexpected error updating order with id '{}': {}",
                    id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteOrder(String id) {
        log.info("OrderServiceImpl | deleteOrder | Deleting order with id: {}", id);
        try {
            // Check existence first in a separate transaction
            if (!doesOrderExistById(id)) {
                log.error("OrderServiceImpl | deleteOrder | Order not found with id: {}", id);
                throw new OrderNotFoundException(id);
            }

            // Then delete in the current transaction
            orderRepository.deleteById(id);
            log.info("OrderServiceImpl | deleteOrder | Deleted order with id: {}", id);
        } catch (OrderNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("OrderServiceImpl | deleteOrder | Database error deleting order with id '{}': {}",
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderServiceImpl | deleteOrder | Unexpected error deleting order with id '{}': {}",
                    id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Helper method to find an order by ID.
     * Uses a separate transaction to avoid issues with the main transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    protected Order findOrderById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("OrderServiceImpl | findOrderById | Order not found with id: {}", id);
                    return new OrderNotFoundException(id);
                });
    }

    /**
     * Helper method to check if an order exists by ID.
     * Uses a separate transaction to avoid issues with the main transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    protected boolean doesOrderExistById(String id) {
        return orderRepository.existsById(id);
    }
}