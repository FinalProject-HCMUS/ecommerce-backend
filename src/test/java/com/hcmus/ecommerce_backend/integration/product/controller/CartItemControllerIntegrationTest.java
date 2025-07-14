package com.hcmus.ecommerce_backend.integration.product.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmus.ecommerce_backend.integration.BaseIntegrationTest;
import com.hcmus.ecommerce_backend.product.model.dto.request.cart_item.CreateCartItemRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.cart_item.UpdateCartItemRequest;
import com.hcmus.ecommerce_backend.product.model.entity.CartItem;
import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.product.model.entity.Color;
import com.hcmus.ecommerce_backend.product.model.entity.Product;
import com.hcmus.ecommerce_backend.product.model.entity.ProductColorSize;
import com.hcmus.ecommerce_backend.product.model.entity.Size;
import com.hcmus.ecommerce_backend.product.repository.CartItemRepository;
import com.hcmus.ecommerce_backend.category.repository.CategoryRepository;
import com.hcmus.ecommerce_backend.product.repository.ColorRepository;
import com.hcmus.ecommerce_backend.product.repository.ProductColorSizeRepository;
import com.hcmus.ecommerce_backend.product.repository.ProductRepository;
import com.hcmus.ecommerce_backend.product.repository.SizeRepository;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
    "spring.security.enabled=false"
})
@WithMockUser(username = "testuser", roles = {"USER", "ADMIN"})
public class CartItemControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private ProductColorSizeRepository productColorSizeRepository;

    private CartItem testCartItem1;
    private CartItem testCartItem2;
    private CartItem testCartItem3;
    private User testUser1;
    private User testUser2;
    private ProductColorSize testProductColorSize1;
    private ProductColorSize testProductColorSize2;
    private Product testProduct1;
    private Product testProduct2;
    private Category testCategory;
    private Color testColor;
    private Size testSize;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        // Clean up existing data in correct order
        cartItemRepository.deleteAll();
        productColorSizeRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        colorRepository.deleteAll();
        sizeRepository.deleteAll();
        userRepository.deleteAll();

        setupTestData();
    }

    private void setupTestData() {
        // Create test users
        testUser1 = User.builder()
                .email("testuser1@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .enabled(true)
                .build();
        testUser1.setCreatedAt(LocalDateTime.now());
        testUser1.setUpdatedAt(LocalDateTime.now());
        testUser1 = userRepository.save(testUser1);

        testUser2 = User.builder()
                .email("testuser2@example.com")
                .password("password123")
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("0987654321")
                .enabled(true)
                .build();
        testUser2.setCreatedAt(LocalDateTime.now());
        testUser2.setUpdatedAt(LocalDateTime.now());
        testUser2 = userRepository.save(testUser2);

        // Create test category
        testCategory = Category.builder()
                .name("Test Category")
                .description("Test category description")
                .build();
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());
        testCategory = categoryRepository.save(testCategory);

        // Create test color
        testColor = Color.builder()
                .name("Red")
                .code("#FF0000")
                .build();
        testColor.setCreatedAt(LocalDateTime.now());
        testColor.setUpdatedAt(LocalDateTime.now());
        testColor = colorRepository.save(testColor);

        // Create test size
        testSize = Size.builder()
                .name("M")
                .minHeight(165)
                .maxHeight(175)
                .minWeight(60)
                .maxWeight(70)
                .build();
        testSize.setCreatedAt(LocalDateTime.now());
        testSize.setUpdatedAt(LocalDateTime.now());
        testSize = sizeRepository.save(testSize);

        // Create test products
        testProduct1 = Product.builder()
                .name("Test Product 1")
                .description("Test product 1 description")
                .price(100.0)
                .category(testCategory)
                .enable(true)
                .build();
        testProduct1.setCreatedAt(LocalDateTime.now());
        testProduct1.setUpdatedAt(LocalDateTime.now());
        testProduct1 = productRepository.save(testProduct1);

        testProduct2 = Product.builder()
                .name("Test Product 2")
                .description("Test product 2 description")
                .price(200.0)
                .category(testCategory)
                .enable(true)
                .build();
        testProduct2.setCreatedAt(LocalDateTime.now());
        testProduct2.setUpdatedAt(LocalDateTime.now());
        testProduct2 = productRepository.save(testProduct2);

        // Create test product color sizes
        testProductColorSize1 = ProductColorSize.builder()
                .product(testProduct1)
                .color(testColor)
                .size(testSize)
                .quantity(20)
                .build();
        testProductColorSize1.setCreatedAt(LocalDateTime.now());
        testProductColorSize1.setUpdatedAt(LocalDateTime.now());
        testProductColorSize1 = productColorSizeRepository.save(testProductColorSize1);

        testProductColorSize2 = ProductColorSize.builder()
                .product(testProduct2)
                .color(testColor)
                .size(testSize)
                .quantity(15)
                .build();
        testProductColorSize2.setCreatedAt(LocalDateTime.now());
        testProductColorSize2.setUpdatedAt(LocalDateTime.now());
        testProductColorSize2 = productColorSizeRepository.save(testProductColorSize2);

        // Create test cart items
        testCartItem1 = CartItem.builder()
                .quantity(2)
                .userId(testUser1.getId())
                .itemId(testProductColorSize1.getId())
                .build();
        testCartItem1.setCreatedAt(LocalDateTime.now());
        testCartItem1.setUpdatedAt(LocalDateTime.now());
        testCartItem1 = cartItemRepository.save(testCartItem1);

        testCartItem2 = CartItem.builder()
                .quantity(1)
                .userId(testUser1.getId())
                .itemId(testProductColorSize2.getId())
                .build();
        testCartItem2.setCreatedAt(LocalDateTime.now());
        testCartItem2.setUpdatedAt(LocalDateTime.now());
        testCartItem2 = cartItemRepository.save(testCartItem2);

        testCartItem3 = CartItem.builder()
                .quantity(3)
                .userId(testUser2.getId())
                .itemId(testProductColorSize1.getId())
                .build();
        testCartItem3.setCreatedAt(LocalDateTime.now());
        testCartItem3.setUpdatedAt(LocalDateTime.now());
        testCartItem3 = cartItemRepository.save(testCartItem3);
    }

    // Test GET /cart-items - Get all cart items with pagination
    @Test
    void getAllCartItems_WithDefaultPagination_ShouldReturnPaginatedCartItems() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/cart-items"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.number").value(0))
                .andExpect(jsonPath("$.data.content[0].id").exists())
                .andExpect(jsonPath("$.data.content[0].quantity").exists())
                .andExpect(jsonPath("$.data.content[0].userId").exists())
                .andExpect(jsonPath("$.data.content[0].itemId").exists())
                .andDo(print());
    }

    @Test
    void getAllCartItems_WithCustomPagination_ShouldReturnPaginatedCartItems() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/cart-items")
                .param("page", "0")
                .param("size", "2")
                .param("sort", "quantity,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.number").value(0))
                .andDo(print());
    }

    @Test
    void getAllCartItems_WithEmptyDatabase_ShouldReturnEmptyPage() throws Exception {
        // Clean up test data
        cartItemRepository.deleteAll();

        // Act & Assert
        mockMvc.perform(get("/cart-items"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.totalPages").value(0))
                .andDo(print());
    }

    // Test GET /cart-items/{id} - Get cart item by ID
    @Test
    void getCartItemById_WithExistingId_ShouldReturnCartItem() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/cart-items/{id}", testCartItem1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(testCartItem1.getId()))
                .andExpect(jsonPath("$.data.quantity").value(2))
                .andExpect(jsonPath("$.data.userId").value(testUser1.getId()))
                .andExpect(jsonPath("$.data.itemId").value(testProductColorSize1.getId()))
                .andDo(print());
    }

    @Test
    void getCartItemById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/cart-items/{id}", "non-existent-id"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void getCartItemsByUserId_WithNonExistentUserId_ShouldReturnEmptyList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/cart-items/user/{userId}", "non-existent-user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());
    }

    @Test
    void getCartItemsByUserId_WithUserWithoutCartItems_ShouldReturnEmptyList() throws Exception {
        // Create a user without cart items
        User userWithoutCartItems = User.builder()
                .email("empty@example.com")
                .password("password123")
                .firstName("Empty")
                .lastName("User")
                .phoneNumber("1111111111")
                .enabled(true)
                .build();
        userWithoutCartItems.setCreatedAt(LocalDateTime.now());
        userWithoutCartItems.setUpdatedAt(LocalDateTime.now());
        userWithoutCartItems = userRepository.save(userWithoutCartItems);

        // Act & Assert
        mockMvc.perform(get("/cart-items/user/{userId}", userWithoutCartItems.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());
    }

    // Test POST /cart-items - Create new cart item
    @Test
    void createCartItem_WithValidData_ShouldCreateCartItem() throws Exception {
        // Arrange
        CreateCartItemRequest request = CreateCartItemRequest.builder()
                .quantity(5)
                .userId(testUser2.getId())
                .itemId(testProductColorSize2.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/cart-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.quantity").value(5))
                .andExpect(jsonPath("$.data.userId").value(testUser2.getId()))
                .andExpect(jsonPath("$.data.itemId").value(testProductColorSize2.getId()))
                .andDo(print());
    }

    @Test
    void createCartItem_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateCartItemRequest request = CreateCartItemRequest.builder()
                .quantity(2)
                // Missing userId and itemId
                .build();

        // Act & Assert
        mockMvc.perform(post("/cart-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createCartItem_WithInvalidQuantity_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateCartItemRequest request = CreateCartItemRequest.builder()
                .quantity(0) // Invalid quantity
                .userId(testUser1.getId())
                .itemId(testProductColorSize1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/cart-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createCartItem_WithNegativeQuantity_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateCartItemRequest request = CreateCartItemRequest.builder()
                .quantity(-1) // Negative quantity
                .userId(testUser1.getId())
                .itemId(testProductColorSize1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/cart-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createCartItem_WithEmptyIds_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateCartItemRequest request = CreateCartItemRequest.builder()
                .quantity(2)
                .userId("") // Empty userId
                .itemId("") // Empty itemId
                .build();

        // Act & Assert
        mockMvc.perform(post("/cart-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createCartItem_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/cart-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test PUT /cart-items/{id} - Update cart item
    @Test
    void updateCartItem_WithValidData_ShouldUpdateCartItem() throws Exception {
        // Arrange
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(10)
                .userId(testUser1.getId())
                .itemId(testProductColorSize1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(put("/cart-items/{id}", testCartItem1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(testCartItem1.getId()))
                .andExpect(jsonPath("$.data.quantity").value(10))
                .andExpect(jsonPath("$.data.userId").value(testUser1.getId()))
                .andExpect(jsonPath("$.data.itemId").value(testProductColorSize1.getId()))
                .andDo(print());
    }

    @Test
    void updateCartItem_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Arrange
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(5)
                .userId(testUser1.getId())
                .itemId(testProductColorSize1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(put("/cart-items/{id}", "non-existent-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void updateCartItem_WithInvalidQuantity_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(0) // Invalid quantity
                .userId(testUser1.getId())
                .itemId(testProductColorSize1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(put("/cart-items/{id}", testCartItem1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void updateCartItem_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/cart-items/{id}", testCartItem1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test DELETE /cart-items/{id} - Delete cart item
    @Test
    void deleteCartItem_WithExistingId_ShouldDeleteCartItem() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/cart-items/{id}", testCartItem1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andDo(print());

        // Verify deletion
        mockMvc.perform(get("/cart-items/{id}", testCartItem1.getId()))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void deleteCartItem_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/cart-items/{id}", "non-existent-id"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    // Test DELETE /cart-items/user/{userId}/item/{itemId} - Delete cart item by user and item
    @Test
    void deleteCartItemByUserIdAndItemId_WithExistingUserAndItem_ShouldDeleteCartItem() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/cart-items/user/{userId}/item/{itemId}", 
                testUser1.getId(), testProductColorSize1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andDo(print());

        // Verify deletion
        mockMvc.perform(get("/cart-items/{id}", testCartItem1.getId()))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void deleteCartItemByUserIdAndItemId_WithNonExistentUser_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/cart-items/user/{userId}/item/{itemId}", 
                "non-existent-user", testProductColorSize1.getId()))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void deleteCartItemByUserIdAndItemId_WithNonExistentItem_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/cart-items/user/{userId}/item/{itemId}", 
                testUser1.getId(), "non-existent-item"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void deleteCartItemByUserIdAndItemId_WithNonExistentCombination_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/cart-items/user/{userId}/item/{itemId}", 
                testUser2.getId(), testProductColorSize2.getId())) // This combination doesn't exist
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    // Test data integrity
    @Test
    void createCartItem_ShouldMaintainDataIntegrity() throws Exception {
        // Arrange
        CreateCartItemRequest request = CreateCartItemRequest.builder()
                .quantity(4)
                .userId(testUser2.getId())
                .itemId(testProductColorSize2.getId())
                .build();

        // Act
        mockMvc.perform(post("/cart-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print());

        // Assert - Check that total cart items count increased
        mockMvc.perform(get("/cart-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(4)) // Original 3 + new 1
                .andDo(print());
    }

    // Test concurrent operations
    // @Test
    // @Transactional
    // void concurrentOperations_ShouldHandleCorrectly() throws Exception {
    //     // Multiple operations on cart items
    //     mockMvc.perform(get("/cart-items"))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.data.totalElements").value(3))
    //             .andDo(print());

    //     mockMvc.perform(get("/cart-items/{id}", testCartItem1.getId()))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.data.id").value(testCartItem1.getId()))
    //             .andDo(print());

    //     mockMvc.perform(get("/cart-items/user/{userId}", testUser1.getId()))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.data", hasSize(2)))
    //             .andDo(print());
    // }

    // Test boundary values
    @Test
    void createCartItem_WithMinQuantity_ShouldCreateCartItem() throws Exception {
        // Arrange
        CreateCartItemRequest request = CreateCartItemRequest.builder()
                .quantity(1) // Minimum valid quantity
                .userId(testUser2.getId())
                .itemId(testProductColorSize2.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/cart-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.quantity").value(1))
                .andDo(print());
    }

    @Test
    void createCartItem_WithMaxQuantity_ShouldCreateCartItem() throws Exception {
        // Arrange
        CreateCartItemRequest request = CreateCartItemRequest.builder()
                .quantity(20) // Maximum available quantity
                .userId(testUser2.getId())
                .itemId(testProductColorSize1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/cart-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.quantity").value(20))
                .andDo(print());
    }

    // Test pagination edge cases
    @Test
    void getAllCartItems_WithPageBeyondRange_ShouldReturnEmptyPage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/cart-items")
                .param("page", "10")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.number").value(10))
                .andDo(print());
    }

    @Test
    void getAllCartItems_WithLargePageSize_ShouldReturnAllCartItems() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/cart-items")
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.size").value(100))
                .andDo(print());
    }

    // Test sorting
    @Test
    void getAllCartItems_WithMultipleSortFields_ShouldReturnSortedResults() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/cart-items")
                .param("sort", "userId,asc", "quantity,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andDo(print());
    }

    // Test null values handling
    @Test
    void createCartItem_WithNullValues_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/cart-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":null,\"userId\":null,\"itemId\":null}"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test edge case: update cart item with same data
    @Test
    void updateCartItem_WithSameData_ShouldUpdateCartItem() throws Exception {
        // Arrange - Update with same values
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(2) // Same quantity
                .userId(testUser1.getId()) // Same user
                .itemId(testProductColorSize1.getId()) // Same item
                .build();

        // Act & Assert
        mockMvc.perform(put("/cart-items/{id}", testCartItem1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(testCartItem1.getId()))
                .andExpect(jsonPath("$.data.quantity").value(2))
                .andDo(print());
    }

    // Test large quantity values
    @Test
    void createCartItem_WithLargeQuantity_ShouldCreateCartItem() throws Exception {
        // First, create a product with large stock
        ProductColorSize largeStockPcs = ProductColorSize.builder()
                .product(testProduct1)
                .color(testColor)
                .size(testSize)
                .quantity(10000)
                .build();
        largeStockPcs.setCreatedAt(LocalDateTime.now());
        largeStockPcs.setUpdatedAt(LocalDateTime.now());
        largeStockPcs = productColorSizeRepository.save(largeStockPcs);

        // Arrange
        CreateCartItemRequest request = CreateCartItemRequest.builder()
                .quantity(5000) // Large quantity
                .userId(testUser2.getId())
                .itemId(largeStockPcs.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/cart-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.quantity").value(5000))
                .andDo(print());
    }
}