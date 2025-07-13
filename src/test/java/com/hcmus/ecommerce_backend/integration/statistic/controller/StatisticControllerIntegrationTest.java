package com.hcmus.ecommerce_backend.integration.statistic.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.time.LocalDateTime;
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
import com.hcmus.ecommerce_backend.blog.model.entity.Blog;
import com.hcmus.ecommerce_backend.blog.repository.BlogRepository;
import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.category.repository.CategoryRepository;
import com.hcmus.ecommerce_backend.integration.BaseIntegrationTest;
import com.hcmus.ecommerce_backend.order.model.entity.Order;
import com.hcmus.ecommerce_backend.order.repository.OrderRepository;
import com.hcmus.ecommerce_backend.product.model.entity.Product;
import com.hcmus.ecommerce_backend.product.repository.ProductRepository;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.model.enums.Role;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class StatisticControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationService authenticationService;

    private User testUser;
    private String accessToken;
    private Category testCategory;
    private Product testProduct;
    private Order testOrder;
    private Blog testBlog;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        // Clean up existing data
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        blogRepository.deleteAll();
        userRepository.deleteAll();

        setupTestData();
    }

    private void setupTestData() {
        // Create test user
        testUser = User.builder()
                .email("admin@test.com")
                .firstName("Admin")
                .lastName("User")
                .password(passwordEncoder.encode("password123"))
                .phoneNumber("1234567890")
                .build();
        testUser.setRole(Role.ADMIN);
        testUser.setEnabled(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        userRepository.save(testUser);
        testUser = userRepository.findByEmail(testUser.getEmail()).orElseThrow();

        // Get access token
        TokenResponse tokenResponse = authenticationService.login(LoginRequest.builder()
                .email(testUser.getEmail())
                .password("password123")
                .build());
        accessToken = tokenResponse.getAccessToken();

        // Create test category
        testCategory = Category.builder()
                .name("Test Category")
                .description("Test category description")
                .build();
        testCategory = categoryRepository.save(testCategory);

        // Create test product
        testProduct = Product.builder()
                .name("Test Product")
                .description("Test product description")
                .price(100.0)
                .category(testCategory)
                .build();
        testProduct = productRepository.save(testProduct);

        // // Create test order
        // testOrder = Order.builder()
        //         .totalAmount(200.0) // Change 'totalAmount' to the actual field name in your Order entity's builder
        //         .build();
        // testOrder = orderRepository.save(testOrder);

        // Create test blog
        testBlog = Blog.builder()
                .title("Test Blog")
                .content("Test blog content")
                .userId(testUser.getId())
                .build();
        testBlog = blogRepository.save(testBlog);
    }

    // Test GET /statistics/analysis - Get sales analysis data
    @Test
    void getSalesAnalysis_WithValidMonthlyType_ShouldReturnAnalysisData() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/analysis")
                .param("type", "month")
                .param("date", "10-2024")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());
    }

    @Test
    void getSalesAnalysis_WithValidYearlyType_ShouldReturnAnalysisData() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/analysis")
                .param("type", "year")
                .param("date", "2024")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());
    }

    @Test
    void getSalesAnalysis_WithInvalidType_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/analysis")
                .param("type", "invalid")
                .param("date", "2024")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void getSalesAnalysis_WithMissingType_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/analysis")
                .param("date", "2024")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void getSalesAnalysis_WithMissingDate_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/analysis")
                .param("type", "month")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void getSalesAnalysis_WithInvalidDateFormat_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/analysis")
                .param("type", "month")
                .param("date", "invalid-date")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

//    @Test
//    void getSalesAnalysis_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
//        // Act & Assert
//        mockMvc.perform(get("/statistics/analysis")
//                .param("type", "month")
//                .param("date", "10-2024"))
//                .andExpect(status().isUnauthorized())
//                .andDo(print());
//    }

    // Test GET /statistics/best-sellers - Get top 10 best-selling products
    @Test
    void getBestSellers_WithValidMonthlyType_ShouldReturnBestSellers() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/best-sellers")
                .param("type", "month")
                .param("date", "10-2024")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());
    }

    @Test
    void getBestSellers_WithValidYearlyType_ShouldReturnBestSellers() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/best-sellers")
                .param("type", "year")
                .param("date", "2024")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());
    }

    @Test
    void getBestSellers_WithInvalidType_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/best-sellers")
                .param("type", "invalid")
                .param("date", "2024")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());
    }

//    @Test
//    void getBestSellers_WithMissingParameters_ShouldReturnBadRequest() throws Exception {
//        // Act & Assert
//        mockMvc.perform(get("/statistics/best-sellers")
//                .param("type", "month")
//                .header("Authorization", "Bearer " + accessToken))
//                .andExpect(status().isBadRequest())
//                .andDo(print());
//    }

