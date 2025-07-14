package com.hcmus.ecommerce_backend.unit.common.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.category.repository.CategoryRepository;
import com.hcmus.ecommerce_backend.common.exception.PaymentException;
import com.hcmus.ecommerce_backend.common.model.entity.SystemSetting;
import com.hcmus.ecommerce_backend.common.model.enums.VNPayKeys;
import com.hcmus.ecommerce_backend.common.repository.SystemSettingRepository;
import com.hcmus.ecommerce_backend.common.service.impl.VNPayPaymentServiceImpl;
import com.hcmus.ecommerce_backend.order.exception.InsufficientInventoryException;
import com.hcmus.ecommerce_backend.order.exception.OrderNotFoundException;
import com.hcmus.ecommerce_backend.order.model.dto.request.CheckoutOrderDetailRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.CheckoutRequest;
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

@ExtendWith(MockitoExtension.class)
public class VNPayPaymentServiceImplTest {

    @Mock
    private SystemSettingRepository systemSettingRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailService orderDetailService;

    @Mock
    private ProductColorSizeRepository productColorSizeRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderTrackService orderTrackService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private VNPayPaymentServiceImpl vnPayPaymentService;

    private SystemSetting tmnCodeSetting;
    private SystemSetting hashSecretSetting;
    private SystemSetting urlSetting;
    private SystemSetting returnUrlSetting;
    private Order testOrder;
    private Product testProduct;
    private Category testCategory;
    private ProductColorSize testProductColorSize;
    private CheckoutRequest checkoutRequest;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        // Setup SystemSetting mocks
        tmnCodeSetting = SystemSetting.builder()
                .key(VNPayKeys.VNP_TMNCODE.name())
                .value("DEMO_TMN_CODE")
                .build();

        hashSecretSetting = SystemSetting.builder()
                .key(VNPayKeys.VNP_HASHSECRET.name())
                .value("DEMO_HASH_SECRET")
                .build();

        urlSetting = SystemSetting.builder()
                .key(VNPayKeys.VNP_URL.name())
                .value("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html")
                .build();

        returnUrlSetting = SystemSetting.builder()
                .key(VNPayKeys.VNP_RETURNURL.name())
                .value("http://localhost:8080/return")
                .build();

        // Setup test entities
        testCategory = Category.builder()
                .id("category-1")
                .name("Test Category")
                .stock(100)
                .build();

        testProduct = Product.builder()
                .id("product-1")
                .name("Test Product")
                .price(100.0)
                .cost(50.0)
                .total(20)
                .inStock(true)
                .category(testCategory)
                .build();

        testProductColorSize = ProductColorSize.builder()
                .id("item-1")
                .product(testProduct)
                .quantity(15)
                .build();

        testOrder = Order.builder()
                .id("order-1")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .customerId("customer-1")
                .address("123 Test Street")
                .paymentMethod(PaymentMethod.VN_PAY)
                .status(Status.NEW)
                .isPaid(false)
                .shippingCost(10.0)
                .productCost(50.0)
                .subTotal(100.0)
                .total(110.0)
                .build();

        orderResponse = OrderResponse.builder()
                .id("order-1")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .customerId("customer-1")
                .address("123 Test Street")
                .paymentMethod(PaymentMethod.VN_PAY)
                .status(Status.NEW)
                .isPaid(false)
                .shippingCost(10.0)
                .productCost(50.0)
                .subTotal(100.0)
                .total(110.0)
                .build();

        checkoutRequest = CheckoutRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .customerId("customer-1")
                .address("123 Test Street")
                .orderDetails(Arrays.asList(
                        CheckoutOrderDetailRequest.builder()
                                .itemId("item-1")
                                .quantity(2)
                                .build()
                ))
                .build();

