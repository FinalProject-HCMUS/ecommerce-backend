package com.hcmus.ecommerce_backend.integration.message.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmus.ecommerce_backend.integration.BaseIntegrationTest;
import com.hcmus.ecommerce_backend.message.model.dto.request.CreateMessageRequest;
import com.hcmus.ecommerce_backend.message.model.entity.Conversation;
import com.hcmus.ecommerce_backend.message.model.entity.Message;
import com.hcmus.ecommerce_backend.message.repository.ConversationRepository;
import com.hcmus.ecommerce_backend.message.repository.MessageRepository;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.model.enums.Role;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
    "spring.security.enabled=false"
})
@WithMockUser(username = "testuser", roles = {"USER", "ADMIN"})
public class MessagerControllerIntegrationTest extends BaseIntegrationTest {

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

    private User testUser;
    private User testAdmin;
    private User testCustomer1;
    private User testCustomer2;
    private Conversation testConversation1;
    private Conversation testConversation2;
    private Message testMessage1;
    private Message testMessage2;
    private Message testMessage3;

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

        // Create test customers
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

        // Create test messages
        testMessage1 = Message.builder()
                .content("Hello from customer1")
                .conversation(testConversation1)
                .userId(testCustomer1.getId())
                .messageType("TEXT")
                .isRead(false)
                .build();
        testMessage1 = messageRepository.save(testMessage1);

        testMessage2 = Message.builder()
                .content("Reply from admin")
                .conversation(testConversation1)
                .userId(testAdmin.getId())
                .messageType("TEXT")
                .isRead(true)
                .build();
        testMessage2 = messageRepository.save(testMessage2);

