package com.hcmus.ecommerce_backend.integration.message.controller;

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
import com.hcmus.ecommerce_backend.integration.BaseIntegrationTest;
import com.hcmus.ecommerce_backend.message.model.dto.request.CreateConversationRequest;
import com.hcmus.ecommerce_backend.message.model.dto.request.UpdateConversationRequest;
import com.hcmus.ecommerce_backend.message.model.entity.Conversation;
import com.hcmus.ecommerce_backend.message.model.entity.Message;
import com.hcmus.ecommerce_backend.message.repository.ConversationRepository;
import com.hcmus.ecommerce_backend.message.repository.MessageRepository;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.model.enums.Role;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ConversationControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationService authenticationService;

    private User testUser;
    private User testAdmin;
    private User testCustomer1;
    private User testCustomer2;
    private String userAccessToken;
    private String adminAccessToken;
    private Conversation testConversation1;
    private Conversation testConversation2;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        // Clean up existing data in correct order
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        userRepository.deleteAll();

        setupTestData();
    }

    private void setupTestData() {
        // Create test users
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

        // Create test customer 1
        testCustomer1 = User.builder()
                .email("customer1@test.com")
                .firstName("Customer1")
                .lastName("Test")
                .password(passwordEncoder.encode("password123"))
                .phoneNumber("1111111111")
                .build();
        testCustomer1.setRole(Role.USER);
        testCustomer1.setEnabled(true);
        testCustomer1.setCreatedAt(LocalDateTime.now());
        testCustomer1.setUpdatedAt(LocalDateTime.now());
        userRepository.save(testCustomer1);
        testCustomer1 = userRepository.findByEmail(testCustomer1.getEmail()).orElseThrow();

        // Create test customer 2
        testCustomer2 = User.builder()
                .email("customer2@test.com")
                .firstName("Customer2")
                .lastName("Test")
                .password(passwordEncoder.encode("password123"))
                .phoneNumber("2222222222")
                .build();
        testCustomer2.setRole(Role.USER);
        testCustomer2.setEnabled(true);
        testCustomer2.setCreatedAt(LocalDateTime.now());
        testCustomer2.setUpdatedAt(LocalDateTime.now());
        userRepository.save(testCustomer2);
        testCustomer2 = userRepository.findByEmail(testCustomer2.getEmail()).orElseThrow();

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

        // Create test conversations
        testConversation1 = Conversation.builder()
                .customer(testCustomer1)
                .isAdminRead(false)
                .isCustomerRead(true)
                .build();
        testConversation1 = conversationRepository.save(testConversation1);

        testConversation2 = Conversation.builder()
                .customer(testCustomer2)
                .isAdminRead(true)
                .isCustomerRead(false)
                .build();
        testConversation2 = conversationRepository.save(testConversation2);
    }

    // Test GET /conversations - Get all conversations with pagination
    @Test
    void getAllConversationsPaginated_WithDefaultPagination_ShouldReturnPaginatedConversations() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/conversations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.number").value(0))
                .andDo(print());
    }

    @Test
    void getAllConversationsPaginated_WithCustomPagination_ShouldReturnPaginatedConversations() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/conversations")
                .param("page", "0")
                .param("size", "1")
                .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.size").value(1))
                .andExpect(jsonPath("$.data.number").value(0))
                .andDo(print());
    }

    @Test
    void getAllConversationsPaginated_WithEmptyDatabase_ShouldReturnEmptyPage() throws Exception {
        // Clean up test data
        conversationRepository.deleteAll();

        // Act & Assert
        mockMvc.perform(get("/conversations"))
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

    @Test
    void searchConversations_WithNonExistentKeyword_ShouldReturnEmptyPage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/conversations/search")
                .param("keyword", "non-existent-customer"))
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
    void searchConversations_WithEmptyKeyword_ShouldReturnAllConversations() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/conversations/search")
                .param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andDo(print());
    }

    @Test
    void searchConversations_WithNullKeyword_ShouldReturnAllConversations() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/conversations/search"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andDo(print());
    }

    @Test
    void searchConversations_WithPaginationAndSorting_ShouldReturnPaginatedResults() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/conversations/search")
                .param("keyword", "")
                .param("page", "0")
                .param("size", "1")
                .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.size").value(1))
                .andDo(print());
    }

    @Test
    void getConversationById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/conversations/{id}", "non-existent-id"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void getConversationsByCustomerId_WithNonExistentCustomerId_ShouldReturnEmptyList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/conversations/customer/{customerId}", "non-existent-customer"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());
    }

    @Test
    void createConversation_WithMissingCustomerId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateConversationRequest request = CreateConversationRequest.builder()
                .build(); // Missing customerId

        // Act & Assert
        mockMvc.perform(post("/conversations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createConversation_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/conversations")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void updateConversation_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Arrange
        UpdateConversationRequest request = UpdateConversationRequest.builder()
                .isAdminRead(true)
                .build();

        // Act & Assert
        mockMvc.perform(put("/conversations/{id}", "non-existent-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void updateConversation_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/conversations/{id}", testConversation1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test DELETE /conversations/{id} - Delete conversation
    @Test
    void deleteConversation_WithExistingId_ShouldDeleteConversation() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/conversations/{id}", testConversation1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andDo(print());

        // Verify deletion
        mockMvc.perform(get("/conversations/{id}", testConversation1.getId()))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void deleteConversation_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/conversations/{id}", "non-existent-id"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void deleteConversation_WithAssociatedMessages_ShouldDeleteConversationAndMessages() throws Exception {
        // Create a message for the conversation
        Message testMessage = Message.builder()
                .content("Test message")
                .conversation(testConversation1)
                .userId(testCustomer1.getId())
                .messageType("TEXT")
                .build();
        messageRepository.save(testMessage);

        // Act & Assert
        mockMvc.perform(delete("/conversations/{id}", testConversation1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andDo(print());

        // Verify deletion
        mockMvc.perform(get("/conversations/{id}", testConversation1.getId()))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    // Test pagination edge cases
    @Test
    void getAllConversationsPaginated_WithLargePageSize_ShouldReturnAllConversations() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/conversations")
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.size").value(100))
                .andDo(print());
    }

    @Test
    void getAllConversationsPaginated_WithPageBeyondRange_ShouldReturnEmptyPage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/conversations")
                .param("page", "10")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.number").value(10))
                .andDo(print());
    }

    @Test
    void getAllConversationsPaginated_WithMultipleSortFields_ShouldReturnSortedResults() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/conversations")
                .param("sort", "isAdminRead,asc", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andDo(print());
    }

    // Test data integrity
    @Test
    void createConversation_ShouldMaintainDataIntegrity() throws Exception {
        // Create a new customer
        User newCustomer = User.builder()
                .email("integrity@test.com")
                .firstName("Integrity")
                .lastName("Test")
                .password(passwordEncoder.encode("password123"))
                .phoneNumber("4444444444")
                .build();
        newCustomer.setRole(Role.USER);
        newCustomer.setEnabled(true);
        newCustomer.setCreatedAt(LocalDateTime.now());
        newCustomer.setUpdatedAt(LocalDateTime.now());
        userRepository.save(newCustomer);
        newCustomer = userRepository.findByEmail(newCustomer.getEmail()).orElseThrow();

        // Arrange
        CreateConversationRequest request = CreateConversationRequest.builder()
                .customerId(newCustomer.getId())
                .build();

        // Act
        mockMvc.perform(post("/conversations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print());

        // Assert - Check that total conversations count increased
        mockMvc.perform(get("/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3)) // Original 2 + new 1
                .andDo(print());
    }

    // Test concurrent operations
    @Test
    @Transactional
    void concurrentOperations_ShouldHandleCorrectly() throws Exception {
        // Multiple operations on conversations
        mockMvc.perform(get("/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andDo(print());

        mockMvc.perform(get("/conversations/{id}", testConversation1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testConversation1.getId()))
                .andDo(print());

        mockMvc.perform(get("/conversations/customer/{customerId}", testCustomer1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andDo(print());
    }

    @Test
    void getAllConversationsPaginated_WithNegativePage_ShouldHandleCorrectly() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/conversations")
                .param("page", "-1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.number").value(0)) // Should default to 0
                .andDo(print());
    }
}