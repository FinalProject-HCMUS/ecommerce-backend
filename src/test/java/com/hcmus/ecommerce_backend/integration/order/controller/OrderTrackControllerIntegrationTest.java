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
import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderTrackRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateOrderTrackRequest;
import com.hcmus.ecommerce_backend.order.model.entity.Order;
import com.hcmus.ecommerce_backend.order.model.entity.OrderDetail;
import com.hcmus.ecommerce_backend.order.model.entity.OrderTrack;
import com.hcmus.ecommerce_backend.order.model.enums.PaymentMethod;
import com.hcmus.ecommerce_backend.order.model.enums.Status;
import com.hcmus.ecommerce_backend.order.repository.OrderDetailRepository;
import com.hcmus.ecommerce_backend.order.repository.OrderRepository;
import com.hcmus.ecommerce_backend.order.repository.OrderTrackRepository;
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
public class OrderTrackControllerIntegrationTest extends BaseIntegrationTest {

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
    private OrderTrackRepository orderTrackRepository;

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
    private OrderTrack testOrderTrack;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        // Clean up existing data in correct order
        orderTrackRepository.deleteAll();
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

        // Create test order track
        testOrderTrack = OrderTrack.builder()
                .order(testOrder)
                .notes("Order has been created")
                .status(Status.NEW)
                .build();
        testOrderTrack = orderTrackRepository.save(testOrderTrack);
    }

    // Test GET /order-tracks - Get all order tracks
    @Test
    void getAllOrderTracks_WithEmptyDatabase_ShouldReturnEmptyList() throws Exception {
        // Clean up test data
        orderTrackRepository.deleteAll();

        // Act & Assert
        mockMvc.perform(get("/order-tracks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());
    }

    @Test
    void getAllOrderTracks_WithExistingOrderTracks_ShouldReturnOrderTracks() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/order-tracks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(testOrderTrack.getId()))
                .andExpect(jsonPath("$.data[0].notes").value("Order has been created"))
                .andExpect(jsonPath("$.data[0].status").value("NEW"))
                .andDo(print());
    }

    // Test GET /order-tracks/{id} - Get order track by ID
    @Test
    void getOrderTrackById_WithExistingId_ShouldReturnOrderTrack() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/order-tracks/{id}", testOrderTrack.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(testOrderTrack.getId()))
                .andExpect(jsonPath("$.data.notes").value("Order has been created"))
                .andExpect(jsonPath("$.data.status").value("NEW"))
                .andDo(print());
    }

    @Test
    void getOrderTrackById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/order-tracks/{id}", "non-existent-id"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    // Test GET /order-tracks/order/{orderId} - Get order tracks by order ID
    @Test
    void getOrderTracksByOrderId_WithExistingOrderId_ShouldReturnOrderTracks() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/order-tracks/order/{orderId}", testOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(testOrderTrack.getId()))
                .andExpect(jsonPath("$.data[0].notes").value("Order has been created"))
                .andExpect(jsonPath("$.data[0].status").value("NEW"))
                .andDo(print());
    }

    @Test
    void getOrderTracksByOrderId_WithNonExistentOrderId_ShouldReturnEmptyList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/order-tracks/order/{orderId}", "non-existent-order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());
    }

    // Test GET /order-tracks/status/{status} - Get order tracks by status
    @Test
    void getOrderTracksByStatus_WithExistingStatus_ShouldReturnOrderTracks() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/order-tracks/status/{status}", "NEW"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(testOrderTrack.getId()))
                .andExpect(jsonPath("$.data[0].status").value("NEW"))
                .andDo(print());
    }

    @Test
    void getOrderTracksByStatus_WithNonExistentStatus_ShouldReturnEmptyList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/order-tracks/status/{status}", "CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());
    }

    @Test
    void getOrderTracksByStatus_WithInvalidStatus_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/order-tracks/status/{status}", "INVALID_STATUS"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test POST /order-tracks - Create new order track
    @Test
    void createOrderTrack_WithValidData_ShouldCreateOrderTrack() throws Exception {
        // Arrange
        CreateOrderTrackRequest request = CreateOrderTrackRequest.builder()
                .orderId(testOrder.getId())
                .notes("Order is being processed")
                .status(Status.PROCESSING)
                .build();

        // Act & Assert
        mockMvc.perform(post("/order-tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.notes").value("Order is being processed"))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"))
                .andDo(print());
    }

    @Test
    void createOrderTrack_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateOrderTrackRequest request = CreateOrderTrackRequest.builder()
                .notes("Order is being processed")
                // Missing orderId and status
                .build();

        // Act & Assert
        mockMvc.perform(post("/order-tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createOrderTrack_WithNonExistentOrder_ShouldReturnNotFound() throws Exception {
        // Arrange
        CreateOrderTrackRequest request = CreateOrderTrackRequest.builder()
                .orderId("non-existent-order")
                .notes("Order is being processed")
                .status(Status.PROCESSING)
                .build();

        // Act & Assert
        mockMvc.perform(post("/order-tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void createOrderTrack_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/order-tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test PUT /order-tracks/{id} - Update order track
    @Test
    void updateOrderTrack_WithValidData_ShouldUpdateOrderTrack() throws Exception {
        // Arrange
        UpdateOrderTrackRequest request = UpdateOrderTrackRequest.builder()
                .notes("Order has been shipped")
                .status(Status.SHIPPING)
                .orderId(testOrder.getId())
                .build();

        // Act & Assert
        mockMvc.perform(put("/order-tracks/{id}", testOrderTrack.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.notes").value("Order has been shipped"))
                .andExpect(jsonPath("$.data.status").value("SHIPPING"))
                .andDo(print());
    }

    @Test
    void updateOrderTrack_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Arrange
        UpdateOrderTrackRequest request = UpdateOrderTrackRequest.builder()
                .notes("Updated notes")
                .status(Status.PROCESSING)
                .build();

        // Act & Assert
        mockMvc.perform(put("/order-tracks/{id}", "non-existent-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void updateOrderTrack_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/order-tracks/{id}", testOrderTrack.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test DELETE /order-tracks/{id} - Delete order track
    @Test
    void deleteOrderTrack_WithExistingId_ShouldDeleteOrderTrack() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/order-tracks/{id}", testOrderTrack.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andDo(print());

        // Verify deletion
        mockMvc.perform(get("/order-tracks/{id}", testOrderTrack.getId()))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void deleteOrderTrack_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/order-tracks/{id}", "non-existent-id"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    // Test different status values
    @Test
    void createOrderTrack_WithAllStatusValues_ShouldCreateOrderTrack() throws Exception {
        Status[] statuses = {Status.NEW, Status.PROCESSING, Status.PACKAGED, Status.PICKED, 
                           Status.SHIPPING, Status.DELIVERED, Status.CANCELLED, Status.REFUNDED};

        for (Status status : statuses) {
            CreateOrderTrackRequest request = CreateOrderTrackRequest.builder()
                    .orderId(testOrder.getId())
                    .notes("Order status: " + status.name())
                    .status(status)
                    .build();

            mockMvc.perform(post("/order-tracks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.status").value(status.name()))
                    .andDo(print());
        }
    }

    @Test
    void createOrderTrack_WithEmptyNotes_ShouldCreateOrderTrack() throws Exception {
        // Arrange
        CreateOrderTrackRequest request = CreateOrderTrackRequest.builder()
                .orderId(testOrder.getId())
                .notes("") // Empty notes
                .status(Status.PROCESSING)
                .build();

        // Act & Assert
        mockMvc.perform(post("/order-tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.notes").value(""))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"))
                .andDo(print());
    }

    @Test
    void createOrderTrack_WithNullNotes_ShouldCreateOrderTrack() throws Exception {
        // Arrange
        CreateOrderTrackRequest request = CreateOrderTrackRequest.builder()
                .orderId(testOrder.getId())
                .notes(null) // Null notes
                .status(Status.PROCESSING)
                .build();

        // Act & Assert
        mockMvc.perform(post("/order-tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PROCESSING"))
                .andDo(print());
    }

    @Test
    void createOrderTrack_WithLongNotes_ShouldCreateOrderTrack() throws Exception {
        // Arrange
        String longNotes = "This is a very long note that contains detailed information about the order tracking. " +
                          "It includes multiple sentences and provides comprehensive details about the order status. " +
                          "The note can be used to store additional information that might be useful for tracking purposes.";

        CreateOrderTrackRequest request = CreateOrderTrackRequest.builder()
                .orderId(testOrder.getId())
                .notes(longNotes)
                .status(Status.PROCESSING)
                .build();

        // Act & Assert
        mockMvc.perform(post("/order-tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.notes").value(longNotes))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"))
                .andDo(print());
    }

    @Test
    void createOrderTrack_WithSpecialCharactersInNotes_ShouldCreateOrderTrack() throws Exception {
        // Arrange
        String specialNotes = "Order #123 - Package ready! 📦 Cost: $99.99 @ 2024-01-01";

        CreateOrderTrackRequest request = CreateOrderTrackRequest.builder()
                .orderId(testOrder.getId())
                .notes(specialNotes)
                .status(Status.PROCESSING)
                .build();

        // Act & Assert
        mockMvc.perform(post("/order-tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.notes").value(specialNotes))
                .andExpect(jsonPath("$.data.status").value("PROCESSING"))
                .andDo(print());
    }

    // Test multiple order tracks for one order
    @Test
    void createMultipleOrderTracks_ForSameOrder_ShouldCreateAllTracks() throws Exception {
        // Create additional order tracks for the same order
        CreateOrderTrackRequest request1 = CreateOrderTrackRequest.builder()
                .orderId(testOrder.getId())
                .notes("Order confirmed")
                .status(Status.PROCESSING)
                .build();

        CreateOrderTrackRequest request2 = CreateOrderTrackRequest.builder()
                .orderId(testOrder.getId())
                .notes("Order packaged")
                .status(Status.PACKAGED)
                .build();

        CreateOrderTrackRequest request3 = CreateOrderTrackRequest.builder()
                .orderId(testOrder.getId())
                .notes("Order shipped")
                .status(Status.SHIPPING)
                .build();

        // Create all tracks
        mockMvc.perform(post("/order-tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(post("/order-tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated())
                .andDo(print());

        mockMvc.perform(post("/order-tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isCreated())
                .andDo(print());

        // Verify all tracks exist for the order
        mockMvc.perform(get("/order-tracks/order/{orderId}", testOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(4))) // Original + 3 new tracks
                .andDo(print());
    }

    // Test data integrity
    @Test
    void createOrderTrack_ShouldMaintainDataIntegrity() throws Exception {
        // Arrange
        CreateOrderTrackRequest request = CreateOrderTrackRequest.builder()
                .orderId(testOrder.getId())
                .notes("New order track")
                .status(Status.PROCESSING)
                .build();

        // Act
        mockMvc.perform(post("/order-tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print());

        // Assert - Check that total order tracks count increased
        mockMvc.perform(get("/order-tracks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2))) // Original + new one
                .andDo(print());
    }

    // Test concurrent operations
    @Test
    @Transactional
    void concurrentOperations_ShouldHandleCorrectly() throws Exception {
        // Multiple operations on order tracks
        mockMvc.perform(get("/order-tracks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andDo(print());

        mockMvc.perform(get("/order-tracks/{id}", testOrderTrack.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testOrderTrack.getId()))
                .andDo(print());

        mockMvc.perform(get("/order-tracks/order/{orderId}", testOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andDo(print());

        mockMvc.perform(get("/order-tracks/status/{status}", "NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andDo(print());
    }

    // Test edge cases
    @Test
    void getAllOrderTracks_WithMultipleOrderTracks_ShouldReturnAllTracks() throws Exception {
        // Create additional order tracks
        OrderTrack orderTrack2 = OrderTrack.builder()
                .order(testOrder)
                .notes("Order confirmed")
                .status(Status.PROCESSING)
                .build();
        orderTrackRepository.save(orderTrack2);

        OrderTrack orderTrack3 = OrderTrack.builder()
                .order(testOrder)
                .notes("Order shipped")
                .status(Status.SHIPPING)
                .build();
        orderTrackRepository.save(orderTrack3);

        // Act & Assert
        mockMvc.perform(get("/order-tracks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andDo(print());
    }

    @Test
    void getOrderTracksByStatus_WithMultipleStatusMatches_ShouldReturnAllMatches() throws Exception {
        // Create additional order tracks with same status
        OrderTrack orderTrack2 = OrderTrack.builder()
                .order(testOrder)
                .notes("Another NEW order track")
                .status(Status.NEW)
                .build();
        orderTrackRepository.save(orderTrack2);

        // Act & Assert
        mockMvc.perform(get("/order-tracks/status/{status}", "NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andDo(print());
    }

    // Test validation edge cases
    @Test
    void createOrderTrack_WithBlankOrderId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateOrderTrackRequest request = CreateOrderTrackRequest.builder()
                .orderId("") // Blank order ID
                .notes("Test notes")
                .status(Status.PROCESSING)
                .build();

        // Act & Assert
        mockMvc.perform(post("/order-tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
}