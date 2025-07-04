package com.hcmus.ecommerce_backend.unit.message.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.hcmus.ecommerce_backend.message.exception.ConversationNotFoundException;
import com.hcmus.ecommerce_backend.message.exception.MessageNotFoundException;
import com.hcmus.ecommerce_backend.message.model.dto.request.CreateMessageRequest;
import com.hcmus.ecommerce_backend.message.model.dto.response.MessageResponse;
import com.hcmus.ecommerce_backend.message.model.entity.Conversation;
import com.hcmus.ecommerce_backend.message.model.entity.Message;
import com.hcmus.ecommerce_backend.message.model.mapper.MessageMapper;
import com.hcmus.ecommerce_backend.message.repository.ConversationRepository;
import com.hcmus.ecommerce_backend.message.repository.MessageRepository;
import com.hcmus.ecommerce_backend.message.service.impl.MessageServiceImpl;
import com.hcmus.ecommerce_backend.user.model.entity.User;


@ExtendWith(MockitoExtension.class)
public class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private MessageServiceImpl messageService;

    private Message testMessage;
    private MessageResponse testMessageResponse;
    private CreateMessageRequest createMessageRequest;
    private Conversation testConversation;
    private User testUser;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user-id-1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        testConversation = Conversation.builder()
                .id("conversation-id-1")
                .customer(testUser)
                .isAdminRead(false)
                .isCustomerRead(true)
                .build();
        testConversation.setCreatedAt(LocalDateTime.now());

        testMessage = Message.builder()
                .id("message-id-1")
                .content("Hello, I have a question about my order.")
                .userId("user-id-1")
                .conversation(testConversation)
                .messageType("TEXT")
                .contentUrl(null)
                .isRead(false)
                .readAt(null)
                .build();
        testMessage.setCreatedAt(LocalDateTime.now());

        testMessageResponse = MessageResponse.builder()
                .id("message-id-1")
                .content("Hello, I have a question about my order.")
                .userId("user-id-1")
                .conversationId("conversation-id-1")
                .messageType("TEXT")
                .contentUrl(null)
                .createdAt(LocalDateTime.now())
                .build();

        createMessageRequest = CreateMessageRequest.builder()
                .content("New message content")
                .userId("user-id-1")
                .conversationId("conversation-id-1")
                .messageType("TEXT")
                .contentUrl(null)
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAllMessagesPaginated_ShouldReturnPaginatedMessages() {
        // Arrange
        Page<Message> messagePage = new PageImpl<>(Arrays.asList(testMessage));
        
        when(messageRepository.findAll(pageable)).thenReturn(messagePage);
        when(messageMapper.toResponse(testMessage)).thenReturn(testMessageResponse);

        // Act
        Page<MessageResponse> result = messageService.getAllMessagesPaginated(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testMessageResponse.getId(), result.getContent().get(0).getId());
        verify(messageRepository).findAll(pageable);
        verify(messageMapper).toResponse(testMessage);
    }

    @Test
    void getAllMessagesPaginated_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        when(messageRepository.findAll(pageable))
                .thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, 
                () -> messageService.getAllMessagesPaginated(pageable));
        verify(messageRepository).findAll(pageable);
    }

    @Test
    void getAllMessagesPaginated_WhenUnexpectedError_ShouldThrowException() {
        // Arrange
        when(messageRepository.findAll(pageable))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThrows(RuntimeException.class, 
                () -> messageService.getAllMessagesPaginated(pageable));
        verify(messageRepository).findAll(pageable);
    }

    @Test
    void searchMessages_WithoutKeyword_ShouldReturnAllMessages() {
        // Arrange
        Page<Message> messagePage = new PageImpl<>(Arrays.asList(testMessage));
        
        when(messageRepository.findAll(pageable)).thenReturn(messagePage);
        when(messageMapper.toResponse(testMessage)).thenReturn(testMessageResponse);

        // Act
        Page<MessageResponse> result = messageService.searchMessages(null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(messageRepository).findAll(pageable);
        verify(messageMapper).toResponse(testMessage);
    }

    @Test
    void searchMessages_WithEmptyKeyword_ShouldReturnAllMessages() {
        // Arrange
        Page<Message> messagePage = new PageImpl<>(Arrays.asList(testMessage));
        
        when(messageRepository.findAll(pageable)).thenReturn(messagePage);
        when(messageMapper.toResponse(testMessage)).thenReturn(testMessageResponse);

        // Act
        Page<MessageResponse> result = messageService.searchMessages("  ", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(messageRepository).findAll(pageable);
        verify(messageMapper).toResponse(testMessage);
    }

    @Test
    void searchMessages_WithKeyword_ShouldReturnFilteredMessages() {
        // Arrange
        String keyword = "question";
        Page<Message> messagePage = new PageImpl<>(Arrays.asList(testMessage));
        
        when(messageRepository.searchMessages(keyword, pageable)).thenReturn(messagePage);
        when(messageMapper.toResponse(testMessage)).thenReturn(testMessageResponse);

        // Act
        Page<MessageResponse> result = messageService.searchMessages(keyword, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(messageRepository).searchMessages(keyword, pageable);
        verify(messageMapper).toResponse(testMessage);
    }

    @Test
    void searchMessages_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        when(messageRepository.findAll(pageable))
                .thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, 
                () -> messageService.searchMessages(null, pageable));
        verify(messageRepository).findAll(pageable);
    }

    @Test
    void markMessageAsRead_WithValidId_ShouldMarkAsRead() {
        // Arrange
        String messageId = "message-id-1";
        Message readMessage = Message.builder()
                .id(messageId)
                .content(testMessage.getContent())
                .userId(testMessage.getUserId())
                .conversation(testMessage.getConversation())
                .messageType(testMessage.getMessageType())
                .isRead(true)
                .readAt(LocalDateTime.now())
                .build();

        when(messageRepository.findById(messageId)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(testMessage)).thenReturn(readMessage);
        when(messageMapper.toResponse(readMessage)).thenReturn(testMessageResponse);

        // Act
        MessageResponse result = messageService.markMessageAsRead(messageId);

        // Assert
        assertNotNull(result);
        assertTrue(testMessage.isRead());
        assertNotNull(testMessage.getReadAt());
        verify(messageRepository).findById(messageId);
        verify(messageRepository).save(testMessage);
        verify(messageMapper).toResponse(readMessage);
    }

    @Test
    void markMessageAsRead_WithInvalidId_ShouldThrowMessageNotFoundException() {
        // Arrange
        String messageId = "invalid-id";
        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MessageNotFoundException.class, 
                () -> messageService.markMessageAsRead(messageId));
        verify(messageRepository).findById(messageId);
        verify(messageRepository, never()).save(any());
    }

    @Test
    void markMessageAsRead_WhenUnexpectedError_ShouldThrowException() {
        // Arrange
        String messageId = "message-id-1";
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(testMessage));
        when(messageRepository.save(testMessage)).thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThrows(RuntimeException.class, 
                () -> messageService.markMessageAsRead(messageId));
        verify(messageRepository).save(testMessage);
    }

    @Test
    void getMessageById_WithValidId_ShouldReturnMessageResponse() {
        // Arrange
        String messageId = "message-id-1";
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(testMessage));
        when(messageMapper.toResponse(testMessage)).thenReturn(testMessageResponse);

        // Act
        MessageResponse result = messageService.getMessageById(messageId);

        // Assert
        assertNotNull(result);
        assertEquals(testMessageResponse.getId(), result.getId());
        verify(messageRepository).findById(messageId);
        verify(messageMapper).toResponse(testMessage);
    }

    @Test
    void getMessageById_WithInvalidId_ShouldThrowMessageNotFoundException() {
        // Arrange
        String messageId = "invalid-id";
        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MessageNotFoundException.class, 
                () -> messageService.getMessageById(messageId));
        verify(messageRepository).findById(messageId);
        verify(messageMapper, never()).toResponse(any());
    }

    @Test
    void getMessageById_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        String messageId = "message-id-1";
        when(messageRepository.findById(messageId))
                .thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, 
                () -> messageService.getMessageById(messageId));
        verify(messageRepository).findById(messageId);
    }

    @Test
    void getMessagesByConversationId_ShouldReturnConversationMessages() {
        // Arrange
        String conversationId = "conversation-id-1";
        List<Message> messages = Arrays.asList(testMessage);
        
        when(messageRepository.findByConversationId(conversationId)).thenReturn(messages);
        when(messageMapper.toResponse(testMessage)).thenReturn(testMessageResponse);

        // Act
        List<MessageResponse> result = messageService.getMessagesByConversationId(conversationId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMessageResponse.getId(), result.get(0).getId());
        verify(messageRepository).findByConversationId(conversationId);
        verify(messageMapper).toResponse(testMessage);
    }

    @Test
    void getMessagesByConversationId_WhenNoMessages_ShouldReturnEmptyList() {
        // Arrange
        String conversationId = "conversation-id-1";
        
        when(messageRepository.findByConversationId(conversationId)).thenReturn(Collections.emptyList());

        // Act
        List<MessageResponse> result = messageService.getMessagesByConversationId(conversationId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(messageRepository).findByConversationId(conversationId);
        verify(messageMapper, never()).toResponse(any());
    }

    @Test
    void getMessagesByConversationId_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        String conversationId = "conversation-id-1";
        when(messageRepository.findByConversationId(conversationId))
                .thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, 
                () -> messageService.getMessagesByConversationId(conversationId));
        verify(messageRepository).findByConversationId(conversationId);
    }

    @Test
    void getMessagesByConversationIdPaginated_ShouldReturnPaginatedMessages() {
        // Arrange
        String conversationId = "conversation-id-1";
        Page<Message> messagePage = new PageImpl<>(Arrays.asList(testMessage));
        
        when(messageRepository.findByConversationId(conversationId, pageable)).thenReturn(messagePage);
        when(messageMapper.toResponse(testMessage)).thenReturn(testMessageResponse);

        // Act
        Page<MessageResponse> result = messageService.getMessagesByConversationIdPaginated(conversationId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(messageRepository).findByConversationId(conversationId, pageable);
        verify(messageMapper).toResponse(testMessage);
    }

    @Test
    void getMessagesByConversationIdPaginated_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        String conversationId = "conversation-id-1";
        when(messageRepository.findByConversationId(conversationId, pageable))
                .thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, 
                () -> messageService.getMessagesByConversationIdPaginated(conversationId, pageable));
        verify(messageRepository).findByConversationId(conversationId, pageable);
    }

    @Test
    void searchMessagesByConversation_WithoutKeyword_ShouldReturnAllConversationMessages() {
        // Arrange
        String conversationId = "conversation-id-1";
        Page<Message> messagePage = new PageImpl<>(Arrays.asList(testMessage));
        
        when(messageRepository.findByConversationId(conversationId, pageable)).thenReturn(messagePage);
        when(messageMapper.toResponse(testMessage)).thenReturn(testMessageResponse);

        // Act
        Page<MessageResponse> result = messageService.searchMessagesByConversation(conversationId, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(messageRepository).findByConversationId(conversationId, pageable);
        verify(messageMapper).toResponse(testMessage);
    }

    @Test
    void searchMessagesByConversation_WithEmptyKeyword_ShouldReturnAllConversationMessages() {
        // Arrange
        String conversationId = "conversation-id-1";
        Page<Message> messagePage = new PageImpl<>(Arrays.asList(testMessage));
        
        when(messageRepository.findByConversationId(conversationId, pageable)).thenReturn(messagePage);
        when(messageMapper.toResponse(testMessage)).thenReturn(testMessageResponse);

        // Act
        Page<MessageResponse> result = messageService.searchMessagesByConversation(conversationId, "  ", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(messageRepository).findByConversationId(conversationId, pageable);
        verify(messageMapper).toResponse(testMessage);
    }

    @Test
    void searchMessagesByConversation_WithKeyword_ShouldReturnFilteredMessages() {
        // Arrange
        String conversationId = "conversation-id-1";
        String keyword = "question";
        Page<Message> messagePage = new PageImpl<>(Arrays.asList(testMessage));
        
        when(messageRepository.searchMessagesByConversation(conversationId, keyword, pageable))
                .thenReturn(messagePage);
        when(messageMapper.toResponse(testMessage)).thenReturn(testMessageResponse);

        // Act
        Page<MessageResponse> result = messageService.searchMessagesByConversation(conversationId, keyword, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(messageRepository).searchMessagesByConversation(conversationId, keyword, pageable);
        verify(messageMapper).toResponse(testMessage);
    }

    @Test
    void searchMessagesByConversation_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        String conversationId = "conversation-id-1";
        when(messageRepository.findByConversationId(conversationId, pageable))
                .thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, 
                () -> messageService.searchMessagesByConversation(conversationId, null, pageable));
        verify(messageRepository).findByConversationId(conversationId, pageable);
    }

    @Test
    void getMessagesByUserId_ShouldReturnUserMessages() {
        // Arrange
        String userId = "user-id-1";
        List<Message> messages = Arrays.asList(testMessage);
        
        when(messageRepository.findByUserId(userId)).thenReturn(messages);
        when(messageMapper.toResponse(testMessage)).thenReturn(testMessageResponse);

        // Act
        List<MessageResponse> result = messageService.getMessagesByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMessageResponse.getId(), result.get(0).getId());
        verify(messageRepository).findByUserId(userId);
        verify(messageMapper).toResponse(testMessage);
    }

    @Test
    void getMessagesByUserId_WhenNoMessages_ShouldReturnEmptyList() {
        // Arrange
        String userId = "user-id-1";
        
        when(messageRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // Act
        List<MessageResponse> result = messageService.getMessagesByUserId(userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(messageRepository).findByUserId(userId);
        verify(messageMapper, never()).toResponse(any());
    }

    @Test
    void getMessagesByUserId_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        String userId = "user-id-1";
        when(messageRepository.findByUserId(userId))
                .thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, 
                () -> messageService.getMessagesByUserId(userId));
        verify(messageRepository).findByUserId(userId);
    }

    @Test
    void createMessage_WithValidRequest_ShouldReturnCreatedMessage() {
        // Arrange
        when(conversationRepository.findById(createMessageRequest.getConversationId()))
                .thenReturn(Optional.of(testConversation));
        when(messageMapper.toEntity(createMessageRequest)).thenReturn(testMessage);
        when(messageRepository.save(testMessage)).thenReturn(testMessage);
        when(conversationRepository.save(testConversation)).thenReturn(testConversation);
        when(messageMapper.toResponse(testMessage)).thenReturn(testMessageResponse);

        // Act
        MessageResponse result = messageService.createMessage(createMessageRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testMessageResponse.getId(), result.getId());
        verify(conversationRepository).findById(createMessageRequest.getConversationId());
        verify(messageRepository).save(testMessage);
        verify(conversationRepository).save(testConversation);
        verify(messageMapper).toEntity(createMessageRequest);
        verify(messageMapper).toResponse(testMessage);
    }

    @Test
    void createMessage_WhenCustomerSendsMessage_ShouldUpdateReadStatus() {
        // Arrange
        createMessageRequest.setUserId(testConversation.getCustomer().getId());
        
        when(conversationRepository.findById(createMessageRequest.getConversationId()))
                .thenReturn(Optional.of(testConversation));
        when(messageMapper.toEntity(createMessageRequest)).thenReturn(testMessage);
        when(messageRepository.save(testMessage)).thenReturn(testMessage);
        when(conversationRepository.save(testConversation)).thenReturn(testConversation);
        when(messageMapper.toResponse(testMessage)).thenReturn(testMessageResponse);

        // Act
        MessageResponse result = messageService.createMessage(createMessageRequest);

        // Assert
        assertNotNull(result);
        assertTrue(testConversation.isCustomerRead());
        assertFalse(testConversation.isAdminRead());
        verify(conversationRepository).save(testConversation);
    }

    @Test
    void createMessage_WhenAdminSendsMessage_ShouldUpdateReadStatus() {
        // Arrange
        createMessageRequest.setUserId("admin-id");
        
        when(conversationRepository.findById(createMessageRequest.getConversationId()))
                .thenReturn(Optional.of(testConversation));
        when(messageMapper.toEntity(createMessageRequest)).thenReturn(testMessage);
        when(messageRepository.save(testMessage)).thenReturn(testMessage);
        when(conversationRepository.save(testConversation)).thenReturn(testConversation);
        when(messageMapper.toResponse(testMessage)).thenReturn(testMessageResponse);

        // Act
        MessageResponse result = messageService.createMessage(createMessageRequest);

        // Assert
        assertNotNull(result);
        assertTrue(testConversation.isAdminRead());
        assertFalse(testConversation.isCustomerRead());
        verify(conversationRepository).save(testConversation);
    }

    @Test
    void createMessage_WithInvalidConversation_ShouldThrowConversationNotFoundException() {
        // Arrange
        when(conversationRepository.findById(createMessageRequest.getConversationId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ConversationNotFoundException.class, 
                () -> messageService.createMessage(createMessageRequest));
        verify(conversationRepository).findById(createMessageRequest.getConversationId());
        verify(messageRepository, never()).save(any());
    }

    @Test
    void createMessage_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        when(conversationRepository.findById(createMessageRequest.getConversationId()))
                .thenReturn(Optional.of(testConversation));
        when(messageMapper.toEntity(createMessageRequest)).thenReturn(testMessage);
        when(messageRepository.save(testMessage))
                .thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, 
                () -> messageService.createMessage(createMessageRequest));
        verify(messageRepository).save(testMessage);
    }

    @Test
    void deleteMessage_WithValidId_ShouldDeleteMessage() {
        // Arrange
        String messageId = "message-id-1";
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(testMessage));

        // Act
        assertDoesNotThrow(() -> messageService.deleteMessage(messageId));

        // Assert
        verify(messageRepository).findById(messageId);
        verify(messageRepository).delete(testMessage);
    }

    @Test
    void deleteMessage_WithInvalidId_ShouldThrowMessageNotFoundException() {
        // Arrange
        String messageId = "invalid-id";
        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MessageNotFoundException.class, 
                () -> messageService.deleteMessage(messageId));
        verify(messageRepository).findById(messageId);
        verify(messageRepository, never()).delete(any());
    }

    @Test
    void deleteMessage_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        String messageId = "message-id-1";
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(testMessage));
        doThrow(new DataAccessException("Database error") {}).when(messageRepository).delete(testMessage);

        // Act & Assert
        assertThrows(DataAccessException.class, 
                () -> messageService.deleteMessage(messageId));
        verify(messageRepository).delete(testMessage);
    }

    @Test
    void findMessageById_WithValidId_ShouldReturnMessage() {
        // Arrange
        String messageId = "message-id-1";
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(testMessage));

        // Act
        Message result = messageService.findMessageById(messageId);

        // Assert
        assertNotNull(result);
        assertEquals(testMessage.getId(), result.getId());
        verify(messageRepository).findById(messageId);
    }

    @Test
    void findMessageById_WithInvalidId_ShouldThrowMessageNotFoundException() {
        // Arrange
        String messageId = "invalid-id";
        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MessageNotFoundException.class, 
                () -> messageService.findMessageById(messageId));
        verify(messageRepository).findById(messageId);
    }
}
