package com.hcmus.ecommerce_backend.integration.message.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import com.hcmus.ecommerce_backend.integration.BaseIntegrationTest;
import com.hcmus.ecommerce_backend.message.controller.ChatController;
import com.hcmus.ecommerce_backend.message.model.dto.request.CreateMessageRequest;
import com.hcmus.ecommerce_backend.message.model.dto.response.MessageResponse;
import com.hcmus.ecommerce_backend.message.model.dto.websocket.ChatMessage;
import com.hcmus.ecommerce_backend.message.model.entity.Conversation;
import com.hcmus.ecommerce_backend.message.repository.ConversationRepository;
import com.hcmus.ecommerce_backend.message.repository.MessageRepository;
import com.hcmus.ecommerce_backend.message.service.MessageService;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.model.enums.Role;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ChatControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ChatController chatController;

    @MockBean
    private MessageService messageService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private User testCustomer;
    private Conversation testConversation;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        // Clean up existing data
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        userRepository.deleteAll();

        setupTestData();
        
        // Reset mocks
        reset(messageService, messagingTemplate);
    }

    private void setupTestData() {
        // Create test user (admin)
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

        // Create test customer
        testCustomer = User.builder()
                .email("customer@test.com")
                .firstName("John")
                .lastName("Doe")
                .password(passwordEncoder.encode("password123"))
                .phoneNumber("0987654321")
                .build();
        testCustomer.setRole(Role.USER);
        testCustomer.setEnabled(true);
        testCustomer.setCreatedAt(LocalDateTime.now());
        testCustomer.setUpdatedAt(LocalDateTime.now());

        userRepository.save(testCustomer);
        testCustomer = userRepository.findByEmail(testCustomer.getEmail()).orElseThrow();

        // Create test conversation
        testConversation = Conversation.builder()
                .customer(testCustomer)
                .isAdminRead(false)
                .isCustomerRead(true)
                .build();
        testConversation = conversationRepository.save(testConversation);
    }

    @Test
    void sendMessage_WithValidChatMessage_ShouldCallServiceAndSendToTopic() throws Exception {
        // Arrange
        String conversationId = testConversation.getId();
        ChatMessage chatMessage = new ChatMessage(
                "Hello, this is a test message",
                testCustomer.getId(),
                conversationId,
                "TEXT"
        );

        MessageResponse mockResponse = MessageResponse.builder()
                .id("test-message-id")
                .content("Hello, this is a test message")
                .userId(testCustomer.getId())
                .conversationId(conversationId)
                .messageType("TEXT")
                .createdAt(LocalDateTime.now())
                .build();

        when(messageService.createMessage(any(CreateMessageRequest.class))).thenReturn(mockResponse);

        // Act
        chatController.sendMessage(conversationId, chatMessage);

        // Assert
        // Verify messageService.createMessage was called with correct parameters
        ArgumentCaptor<CreateMessageRequest> requestCaptor = ArgumentCaptor.forClass(CreateMessageRequest.class);
        verify(messageService, times(1)).createMessage(requestCaptor.capture());

        CreateMessageRequest capturedRequest = requestCaptor.getValue();
        assertEquals("Hello, this is a test message", capturedRequest.getContent());
        assertEquals(testCustomer.getId(), capturedRequest.getUserId());
        assertEquals(conversationId, capturedRequest.getConversationId());
        assertEquals("TEXT", capturedRequest.getMessageType());
        assertNull(capturedRequest.getContentUrl());

        // Verify messagingTemplate.convertAndSend was called with correct parameters
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MessageResponse> messageCaptor = ArgumentCaptor.forClass(MessageResponse.class);
        verify(messagingTemplate, times(1)).convertAndSend(destinationCaptor.capture(), messageCaptor.capture());

        String capturedDestination = destinationCaptor.getValue();
        MessageResponse capturedMessage = messageCaptor.getValue();
        assertEquals("/topic/conversation/" + conversationId, capturedDestination);
        assertEquals(mockResponse, capturedMessage);
    }

    @Test
    void sendMessage_WithImageMessage_ShouldCallServiceAndSendToTopic() throws Exception {
        // Arrange
        String conversationId = testConversation.getId();
        ChatMessage chatMessage = new ChatMessage(
                "Check out this image!",
                testCustomer.getId(),
                conversationId,
                "IMAGE",
                "https://example.com/image.jpg"
        );

        MessageResponse mockResponse = MessageResponse.builder()
                .id("test-message-id")
                .content("Check out this image!")
                .userId(testCustomer.getId())
                .conversationId(conversationId)
                .messageType("IMAGE")
                .contentUrl("https://example.com/image.jpg")
                .createdAt(LocalDateTime.now())
                .build();

        when(messageService.createMessage(any(CreateMessageRequest.class))).thenReturn(mockResponse);

        // Act
        chatController.sendMessage(conversationId, chatMessage);

        // Assert
        // Verify messageService.createMessage was called with correct parameters
        ArgumentCaptor<CreateMessageRequest> requestCaptor = ArgumentCaptor.forClass(CreateMessageRequest.class);
        verify(messageService, times(1)).createMessage(requestCaptor.capture());

        CreateMessageRequest capturedRequest = requestCaptor.getValue();
        assertEquals("Check out this image!", capturedRequest.getContent());
        assertEquals(testCustomer.getId(), capturedRequest.getUserId());
        assertEquals(conversationId, capturedRequest.getConversationId());
        assertEquals("IMAGE", capturedRequest.getMessageType());
        assertEquals("https://example.com/image.jpg", capturedRequest.getContentUrl());

        // Verify messagingTemplate.convertAndSend was called
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(MessageResponse.class));
    }

    @Test
    void sendMessage_WithEmptyContent_ShouldCallServiceAndSendToTopic() throws Exception {
        // Arrange
        String conversationId = testConversation.getId();
        ChatMessage chatMessage = new ChatMessage(
                "",
                testCustomer.getId(),
                conversationId,
                "TEXT"
        );

        MessageResponse mockResponse = MessageResponse.builder()
                .id("test-message-id")
                .content("")
                .userId(testCustomer.getId())
                .conversationId(conversationId)
                .messageType("TEXT")
                .createdAt(LocalDateTime.now())
                .build();

        when(messageService.createMessage(any(CreateMessageRequest.class))).thenReturn(mockResponse);

        // Act
        chatController.sendMessage(conversationId, chatMessage);

        // Assert
        verify(messageService, times(1)).createMessage(any(CreateMessageRequest.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(MessageResponse.class));
    }

    @Test
    void sendMessage_WithNullContentUrl_ShouldCallServiceAndSendToTopic() throws Exception {
        // Arrange
        String conversationId = testConversation.getId();
        ChatMessage chatMessage = new ChatMessage(
                "Text message without URL",
                testCustomer.getId(),
                conversationId,
                "TEXT"
        );
        // contentUrl is null by default

        MessageResponse mockResponse = MessageResponse.builder()
                .id("test-message-id")
                .content("Text message without URL")
                .userId(testCustomer.getId())
                .conversationId(conversationId)
                .messageType("TEXT")
                .createdAt(LocalDateTime.now())
                .build();

        when(messageService.createMessage(any(CreateMessageRequest.class))).thenReturn(mockResponse);

        // Act
        chatController.sendMessage(conversationId, chatMessage);

        // Assert
        ArgumentCaptor<CreateMessageRequest> requestCaptor = ArgumentCaptor.forClass(CreateMessageRequest.class);
        verify(messageService, times(1)).createMessage(requestCaptor.capture());

        CreateMessageRequest capturedRequest = requestCaptor.getValue();
        assertEquals("Text message without URL", capturedRequest.getContent());
        assertEquals(testCustomer.getId(), capturedRequest.getUserId());
        assertEquals(conversationId, capturedRequest.getConversationId());
        assertEquals("TEXT", capturedRequest.getMessageType());
        assertNull(capturedRequest.getContentUrl());

        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(MessageResponse.class));
    }

    @Test
    void sendMessage_WithSpecialCharacters_ShouldCallServiceAndSendToTopic() throws Exception {
        // Arrange
        String conversationId = testConversation.getId();
        String specialContent = "Hello! 🌟 Special chars: @#$%^&*()_+= 测试中文 العربية";
        ChatMessage chatMessage = new ChatMessage(
                specialContent,
                testCustomer.getId(),
                conversationId,
                "TEXT"
        );

        MessageResponse mockResponse = MessageResponse.builder()
                .id("test-message-id")
                .content(specialContent)
                .userId(testCustomer.getId())
                .conversationId(conversationId)
                .messageType("TEXT")
                .createdAt(LocalDateTime.now())
                .build();

        when(messageService.createMessage(any(CreateMessageRequest.class))).thenReturn(mockResponse);

        // Act
        chatController.sendMessage(conversationId, chatMessage);

        // Assert
        ArgumentCaptor<CreateMessageRequest> requestCaptor = ArgumentCaptor.forClass(CreateMessageRequest.class);
        verify(messageService, times(1)).createMessage(requestCaptor.capture());

        CreateMessageRequest capturedRequest = requestCaptor.getValue();
        assertEquals(specialContent, capturedRequest.getContent());

        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(MessageResponse.class));
    }

    @Test
    void sendMessage_WithDifferentMessageTypes_ShouldCallServiceForEach() throws Exception {
        // Test different message types
        String[] messageTypes = {"TEXT", "IMAGE", "FILE", "AUDIO", "VIDEO"};
        String conversationId = testConversation.getId();

        for (String messageType : messageTypes) {
            // Arrange
            ChatMessage chatMessage = new ChatMessage(
                    "Message of type " + messageType,
                    testCustomer.getId(),
                    conversationId,
                    messageType
            );

            MessageResponse mockResponse = MessageResponse.builder()
                    .id("test-message-id-" + messageType)
                    .content("Message of type " + messageType)
                    .userId(testCustomer.getId())
                    .conversationId(conversationId)
                    .messageType(messageType)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(messageService.createMessage(any(CreateMessageRequest.class))).thenReturn(mockResponse);

            // Act
            chatController.sendMessage(conversationId, chatMessage);

            // Assert
            verify(messageService, times(1)).createMessage(any(CreateMessageRequest.class));
            verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(MessageResponse.class));

            // Reset mocks for next iteration
            reset(messageService, messagingTemplate);
        }
    }

    @Test
    void sendMessage_WithLongContent_ShouldCallServiceAndSendToTopic() throws Exception {
        // Arrange
        String conversationId = testConversation.getId();
        String longContent = "This is a very long message content that exceeds normal length to test handling of large messages. ".repeat(10);
        
        ChatMessage chatMessage = new ChatMessage(
                longContent,
                testCustomer.getId(),
                conversationId,
                "TEXT"
        );

        MessageResponse mockResponse = MessageResponse.builder()
                .id("test-message-id")
                .content(longContent)
                .userId(testCustomer.getId())
                .conversationId(conversationId)
                .messageType("TEXT")
                .createdAt(LocalDateTime.now())
                .build();

        when(messageService.createMessage(any(CreateMessageRequest.class))).thenReturn(mockResponse);

        // Act
        chatController.sendMessage(conversationId, chatMessage);

        // Assert
        ArgumentCaptor<CreateMessageRequest> requestCaptor = ArgumentCaptor.forClass(CreateMessageRequest.class);
        verify(messageService, times(1)).createMessage(requestCaptor.capture());

        CreateMessageRequest capturedRequest = requestCaptor.getValue();
        assertEquals(longContent, capturedRequest.getContent());

        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(MessageResponse.class));
    }

    @Test
    void sendMessage_ServiceThrowsException_ShouldNotCallMessagingTemplate() throws Exception {
        // Arrange
        String conversationId = testConversation.getId();
        ChatMessage chatMessage = new ChatMessage(
                "Test message",
                testCustomer.getId(),
                conversationId,
                "TEXT"
        );

        when(messageService.createMessage(any(CreateMessageRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert - Should throw exception
        assertThrows(RuntimeException.class, () -> {
            chatController.sendMessage(conversationId, chatMessage);
        });

        // Verify service was called but messaging template was not
        verify(messageService, times(1)).createMessage(any(CreateMessageRequest.class));
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(MessageResponse.class));
    }

    @Test
    void joinConversation_WithEmptyContent_ShouldSetSessionAttributesAndSendJoinMessage() throws Exception {
        // Arrange
        String conversationId = testConversation.getId();
        ChatMessage chatMessage = new ChatMessage(
                "", // Empty content
                testCustomer.getId(),
                conversationId,
                "JOIN"
        );

        // Mock SimpMessageHeaderAccessor
        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        Map<String, Object> sessionAttributes = new HashMap<>();
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);

        // Act
        chatController.joinConversation(conversationId, chatMessage, headerAccessor);

        // Assert
        // Verify session attributes were set
        assertEquals(testCustomer.getId(), sessionAttributes.get("userId"));
        assertEquals(conversationId, sessionAttributes.get("conversationId"));

        // Verify messagingTemplate.convertAndSend was called
        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), messageCaptor.capture());

        ChatMessage capturedMessage = messageCaptor.getValue();
        assertEquals("User joined", capturedMessage.getContent()); // Should be "User joined", not empty
        assertEquals("JOIN", capturedMessage.getMessageType());
    }
}