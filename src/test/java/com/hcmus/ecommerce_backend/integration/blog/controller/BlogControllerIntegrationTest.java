package com.hcmus.ecommerce_backend.integration.blog.controller;

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
import com.hcmus.ecommerce_backend.blog.model.dto.request.CreateBlogRequest;
import com.hcmus.ecommerce_backend.blog.model.dto.request.UpdateBlogRequest;
import com.hcmus.ecommerce_backend.blog.model.entity.Blog;
import com.hcmus.ecommerce_backend.blog.repository.BlogRepository;
import com.hcmus.ecommerce_backend.integration.BaseIntegrationTest;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.model.enums.Role;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BlogControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationService authenticationService;

    private User testUser1;
    private User testUser2;
    private Blog testBlog1;
    private Blog testBlog2;
    private Blog testBlog3;
    private String accessToken;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        // Clean up any existing data
        blogRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        testUser1 = User.builder()
                .email("user1@test.com")
                .firstName("John")
                .lastName("Doe")
                .password(passwordEncoder.encode("password123"))
                .phoneNumber("1234567890")
                .build();
        testUser1.setRole(Role.ADMIN);
        testUser1.setEnabled(true);
        testUser1.setCreatedAt(LocalDateTime.now());
        testUser1.setUpdatedAt(LocalDateTime.now());

        testUser2 = User.builder()
                .email("user2@test.com")
                .firstName("Jane")
                .lastName("Smith")
                .password(passwordEncoder.encode("password123"))
                .phoneNumber("0987654321")
                .build();
        
        testUser2.setEnabled(true);
        testUser2.setCreatedAt(LocalDateTime.now());
        testUser2.setUpdatedAt(LocalDateTime.now());

        userRepository.saveAll(List.of(testUser1, testUser2));
        testUser1 = userRepository.findByEmail(testUser1.getEmail()).orElseThrow();
        testUser2 = userRepository.findByEmail(testUser2.getEmail()).orElseThrow();

        TokenResponse tokenResponse = authenticationService.login(LoginRequest.builder().email(testUser1.getEmail())
                .password(("password123"))
                .build());
        accessToken = tokenResponse.getAccessToken();

        // Create test blogs
        testBlog1 = Blog.builder()
                .title("Spring Boot Tutorial")
                .content("This is a comprehensive tutorial about Spring Boot framework")
                .image("https://example.com/spring-boot.jpg")
                .userId(testUser1.getId())
                .build();
        testBlog1.setCreatedAt(LocalDateTime.now().minusDays(3));
        testBlog1.setUpdatedAt(LocalDateTime.now().minusDays(3));

        testBlog2 = Blog.builder()
                .title("Java Best Practices")
                .content("Learn about Java coding best practices and design patterns")
                .image("https://example.com/java.jpg")
                .userId(testUser1.getId())
                .build();
        testBlog2.setCreatedAt(LocalDateTime.now().minusDays(2));
        testBlog2.setUpdatedAt(LocalDateTime.now().minusDays(2));

        testBlog3 = Blog.builder()
                .title("Database Design")
                .content("Understanding database normalization and design principles")
                .image("https://example.com/database.jpg")
                .userId("user-2")
                .build();
        testBlog3.setCreatedAt(LocalDateTime.now().minusDays(1));
        testBlog3.setUpdatedAt(LocalDateTime.now().minusDays(1));
    }

    // Test GET /blogs - Get all blogs
    @Test
    void getAllBlogs_WithoutParameters_ShouldReturnAllBlogsWithDefaultPagination() throws Exception {
        // Arrange
        blogRepository.saveAll(List.of(testBlog1, testBlog2, testBlog3));

        // Act & Assert
        mockMvc.perform(get("/blogs").header("Authorization", "Bearer " + accessToken))
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
    void getAllBlogs_WithPaginationParameters_ShouldReturnPaginatedResults() throws Exception {
        // Arrange
        blogRepository.saveAll(List.of(testBlog1, testBlog2, testBlog3));

        // Act & Assert
        mockMvc.perform(get("/blogs")
                .param("page", "0")
                .param("size", "2")
                .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.number").value(0))
                .andExpect(jsonPath("$.data.content[0].title").value("Database Design"))
                .andExpect(jsonPath("$.data.content[1].title").value("Java Best Practices"));
    }

    @Test
    void getAllBlogs_WithKeySearch_ShouldReturnFilteredResults() throws Exception {
        // Arrange
        blogRepository.saveAll(List.of(testBlog1, testBlog2, testBlog3));

        // Act & Assert
        mockMvc.perform(get("/blogs")
                .param("keysearch", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title").value("Java Best Practices"));
    }

    @Test
    void getAllBlogs_WithKeySearchInContent_ShouldReturnFilteredResults() throws Exception {
        // Arrange
        blogRepository.saveAll(List.of(testBlog1, testBlog2, testBlog3));

        // Act & Assert
        mockMvc.perform(get("/blogs")
                .param("keysearch", "tutorial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title").value("Spring Boot Tutorial"));
    }

    @Test
    void getAllBlogs_WithEmptyKeySearch_ShouldReturnAllBlogs() throws Exception {
        // Arrange
        blogRepository.saveAll(List.of(testBlog1, testBlog2, testBlog3));

        // Act & Assert
        mockMvc.perform(get("/blogs")
                .param("keysearch", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(3)));
    }

    @Test
    void getAllBlogs_WithNonMatchingKeySearch_ShouldReturnEmptyResults() throws Exception {
        // Arrange
        blogRepository.saveAll(List.of(testBlog1, testBlog2, testBlog3));

        // Act & Assert
        mockMvc.perform(get("/blogs")
                .param("keysearch", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(0)))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    void getAllBlogs_WithSortingByMultipleFields_ShouldReturnSortedResults() throws Exception {
        // Arrange
        blogRepository.saveAll(List.of(testBlog1, testBlog2, testBlog3));

        // Act & Assert
        mockMvc.perform(get("/blogs")
                .param("sort", "userId,asc", "title,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(3)));
    }

    // Test GET /blogs/{id} - Get blog by ID
    @Test
    void getBlogById_WithValidId_ShouldReturnBlog() throws Exception {
        // Arrange
        Blog savedBlog = blogRepository.save(testBlog1);

        // Act & Assert
        mockMvc.perform(get("/blogs/{id}", savedBlog.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.id").value(savedBlog.getId()))
                .andExpect(jsonPath("$.data.title").value("Spring Boot Tutorial"))
                .andExpect(jsonPath("$.data.content").value("This is a comprehensive tutorial about Spring Boot framework"))
                .andExpect(jsonPath("$.data.image").value("https://example.com/spring-boot.jpg"));
    }

    @Test
    void getBlogById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/blogs/{id}", "non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBlogById_WithInvalidIdFormat_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/blogs/{id}", "invalid-uuid-format"))
                .andExpect(status().isNotFound());
    }

    // Test POST /blogs - Create blog
    @Test
    void createBlog_WithValidRequest_ShouldCreateBlogAndReturnCreated() throws Exception {
        // Arrange
        CreateBlogRequest request = CreateBlogRequest.builder()
                .title("New Blog Post")
                .content("This is a new blog post content")
                .image("https://example.com/new-blog.jpg")
                .userId(testUser1.getId()) // Use testUser1's ID
                .build();

        // Act & Assert
        mockMvc.perform(post("/blogs").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.httpStatus").value("CREATED"))
                .andExpect(jsonPath("$.data.title").value("New Blog Post"))
                .andExpect(jsonPath("$.data.content").value("This is a new blog post content"))
                .andExpect(jsonPath("$.data.image").value("https://example.com/new-blog.jpg"))
                .andExpect(jsonPath("$.data.id").isNotEmpty());

        // Verify blog was saved to database
        List<Blog> blogs = blogRepository.findAll();
        assertEquals(1, blogs.size());
        assertEquals("New Blog Post", blogs.get(0).getTitle());
    }

    @Test
    void createBlog_WithDuplicateTitleAndUserId_ShouldReturnConflict() throws Exception {
        // Arrange
        blogRepository.save(testBlog1);
        
        CreateBlogRequest request = CreateBlogRequest.builder()
                .title("Spring Boot Tutorial") // Same title as testBlog1
                .content("Different content")
                .userId(testUser1.getId()) // Same user as testBlog1
                .build();

        // Act & Assert
        mockMvc.perform(post("/blogs").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createBlog_WithNonExistentUser_ShouldReturnNotFound() throws Exception {
        // Arrange
        CreateBlogRequest request = CreateBlogRequest.builder()
                .title("New Blog Post")
                .content("Content")
                .userId("non-existent-user")
                .build();

        // Act & Assert
        mockMvc.perform(post("/blogs").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBlog_WithBlankTitle_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateBlogRequest request = CreateBlogRequest.builder()
                .title("   ") // Blank title
                .content("Content")
                .userId(testUser1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/blogs").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBlog_WithBlankContent_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateBlogRequest request = CreateBlogRequest.builder()
                .title("Valid Title")
                .content("") // Empty content
                .userId(testUser1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/blogs").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBlog_WithNullUserId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateBlogRequest request = CreateBlogRequest.builder()
                .title("Valid Title")
                .content("Valid Content")
                .userId(null) // Null user ID
                .build();

        // Act & Assert
        mockMvc.perform(post("/blogs").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBlog_WithoutImage_ShouldCreateBlogSuccessfully() throws Exception {
        // Arrange
        CreateBlogRequest request = CreateBlogRequest.builder()
                .title("Blog Without Image")
                .content("Content without image")
                .userId(testUser1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/blogs").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Blog Without Image"))
                .andExpect(jsonPath("$.data.image").isEmpty());
    }

    @Test
    void createBlog_WithMalformedJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/blogs").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    // Test PUT /blogs/{id} - Update blog
    @Test
    void updateBlog_WithValidRequest_ShouldUpdateBlogAndReturnOk() throws Exception {
        // Arrange
        Blog savedBlog = blogRepository.save(testBlog1);
        
        UpdateBlogRequest request = UpdateBlogRequest.builder()
                .id(savedBlog.getId())
                .title("Updated Spring Boot Tutorial")
                .content("Updated comprehensive tutorial content")
                .image("https://example.com/updated-spring-boot.jpg")
                .userId(testUser1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(put("/blogs/{id}", savedBlog.getId()).header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data.id").value(savedBlog.getId()))
                .andExpect(jsonPath("$.data.title").value("Updated Spring Boot Tutorial"))
                .andExpect(jsonPath("$.data.content").value("Updated comprehensive tutorial content"))
                .andExpect(jsonPath("$.data.image").value("https://example.com/updated-spring-boot.jpg"));

        // Verify blog was updated in database
        Blog updatedBlog = blogRepository.findById(savedBlog.getId()).orElseThrow();
        assertEquals("Updated Spring Boot Tutorial", updatedBlog.getTitle());
    }

    @Test
    void updateBlog_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Arrange
        UpdateBlogRequest request = UpdateBlogRequest.builder()
                .id("non-existent-id")
                .title("Updated Title")
                .content("Updated Content")
                .userId(testUser1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(put("/blogs/{id}", "non-existent-id").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBlog_WithDuplicateTitleAndUserId_ShouldReturnConflict() throws Exception {
        // Arrange
        Blog savedBlog1 = blogRepository.save(testBlog1);
        Blog savedBlog2 = blogRepository.save(testBlog2);
        
        UpdateBlogRequest request = UpdateBlogRequest.builder()
                .id(savedBlog1.getId())
                .title("Java Best Practices") // Same title as testBlog2
                .content("Updated content")
                .userId(testUser1.getId()) // Same user as testBlog2
                .build();

        // Act & Assert
        mockMvc.perform(put("/blogs/{id}", savedBlog1.getId()).header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateBlog_WithSameTitle_ShouldUpdateSuccessfully() throws Exception {
        // Arrange
        Blog savedBlog = blogRepository.save(testBlog1);
        
        UpdateBlogRequest request = UpdateBlogRequest.builder()
                .id(savedBlog.getId())
                .title("Spring Boot Tutorial") // Same title, should be allowed
                .content("Updated content with same title")
                .userId(testUser1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(put("/blogs/{id}", savedBlog.getId()).header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Spring Boot Tutorial"))
                .andExpect(jsonPath("$.data.content").value("Updated content with same title"));
    }

    @Test
    void updateBlog_WithBlankTitle_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Blog savedBlog = blogRepository.save(testBlog1);
        
        UpdateBlogRequest request = UpdateBlogRequest.builder()
                .id(savedBlog.getId())
                .title("   ") // Blank title
                .content("Updated content")
                .userId(testUser1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(put("/blogs/{id}", savedBlog.getId()).header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBlog_WithNullId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Blog savedBlog = blogRepository.save(testBlog1);
        
        UpdateBlogRequest request = UpdateBlogRequest.builder()
                .id(null) // Null ID
                .title("Updated Title")
                .content("Updated content")
                .userId(testUser1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(put("/blogs/{id}", savedBlog.getId()).header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Test DELETE /blogs/{id} - Delete blog
    @Test
    void deleteBlog_WithValidId_ShouldDeleteBlogAndReturnOk() throws Exception {
        // Arrange
        Blog savedBlog = blogRepository.save(testBlog1);
        String blogId = savedBlog.getId();

        // Act & Assert
        mockMvc.perform(delete("/blogs/{id}", blogId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true));

        // Verify blog was deleted from database
        assertFalse(blogRepository.existsById(blogId));
    }

    @Test
    void deleteBlog_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/blogs/{id}", "non-existent-id").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBlog_WithInvalidIdFormat_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/blogs/{id}", "invalid-uuid").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    // Test edge cases and error scenarios
    @Test
    void getAllBlogs_WithVeryLargePage_ShouldReturnEmptyResults() throws Exception {
        // Arrange
        blogRepository.saveAll(List.of(testBlog1, testBlog2, testBlog3));

        // Act & Assert
        mockMvc.perform(get("/blogs").header("Authorization", "Bearer " + accessToken)
                .param("page", "999")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.number").value(999));
    }

    @Test
    void getAllBlogs_WithNegativePage_ShouldHandleGracefully() throws Exception {
        // Arrange
        blogRepository.saveAll(List.of(testBlog1, testBlog2, testBlog3));

        // Act & Assert - Spring handles negative page numbers by treating them as 0
        mockMvc.perform(get("/blogs").header("Authorization", "Bearer " + accessToken)
                .param("page", "-1")
                .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllBlogs_WithZeroSize_ShouldHandleGracefully() throws Exception {
        // Arrange
        blogRepository.saveAll(List.of(testBlog1, testBlog2, testBlog3));

        // Act & Assert
        mockMvc.perform(get("/blogs").header("Authorization", "Bearer " + accessToken)
                .param("page", "0")
                .param("size", "0"))
                .andExpect(status().isOk());
    }

    // @Test
    // void getAllBlogs_WithInvalidSortField_ShouldHandleGracefully() throws Exception {
    //     // Arrange
    //     blogRepository.saveAll(List.of(testBlog1, testBlog2, testBlog3));

    //     // Act & Assert
    //     mockMvc.perform(get("/blogs").header("Authorization", "Bearer " + accessToken)
    //             .param("sort", "nonexistentfield,asc"))
    //             .andExpect(status().isOk());
    // }

    @Test
    void createBlog_WithVeryLongContent_ShouldCreateSuccessfully() throws Exception {
        // Arrange
        String longContent = "A".repeat(5000);
        CreateBlogRequest request = CreateBlogRequest.builder()
                .title("Blog with Long Content")
                .content(longContent)
                .userId(testUser1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/blogs").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.content").value(longContent));
    }

    @Test
    void updateBlog_WithDifferentUserIdInPath_ShouldStillWork() throws Exception {
        // Arrange
        Blog savedBlog = blogRepository.save(testBlog1);
        
        UpdateBlogRequest request = UpdateBlogRequest.builder()
                .id(savedBlog.getId())
                .title("Updated Title")
                .content("Updated content")
                .userId(testUser1.getId()) // Same user as original
                .build();

        // Act & Assert
        mockMvc.perform(put("/blogs/{id}", savedBlog.getId()).header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // Test concurrent operations
    @Test
    @Transactional
    void createAndDeleteBlog_ConcurrentOperations_ShouldWorkCorrectly() throws Exception {
        // Arrange
        CreateBlogRequest createRequest = CreateBlogRequest.builder()
                .title("Concurrent Test Blog")
                .content("Test content")
                .userId(testUser1.getId())
                .build();

        // Act - Create blog
        String response = mockMvc.perform(post("/blogs").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Extract blog ID from response
        String blogId = objectMapper.readTree(response).get("data").get("id").asText();

        // Act - Delete the same blog
        mockMvc.perform(delete("/blogs/{id}", blogId).header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Assert - Blog should not exist
        assertFalse(blogRepository.existsById(blogId));
    }

    @Test
    void getAllBlogs_WithEmptyDatabase_ShouldReturnEmptyPage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/blogs").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.totalPages").value(0));
    }

    @Test
    void createBlog_WithUnicodeCharacters_ShouldCreateSuccessfully() throws Exception {
        // Arrange
        CreateBlogRequest request = CreateBlogRequest.builder()
                .title("Blog with Unicode: 测试 🌟 émojis")
                .content("Content with special characters: åäö ñ 中文")
                .userId(testUser1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/blogs").header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Blog with Unicode: 测试 🌟 émojis"))
                .andExpect(jsonPath("$.data.content").value("Content with special characters: åäö ñ 中文"));
    }
}