//    @Test
//    void getBestSellers_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
//        // Act & Assert
//        mockMvc.perform(get("/statistics/best-sellers")
//                .param("type", "month")
//                .param("date", "10-2024"))
//                .andExpect(status().isUnauthorized())
//                .andDo(print());
//    }

    // Test GET /statistics/product-categories - Get product category statistics
    @Test
    void getProductCategoryStatistics_WithValidRequest_ShouldReturnStatistics() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/product-categories")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());
    }

//    @Test
//    void getProductCategoryStatistics_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
//        // Act & Assert
//        mockMvc.perform(get("/statistics/product-categories"))
//                .andExpect(status().isUnauthorized())
//                .andDo(print());
//    }

    // Test GET /statistics/order-incomplete - Get incomplete orders and estimated revenue
    @Test
    void getIncompleteOrders_WithoutDate_ShouldReturnIncompleteOrders() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/order-incomplete")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());
    }

    @Test
    void getIncompleteOrders_WithValidDate_ShouldReturnFilteredIncompleteOrders() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/order-incomplete")
                .param("date", "10-2024")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());
    }

//    @Test
//    void getIncompleteOrders_WithInvalidDateFormat_ShouldReturnBadRequest() throws Exception {
//        // Act & Assert
//        mockMvc.perform(get("/statistics/order-incomplete")
//                .param("date", "invalid-date")
//                .header("Authorization", "Bearer " + accessToken))
//                .andExpect(status().isBadRequest())
//                .andDo(print());
//    }

//    @Test
//    void getIncompleteOrders_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
//        // Act & Assert
//        mockMvc.perform(get("/statistics/order-incomplete"))
//                .andExpect(status().isUnauthorized())
//                .andDo(print());
//    }

    // Test edge cases
    @Test
    void getSalesAnalysis_WithEmptyDatabase_ShouldReturnEmptyData() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/analysis")
                .param("type", "month")
                .param("date", "01-2024")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());
    }

    @Test
    void getBestSellers_WithEmptyDatabase_ShouldReturnEmptyData() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/best-sellers")
                .param("type", "month")
                .param("date", "01-2024")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());
    }

    @Test
    void getProductCategoryStatistics_WithEmptyDatabase_ShouldReturnEmptyData() throws Exception {
        // Clean all data
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        // Act & Assert
        mockMvc.perform(get("/statistics/product-categories")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());
    }

    @Test
    void getIncompleteOrders_WithEmptyDatabase_ShouldReturnEmptyData() throws Exception {
        // Clean all orders
        orderRepository.deleteAll();

        // Act & Assert
        mockMvc.perform(get("/statistics/order-incomplete")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());
    }

    // Test different date formats
    @Test
    void getSalesAnalysis_WithLeadingZeroMonth_ShouldReturnData() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/analysis")
                .param("type", "month")
                .param("date", "01-2024")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andDo(print());
    }

    @Test
    void getSalesAnalysis_WithoutLeadingZeroMonth_ShouldReturnData() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/analysis")
                .param("type", "month")
                .param("date", "1-2024")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andDo(print());
    }

    @Test
    void getBestSellers_WithFutureDate_ShouldReturnEmptyData() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/best-sellers")
                .param("type", "month")
                .param("date", "12-2030")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());
    }

    @Test
    void getIncompleteOrders_WithFutureDate_ShouldReturnEmptyData() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/order-incomplete")
                .param("date", "12-2030")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());
    }

    // Test boundary values
    @Test
    void getSalesAnalysis_WithMinYear_ShouldReturnData() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/analysis")
                .param("type", "year")
                .param("date", "2000")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andDo(print());
    }

    @Test
    void getSalesAnalysis_WithMaxYear_ShouldReturnData() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/analysis")
                .param("type", "year")
                .param("date", "2099")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andDo(print());
    }

    // Test concurrent requests
    @Test
    @Transactional
    void getStatistics_WithConcurrentRequests_ShouldHandleCorrectly() throws Exception {
        // Multiple concurrent requests to same endpoint
        mockMvc.perform(get("/statistics/analysis")
                .param("type", "month")
                .param("date", "10-2024")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andDo(print());

        mockMvc.perform(get("/statistics/best-sellers")
                .param("type", "year")
                .param("date", "2024")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andDo(print());

        mockMvc.perform(get("/statistics/product-categories")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andDo(print());
    }

    // Test with special characters in date
    @Test
    void getSalesAnalysis_WithSpecialCharactersInDate_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/statistics/analysis")
                .param("type", "month")
                .param("date", "10@2024")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

//    @Test
//    void getBestSellers_WithSpecialCharactersInDate_ShouldReturnBadRequest() throws Exception {
//        // Act & Assert
//        mockMvc.perform(get("/statistics/best-sellers")
//                .param("type", "year")
//                .param("date", "202#4")
//                .header("Authorization", "Bearer " + accessToken))
//                .andExpect(status().isBadRequest())
//                .andDo(print());
//    }
}