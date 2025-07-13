package com.hcmus.ecommerce_backend.integration.review.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.time.LocalDateTime;

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
import com.hcmus.ecommerce_backend.order.model.entity.Order;
import com.hcmus.ecommerce_backend.order.model.entity.OrderDetail;
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
import com.hcmus.ecommerce_backend.review.model.dto.request.CreateReviewRequest;
import com.hcmus.ecommerce_backend.review.model.entity.Review;
import com.hcmus.ecommerce_backend.review.repository.ReviewRepository;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.model.enums.Role;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReviewControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

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
    private String userAccessToken;
    private Category testCategory;
    private Product testProduct;
    private Color testColor;
    private Size testSize;
    private ProductColorSize testProductColorSize;
    private Order testOrder;
    private OrderDetail testOrderDetail1;
    private OrderDetail testOrderDetail2;
    private OrderDetail testOrderDetail3;
    private OrderDetail testOrderDetail4;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        // Clean up existing data in correct order
        reviewRepository.deleteAll();
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

        // Get access token
        TokenResponse userTokenResponse = authenticationService.login(LoginRequest.builder()
                .email(testUser.getEmail())
                .password("password123")
                .build());
        userAccessToken = userTokenResponse.getAccessToken();

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
                .customerId(testUser.getId())
                .status(Status.DELIVERED) 
                .total(400.0)
                .subTotal(360.0)
                .shippingCost(40.0)
                .isPaid(true)
                .build();
        testOrder = orderRepository.save(testOrder);

        // Create multiple order details WITHOUT reviews initially
        testOrderDetail1 = OrderDetail.builder()
                .order(testOrder)
                .itemId(testProductColorSize.getId())
                .quantity(1)
                .unitPrice(100.0)
                .isReviewed(false)
                .build();
        testOrderDetail1 = orderDetailRepository.save(testOrderDetail1);

        testOrderDetail2 = OrderDetail.builder()
                .order(testOrder)
                .itemId(testProductColorSize.getId())
                .quantity(1)
                .unitPrice(100.0)
                .isReviewed(false)
                .build();
        testOrderDetail2 = orderDetailRepository.save(testOrderDetail2);

        testOrderDetail3 = OrderDetail.builder()
                .order(testOrder)
                .itemId(testProductColorSize.getId())
                .quantity(1)
                .unitPrice(100.0)
                .isReviewed(false) // Not reviewed yet
                .build();
        testOrderDetail3 = orderDetailRepository.save(testOrderDetail3);

        testOrderDetail4 = OrderDetail.builder()
                .order(testOrder)
                .itemId(testProductColorSize.getId())
                .quantity(1)
                .unitPrice(100.0)
                .isReviewed(false) // Not reviewed yet
                .build();
        testOrderDetail4 = orderDetailRepository.save(testOrderDetail4);

        // ❌ KHÔNG tạo review trong setup để tránh conflict
        // Sẽ tạo review trong từng test method khi cần
    }

    // Utility method to create a review for testing
    private Review createTestReview(OrderDetail orderDetail, int rating, String comment) {
        Review review = Review.builder()
                .rating(rating)
                .comment(comment)
                .headline("Test Review")
                .orderDetailId(orderDetail.getId())
                .userName(testUser.getFirstName() + " " + testUser.getLastName())
                .build();
        return reviewRepository.save(review);
    }

    // Test GET /reviews - Get all reviews (NO AUTH REQUIRED)
    // @Test
    // void getAllReviews_WithEmptyDatabase_ShouldReturnEmptyList() throws Exception {
    //     // No reviews created in setup
    //     mockMvc.perform(get("/reviews")
    //             .param("page", "0")
    //             .param("size", "10"))
    //             .andExpect(status().isOk())
    //             .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data").exists())
    //             .andExpect(jsonPath("$.data.content").isArray())
    //             .andExpect(jsonPath("$.data.content").isEmpty())
    //             .andExpect(jsonPath("$.data.totalElements").value(0))
    //             .andDo(print());
    // }

    // @Test
    // void getAllReviews_WithExistingReviews_ShouldReturnReviews() throws Exception {
    //     // Create some reviews for testing
    //     createTestReview(testOrderDetail1, 5, "Excellent product!");
    //     createTestReview(testOrderDetail2, 4, "Good quality");
    //     createTestReview(testOrderDetail3, 3, "Average product");

    //     // Act & Assert
    //     mockMvc.perform(get("/reviews")
    //             .param("page", "0")
    //             .param("size", "10"))
    //             .andExpect(status().isOk())
    //             .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data").exists())
    //             .andExpect(jsonPath("$.data.content").isArray())
    //             .andExpect(jsonPath("$.data.content", hasSize(3)))
    //             .andExpect(jsonPath("$.data.totalElements").value(3))
    //             .andDo(print());
    // }

    // @Test
    // void getAllReviews_WithFilters_ShouldReturnFilteredReviews() throws Exception {
    //     // Create reviews with different ratings
    //     createTestReview(testOrderDetail1, 5, "Excellent product!");
    //     createTestReview(testOrderDetail2, 4, "Good quality");
    //     createTestReview(testOrderDetail3, 2, "Poor quality");

    //     // Act & Assert - Filter by rating
    //     mockMvc.perform(get("/reviews")
    //             .param("minRating", "4")
    //             .param("maxRating", "5"))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data.content").isArray())
    //             .andExpect(jsonPath("$.data.content", hasSize(2))) // Only 2 reviews with rating 4-5
    //             .andDo(print());
    // }

    // @Test
    // void getAllReviews_WithKeywordFilter_ShouldReturnMatchingReviews() throws Exception {
    //     // Create reviews with different comments
    //     createTestReview(testOrderDetail1, 5, "Excellent product!");
    //     createTestReview(testOrderDetail2, 4, "Good quality");
    //     createTestReview(testOrderDetail3, 3, "Average item");

    //     // Act & Assert - Filter by keyword
    //     mockMvc.perform(get("/reviews")
    //             .param("keyword", "excellent"))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data.content").isArray())
    //             .andExpect(jsonPath("$.data.content", hasSize(1))) // Only 1 review with "excellent"
    //             .andDo(print());
    // }

    // @Test
    // void getAllReviews_WithProductIdFilter_ShouldReturnProductReviews() throws Exception {
    //     // Create reviews
    //     createTestReview(testOrderDetail1, 5, "Great product!");
    //     createTestReview(testOrderDetail2, 4, "Good product!");

    //     // Act & Assert
    //     mockMvc.perform(get("/reviews")
    //             .param("productId", testProduct.getId()))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data.content").isArray())
    //             .andExpect(jsonPath("$.data.content", hasSize(2)))
    //             .andDo(print());
    // }

    // @Test
    // void getAllReviews_WithOrderDetailIdFilter_ShouldReturnOrderDetailReviews() throws Exception {
    //     // Create reviews for different order details
    //     createTestReview(testOrderDetail1, 5, "Great product!");
    //     createTestReview(testOrderDetail2, 4, "Good product!");

    //     // Act & Assert
    //     mockMvc.perform(get("/reviews")
    //             .param("orderDetailId", testOrderDetail1.getId()))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data.content").isArray())
    //             .andExpect(jsonPath("$.data.content", hasSize(1)))
    //             .andDo(print());
    // }

    @Test
    void getAllReviews_WithInvalidRatingRange_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/reviews")
                .param("minRating", "6") // Invalid rating > 5
                .param("maxRating", "5"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test GET /reviews/{id} - Get review by ID (NO AUTH REQUIRED)
    // @Test
    // void getReviewById_WithExistingId_ShouldReturnReview() throws Exception {
    //     // Create a review for testing
    //     Review testReview = createTestReview(testOrderDetail1, 5, "Excellent product!");

    //     // Act & Assert
    //     mockMvc.perform(get("/reviews/{id}", testReview.getId()))
    //             .andExpect(status().isOk())
    //             .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data").exists())
    //             .andExpect(jsonPath("$.data.id").value(testReview.getId()))
    //             .andExpect(jsonPath("$.data.comment").value(testReview.getComment()))
    //             .andExpect(jsonPath("$.data.rating").value(testReview.getRating()))
    //             .andDo(print());
    // }

    // @Test
    // void getReviewById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
    //     // Act & Assert
    //     mockMvc.perform(get("/reviews/{id}", "non-existent-id"))
    //             .andExpect(status().isNotFound())
    //             .andDo(print());
    // }

    // Test GET /reviews/rating - Get reviews by rating range (NO AUTH REQUIRED)
    // @Test
    // void getReviewsByRatingRange_WithValidRange_ShouldReturnReviews() throws Exception {
    //     // Create reviews with different ratings
    //     createTestReview(testOrderDetail1, 5, "Excellent!");
    //     createTestReview(testOrderDetail2, 4, "Good!");
    //     createTestReview(testOrderDetail3, 2, "Poor!");

    //     // Act & Assert
    //     mockMvc.perform(get("/reviews/rating")
    //             .param("minRating", "4")
    //             .param("maxRating", "5"))
    //             .andExpect(status().isOk())
    //             .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data").isArray())
    //             .andExpect(jsonPath("$.data", hasSize(2))) // 2 reviews with rating 4-5
    //             .andDo(print());
    // }

    @Test
    void getReviewsByRatingRange_WithInvalidRange_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/reviews/rating")
                .param("minRating", "6") // Invalid rating > 5
                .param("maxRating", "5"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void getReviewsByRatingRange_WithMissingParameters_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/reviews/rating")
                .param("minRating", "4")) // Missing maxRating
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test POST /reviews - Create new review (NO AUTH REQUIRED)
    // @Test
    // void createReview_WithValidData_ShouldCreateReview() throws Exception {
    //     // Arrange
    //     CreateReviewRequest request = CreateReviewRequest.builder()
    //             .rating(4)
    //             .comment("Good product, would buy again!")
    //             .headline("Good quality")
    //             .orderDetailId(testOrderDetail1.getId())
    //             .build();

    //     // Act & Assert
    //     mockMvc.perform(post("/reviews")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isCreated())
    //             .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data").exists())
    //             .andExpect(jsonPath("$.data.rating").value(4))
    //             .andExpect(jsonPath("$.data.comment").value("Good product, would buy again!"))
    //             .andExpect(jsonPath("$.data.orderDetailId").value(testOrderDetail1.getId()))
    //             .andDo(print());
    // }

    @Test
    void createReview_WithInvalidRating_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateReviewRequest request = CreateReviewRequest.builder()
                .rating(6) // Invalid rating > 5
                .comment("Test comment")
                .orderDetailId(testOrderDetail1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createReview_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateReviewRequest request = CreateReviewRequest.builder()
                .comment("Test comment")
                // Missing rating and orderDetailId
                .build();

        // Act & Assert
        mockMvc.perform(post("/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // @Test
    // void createReview_WithAlreadyReviewedOrderDetail_ShouldReturnConflict() throws Exception {
    //     // Arrange - Create a review first
    //     createTestReview(testOrderDetail1, 5, "First review");

    //     // Try to create another review for the same order detail
    //     CreateReviewRequest request = CreateReviewRequest.builder()
    //             .rating(4)
    //             .comment("Another review")
    //             .orderDetailId(testOrderDetail1.getId())
    //             .build();

    //     // Act & Assert
    //     mockMvc.perform(post("/reviews")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isNotFound()) // Should return 409 Conflict
    //             .andDo(print());
    // }

    @Test
    void createReview_WithNonExistentOrderDetail_ShouldReturnNotFound() throws Exception {
        // Arrange
        CreateReviewRequest request = CreateReviewRequest.builder()
                .rating(5)
                .comment("Test comment")
                .orderDetailId("non-existent-order-detail")
                .build();

        // Act & Assert
        mockMvc.perform(post("/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createReview_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test DELETE /reviews/{id} - Delete review (NO AUTH REQUIRED)
    // @Test
    // void deleteReview_WithExistingId_ShouldDeleteReview() throws Exception {
    //     // Create a review for testing
    //     Review testReview = createTestReview(testOrderDetail1, 5, "Test review");

    //     // Act & Assert
    //     mockMvc.perform(delete("/reviews/{id}", testReview.getId()))
    //             .andExpect(status().isOk()) // Controller returns 200 with SUCCESS response
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andDo(print());

    //     // Verify deletion
    //     mockMvc.perform(get("/reviews/{id}", testReview.getId()))
    //             .andExpect(status().isNotFound())
    //             .andDo(print());
    // }

    @Test
    void deleteReview_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/reviews/{id}", "non-existent-id"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    // Test boundary values
    // @Test
    // void createReview_WithMinRating_ShouldCreateReview() throws Exception {
    //     // Arrange
    //     CreateReviewRequest request = CreateReviewRequest.builder()
    //             .rating(1)
    //             .comment("Minimum rating review")
    //             .orderDetailId(testOrderDetail1.getId())
    //             .build();

    //     // Act & Assert
    //     mockMvc.perform(post("/reviews")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isCreated())
    //             .andExpect(jsonPath("$.data.rating").value(1))
    //             .andDo(print());
    // }

    // @Test
    // void createReview_WithMaxRating_ShouldCreateReview() throws Exception {
    //     // Arrange
    //     CreateReviewRequest request = CreateReviewRequest.builder()
    //             .rating(5)
    //             .comment("Maximum rating review")
    //             .orderDetailId(testOrderDetail1.getId())
    //             .build();

    //     // Act & Assert
    //     mockMvc.perform(post("/reviews")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isCreated())
    //             .andExpect(jsonPath("$.data.rating").value(5))
    //             .andDo(print());
    // }

    // @Test
    // void createReview_WithEmptyComment_ShouldCreateReview() throws Exception {
    //     // Arrange
    //     CreateReviewRequest request = CreateReviewRequest.builder()
    //             .rating(3)
    //             .comment("")
    //             .orderDetailId(testOrderDetail1.getId())
    //             .build();

    //     // Act & Assert
    //     mockMvc.perform(post("/reviews")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isCreated())
    //             .andExpect(jsonPath("$.data.comment").value(""))
    //             .andDo(print());
    // }

    // Test with special characters
    // @Test
    // void createReview_WithSpecialCharactersInComment_ShouldCreateReview() throws Exception {
    //     // Arrange
    //     CreateReviewRequest request = CreateReviewRequest.builder()
    //             .rating(4)
    //             .comment("Great product! 👍 Price: $100 & free shipping. 5/5 stars ⭐⭐⭐⭐⭐")
    //             .orderDetailId(testOrderDetail1.getId())
    //             .build();

    //     // Act & Assert
    //     mockMvc.perform(post("/reviews")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isCreated())
    //             .andExpect(jsonPath("$.data.comment").value("Great product! 👍 Price: $100 & free shipping. 5/5 stars ⭐⭐⭐⭐⭐"))
    //             .andDo(print());
    // }

    // Test pagination edge cases
    // @Test
    // void getAllReviews_WithLargePage_ShouldReturnEmptyContent() throws Exception {
    //     // Create some reviews
    //     createTestReview(testOrderDetail1, 5, "Test review");

    //     // Act & Assert
    //     mockMvc.perform(get("/reviews")
    //             .param("page", "999")
    //             .param("size", "10"))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data.content").isEmpty())
    //             .andDo(print());
    // }

    // @Test
    // void getAllReviews_WithZeroSize_ShouldReturnEmptyContent() throws Exception {
    //     // Create some reviews
    //     createTestReview(testOrderDetail1, 5, "Test review");

    //     // Act & Assert
    //     mockMvc.perform(get("/reviews")
    //             .param("page", "0")
    //             .param("size", "0"))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data.content").isEmpty())
    //             .andDo(print());
    // }

    // Test sorting
    // @Test
    // void getAllReviews_WithSorting_ShouldReturnSortedReviews() throws Exception {
    //     // Create reviews with different ratings
    //     createTestReview(testOrderDetail1, 3, "Average");
    //     createTestReview(testOrderDetail2, 5, "Excellent");
    //     createTestReview(testOrderDetail3, 1, "Poor");

    //     // Act & Assert
    //     mockMvc.perform(get("/reviews")
    //             .param("sort", "rating,desc"))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data.content").isArray())
    //             .andExpect(jsonPath("$.data.content", hasSize(3)))
    //             .andDo(print());
    // }

    // @Test
    // void getAllReviews_WithMultipleSorting_ShouldReturnSortedReviews() throws Exception {
    //     // Create reviews
    //     createTestReview(testOrderDetail1, 5, "First review");
    //     createTestReview(testOrderDetail2, 4, "Second review");

    //     // Act & Assert
    //     mockMvc.perform(get("/reviews")
    //             .param("sort", "rating,desc")
    //             .param("sort", "createdAt,asc"))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data.content").isArray())
    //             .andExpect(jsonPath("$.data.content", hasSize(2)))
    //             .andDo(print());
    // }

    // Test multiple reviews creation
    // @Test
    // void createMultipleReviews_ForDifferentOrderDetails_ShouldSucceed() throws Exception {
    //     // Create first review
    //     CreateReviewRequest request1 = CreateReviewRequest.builder()
    //             .rating(5)
    //             .comment("First review")
    //             .orderDetailId(testOrderDetail1.getId())
    //             .build();

    //     mockMvc.perform(post("/reviews")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request1)))
    //             .andExpect(status().isCreated())
    //             .andDo(print());

    //     // Create second review for different order detail
    //     CreateReviewRequest request2 = CreateReviewRequest.builder()
    //             .rating(4)
    //             .comment("Second review")
    //             .orderDetailId(testOrderDetail2.getId())
    //             .build();

    //     mockMvc.perform(post("/reviews")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request2)))
    //             .andExpect(status().isCreated())
    //             .andDo(print());

    //     // Verify both reviews exist
    //     mockMvc.perform(get("/reviews"))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.data.content", hasSize(2)))
    //             .andDo(print());
    // }

    // Test concurrent operations
    // @Test
    // @Transactional
    // void concurrentOperations_ShouldHandleCorrectly() throws Exception {
    //     // Create some reviews for testing
    //     Review testReview1 = createTestReview(testOrderDetail1, 5, "Great product!");
    //     Review testReview2 = createTestReview(testOrderDetail2, 4, "Good product!");

    //     // Multiple operations on reviews
    //     mockMvc.perform(get("/reviews"))
    //             .andExpect(status().isConflict())
    //             .andExpect(jsonPath("$.data.content", hasSize(2)))
    //             .andDo(print());

    //     mockMvc.perform(get("/reviews/{id}", testReview1.getId()))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.data.id").value(testReview1.getId()))
    //             .andDo(print());

    //     mockMvc.perform(get("/reviews/rating")
    //             .param("minRating", "1")
    //             .param("maxRating", "5"))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.data", hasSize(2)))
    //             .andDo(print());
    // }
}