        // Reset private fields
        ReflectionTestUtils.setField(vnPayPaymentService, "tmnCode", null);
        ReflectionTestUtils.setField(vnPayPaymentService, "hashSecret", null);
        ReflectionTestUtils.setField(vnPayPaymentService, "url", null);
        ReflectionTestUtils.setField(vnPayPaymentService, "returnUrl", null);
    }

    @Test
    void checkoutVNPay_WithInsufficientInventory_ShouldThrowException() {
        // Given
        testProductColorSize.setQuantity(1); // Less than requested quantity of 2
        when(productColorSizeRepository.findByIdWithLock("item-1")).thenReturn(Optional.of(testProductColorSize));

        // When & Then
        assertThrows(InsufficientInventoryException.class,
                () -> vnPayPaymentService.checkoutVNPay(checkoutRequest));

        verify(productColorSizeRepository).findByIdWithLock("item-1");
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderDetailService, never()).addOrderDetails(anyList());
    }

    @Test
    void checkoutVNPay_WithNonExistentProductColorSize_ShouldThrowException() {
        // Given
        when(productColorSizeRepository.findByIdWithLock("item-1")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductColorSizeNotFoundException.class,
                () -> vnPayPaymentService.checkoutVNPay(checkoutRequest));

        verify(productColorSizeRepository).findByIdWithLock("item-1");
        verify(orderRepository, never()).save(any(Order.class));
        verify(orderDetailService, never()).addOrderDetails(anyList());
    }

    // Test createPaymentUrl
    @Test
    void createPaymentUrl_WithValidParameters_ShouldReturnPaymentUrl() {
        // Given
        String orderId = "order-1";
        double amount = 100.0;
        
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_TMNCODE.name())).thenReturn(Optional.of(tmnCodeSetting));
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_HASHSECRET.name())).thenReturn(Optional.of(hashSecretSetting));
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_URL.name())).thenReturn(Optional.of(urlSetting));
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_RETURNURL.name())).thenReturn(Optional.of(returnUrlSetting));
        when(cacheManager.getCache("paymentTransactions")).thenReturn(cache);
        doNothing().when(cache).put(anyString(), anyString());

        // When
        String result = vnPayPaymentService.createPaymentUrl(orderId, amount);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"));
        assertTrue(result.contains("vnp_TmnCode=DEMO_TMN_CODE"));
        assertTrue(result.contains("vnp_Amount=10000")); // 100.0 * 100
        assertTrue(result.contains("vnp_SecureHash="));

        verify(systemSettingRepository).findByKey(VNPayKeys.VNP_TMNCODE.name());
        verify(systemSettingRepository).findByKey(VNPayKeys.VNP_HASHSECRET.name());
        verify(systemSettingRepository).findByKey(VNPayKeys.VNP_URL.name());
        verify(systemSettingRepository).findByKey(VNPayKeys.VNP_RETURNURL.name());
        verify(cache).put(anyString(), eq(orderId));
    }

    @Test
    void createPaymentUrl_WithCachedConfiguration_ShouldNotQueryDatabase() {
        // Given
        String orderId = "order-1";
        double amount = 100.0;
        
        // Set cached configuration
        ReflectionTestUtils.setField(vnPayPaymentService, "tmnCode", "CACHED_TMN_CODE");
        ReflectionTestUtils.setField(vnPayPaymentService, "hashSecret", "CACHED_HASH_SECRET");
        ReflectionTestUtils.setField(vnPayPaymentService, "url", "https://cached.vnpayment.vn");
        ReflectionTestUtils.setField(vnPayPaymentService, "returnUrl", "http://cached.localhost:8080/return");
        
        when(cacheManager.getCache("paymentTransactions")).thenReturn(cache);
        doNothing().when(cache).put(anyString(), anyString());

        // When
        String result = vnPayPaymentService.createPaymentUrl(orderId, amount);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("https://cached.vnpayment.vn"));
        assertTrue(result.contains("vnp_TmnCode=CACHED_TMN_CODE"));
        
        // Should not query database when configuration is cached
        verify(systemSettingRepository, never()).findByKey(anyString());
        verify(cache).put(anyString(), eq(orderId));
    }

    @Test
    void createPaymentUrl_WithMissingTmnCode_ShouldThrowException() {
        // Given
        String orderId = "order-1";
        double amount = 100.0;
        
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_TMNCODE.name())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(PaymentException.class,
                () -> vnPayPaymentService.createPaymentUrl(orderId, amount));

        verify(systemSettingRepository).findByKey(VNPayKeys.VNP_TMNCODE.name());
    }

    // Test createRetryPaymentUrl
    @Test
    void createRetryPaymentUrl_WithValidOrderId_ShouldReturnPaymentUrl() {
        // Given
        String orderId = "order-1";
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        
        // Mock createPaymentUrl dependencies
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_TMNCODE.name())).thenReturn(Optional.of(tmnCodeSetting));
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_HASHSECRET.name())).thenReturn(Optional.of(hashSecretSetting));
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_URL.name())).thenReturn(Optional.of(urlSetting));
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_RETURNURL.name())).thenReturn(Optional.of(returnUrlSetting));
        when(cacheManager.getCache("paymentTransactions")).thenReturn(cache);
        doNothing().when(cache).put(anyString(), anyString());

        // When
        String result = vnPayPaymentService.createRetryPaymentUrl(orderId);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"));
        verify(orderRepository).findById(orderId);
    }

    @Test
    void createRetryPaymentUrl_WithNonExistentOrder_ShouldThrowException() {
        // Given
        String orderId = "non-existent-order";
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderNotFoundException.class,
                () -> vnPayPaymentService.createRetryPaymentUrl(orderId));

        verify(orderRepository).findById(orderId);
    }

    @Test
    void createRetryPaymentUrl_WithPaidOrder_ShouldThrowException() {
        // Given
        String orderId = "order-1";
        testOrder.setIsPaid(true); // Order is already paid
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThrows(PaymentException.class,
                () -> vnPayPaymentService.createRetryPaymentUrl(orderId));

        verify(orderRepository).findById(orderId);
    }

    @Test
    void createRetryPaymentUrl_WithCancelledOrder_ShouldThrowException() {
        // Given
        String orderId = "order-1";
        testOrder.setStatus(Status.CANCELLED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThrows(PaymentException.class,
                () -> vnPayPaymentService.createRetryPaymentUrl(orderId));

        verify(orderRepository).findById(orderId);
    }

    @Test
    void createRetryPaymentUrl_WithNonVNPayOrder_ShouldThrowException() {
        // Given
        String orderId = "order-1";
        testOrder.setPaymentMethod(PaymentMethod.COD);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // When & Then
        assertThrows(PaymentException.class,
                () -> vnPayPaymentService.createRetryPaymentUrl(orderId));

        verify(orderRepository).findById(orderId);
    }

    @Test
    void createPaymentUrl_WithLargeAmount_ShouldHandleCorrectly() {
        // Given
        String orderId = "order-1";
        double amount = 999999.99;
        
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_TMNCODE.name())).thenReturn(Optional.of(tmnCodeSetting));
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_HASHSECRET.name())).thenReturn(Optional.of(hashSecretSetting));
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_URL.name())).thenReturn(Optional.of(urlSetting));
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_RETURNURL.name())).thenReturn(Optional.of(returnUrlSetting));
        when(cacheManager.getCache("paymentTransactions")).thenReturn(cache);
        doNothing().when(cache).put(anyString(), anyString());

        // When
        String result = vnPayPaymentService.createPaymentUrl(orderId, amount);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("vnp_Amount=99999999")); // 999999.99 * 100
        verify(cache).put(anyString(), eq(orderId));
    }

    @Test
    void createPaymentUrl_WithZeroAmount_ShouldHandleCorrectly() {
        // Given
        String orderId = "order-1";
        double amount = 0.0;
        
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_TMNCODE.name())).thenReturn(Optional.of(tmnCodeSetting));
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_HASHSECRET.name())).thenReturn(Optional.of(hashSecretSetting));
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_URL.name())).thenReturn(Optional.of(urlSetting));
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_RETURNURL.name())).thenReturn(Optional.of(returnUrlSetting));
        when(cacheManager.getCache("paymentTransactions")).thenReturn(cache);
        doNothing().when(cache).put(anyString(), anyString());

        // When
        String result = vnPayPaymentService.createPaymentUrl(orderId, amount);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("vnp_Amount=0"));
        verify(cache).put(anyString(), eq(orderId));
    }

    @Test
    void createPaymentUrl_WithNullCache_ShouldNotFailCreation() {
        // Given
        String orderId = "order-1";
        double amount = 100.0;
        
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_TMNCODE.name())).thenReturn(Optional.of(tmnCodeSetting));
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_HASHSECRET.name())).thenReturn(Optional.of(hashSecretSetting));
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_URL.name())).thenReturn(Optional.of(urlSetting));
        when(systemSettingRepository.findByKey(VNPayKeys.VNP_RETURNURL.name())).thenReturn(Optional.of(returnUrlSetting));
        when(cacheManager.getCache("paymentTransactions")).thenReturn(null);

        // When
        String result = vnPayPaymentService.createPaymentUrl(orderId, amount);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"));
        verify(cacheManager).getCache("paymentTransactions");
    }
}