package com.hcmus.ecommerce_backend.unit.order.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.category.repository.CategoryRepository;
import com.hcmus.ecommerce_backend.common.service.EmailService;
import com.hcmus.ecommerce_backend.order.exception.InsufficientInventoryException;
import com.hcmus.ecommerce_backend.order.exception.OrderNotFoundException;
import com.hcmus.ecommerce_backend.order.model.dto.request.*;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderResponse;
import com.hcmus.ecommerce_backend.order.model.entity.Order;
import com.hcmus.ecommerce_backend.order.model.enums.PaymentMethod;
import com.hcmus.ecommerce_backend.order.model.enums.Status;
import com.hcmus.ecommerce_backend.order.model.mapper.OrderMapper;
import com.hcmus.ecommerce_backend.order.repository.OrderRepository;
import com.hcmus.ecommerce_backend.order.service.OrderDetailService;
import com.hcmus.ecommerce_backend.order.service.OrderTrackService;
import com.hcmus.ecommerce_backend.order.service.impl.OrderServiceImpl;
import com.hcmus.ecommerce_backend.product.exception.ProductColorSizeNotFoundException;
import com.hcmus.ecommerce_backend.product.model.entity.Product;
import com.hcmus.ecommerce_backend.product.model.entity.ProductColorSize;
import com.hcmus.ecommerce_backend.product.repository.CartItemRepository;
import com.hcmus.ecommerce_backend.product.repository.ProductColorSizeRepository;
import com.hcmus.ecommerce_backend.product.repository.ProductRepository;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderDetailService orderDetailService;
    @Mock
    private OrderTrackService orderTrackService;
    @Mock
    private ProductColorSizeRepository productColorSizeRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private OrderResponse orderResponse;
    private CheckoutRequest checkoutRequest;
    private CreateOrderRequest createOrderRequest;
    private UpdateOrderRequest updateOrderRequest;
    private ProductColorSize productColorSize;
    private Product product;
    private Category category;
    private User user;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Setup Category
        category = new Category();
        category.setId("cat-1");
        category.setStock(100);

        // Setup Product
        product = new Product();
        product.setId("product-1");
        product.setName("Test Product");
        product.setPrice(25.0);
        product.setCost(15.0);
        product.setTotal(50);
        product.setInStock(true);
        product.setCategory(category);

        // Setup ProductColorSize
        productColorSize = new ProductColorSize();
        productColorSize.setId("item-1");
        productColorSize.setQuantity(10);
        productColorSize.setProduct(product);

        // Setup Order
        order = new Order();
        order.setId("order-1");
        order.setCustomerId("customer-1");
        order.setFirstName("John");
        order.setLastName("Doe");
        order.setPhoneNumber("123456789");
        order.setAddress("123 Test St");
        order.setStatus(Status.NEW);
        order.setPaymentMethod(PaymentMethod.COD);
        order.setIsPaid(true);
        order.setShippingCost(10.0);
        order.setProductCost(15.0);
        order.setSubTotal(25.0);
        order.setTotal(35.0);
        order.setCreatedAt(LocalDateTime.now());

        // Setup OrderResponse
        orderResponse = OrderResponse.builder()
                .id("order-1")
                .customerId("customer-1")
                .firstName("John")
                .lastName("Doe")
                .total(35.0)
                .build();

        // Setup User
        user = new User();
        user.setId("customer-1");
        user.setEmail("john.doe@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");

        // Setup requests
        CheckoutOrderDetailRequest detailRequest = CheckoutOrderDetailRequest.builder()
                .itemId("item-1")
                .quantity(2)
                .build();

        checkoutRequest = CheckoutRequest.builder()
                .customerId("customer-1")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("123456789")
                .address("123 Test St")
                .paymentMethod(PaymentMethod.COD)
                .orderDetails(Arrays.asList(detailRequest))
                .build();

        createOrderRequest = CreateOrderRequest.builder()
                .customerId("customer-1")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("123456789")
                .address("123 Test St")
                .paymentMethod(PaymentMethod.COD)
                .shippingCost(10.0)
                .build();

        updateOrderRequest = UpdateOrderRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("987654321")
                .address("456 New St")
                .status(Status.PROCESSING)
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void checkout_Success() {
        // Given
        when(productColorSizeRepository.findByIdWithLock("item-1")).thenReturn(Optional.of(productColorSize));
        when(orderMapper.toEntity(any(CreateOrderRequest.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderDetailService.addOrderDetails(anyList())).thenReturn(Collections.emptyList());
        when(orderTrackService.createOrderTrack(any(CreateOrderTrackRequest.class))).thenReturn(null);
        when(userRepository.findById("customer-1")).thenReturn(Optional.of(user));
        when(orderDetailService.getOrderDetailsWithProductByOrderId("order-1")).thenReturn(Collections.emptyList());
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // When
        OrderResponse result = orderService.checkout(checkoutRequest);

        // Then
        assertNotNull(result);
        assertEquals(orderResponse, result);
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(orderDetailService).addOrderDetails(anyList());
        verify(orderTrackService).createOrderTrack(any(CreateOrderTrackRequest.class));
        verify(emailService).sendOrderConfirmationEmail(anyString(), anyString(), anyString(), 
                anyDouble(), anyList(), anyString(), anyDouble(), anyDouble(), any(PaymentMethod.class), any(LocalDateTime.class));
        verify(cartItemRepository).deleteByUserIdAndItemId("customer-1", "item-1");
    }

    @Test
    void checkout_InsufficientInventory() {
        // Given
        productColorSize.setQuantity(1); // Less than requested quantity (2)
        when(productColorSizeRepository.findByIdWithLock("item-1")).thenReturn(Optional.of(productColorSize));

        // When & Then
        assertThrows(InsufficientInventoryException.class, () -> orderService.checkout(checkoutRequest));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void checkout_ProductNotFound() {
        // Given
        when(productColorSizeRepository.findByIdWithLock("item-1")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductColorSizeNotFoundException.class, () -> orderService.checkout(checkoutRequest));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void checkout_CODPayment() {
        // Given
        checkoutRequest.setPaymentMethod(PaymentMethod.COD);
        when(productColorSizeRepository.findByIdWithLock("item-1")).thenReturn(Optional.of(productColorSize));
        when(orderMapper.toEntity(any(CreateOrderRequest.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderDetailService.addOrderDetails(anyList())).thenReturn(Collections.emptyList());
        when(orderTrackService.createOrderTrack(any(CreateOrderTrackRequest.class))).thenReturn(null);
        when(userRepository.findById("customer-1")).thenReturn(Optional.of(user));
        when(orderDetailService.getOrderDetailsWithProductByOrderId("order-1")).thenReturn(Collections.emptyList());
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // When
        OrderResponse result = orderService.checkout(checkoutRequest);

        // Then
        assertNotNull(result);
        verify(orderRepository, times(2)).save(any(Order.class));
    }

    @Test
    void checkout_EmailServiceFails() {
        // Given
        when(productColorSizeRepository.findByIdWithLock("item-1")).thenReturn(Optional.of(productColorSize));
        when(orderMapper.toEntity(any(CreateOrderRequest.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderDetailService.addOrderDetails(anyList())).thenReturn(Collections.emptyList());
        when(orderTrackService.createOrderTrack(any(CreateOrderTrackRequest.class))).thenReturn(null);
        when(userRepository.findById("customer-1")).thenReturn(Optional.of(user));
        when(orderDetailService.getOrderDetailsWithProductByOrderId("order-1")).thenReturn(Collections.emptyList());
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);
        doThrow(new RuntimeException("Email service error")).when(emailService)
                .sendOrderConfirmationEmail(anyString(), anyString(), anyString(), anyDouble(), anyList(), 
                        anyString(), anyDouble(), anyDouble(), any(PaymentMethod.class), any(LocalDateTime.class));

        // When
        OrderResponse result = orderService.checkout(checkoutRequest);

        // Then - Should complete successfully despite email failure
        assertNotNull(result);
        verify(emailService).sendOrderConfirmationEmail(anyString(), anyString(), anyString(), 
                anyDouble(), anyList(), anyString(), anyDouble(), anyDouble(), any(PaymentMethod.class), any(LocalDateTime.class));
    }

    @Test
    void getAllOrders_WithoutFilters() {
        // Given
        List<Order> orders = Arrays.asList(order);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(orderPage);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // When
        Page<OrderResponse> result = orderService.getAllOrders(null, null, null, null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(orderResponse, result.getContent().get(0));
        verify(orderRepository).findAll(pageable);
    }

    @Test
    void getAllOrders_WithFilters() {
        // Given
        List<Order> orders = Arrays.asList(order);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);
        when(orderRepository.findOrdersWithFilters("test", Status.NEW, PaymentMethod.COD, "customer-1", pageable))
                .thenReturn(orderPage);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // When
        Page<OrderResponse> result = orderService.getAllOrders("test", Status.NEW, PaymentMethod.COD, "customer-1", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository).findOrdersWithFilters("test", Status.NEW, PaymentMethod.COD, "customer-1", pageable);
    }

    @Test
    void getAllOrders_DatabaseError() {
        // Given
        when(orderRepository.findAll(pageable)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> orderService.getAllOrders(null, null, null, null, pageable));
    }

    @Test
    void searchOrders_WithoutFilters() {
        // Given
        List<Order> orders = Arrays.asList(order);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(orderPage);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // When
        Page<OrderResponse> result = orderService.searchOrders(null, null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository).findAll(pageable);
    }

    @Test
    void searchOrders_WithFilters() {
        // Given
        List<Order> orders = Arrays.asList(order);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);
        when(orderRepository.searchOrders("test", Status.NEW, pageable)).thenReturn(orderPage);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // When
        Page<OrderResponse> result = orderService.searchOrders("test", Status.NEW, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository).searchOrders("test", Status.NEW, pageable);
    }

    @Test
    void getOrderById_Success() {
        // Given
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // When
        OrderResponse result = orderService.getOrderById("order-1");

        // Then
        assertNotNull(result);
        assertEquals(orderResponse, result);
        verify(orderRepository).findById("order-1");
    }

    @Test
    void getOrderById_NotFound() {
        // Given
        when(orderRepository.findById("non-existent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById("non-existent"));
    }

    @Test
    void getOrdersByCustomerId_Success() {
        // Given
        List<Order> orders = Arrays.asList(order);
        when(orderRepository.findByCustomerId("customer-1")).thenReturn(orders);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // When
        List<OrderResponse> result = orderService.getOrdersByCustomerId("customer-1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(orderResponse, result.get(0));
        verify(orderRepository).findByCustomerId("customer-1");
    }

    @Test
    void getOrdersByCustomerId_DatabaseError() {
        // Given
        when(orderRepository.findByCustomerId("customer-1"))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When
        List<OrderResponse> result = orderService.getOrdersByCustomerId("customer-1");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository).findByCustomerId("customer-1");
    }

    @Test
    void createOrder_Success() {
        // Given
        when(orderMapper.toEntity(createOrderRequest)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // When
        OrderResponse result = orderService.createOrder(createOrderRequest);

        // Then
        assertNotNull(result);
        assertEquals(orderResponse, result);
        verify(orderMapper).toEntity(createOrderRequest);
        verify(orderRepository).save(order);
    }

    @Test
    void createOrder_DatabaseError() {
        // Given
        when(orderMapper.toEntity(createOrderRequest)).thenReturn(order);
        when(orderRepository.save(order)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> orderService.createOrder(createOrderRequest));
    }

    @Test
    void updateOrder_Success() {
        // Given
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // When
        OrderResponse result = orderService.updateOrder("order-1", updateOrderRequest);

        // Then
        assertNotNull(result);
        assertEquals(orderResponse, result);
        verify(orderMapper).updateEntity(updateOrderRequest, order);
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrder_NotFound() {
        // Given
        when(orderRepository.findById("non-existent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderNotFoundException.class, () -> orderService.updateOrder("non-existent", updateOrderRequest));
    }

    @Test
    void deleteOrder_Success() {
        // Given
        when(orderRepository.existsById("order-1")).thenReturn(true);

        // When
        assertDoesNotThrow(() -> orderService.deleteOrder("order-1"));

        // Then
        verify(orderRepository).existsById("order-1");
        verify(orderRepository).deleteById("order-1");
    }

    @Test
    void deleteOrder_NotFound() {
        // Given
        when(orderRepository.existsById("non-existent")).thenReturn(false);

        // When & Then
        assertThrows(OrderNotFoundException.class, () -> orderService.deleteOrder("non-existent"));
        verify(orderRepository, never()).deleteById(anyString());
    }

    @Test
    void deleteOrder_DatabaseError() {
        // Given
        when(orderRepository.existsById("order-1")).thenReturn(true);
        doThrow(new DataIntegrityViolationException("Database error")).when(orderRepository).deleteById("order-1");

        // When & Then
        assertThrows(DataAccessException.class, () -> orderService.deleteOrder("order-1"));
    }

    @Test
    void checkout_InventoryUpdatesCorrectly() {
        // Given
        when(productColorSizeRepository.findByIdWithLock("item-1")).thenReturn(Optional.of(productColorSize));
        when(orderMapper.toEntity(any(CreateOrderRequest.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderDetailService.addOrderDetails(anyList())).thenReturn(Collections.emptyList());
        when(orderTrackService.createOrderTrack(any(CreateOrderTrackRequest.class))).thenReturn(null);
        when(userRepository.findById("customer-1")).thenReturn(Optional.of(user));
        when(orderDetailService.getOrderDetailsWithProductByOrderId("order-1")).thenReturn(Collections.emptyList());
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // When
        orderService.checkout(checkoutRequest);

        // Then
        verify(productColorSizeRepository).save(productColorSize);
        verify(productRepository).save(product);
        verify(categoryRepository).save(category);
        assertEquals(8, productColorSize.getQuantity()); // 10 - 2
        assertEquals(48, product.getTotal()); // 50 - 2
        assertEquals(98, category.getStock()); // 100 - 2
    }

    @Test
    void checkout_ProductOutOfStock() {
        // Given
        product.setTotal(2);
        when(productColorSizeRepository.findByIdWithLock("item-1")).thenReturn(Optional.of(productColorSize));
        when(orderMapper.toEntity(any(CreateOrderRequest.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderDetailService.addOrderDetails(anyList())).thenReturn(Collections.emptyList());
        when(orderTrackService.createOrderTrack(any(CreateOrderTrackRequest.class))).thenReturn(null);
        when(userRepository.findById("customer-1")).thenReturn(Optional.of(user));
        when(orderDetailService.getOrderDetailsWithProductByOrderId("order-1")).thenReturn(Collections.emptyList());
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // When
        orderService.checkout(checkoutRequest);

        // Then
        assertFalse(product.isInStock());
        assertEquals(0, product.getTotal());
    }

    @Test
    void checkout_UserNotFoundForEmail() {
        // Given
        when(productColorSizeRepository.findByIdWithLock("item-1")).thenReturn(Optional.of(productColorSize));
        when(orderMapper.toEntity(any(CreateOrderRequest.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderDetailService.addOrderDetails(anyList())).thenReturn(Collections.emptyList());
        when(orderTrackService.createOrderTrack(any(CreateOrderTrackRequest.class))).thenReturn(null);
        when(userRepository.findById("customer-1")).thenReturn(Optional.empty());
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // When
        OrderResponse result = orderService.checkout(checkoutRequest);

        // Then
        assertNotNull(result);
        verify(emailService, never()).sendOrderConfirmationEmail(anyString(), anyString(), anyString(), 
                anyDouble(), anyList(), anyString(), anyDouble(), anyDouble(), any(PaymentMethod.class), any(LocalDateTime.class));
    }

    @Test
    void checkout_CartCleanupFails() {
        // Given
        when(productColorSizeRepository.findByIdWithLock("item-1")).thenReturn(Optional.of(productColorSize));
        when(orderMapper.toEntity(any(CreateOrderRequest.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderDetailService.addOrderDetails(anyList())).thenReturn(Collections.emptyList());
        when(orderTrackService.createOrderTrack(any(CreateOrderTrackRequest.class))).thenReturn(null);
        when(userRepository.findById("customer-1")).thenReturn(Optional.of(user));
        when(orderDetailService.getOrderDetailsWithProductByOrderId("order-1")).thenReturn(Collections.emptyList());
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);
        doThrow(new RuntimeException("Cart cleanup error")).when(cartItemRepository)
                .deleteByUserIdAndItemId("customer-1", "item-1");

        // When
        OrderResponse result = orderService.checkout(checkoutRequest);

        // Then - Should complete successfully despite cart cleanup failure
        assertNotNull(result);
        verify(cartItemRepository).deleteByUserIdAndItemId("customer-1", "item-1");
    }

    @Test
    void checkout_EmptyOrderDetails() {
        // Given
        checkoutRequest.setOrderDetails(Collections.emptyList());
        when(orderMapper.toEntity(any(CreateOrderRequest.class))).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderDetailService.addOrderDetails(anyList())).thenReturn(Collections.emptyList());
        when(orderTrackService.createOrderTrack(any(CreateOrderTrackRequest.class))).thenReturn(null);
        when(userRepository.findById("customer-1")).thenReturn(Optional.of(user));
        when(orderDetailService.getOrderDetailsWithProductByOrderId("order-1")).thenReturn(Collections.emptyList());
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // When
        OrderResponse result = orderService.checkout(checkoutRequest);

        // Then
        assertNotNull(result);
        verify(productColorSizeRepository, never()).findByIdWithLock(anyString());
    }

    @Test
    void getAllOrders_EmptyKeyword() {
        // Given
        List<Order> orders = Arrays.asList(order);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(orderPage);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // When
        Page<OrderResponse> result = orderService.getAllOrders("", null, null, null, pageable);

        // Then
        assertNotNull(result);
        verify(orderRepository).findAll(pageable);
    }

    @Test
    void searchOrders_EmptyKeyword() {
        // Given
        List<Order> orders = Arrays.asList(order);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(orderPage);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // When
        Page<OrderResponse> result = orderService.searchOrders("  ", null, pageable);

        // Then
        assertNotNull(result);
        verify(orderRepository).findAll(pageable);
    }
}