        testMessage3 = Message.builder()
                .content("Hello from customer2")
                .conversation(testConversation2)
                .userId(testCustomer2.getId())
                .messageType("TEXT")
                .isRead(false)
                .build();
        testMessage3 = messageRepository.save(testMessage3);
    }

    // Test GET /messages - Get all messages with pagination
    @Test
    void getAllMessagesPaginated_WithDefaultPagination_ShouldReturnPaginatedMessages() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages"))
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
                .andDo(print());
    }

    @Test
    void getAllMessagesPaginated_WithCustomPagination_ShouldReturnPaginatedMessages() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages")
                .param("page", "0")
                .param("size", "2")
                .param("sort", "createdAt,desc"))
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
    void getAllMessagesPaginated_WithEmptyDatabase_ShouldReturnEmptyPage() throws Exception {
        // Clean up test data
        messageRepository.deleteAll();

        // Act & Assert
        mockMvc.perform(get("/messages"))
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

    // Test GET /messages/search - Search messages
    @Test
    void searchMessages_WithValidKeyword_ShouldReturnMatchingMessages() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages/search")
                .param("keyword", "customer1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].content").value("Hello from customer1"))
                .andDo(print());
    }

    @Test
    void searchMessages_WithNonExistentKeyword_ShouldReturnEmptyPage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages/search")
                .param("keyword", "nonexistent"))
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
    void searchMessages_WithEmptyKeyword_ShouldReturnAllMessages() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages/search")
                .param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andDo(print());
    }

    @Test
    void searchMessages_WithNullKeyword_ShouldReturnAllMessages() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages/search"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andDo(print());
    }

    @Test
    void getMessageById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages/{id}", "non-existent-id"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    // Test GET /messages/conversation/{conversationId} - Get messages by conversation ID
    @Test
    void getMessagesByConversationId_WithExistingConversationId_ShouldReturnMessages() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages/conversation/{conversationId}", testConversation1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].conversationId").value(testConversation1.getId()))
                .andExpect(jsonPath("$.data[1].conversationId").value(testConversation1.getId()))
                .andDo(print());
    }

    @Test
    void getMessagesByConversationId_WithNonExistentConversationId_ShouldReturnEmptyList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages/conversation/{conversationId}", "non-existent-conversation"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());
    }

    // Test GET /messages/conversation/{conversationId}/paginated - Get messages by conversation ID with pagination
    @Test
    void getMessagesByConversationIdPaginated_WithExistingConversationId_ShouldReturnPaginatedMessages() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages/conversation/{conversationId}/paginated", testConversation1.getId()))
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
    void getMessagesByConversationIdPaginated_WithCustomPagination_ShouldReturnPaginatedMessages() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages/conversation/{conversationId}/paginated", testConversation1.getId())
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

    // Test GET /messages/conversation/{conversationId}/search - Search messages by conversation
    @Test
    void searchMessagesByConversation_WithValidKeyword_ShouldReturnMatchingMessages() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages/conversation/{conversationId}/search", testConversation1.getId())
                .param("keyword", "Hello"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].content").value("Hello from customer1"))
                .andDo(print());
    }

    @Test
    void searchMessagesByConversation_WithNonExistentKeyword_ShouldReturnEmptyPage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages/conversation/{conversationId}/search", testConversation1.getId())
                .param("keyword", "nonexistent"))
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
    void searchMessagesByConversation_WithEmptyKeyword_ShouldReturnAllMessagesInConversation() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages/conversation/{conversationId}/search", testConversation1.getId())
                .param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andDo(print());
    }

    // Test GET /messages/user/{userId} - Get messages by user ID
    @Test
    void getMessagesByUserId_WithExistingUserId_ShouldReturnMessages() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages/user/{userId}", testCustomer1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].userId").value(testCustomer1.getId()))
                .andExpect(jsonPath("$.data[0].content").value("Hello from customer1"))
                .andDo(print());
    }

    @Test
    void getMessagesByUserId_WithNonExistentUserId_ShouldReturnEmptyList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages/user/{userId}", "non-existent-user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());
    }

    // Test POST /messages - Create new message
    @Test
    void createMessage_WithValidData_ShouldCreateMessage() throws Exception {
        // Arrange
        CreateMessageRequest request = CreateMessageRequest.builder()
                .content("New test message")
                .userId(testCustomer1.getId())
                .conversationId(testConversation1.getId())
                .messageType("TEXT")
                .build();

        // Act & Assert
        mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").value("New test message"))
                .andExpect(jsonPath("$.data.userId").value(testCustomer1.getId()))
                .andExpect(jsonPath("$.data.conversationId").value(testConversation1.getId()))
                .andExpect(jsonPath("$.data.messageType").value("TEXT"))
                .andDo(print());
    }

    @Test
    void createMessage_WithImageMessage_ShouldCreateMessage() throws Exception {
        // Arrange
        CreateMessageRequest request = CreateMessageRequest.builder()
                .content("Check out this image!")
                .userId(testCustomer1.getId())
                .conversationId(testConversation1.getId())
                .messageType("IMAGE")
                .contentUrl("https://example.com/image.jpg")
                .build();

        // Act & Assert
        mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").value("Check out this image!"))
                .andExpect(jsonPath("$.data.messageType").value("IMAGE"))
                .andExpect(jsonPath("$.data.contentUrl").value("https://example.com/image.jpg"))
                .andDo(print());
    }

    @Test
    void createMessage_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateMessageRequest request = CreateMessageRequest.builder()
                .content("Test message")
                // Missing userId, conversationId, messageType
                .build();

        // Act & Assert
        mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createMessage_WithNonExistentConversation_ShouldReturnNotFound() throws Exception {
        // Arrange
        CreateMessageRequest request = CreateMessageRequest.builder()
                .content("Test message")
                .userId(testCustomer1.getId())
                .conversationId("non-existent-conversation")
                .messageType("TEXT")
                .build();

        // Act & Assert
        mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void createMessage_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test DELETE /messages/{id} - Delete message
    @Test
    void deleteMessage_WithExistingId_ShouldDeleteMessage() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/messages/{id}", testMessage1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andDo(print());

        // Verify deletion
        mockMvc.perform(get("/messages/{id}", testMessage1.getId()))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void deleteMessage_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/messages/{id}", "non-existent-id"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void markMessageAsRead_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/messages/{id}/read", "non-existent-id"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    // Test different message types
    @Test
    void createMessage_WithAllMessageTypes_ShouldCreateMessages() throws Exception {
        String[] messageTypes = {"TEXT", "IMAGE", "FILE", "AUDIO", "VIDEO"};

        for (String messageType : messageTypes) {
            CreateMessageRequest request = CreateMessageRequest.builder()
                    .content("Message of type " + messageType)
                    .userId(testCustomer1.getId())
                    .conversationId(testConversation1.getId())
                    .messageType(messageType)
                    .contentUrl(messageType.equals("TEXT") ? null : "https://example.com/file." + messageType.toLowerCase())
                    .build();

            mockMvc.perform(post("/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.messageType").value(messageType))
                    .andDo(print());
        }
    }

    // Test special characters in content
    @Test
    void createMessage_WithSpecialCharacters_ShouldCreateMessage() throws Exception {
        // Arrange
        String specialContent = "Hello! 🌟 Special chars: @#$%^&*()_+= 测试中文 العربية";
        CreateMessageRequest request = CreateMessageRequest.builder()
                .content(specialContent)
                .userId(testCustomer1.getId())
                .conversationId(testConversation1.getId())
                .messageType("TEXT")
                .build();

        // Act & Assert
        mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.content").value(specialContent))
                .andDo(print());
    }

    // Test long content
    @Test
    void createMessage_WithLongContent_ShouldCreateMessage() throws Exception {
        // Arrange
        String longContent = "This is a very long message content that exceeds normal length to test handling of large messages. ".repeat(10);
        CreateMessageRequest request = CreateMessageRequest.builder()
                .content(longContent)
                .userId(testCustomer1.getId())
                .conversationId(testConversation1.getId())
                .messageType("TEXT")
                .build();

        // Act & Assert
        mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.content").value(longContent))
                .andDo(print());
    }

    // Test pagination edge cases
    @Test
    void getAllMessagesPaginated_WithLargePageSize_ShouldReturnAllMessages() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages")
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

    @Test
    void getAllMessagesPaginated_WithPageBeyondRange_ShouldReturnEmptyPage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages")
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
    void getAllMessagesPaginated_WithMultipleSortFields_ShouldReturnSortedResults() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages")
                .param("sort", "isRead,asc", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andDo(print());
    }

    // Test data integrity
    @Test
    void createMessage_ShouldMaintainDataIntegrity() throws Exception {
        // Arrange
        CreateMessageRequest request = CreateMessageRequest.builder()
                .content("Data integrity test message")
                .userId(testCustomer1.getId())
                .conversationId(testConversation1.getId())
                .messageType("TEXT")
                .build();

        // Act
        mockMvc.perform(post("/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print());

        // Assert - Check that total messages count increased
        mockMvc.perform(get("/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(4)) // Original 3 + new 1
                .andDo(print());
    }

    // Test concurrent operations
    @Test
    @Transactional
    void concurrentOperations_ShouldHandleCorrectly() throws Exception {
        // Multiple operations on messages
        mockMvc.perform(get("/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andDo(print());

        mockMvc.perform(get("/messages/{id}", testMessage1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testMessage1.getId()))
                .andDo(print());

        mockMvc.perform(get("/messages/conversation/{conversationId}", testConversation1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andDo(print());

        mockMvc.perform(get("/messages/user/{userId}", testCustomer1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andDo(print());
    }

    @Test
    void getAllMessagesPaginated_WithNegativePage_ShouldHandleCorrectly() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages")
                .param("page", "-1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.number").value(0)) // Should default to 0
                .andDo(print());
    }

    // Test complex search scenarios
    @Test
    void searchMessages_WithPartialKeyword_ShouldReturnMatchingMessages() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages/search")
                .param("keyword", "Reply"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].content").value("Reply from admin"))
                .andDo(print());
    }

    @Test
    void searchMessages_CaseInsensitive_ShouldReturnMatchingMessages() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages/search")
                .param("keyword", "HELLO"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(2))) // Should find both "Hello" messages
                .andDo(print());
    }

    @Test
    void searchMessagesByConversation_WithPagination_ShouldReturnPaginatedResults() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/messages/conversation/{conversationId}/search", testConversation1.getId())
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
}