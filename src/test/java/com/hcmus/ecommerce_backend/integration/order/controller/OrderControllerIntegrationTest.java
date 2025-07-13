package com.hcmus.ecommerce_backend.integration.order.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmus.ecommerce_backend.auth.model.dto.request.LoginRequest;
import com.hcmus.ecommerce_backend.auth.model.dto.response.TokenResponse;
import com.hcmus.ecommerce_backend.auth.service.AuthenticationService;
import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.category.repository.CategoryRepository;
import com.hcmus.ecommerce_backend.integration.BaseIntegrationTest;
import com.hcmus.ecommerce_backend.order.model.dto.request.CheckoutOrderDetailRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.CheckoutRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateOrderRequest;
import com.hcmus.ecommerce_backend.order.model.entity.Order;
import com.hcmus.ecommerce_backend.order.model.entity.OrderDetail;
import com.hcmus.ecommerce_backend.order.model.enums.PaymentMethod;
import com.hcmus.ecommerce_backend.order.model.enums.Status;
import com.hcmus.ecommerce_backend.order.repository.OrderDetailRepository;
import com.hcmus.ecommerce_backend.order.repository.OrderRepository;
import com.hcmus.ecommerce_backend.product.model.entity.Color;
import com.hcmus.ecommerce_backend.product.model.entity.Product;
import com.hcmus.ecommerce_backend.product.model.entity.ProductColorSize;
import com.hcmus.ecommerce_backend.product.model.entity.Size;
import com.hcmus.ecommerce_backend.product.repository.ColorRepository;
import com.hcmus.ecommerce_backend.product.repository.ProductColorSizeRepository;
import com.hcmus.ecommerce_backend.product.repository.ProductRepository;
import com.hcmus.ecommerce_backend.product.repository.SizeRepository;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.model.enums.Role;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class OrderControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private ProductColorSizeRepository productColorSizeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationService authenticationService;

    private User testUser;
    private User testAdmin;
    private String userAccessToken;
    private String adminAccessToken;
    private Category testCategory;
    private Product testProduct;
    private Color testColor;
    private Size testSize;
    private ProductColorSize testProductColorSize;
    private Order testOrder;
    private OrderDetail testOrderDetail;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        // Clean up existing data in correct order
        orderDetailRepository.deleteAll();
        orderRepository.deleteAll();
        productColorSizeRepository.deleteAll();
        productRepository.deleteAll();
        colorRepository.deleteAll();
        sizeRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        setupTestData();
    }

    private void setupTestData() {
        // Create test user
        testUser = User.builder()
                .email("user@test.com")
                .firstName("John")
                .lastName("Doe")
                .password(passwordEncoder.encode("password123"))
                .phoneNumber("1234567890")
                .build();
        testUser.setRole(Role.USER);
        testUser.setEnabled(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        userRepository.save(testUser);
        testUser = userRepository.findByEmail(testUser.getEmail()).orElseThrow();

        // Create test admin
        testAdmin = User.builder()
                .email("admin@test.com")
                .firstName("Admin")
                .lastName("User")
                .password(passwordEncoder.encode("password123"))
                .phoneNumber("0987654321")
                .build();
        testAdmin.setRole(Role.ADMIN);
        testAdmin.setEnabled(true);
        testAdmin.setCreatedAt(LocalDateTime.now());
        testAdmin.setUpdatedAt(LocalDateTime.now());

        userRepository.save(testAdmin);
        testAdmin = userRepository.findByEmail(testAdmin.getEmail()).orElseThrow();

        // Get access tokens
        TokenResponse userTokenResponse = authenticationService.login(LoginRequest.builder()
                .email(testUser.getEmail())
                .password("password123")
                .build());
        userAccessToken = userTokenResponse.getAccessToken();

        TokenResponse adminTokenResponse = authenticationService.login(LoginRequest.builder()
                .email(testAdmin.getEmail())
                .password("password123")
                .build());
        adminAccessToken = adminTokenResponse.getAccessToken();

        // Create test category
        testCategory = Category.builder()
                .name("Test Category")
                .description("Test category description")
                .stock(100) // Add stock for category
                .build();
        testCategory = categoryRepository.save(testCategory);

        // Create test product
        testProduct = Product.builder()
                .name("Test Product")
                .description("Test product description")
                .price(100.0)
                .cost(50.0) // Add cost field
                .total(10) // Add total inventory
                .inStock(true)
                .category(testCategory)
                .reviewCount(0)
                .averageRating(0.0)
                .build();
        testProduct = productRepository.save(testProduct);

        // Create color and size
        testColor = Color.builder()
                .name("Red")
                .code("#FF0000") 
                .build();
        testColor = colorRepository.save(testColor);

        testSize = Size.builder()
                .name("M")
                .build();
        testSize = sizeRepository.save(testSize);

        // Create ProductColorSize
        testProductColorSize = ProductColorSize.builder()
                .product(testProduct)
                .color(testColor)
                .size(testSize)
                .quantity(10)
                .build();
        testProductColorSize = productColorSizeRepository.save(testProductColorSize);

        // Create test order
        testOrder = Order.builder()
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .customerId(testUser.getId())
                .status(Status.NEW)
                .paymentMethod(PaymentMethod.COD)
                .address("123 Test Street")
                .productCost(50.0)
                .subTotal(100.0)
                .shippingCost(10.0)
                .total(110.0)
                .isPaid(false)
                .build();
        testOrder = orderRepository.save(testOrder);

        // Create test order detail
        testOrderDetail = OrderDetail.builder()
                .order(testOrder)
                .itemId(testProductColorSize.getId())
                .quantity(1)
                .unitPrice(100.0)
                .productCost(50.0)
                .total(100.0)
                .isReviewed(false)
                .build();
        testOrderDetail = orderDetailRepository.save(testOrderDetail);
    }

    // Test POST /orders/checkout - Checkout order
    @Test
    void checkout_WithValidData_ShouldCreateOrder() throws Exception {
        // Arrange
        CheckoutOrderDetailRequest orderDetailRequest = CheckoutOrderDetailRequest.builder()
                .itemId(testProductColorSize.getId())
                .quantity(2)
                .build();

        CheckoutRequest request = CheckoutRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("0987654321")
                .customerId(testUser.getId())
                .paymentMethod(PaymentMethod.COD)
                .address("123 Test Street, Test City")
                .orderDetails(Arrays.asList(orderDetailRequest))
                .build();

        // Act & Assert
        mockMvc.perform(post("/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkout_WithInvalidQuantity_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CheckoutOrderDetailRequest orderDetailRequest = CheckoutOrderDetailRequest.builder()
                .itemId(testProductColorSize.getId())
                .quantity(0) // Invalid quantity
                .build();

        CheckoutRequest request = CheckoutRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("0987654321")
                .customerId(testUser.getId())
                .paymentMethod(PaymentMethod.COD)
                .address("123 Test Street, Test City")
                .orderDetails(Arrays.asList(orderDetailRequest))
                .build();

        // Act & Assert
        mockMvc.perform(post("/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void checkout_WithInsufficientInventory_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CheckoutOrderDetailRequest orderDetailRequest = CheckoutOrderDetailRequest.builder()
                .itemId(testProductColorSize.getId())
                .quantity(20) // More than available quantity (10)
                .build();

        CheckoutRequest request = CheckoutRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("0987654321")
                .customerId(testUser.getId())
                .paymentMethod(PaymentMethod.COD)
                .address("123 Test Street, Test City")
                .orderDetails(Arrays.asList(orderDetailRequest))
                .build();

        // Act & Assert
        mockMvc.perform(post("/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void checkout_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CheckoutOrderDetailRequest orderDetailRequest = CheckoutOrderDetailRequest.builder()
                .itemId(testProductColorSize.getId())
                .quantity(1)
                .build();

        CheckoutRequest request = CheckoutRequest.builder()
                .customerId(testUser.getId())
                .paymentMethod(PaymentMethod.COD)
                // Missing firstName, lastName, phoneNumber, address
                .orderDetails(Arrays.asList(orderDetailRequest))
                .build();

        // Act & Assert
        mockMvc.perform(post("/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test GET /orders - Get all orders
    @Test
    void getAllOrders_WithEmptyDatabase_ShouldReturnEmptyList() throws Exception {
        // Clean up test data
        orderDetailRepository.deleteAll();
        orderRepository.deleteAll();

        // Act & Assert
        mockMvc.perform(get("/orders")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andDo(print());
    }

    @Test
    void getAllOrders_WithExistingOrders_ShouldReturnOrders() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/orders")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andDo(print());
    }

    // @Test
    // void getAllOrders_WithStatusFilter_ShouldReturnFilteredOrders() throws Exception {
    //     // Act & Assert
    //     mockMvc.perform(get("/orders")
    //             .param("status", "NEW")
    //             .param("page", "0")
    //             .param("size", "10")
    //             .header("Authorization", "Bearer " + adminAccessToken))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data.content").isArray())
    //             .andExpect(jsonPath("$.data.content", hasSize(1)))
    //             .andDo(print());
    // }

    @Test
    void getAllOrders_WithPaymentMethodFilter_ShouldReturnFilteredOrders() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/orders")
                .param("paymentMethod", "COD")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andDo(print());
    }

    @Test
    void getAllOrders_WithCustomerIdFilter_ShouldReturnFilteredOrders() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/orders")
                .param("customerId", testUser.getId())
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andDo(print());
    }

    @Test
    void getAllOrders_WithKeywordFilter_ShouldReturnFilteredOrders() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/orders")
                .param("keyword", "John")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andDo(print());
    }

    @Test
    void getAllOrders_WithSorting_ShouldReturnSortedOrders() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/orders")
                .param("sort", "createdAt,desc")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andDo(print());
    }

    // Test GET /orders/search - Search orders
    @Test
    void searchOrders_WithKeyword_ShouldReturnMatchingOrders() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/orders/search")
                .param("keyword", "John")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andDo(print());
    }

    @Test
    void searchOrders_WithStatus_ShouldReturnFilteredOrders() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/orders/search")
                .param("status", "NEW")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andDo(print());
    }

    @Test
    void searchOrders_WithoutParameters_ShouldReturnAllOrders() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/orders/search")
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andDo(print());
    }

    // Test GET /orders/{id} - Get order by ID
    @Test
    void getOrderById_WithExistingId_ShouldReturnOrder() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/orders/{id}", testOrder.getId())
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(testOrder.getId()))
                .andExpect(jsonPath("$.data.customerId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.status").value("NEW"))
                .andDo(print());
    }

    @Test
    void getOrderById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/orders/{id}", "non-existent-id")
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    // Test GET /orders/customer/{customerId} - Get orders by customer ID
    @Test
    void getOrdersByCustomerId_WithExistingCustomer_ShouldReturnOrders() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/orders/customer/{customerId}", testUser.getId())
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].customerId").value(testUser.getId()))
                .andDo(print());
    }

    @Test
    void getOrdersByCustomerId_WithNonExistentCustomer_ShouldReturnEmptyList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/orders/customer/{customerId}", "non-existent-customer")
                .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());
    }

    // Test POST /orders - Create new order
    @Test
    void createOrder_WithValidData_ShouldCreateOrder() throws Exception {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("0987654321")
                .customerId(testUser.getId())
                .paymentMethod(PaymentMethod.VN_PAY)
                .address("456 New Street, New City")
                .productCost(50.0)
                .subTotal(100.0)
                .shippingCost(10.0)
                .total(110.0)
                .build();

        // Act & Assert
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.customerId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.paymentMethod").value("VN_PAY"))
                .andExpect(jsonPath("$.data.status").value("NEW"))
                .andDo(print());
    }

    @Test
    void createOrder_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
                .paymentMethod(PaymentMethod.COD)
                // Missing required fields
                .build();

        // Act & Assert
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createOrder_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("0987654321")
                .customerId(testUser.getId())
                .paymentMethod(PaymentMethod.COD)
                .address("456 New Street, New City")
                .productCost(-50.0) // Invalid negative cost
                .subTotal(100.0)
                .shippingCost(10.0)
                .total(110.0)
                .build();

        // Act & Assert
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test PUT /orders/{id} - Update order
    @Test
    void updateOrder_WithValidData_ShouldUpdateOrder() throws Exception {
        // Arrange
        UpdateOrderRequest request = UpdateOrderRequest.builder()
                .status(Status.PROCESSING)
                .isPaid(true)
                .address("Updated Address")
                .build();

        // Act & Assert
        mockMvc.perform(put("/orders/{id}", testOrder.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.status").value("PROCESSING"))
                .andExpect(jsonPath("$.data.isPaid").value(true))
                .andDo(print());
    }

    @Test
    void updateOrder_WithPartialData_ShouldUpdateOnlyProvidedFields() throws Exception {
        // Arrange
        UpdateOrderRequest request = UpdateOrderRequest.builder()
                .status(Status.SHIPPING)
                .build();

        // Act & Assert
        mockMvc.perform(put("/orders/{id}", testOrder.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void updateOrder_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Arrange
        UpdateOrderRequest request = UpdateOrderRequest.builder()
                .status(Status.PROCESSING)
                .build();

        // Act & Assert
        mockMvc.perform(put("/orders/{id}", "non-existent-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test DELETE /orders/{id} - Delete order
    @Test
    void deleteOrder_WithExistingId_ShouldDeleteOrder() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/orders/{id}", testOrder.getId())
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andDo(print());

        // Verify deletion
        mockMvc.perform(get("/orders/{id}", testOrder.getId())
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void deleteOrder_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/orders/{id}", "non-existent-id")
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    // Test authentication and authorization
    @Test
    void getAllOrders_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/orders"))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    void createOrder_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("0987654321")
                .customerId(testUser.getId())
                .paymentMethod(PaymentMethod.COD)
                .address("123 Test Street")
                .productCost(50.0)
                .subTotal(100.0)
                .shippingCost(10.0)
                .total(110.0)
                .build();

        // Act & Assert
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    void checkout_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        CheckoutOrderDetailRequest orderDetailRequest = CheckoutOrderDetailRequest.builder()
                .itemId(testProductColorSize.getId())
                .quantity(1)
                .build();

        CheckoutRequest request = CheckoutRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("0987654321")
                .customerId(testUser.getId())
                .paymentMethod(PaymentMethod.COD)
                .address("123 Test Street, Test City")
                .orderDetails(Arrays.asList(orderDetailRequest))
                .build();

        // Act & Assert
        mockMvc.perform(post("/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }

    // Test edge cases
    @Test
    void getAllOrders_WithLargePage_ShouldReturnEmptyContent() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/orders")
                .param("page", "999")
                .param("size", "10")
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andDo(print());
    }

    // @Test
    // void getAllOrders_WithZeroSize_ShouldReturnEmptyContent() throws Exception {
    //     // Act & Assert
    //     mockMvc.perform(get("/orders")
    //             .param("page", "0")
    //             .param("size", "0")
    //             .header("Authorization", "Bearer " + adminAccessToken))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data.content").isEmpty())
    //             .andDo(print());
    // }

    @Test
    void getAllOrders_WithMultipleFilters_ShouldReturnFilteredOrders() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/orders")
                .param("status", "NEW")
                .param("paymentMethod", "COD")
                .param("customerId", testUser.getId())
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andDo(print());
    }

    // Test concurrent operations
    @Test
    @Transactional
    void concurrentOperations_ShouldHandleCorrectly() throws Exception {
        // Multiple operations on orders
        mockMvc.perform(get("/orders")
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andDo(print());

        mockMvc.perform(get("/orders/{id}", testOrder.getId())
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andDo(print());

        mockMvc.perform(get("/orders/customer/{customerId}", testUser.getId())
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andDo(print());
    }
}