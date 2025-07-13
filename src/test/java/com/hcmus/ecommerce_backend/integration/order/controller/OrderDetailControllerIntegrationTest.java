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
import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderDetailRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateOrderDetailRequest;
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
public class OrderDetailControllerIntegrationTest extends BaseIntegrationTest {

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
                .stock(100)
                .build();
        testCategory = categoryRepository.save(testCategory);

        // Create test product
        testProduct = Product.builder()
                .name("Test Product")
                .description("Test product description")
                .price(100.0)
                .cost(50.0)
                .total(10)
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
                .quantity(2)
                .unitPrice(100.0)
                .productCost(50.0)
                .total(200.0)
                .isReviewed(false)
                .build();
        testOrderDetail = orderDetailRepository.save(testOrderDetail);
    }

    // Test GET /order-details - Get all order details
    @Test
    void getAllOrderDetails_WithEmptyDatabase_ShouldReturnEmptyList() throws Exception {
        // Clean up test data
        orderDetailRepository.deleteAll();

        // Act & Assert
        mockMvc.perform(get("/order-details"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());
    }

    @Test
    void createOrderDetail_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateOrderDetailRequest request = CreateOrderDetailRequest.builder()
                .quantity(1)
                // Missing orderId, itemId, unitPrice, productCost, total
                .build();

        // Act & Assert
        mockMvc.perform(post("/order-details")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createOrderDetail_WithInvalidQuantity_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateOrderDetailRequest request = CreateOrderDetailRequest.builder()
                .orderId(testOrder.getId())
                .itemId(testProductColorSize.getId())
                .quantity(0) // Invalid quantity
                .unitPrice(100.0)
                .productCost(50.0)
                .total(0.0)
                .build();

        // Act & Assert
        mockMvc.perform(post("/order-details")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createOrderDetail_WithNegativePrice_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateOrderDetailRequest request = CreateOrderDetailRequest.builder()
                .orderId(testOrder.getId())
                .itemId(testProductColorSize.getId())
                .quantity(1)
                .unitPrice(-100.0) // Invalid negative price
                .productCost(50.0)
                .total(100.0)
                .build();

        // Act & Assert
        mockMvc.perform(post("/order-details")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createOrderDetail_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/order-details")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void updateOrderDetail_WithInvalidQuantity_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UpdateOrderDetailRequest request = UpdateOrderDetailRequest.builder()
                .quantity(-1) // Invalid negative quantity
                .build();

        // Act & Assert
        mockMvc.perform(put("/order-details/{id}", testOrderDetail.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void updateOrderDetail_WithInvalidPrice_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UpdateOrderDetailRequest request = UpdateOrderDetailRequest.builder()
                .unitPrice(-50.0) // Invalid negative price
                .build();

        // Act & Assert
        mockMvc.perform(put("/order-details/{id}", testOrderDetail.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test POST /order-details/batch - Add multiple order details
    @Test
    void addOrderDetails_WithValidData_ShouldCreateMultipleOrderDetails() throws Exception {
        // Arrange
        CreateOrderDetailRequest request1 = CreateOrderDetailRequest.builder()
                .orderId(testOrder.getId())
                .itemId(testProductColorSize.getId())
                .quantity(1)
                .unitPrice(100.0)
                .productCost(50.0)
                .total(100.0)
                .build();

        CreateOrderDetailRequest request2 = CreateOrderDetailRequest.builder()
                .orderId(testOrder.getId())
                .itemId(testProductColorSize.getId())
                .quantity(2)
                .unitPrice(100.0)
                .productCost(50.0)
                .total(200.0)
                .build();

        List<CreateOrderDetailRequest> requests = Arrays.asList(request1, request2);

        // Act & Assert
        mockMvc.perform(post("/order-details/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].quantity").value(1))
                .andExpect(jsonPath("$.data[1].quantity").value(2))
                .andDo(print());
    }

    @Test
    void addOrderDetails_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateOrderDetailRequest request1 = CreateOrderDetailRequest.builder()
                .orderId(testOrder.getId())
                .itemId(testProductColorSize.getId())
                .quantity(1)
                .unitPrice(100.0)
                .productCost(50.0)
                .total(100.0)
                .build();

        CreateOrderDetailRequest request2 = CreateOrderDetailRequest.builder()
                .orderId(testOrder.getId())
                .itemId(testProductColorSize.getId())
                .quantity(0) // Invalid quantity
                .unitPrice(100.0)
                .productCost(50.0)
                .total(0.0)
                .build();

        List<CreateOrderDetailRequest> requests = Arrays.asList(request1, request2);

        // Act & Assert
        mockMvc.perform(post("/order-details/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test boundary values
    @Test
    void createOrderDetail_WithMinimumQuantity_ShouldCreateOrderDetail() throws Exception {
        // Arrange
        CreateOrderDetailRequest request = CreateOrderDetailRequest.builder()
                .orderId(testOrder.getId())
                .itemId(testProductColorSize.getId())
                .quantity(1) // Minimum valid quantity
                .unitPrice(100.0)
                .productCost(50.0)
                .total(100.0)
                .build();

        // Act & Assert
        mockMvc.perform(post("/order-details")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.quantity").value(1))
                .andDo(print());
    }

    @Test
    void createOrderDetail_WithMaximumQuantity_ShouldCreateOrderDetail() throws Exception {
        // Arrange
        CreateOrderDetailRequest request = CreateOrderDetailRequest.builder()
                .orderId(testOrder.getId())
                .itemId(testProductColorSize.getId())
                .quantity(10) // Maximum available quantity
                .unitPrice(100.0)
                .productCost(50.0)
                .total(1000.0)
                .build();

        // Act & Assert
        mockMvc.perform(post("/order-details")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.quantity").value(10))
                .andDo(print());
    }

    // Test data integrity
    @Test
    void createOrderDetail_ShouldMaintainDataIntegrity() throws Exception {
        // Arrange
        CreateOrderDetailRequest request = CreateOrderDetailRequest.builder()
                .orderId(testOrder.getId())
                .itemId(testProductColorSize.getId())
                .quantity(1)
                .unitPrice(100.0)
                .productCost(50.0)
                .total(100.0)
                .build();

        // Act
        mockMvc.perform(post("/order-details")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print());

        // Assert - Check that total order details count increased
        mockMvc.perform(get("/order-details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2))) // Original + new one
                .andDo(print());
    }
}