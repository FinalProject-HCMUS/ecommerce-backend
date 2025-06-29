package com.hcmus.ecommerce_backend.common.service.impl;

import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.category.repository.CategoryRepository;
import com.hcmus.ecommerce_backend.common.exception.PaymentException;
import com.hcmus.ecommerce_backend.common.model.entity.SystemSetting;
import com.hcmus.ecommerce_backend.common.model.enums.VNPayKeys;
import com.hcmus.ecommerce_backend.common.repository.SystemSettingRepository;
import com.hcmus.ecommerce_backend.common.service.PaymentService;
import com.hcmus.ecommerce_backend.order.exception.InsufficientInventoryException;
import com.hcmus.ecommerce_backend.order.exception.OrderNotFoundException;
import com.hcmus.ecommerce_backend.order.model.InventoryItem;
import com.hcmus.ecommerce_backend.order.model.dto.request.CheckoutOrderDetailRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.CheckoutRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderDetailRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderTrackRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderResponse;
import com.hcmus.ecommerce_backend.order.model.entity.Order;
import com.hcmus.ecommerce_backend.order.model.enums.PaymentMethod;
import com.hcmus.ecommerce_backend.order.model.enums.Status;
import com.hcmus.ecommerce_backend.order.model.mapper.OrderMapper;
import com.hcmus.ecommerce_backend.order.repository.OrderRepository;
import com.hcmus.ecommerce_backend.order.service.OrderDetailService;
import com.hcmus.ecommerce_backend.order.service.OrderTrackService;
import com.hcmus.ecommerce_backend.product.exception.ProductColorSizeNotFoundException;
import com.hcmus.ecommerce_backend.product.model.entity.Product;
import com.hcmus.ecommerce_backend.product.model.entity.ProductColorSize;
import com.hcmus.ecommerce_backend.product.repository.CartItemRepository;
import com.hcmus.ecommerce_backend.product.repository.ProductColorSizeRepository;
import com.hcmus.ecommerce_backend.product.repository.ProductRepository;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayPaymentServiceImpl implements PaymentService {

    private final SystemSettingRepository systemSettingRepository;
    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final OrderDetailService orderDetailService;
    private final ProductColorSizeRepository productColorSizeRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderTrackService orderTrackService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private final CacheManager cacheManager;

    private String tmnCode;
    private String hashSecret;
    private String url;
    private String returnUrl;

    private void loadVNPayConfig() {
        if (tmnCode == null || hashSecret == null || url == null || returnUrl == null) {
            SystemSetting tmnCodeSetting = systemSettingRepository.findByKey(VNPayKeys.VNP_TMNCODE.name()).orElse(null);
            SystemSetting hashSecretSetting = systemSettingRepository.findByKey(VNPayKeys.VNP_HASHSECRET.name()).orElse(null);
            SystemSetting urlSetting = systemSettingRepository.findByKey(VNPayKeys.VNP_URL.name()).orElse(null);
            SystemSetting returnUrlSetting = systemSettingRepository.findByKey(VNPayKeys.VNP_RETURNURL.name()).orElse(null);

            if (tmnCodeSetting != null) tmnCode = tmnCodeSetting.getValue();
            if (hashSecretSetting != null) hashSecret = hashSecretSetting.getValue();
            if (urlSetting != null) url = urlSetting.getValue();
            if (returnUrlSetting != null) returnUrl = returnUrlSetting.getValue();
        }
    }

    @Override
    @Transactional
    public OrderResponse checkoutVNPay(CheckoutRequest request) {
        log.info("OrderServiceImpl | checkoutVNPay | Processing VNPay checkout for customer: {}", request.getCustomerId());
        try {
            // 1. Validate inventory and prepare data before any modifications
            List<InventoryItem> inventoryItems = validateInventory(request.getOrderDetails());

            // 2. Create the order using mapper
            Order order = orderMapper.toEntity(CreateOrderRequest.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phoneNumber(request.getPhoneNumber())
                    .paymentMethod(PaymentMethod.VN_PAY) // Set payment method to VNPay
                    .customerId(request.getCustomerId())
                    .address(request.getAddress())
                    .shippingCost(10.0) // Default shipping cost
                    .build());

            // Set fields not handled by mapper
            order.setStatus(Status.NEW);
            order.setIsPaid(false); // VNPay payment is not completed yet

            // Save the order to get an ID
            Order savedOrder = orderRepository.save(order);
            log.info("OrderServiceImpl | checkoutVNPay | Created order with id: {}", savedOrder.getId());

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

            //Remove items from customer's cart
            removeItemsFromCart(request.getCustomerId(),
                    request.getOrderDetails().stream()
                        .map(CheckoutOrderDetailRequest::getItemId)
                        .collect(Collectors.toList()));

            log.info("OrderServiceImpl | checkoutVNPay | Completed checkout for order: {}", savedOrder.getId());
            return orderMapper.toResponse(updatedOrder);
        } catch (Exception e) {
            log.error("OrderServiceImpl | checkoutVNPay | Error during VNPay checkout: {}", e.getMessage(), e);
            throw e;
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
    public String createPaymentUrl(String orderId, double amount) {
        loadVNPayConfig();
        try {
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", tmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf((long) (amount * 100))); 
            vnp_Params.put("vnp_CurrCode", "VND");

            // Tạo mã giao dịch duy nhất (TxnRef)
            String txnRef = UUID.randomUUID().toString().replace("-", "");
            vnp_Params.put("vnp_TxnRef", txnRef);

            log.info("Created txnRef {} for orderId {}", txnRef, orderId);
            Cache paymentCache = cacheManager.getCache("paymentTransactions");
            if (paymentCache != null) {
                paymentCache.put(txnRef, orderId);
            }
            // Hiển thị nội dung thanh toán
            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + txnRef);
            vnp_Params.put("vnp_OrderType", "100009");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", returnUrl);
            vnp_Params.put("vnp_IpAddr", "127.0.0.1");

            // Thời gian tạo và hết hạn
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            formatter.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo")); 
            String createDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", createDate);

            cld.add(Calendar.MINUTE, 60);
            String expireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", expireDate);

            // Tạo chuỗi hash và query
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    // Build hash data - PHẢI encode fieldValue
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    
                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String secureHash = hmacSHA512(hashSecret, hashData.toString());
            query.append("&vnp_SecureHash=").append(secureHash);

            String paymentUrl = url + "?" + query.toString();
            log.info("Created VNPay URL: {}", paymentUrl);
            return paymentUrl;
        } catch (Exception e) {
            log.error("Error creating VNPay payment URL: {}", e.getMessage(), e);
            throw new PaymentException("Error creating payment URL");
        }
    }

    @Override
    @Transactional
    public boolean validatePaymentResponse(Map<String, String> params) {
        loadVNPayConfig();
        try {
            // Verify hash code
            String secureHash = params.get("vnp_SecureHash");
            if (secureHash == null) {
                throw new PaymentException("Missing secure hash");
            }
            
            // Tạo bản sao params để xử lý
            Map<String, String> vnp_Params = new HashMap<>(params);
            vnp_Params.remove("vnp_SecureHash");
            vnp_Params.remove("vnp_SecureHashType"); // Quan trọng: Cần loại bỏ cả vnp_SecureHashType
            
            // Xây dựng hashData đúng cách - dùng encode giống như createPaymentUrl
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }
            
            // Kiểm tra hash
            String calculatedHash = hmacSHA512(hashSecret, hashData.toString());
            if (!calculatedHash.equals(secureHash)) {
                log.error("VNPayPaymentService | validatePaymentResponse | Invalid secure hash. Expected: {}, Actual: {}", 
                        calculatedHash, secureHash);
                throw new PaymentException("Invalid secure hash");
            }
            
            // Phần code còn lại giữ nguyên
            String responseCode = params.get("vnp_ResponseCode");
            if (!"00".equals(responseCode)) {
                log.error("VNPayPaymentService | validatePaymentResponse | Payment failed with code: {}", responseCode);
                throw new PaymentException("Payment failed with code: " + responseCode);
            }
            
            String txnRef = params.get("vnp_TxnRef");
            Cache paymentCache = cacheManager.getCache("paymentTransactions");
            String orderId = paymentCache.get(txnRef, String.class);
            
            if (orderId == null) {
                throw new PaymentException("No order found for transaction: " + txnRef);
            }
            
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + orderId));
                    
            order.setIsPaid(true);
            orderRepository.save(order);
            
            log.info("VNPayPaymentService | validatePaymentResponse | Payment successful for order ID: {}", orderId);
            return true;
        } catch (Exception e) {
            log.error("VNPayPaymentService | validatePaymentResponse | Error: {}", e.getMessage(), e);
            throw new PaymentException("Payment validation failed: " + e.getMessage());
        }
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKey);
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC-SHA512", e);
        }
    }
}