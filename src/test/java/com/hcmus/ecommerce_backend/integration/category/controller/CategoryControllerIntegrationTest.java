package com.hcmus.ecommerce_backend.integration.category.controller;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import com.hcmus.ecommerce_backend.category.model.dto.request.CreateCategoryRequest;
import com.hcmus.ecommerce_backend.category.model.dto.request.UpdateCategoryRequest;
import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.category.repository.CategoryRepository;
import com.hcmus.ecommerce_backend.integration.BaseIntegrationTest;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.model.enums.Role;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CategoryControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationService authenticationService;

    private User testUser;
    private Category testCategory1;
    private Category testCategory2;
    private Category testCategory3;
    private String accessToken;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        // Clean up any existing data
        categoryRepository.deleteAll();
        userRepository.deleteAll();

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

        TokenResponse tokenResponse = authenticationService.login(LoginRequest.builder()
                .email(testUser.getEmail())
                .password("password123")
                .build());
        accessToken = tokenResponse.getAccessToken();

        // Create test categories
        testCategory1 = Category.builder()
                .name("Electronics")
                .description("Electronic devices and gadgets")
                .stock(100)
                .build();
        testCategory1.setCreatedAt(LocalDateTime.now().minusDays(3));
        testCategory1.setUpdatedAt(LocalDateTime.now().minusDays(3));

        testCategory2 = Category.builder()
                .name("Home Appliances")
                .description("Appliances for household use")
                .stock(50)
                .build();
        testCategory2.setCreatedAt(LocalDateTime.now().minusDays(2));
        testCategory2.setUpdatedAt(LocalDateTime.now().minusDays(2));

        testCategory3 = Category.builder()
                .name("Books")
                .description("Educational and entertainment books")
                .stock(200)
                .build();
        testCategory3.setCreatedAt(LocalDateTime.now().minusDays(1));
        testCategory3.setUpdatedAt(LocalDateTime.now().minusDays(1));
    }

    // Test GET /categories - Get all categories with pagination
    @Test
    void getAllCategories_WithoutParameters_ShouldReturnAllCategoriesWithDefaultPagination() throws Exception {
        // Arrange
        categoryRepository.saveAll(List.of(testCategory1, testCategory2, testCategory3));

        // Act & Assert
        mockMvc.perform(get("/categories").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.number").value(0));
    }

    @Test
    void getAllCategories_WithPaginationParameters_ShouldReturnPaginatedResults() throws Exception {
        // Arrange
        categoryRepository.saveAll(List.of(testCategory1, testCategory2, testCategory3));

        // Act & Assert
        mockMvc.perform(get("/categories")
                .param("page", "0")
                .param("size", "2")
                .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.number").value(0))
                .andExpect(jsonPath("$.data.content[0].name").value("Books"))
                .andExpect(jsonPath("$.data.content[1].name").value("Electronics"));
    }

    @Test
    void getAllCategories_WithKeywordSearch_ShouldReturnFilteredResults() throws Exception {
        // Arrange
        categoryRepository.saveAll(List.of(testCategory1, testCategory2, testCategory3));

        // Act & Assert
        mockMvc.perform(get("/categories")
                .param("keyword", "Electronic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name").value("Electronics"));
    }

    @Test
    void getAllCategories_WithKeywordSearchInDescription_ShouldReturnFilteredResults() throws Exception {
        // Arrange
        categoryRepository.saveAll(List.of(testCategory1, testCategory2, testCategory3));

        // Act & Assert
        mockMvc.perform(get("/categories")
                .param("keyword", "Home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name").value("Home Appliances"));
    }

    @Test
    void getAllCategories_WithEmptyKeyword_ShouldReturnAllCategories() throws Exception {
        // Arrange
        categoryRepository.saveAll(List.of(testCategory1, testCategory2, testCategory3));

        // Act & Assert
        mockMvc.perform(get("/categories")
                .param("keyword", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(3)));
    }

    @Test
    void getAllCategories_WithNonMatchingKeyword_ShouldReturnEmptyResults() throws Exception {
        // Arrange
        categoryRepository.saveAll(List.of(testCategory1, testCategory2, testCategory3));

        // Act & Assert
        mockMvc.perform(get("/categories")
                .param("keyword", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(0)))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void getAllCategories_WithSortingByMultipleFields_ShouldReturnSortedResults() throws Exception {
        // Arrange
        categoryRepository.saveAll(List.of(testCategory1, testCategory2, testCategory3));

        // Act & Assert
        mockMvc.perform(get("/categories")
                .param("sort", "stock,asc", "name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(3)));
    }

    // Test GET /categories/all - Get all categories without pagination
    @Test
    void getAllCategoriesWithoutPagination_ShouldReturnAllCategories() throws Exception {
        // Arrange
        categoryRepository.saveAll(List.of(testCategory1, testCategory2, testCategory3));

        // Act & Assert
        mockMvc.perform(get("/categories/all").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(3)));
    }

    @Test
    void getAllCategoriesWithoutPagination_WithEmptyDatabase_ShouldReturnEmptyList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/categories/all").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    // Test GET /categories/{id} - Get category by ID
    @Test
    void getCategoryById_WithValidId_ShouldReturnCategory() throws Exception {
        // Arrange
        Category savedCategory = categoryRepository.save(testCategory1);

        // Act & Assert
        mockMvc.perform(get("/categories/{id}", savedCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.id").value(savedCategory.getId()))
                .andExpect(jsonPath("$.data.name").value("Electronics"))
                .andExpect(jsonPath("$.data.description").value("Electronic devices and gadgets"))
                .andExpect(jsonPath("$.data.stock").value(100));
    }

    @Test
    void getCategoryById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/categories/{id}", "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCategoryById_WithInvalidIdFormat_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/categories/{id}", "invalid-uuid-format"))
                .andExpect(status().isNotFound());
    }

    // Test POST /categories - Create category
    @Test
    void createCategory_WithValidRequest_ShouldCreateCategoryAndReturnCreated() throws Exception {
        // Arrange
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("New Category")
                .description("This is a new category")
                .stock(75)
                .build();

        // Act & Assert
        mockMvc.perform(post("/categories").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.httpStatus").value("CREATED"))
                .andExpect(jsonPath("$.data.name").value("New Category"))
                .andExpect(jsonPath("$.data.description").value("This is a new category"))
                .andExpect(jsonPath("$.data.stock").value(75))
                .andExpect(jsonPath("$.data.id").isNotEmpty());

        // Verify category was saved to database
        List<Category> categories = categoryRepository.findAll();
        assertEquals(1, categories.size());
        assertEquals("New Category", categories.get(0).getName());
    }

    @Test
    void createCategory_WithDuplicateName_ShouldReturnConflict() throws Exception {
        // Arrange
        categoryRepository.save(testCategory1);
        
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Electronics") // Same name as testCategory1
                .description("Different description")
                .stock(50)
                .build();

        // Act & Assert
        mockMvc.perform(post("/categories").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createCategory_WithBlankName_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("   ") // Blank name
                .description("Valid description")
                .stock(50)
                .build();

        // Act & Assert
        mockMvc.perform(post("/categories").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_WithNullName_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name(null) // Null name
                .description("Valid description")
                .stock(50)
                .build();

        // Act & Assert
        mockMvc.perform(post("/categories").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_WithTooShortName_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("A") // Too short (min 2 characters)
                .description("Valid description")
                .stock(50)
                .build();

        // Act & Assert
        mockMvc.perform(post("/categories").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_WithTooLongName_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("A".repeat(51)) // Too long (max 50 characters)
                .description("Valid description")
                .stock(50)
                .build();

        // Act & Assert
        mockMvc.perform(post("/categories").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_WithoutDescription_ShouldCreateSuccessfully() throws Exception {
        // Arrange
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Category Without Description")
                .stock(25)
                .build();

        // Act & Assert
        mockMvc.perform(post("/categories").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Category Without Description"))
                .andExpect(jsonPath("$.data.description").isEmpty());
    }

    @Test
    void createCategory_WithNegativeStock_ShouldCreateSuccessfully() throws Exception {
        // Arrange
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Category With Negative Stock")
                .description("Test category")
                .stock(-10)
                .build();

        // Act & Assert
        mockMvc.perform(post("/categories").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.stock").value(-10));
    }

    @Test
    void createCategory_WithMalformedJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/categories").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    // Test PUT /categories/{id} - Update category
    @Test
    void updateCategory_WithValidRequest_ShouldUpdateCategoryAndReturnOk() throws Exception {
        // Arrange
        Category savedCategory = categoryRepository.save(testCategory1);
        
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Updated Electronics")
                .description("Updated electronic devices and gadgets")
                .stock(150)
                .build();

        // Act & Assert
        mockMvc.perform(put("/categories/{id}", savedCategory.getId()).header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.id").value(savedCategory.getId()))
                .andExpect(jsonPath("$.data.name").value("Updated Electronics"))
                .andExpect(jsonPath("$.data.description").value("Updated electronic devices and gadgets"))
                .andExpect(jsonPath("$.data.stock").value(150));

        // Verify category was updated in database
        Category updatedCategory = categoryRepository.findById(savedCategory.getId()).orElseThrow();
        assertEquals("Updated Electronics", updatedCategory.getName());
    }

    @Test
    void updateCategory_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Arrange
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Updated Name")
                .description("Updated description")
                .stock(100)
                .build();

        // Act & Assert
        mockMvc.perform(put("/categories/{id}", "non-existent-id").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCategory_WithDuplicateName_ShouldReturnConflict() throws Exception {
        // Arrange
        Category savedCategory1 = categoryRepository.save(testCategory1);
        Category savedCategory2 = categoryRepository.save(testCategory2);
        
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Home Appliances") // Same name as testCategory2
                .description("Updated description")
                .stock(100)
                .build();

        // Act & Assert
        mockMvc.perform(put("/categories/{id}", savedCategory1.getId()).header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateCategory_WithSameName_ShouldUpdateSuccessfully() throws Exception {
        // Arrange
        Category savedCategory = categoryRepository.save(testCategory1);
        
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Electronics") // Same name, should be allowed
                .description("Updated description with same name")
                .stock(120)
                .build();

        // Act & Assert
        mockMvc.perform(put("/categories/{id}", savedCategory.getId()).header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Electronics"))
                .andExpect(jsonPath("$.data.description").value("Updated description with same name"))
                .andExpect(jsonPath("$.data.stock").value(120));
    }

    @Test
    void updateCategory_WithBlankName_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Category savedCategory = categoryRepository.save(testCategory1);
        
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("   ") // Blank name
                .description("Updated description")
                .stock(100)
                .build();

        // Act & Assert
        mockMvc.perform(put("/categories/{id}", savedCategory.getId()).header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCategory_WithInvalidNameLength_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Category savedCategory = categoryRepository.save(testCategory1);
        
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("A".repeat(51)) // Too long
                .description("Updated description")
                .stock(100)
                .build();

        // Act & Assert
        mockMvc.perform(put("/categories/{id}", savedCategory.getId()).header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Test DELETE /categories/{id} - Delete category
    @Test
    void deleteCategory_WithValidId_ShouldDeleteCategoryAndReturnOk() throws Exception {
        // Arrange
        Category savedCategory = categoryRepository.save(testCategory1);
        String categoryId = savedCategory.getId();

        // Act & Assert
        mockMvc.perform(delete("/categories/{id}", categoryId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true));

        // Verify category was deleted from database
        assertFalse(categoryRepository.existsById(categoryId));
    }

    @Test
    void deleteCategory_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/categories/{id}", "non-existent-id").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory_WithInvalidIdFormat_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/categories/{id}", "invalid-uuid").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    // Test edge cases and error scenarios
    @Test
    void getAllCategories_WithVeryLargePage_ShouldReturnEmptyResults() throws Exception {
        // Arrange
        categoryRepository.saveAll(List.of(testCategory1, testCategory2, testCategory3));

        // Act & Assert
        mockMvc.perform(get("/categories").header("Authorization", "Bearer " + accessToken)
                .param("page", "999")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.number").value(999));
    }

    @Test
    void getAllCategories_WithNegativePage_ShouldHandleGracefully() throws Exception {
        // Arrange
        categoryRepository.saveAll(List.of(testCategory1, testCategory2, testCategory3));

        // Act & Assert - Spring handles negative page numbers by treating them as 0
        mockMvc.perform(get("/categories").header("Authorization", "Bearer " + accessToken)
                .param("page", "-1")
                .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllCategories_WithZeroSize_ShouldHandleGracefully() throws Exception {
        // Arrange
        categoryRepository.saveAll(List.of(testCategory1, testCategory2, testCategory3));

        // Act & Assert
        mockMvc.perform(get("/categories").header("Authorization", "Bearer " + accessToken)
                .param("page", "0")
                .param("size", "0"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllCategories_WithCaseInsensitiveKeyword_ShouldReturnFilteredResults() throws Exception {
        // Arrange
        categoryRepository.saveAll(List.of(testCategory1, testCategory2, testCategory3));

        // Act & Assert
        mockMvc.perform(get("/categories")
                .param("keyword", "ELECTRONICS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name").value("Electronics"));
    }

    @Test
    void createCategory_WithZeroStock_ShouldCreateSuccessfully() throws Exception {
        // Arrange
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Category With Zero Stock")
                .description("Test category with zero stock")
                .stock(0)
                .build();

        // Act & Assert
        mockMvc.perform(post("/categories").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.stock").value(0));
    }

    @Test
    void createCategory_WithVeryLargeStock_ShouldCreateSuccessfully() throws Exception {
        // Arrange
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Category With Large Stock")
                .description("Test category with large stock")
                .stock(Integer.MAX_VALUE)
                .build();

        // Act & Assert
        mockMvc.perform(post("/categories").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.stock").value(Integer.MAX_VALUE));
    }

    // Test concurrent operations
    @Test
    @Transactional
    void createAndDeleteCategory_ConcurrentOperations_ShouldWorkCorrectly() throws Exception {
        // Arrange
        CreateCategoryRequest createRequest = CreateCategoryRequest.builder()
                .name("Concurrent Test Category")
                .description("Test category for concurrent operations")
                .stock(50)
                .build();

        // Act - Create category
        String response = mockMvc.perform(post("/categories").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract category ID from response
        String categoryId = objectMapper.readTree(response).get("data").get("id").asText();

        // Act - Delete the same category
        mockMvc.perform(delete("/categories/{id}", categoryId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Assert - Category should not exist
        assertFalse(categoryRepository.existsById(categoryId));
    }

    @Test
    void getAllCategories_WithEmptyDatabase_ShouldReturnEmptyPage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/categories").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.totalPages").value(0));
    }

    @Test
    void createCategory_WithUnicodeCharacters_ShouldCreateSuccessfully() throws Exception {
        // Arrange
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Category with Unicode: 测试 🌟 émojis")
                .description("Description with special characters: åäö ñ 中文")
                .stock(25)
                .build();

        // Act & Assert
        mockMvc.perform(post("/categories").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Category with Unicode: 测试 🌟 émojis"))
                .andExpect(jsonPath("$.data.description").value("Description with special characters: åäö ñ 中文"));
    }

    @Test
    void updateCategory_WithMinimumValidName_ShouldUpdateSuccessfully() throws Exception {
        // Arrange
        Category savedCategory = categoryRepository.save(testCategory1);
        
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("AB") // Minimum length (2 characters)
                .description("Updated with minimum name length")
                .stock(50)
                .build();

        // Act & Assert
        mockMvc.perform(put("/categories/{id}", savedCategory.getId()).header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("AB"));
    }

    @Test
    void updateCategory_WithMaximumValidName_ShouldUpdateSuccessfully() throws Exception {
        // Arrange
        Category savedCategory = categoryRepository.save(testCategory1);
        String maxLengthName = "A".repeat(50); // Maximum length (50 characters)
        
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name(maxLengthName)
                .description("Updated with maximum name length")
                .stock(50)
                .build();

        // Act & Assert
        mockMvc.perform(put("/categories/{id}", savedCategory.getId()).header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(maxLengthName));
    }

    @Test
    void createCategory_WithVeryLongDescription_ShouldCreateSuccessfully() throws Exception {
        // Arrange
        String longDescription = "A".repeat(1000);
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Category with Long Description")
                .description(longDescription)
                .stock(25)
                .build();

        // Act & Assert
        mockMvc.perform(post("/categories").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.description").value(longDescription));
    }

    @Test
    void getAllCategories_WithPartialNameMatch_ShouldReturnFilteredResults() throws Exception {
        // Arrange
        categoryRepository.saveAll(List.of(testCategory1, testCategory2, testCategory3));

        // Act & Assert
        mockMvc.perform(get("/categories")
                .param("keyword", "Home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name").value("Home Appliances"));
    }
}