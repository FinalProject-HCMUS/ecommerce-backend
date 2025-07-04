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
import com.hcmus.ecommerce_backend.message.model.dto.request.CreateConversationRequest;
import com.hcmus.ecommerce_backend.message.model.dto.request.UpdateConversationRequest;
import com.hcmus.ecommerce_backend.message.model.dto.response.ConversationResponse;
import com.hcmus.ecommerce_backend.message.model.entity.Conversation;
import com.hcmus.ecommerce_backend.message.model.mapper.ConversationMapper;
import com.hcmus.ecommerce_backend.message.repository.ConversationRepository;
import com.hcmus.ecommerce_backend.message.service.impl.ConversationServiceImpl;
import com.hcmus.ecommerce_backend.user.exception.UserNotFoundException;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;






@ExtendWith(MockitoExtension.class)
class ConversationServiceImplTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ConversationServiceImpl conversationService;

    private Conversation testConversation;
    private ConversationResponse testConversationResponse;
    private CreateConversationRequest createConversationRequest;
    private UpdateConversationRequest updateConversationRequest;
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
        testConversation.setUpdatedAt(LocalDateTime.now());

        testConversationResponse = ConversationResponse.builder()
                .id("conversation-id-1")
                .isAdminRead(false)
                .isCustomerRead(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createConversationRequest = CreateConversationRequest.builder()
                .customerId("user-id-1")
                .build();

        updateConversationRequest = UpdateConversationRequest.builder()
                .isAdminRead(true)
                .isCustomerRead(false)
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAllConversationsPaginated_ShouldReturnPaginatedConversations() {
        // Arrange
        Page<Conversation> conversationPage = new PageImpl<>(Arrays.asList(testConversation));
        
        when(conversationRepository.findAllSortedByLatestMessage(pageable)).thenReturn(conversationPage);
        when(conversationMapper.toResponse(testConversation)).thenReturn(testConversationResponse);

        // Act
        Page<ConversationResponse> result = conversationService.getAllConversationsPaginated(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testConversationResponse.getId(), result.getContent().get(0).getId());
        verify(conversationRepository).findAllSortedByLatestMessage(pageable);
        verify(conversationMapper).toResponse(testConversation);
    }

    @Test
    void getAllConversationsPaginated_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        when(conversationRepository.findAllSortedByLatestMessage(pageable))
                .thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, 
                () -> conversationService.getAllConversationsPaginated(pageable));
        verify(conversationRepository).findAllSortedByLatestMessage(pageable);
    }

    @Test
    void getAllConversationsPaginated_WhenUnexpectedError_ShouldThrowException() {
        // Arrange
        when(conversationRepository.findAllSortedByLatestMessage(pageable))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThrows(RuntimeException.class, 
                () -> conversationService.getAllConversationsPaginated(pageable));
        verify(conversationRepository).findAllSortedByLatestMessage(pageable);
    }

    @Test
    void searchConversations_WithoutKeyword_ShouldReturnAllConversations() {
        // Arrange
        Page<Conversation> conversationPage = new PageImpl<>(Arrays.asList(testConversation));
        
        when(conversationRepository.findAll(pageable)).thenReturn(conversationPage);
        when(conversationMapper.toResponse(testConversation)).thenReturn(testConversationResponse);

        // Act
        Page<ConversationResponse> result = conversationService.searchConversations(null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(conversationRepository).findAll(pageable);
        verify(conversationMapper).toResponse(testConversation);
    }

    @Test
    void searchConversations_WithEmptyKeyword_ShouldReturnAllConversations() {
        // Arrange
        Page<Conversation> conversationPage = new PageImpl<>(Arrays.asList(testConversation));
        
        when(conversationRepository.findAll(pageable)).thenReturn(conversationPage);
        when(conversationMapper.toResponse(testConversation)).thenReturn(testConversationResponse);

        // Act
        Page<ConversationResponse> result = conversationService.searchConversations("  ", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(conversationRepository).findAll(pageable);
        verify(conversationMapper).toResponse(testConversation);
    }

    @Test
    void searchConversations_WithKeyword_ShouldReturnFilteredConversations() {
        // Arrange
        String keyword = "john";
        Page<Conversation> conversationPage = new PageImpl<>(Arrays.asList(testConversation));
        
        when(conversationRepository.searchConversationsSortedByLatestMessage(keyword, pageable))
                .thenReturn(conversationPage);
        when(conversationMapper.toResponse(testConversation)).thenReturn(testConversationResponse);

        // Act
        Page<ConversationResponse> result = conversationService.searchConversations(keyword, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(conversationRepository).searchConversationsSortedByLatestMessage(keyword, pageable);
        verify(conversationMapper).toResponse(testConversation);
    }

    @Test
    void searchConversations_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        when(conversationRepository.findAll(pageable))
                .thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, 
                () -> conversationService.searchConversations(null, pageable));
        verify(conversationRepository).findAll(pageable);
    }

    @Test
    void searchConversations_WhenUnexpectedError_ShouldThrowException() {
        // Arrange
        when(conversationRepository.findAll(pageable))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThrows(RuntimeException.class, 
                () -> conversationService.searchConversations(null, pageable));
        verify(conversationRepository).findAll(pageable);
    }

    @Test
    void getConversationById_WithValidId_ShouldReturnConversationResponse() {
        // Arrange
        String conversationId = "conversation-id-1";
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(testConversation));
        when(conversationMapper.toResponse(testConversation)).thenReturn(testConversationResponse);

        // Act
        ConversationResponse result = conversationService.getConversationById(conversationId);

        // Assert
        assertNotNull(result);
        assertEquals(testConversationResponse.getId(), result.getId());
        verify(conversationRepository).findById(conversationId);
        verify(conversationMapper).toResponse(testConversation);
    }

    @Test
    void getConversationById_WithInvalidId_ShouldThrowConversationNotFoundException() {
        // Arrange
        String conversationId = "invalid-id";
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ConversationNotFoundException.class, 
                () -> conversationService.getConversationById(conversationId));
        verify(conversationRepository).findById(conversationId);
        verify(conversationMapper, never()).toResponse(any());
    }

    @Test
    void getConversationById_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        String conversationId = "conversation-id-1";
        when(conversationRepository.findById(conversationId))
                .thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, 
                () -> conversationService.getConversationById(conversationId));
        verify(conversationRepository).findById(conversationId);
    }

    @Test
    void getConversationsByCustomerId_ShouldReturnCustomerConversations() {
        // Arrange
        String customerId = "user-id-1";
        List<Conversation> conversations = Arrays.asList(testConversation);
        
        when(conversationRepository.findByCustomerId(customerId)).thenReturn(conversations);
        when(conversationMapper.toResponse(testConversation)).thenReturn(testConversationResponse);

        // Act
        List<ConversationResponse> result = conversationService.getConversationsByCustomerId(customerId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testConversationResponse.getId(), result.get(0).getId());
        verify(conversationRepository).findByCustomerId(customerId);
        verify(conversationMapper).toResponse(testConversation);
    }

    @Test
    void getConversationsByCustomerId_WhenNoConversations_ShouldReturnEmptyList() {
        // Arrange
        String customerId = "user-id-1";
        
        when(conversationRepository.findByCustomerId(customerId)).thenReturn(Collections.emptyList());

        // Act
        List<ConversationResponse> result = conversationService.getConversationsByCustomerId(customerId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(conversationRepository).findByCustomerId(customerId);
        verify(conversationMapper, never()).toResponse(any());
    }

    @Test
    void getConversationsByCustomerId_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        String customerId = "user-id-1";
        when(conversationRepository.findByCustomerId(customerId))
                .thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, 
                () -> conversationService.getConversationsByCustomerId(customerId));
        verify(conversationRepository).findByCustomerId(customerId);
    }

    @Test
    void createConversation_WithValidRequest_ShouldReturnCreatedConversation() {
        // Arrange
        when(userRepository.findById(createConversationRequest.getCustomerId()))
                .thenReturn(Optional.of(testUser));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(testConversation);
        when(conversationMapper.toResponse(testConversation)).thenReturn(testConversationResponse);

        // Act
        ConversationResponse result = conversationService.createConversation(createConversationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testConversationResponse.getId(), result.getId());
        verify(userRepository).findById(createConversationRequest.getCustomerId());
        verify(conversationRepository).save(any(Conversation.class));
        verify(conversationMapper).toResponse(testConversation);
    }

    @Test
    void createConversation_WithInvalidCustomer_ShouldThrowUserNotFoundException() {
        // Arrange
        when(userRepository.findById(createConversationRequest.getCustomerId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, 
                () -> conversationService.createConversation(createConversationRequest));
        verify(userRepository).findById(createConversationRequest.getCustomerId());
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void createConversation_WhenCustomerIsNullAfterSave_ShouldThrowIllegalStateException() {
        // Arrange
        Conversation conversationWithNullCustomer = Conversation.builder()
                .id("conversation-id-1")
                .customer(null) // Simulate null customer
                .isAdminRead(false)
                .isCustomerRead(true)
                .build();

        when(userRepository.findById(createConversationRequest.getCustomerId()))
                .thenReturn(Optional.of(testUser));
        when(conversationRepository.save(any(Conversation.class)))
                .thenReturn(conversationWithNullCustomer);

        // Act & Assert
        assertThrows(IllegalStateException.class, 
                () -> conversationService.createConversation(createConversationRequest));
        verify(userRepository).findById(createConversationRequest.getCustomerId());
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void createConversation_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        when(userRepository.findById(createConversationRequest.getCustomerId()))
                .thenReturn(Optional.of(testUser));
        when(conversationRepository.save(any(Conversation.class)))
                .thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, 
                () -> conversationService.createConversation(createConversationRequest));
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void updateConversation_WithValidRequest_ShouldReturnUpdatedConversation() {
        // Arrange
        String conversationId = "conversation-id-1";
        Conversation updatedConversation = Conversation.builder()
                .id(conversationId)
                .customer(testUser)
                .isAdminRead(true)
                .isCustomerRead(false)
                .build();

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(testConversation));
        when(conversationRepository.save(testConversation)).thenReturn(updatedConversation);
        when(conversationMapper.toResponse(updatedConversation)).thenReturn(testConversationResponse);

        // Act
        ConversationResponse result = conversationService.updateConversation(conversationId, updateConversationRequest);

        // Assert
        assertNotNull(result);
        verify(conversationRepository).findById(conversationId);
        verify(conversationMapper).updateEntity(updateConversationRequest, testConversation);
        verify(conversationRepository).save(testConversation);
        verify(conversationMapper).toResponse(updatedConversation);
        
        // Verify that the read statuses were updated
        assertTrue(testConversation.isAdminRead());
        assertFalse(testConversation.isCustomerRead());
    }

    @Test
    void updateConversation_WithPartialRequest_ShouldUpdateOnlyProvidedFields() {
        // Arrange
        String conversationId = "conversation-id-1";
        UpdateConversationRequest partialRequest = UpdateConversationRequest.builder()
                .isAdminRead(true)
                .isCustomerRead(null) // Not provided
                .build();

        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(testConversation));
        when(conversationRepository.save(testConversation)).thenReturn(testConversation);
        when(conversationMapper.toResponse(testConversation)).thenReturn(testConversationResponse);

        // Act
        ConversationResponse result = conversationService.updateConversation(conversationId, partialRequest);

        // Assert
        assertNotNull(result);
        verify(conversationRepository).findById(conversationId);
        verify(conversationRepository).save(testConversation);
        
        // Verify that only isAdminRead was updated
        assertTrue(testConversation.isAdminRead());
        assertTrue(testConversation.isCustomerRead()); // Should remain unchanged
    }

    @Test
    void updateConversation_WithInvalidId_ShouldThrowConversationNotFoundException() {
        // Arrange
        String conversationId = "invalid-id";
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ConversationNotFoundException.class, 
                () -> conversationService.updateConversation(conversationId, updateConversationRequest));
        verify(conversationRepository).findById(conversationId);
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void updateConversation_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        String conversationId = "conversation-id-1";
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(testConversation));
        when(conversationRepository.save(testConversation))
                .thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, 
                () -> conversationService.updateConversation(conversationId, updateConversationRequest));
        verify(conversationRepository).save(testConversation);
    }

    @Test
    void deleteConversation_WithValidId_ShouldDeleteConversation() {
        // Arrange
        String conversationId = "conversation-id-1";
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(testConversation));

        // Act
        assertDoesNotThrow(() -> conversationService.deleteConversation(conversationId));

        // Assert
        verify(conversationRepository).findById(conversationId);
        verify(conversationRepository).delete(testConversation);
    }

    @Test
    void deleteConversation_WithInvalidId_ShouldThrowConversationNotFoundException() {
        // Arrange
        String conversationId = "invalid-id";
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ConversationNotFoundException.class, 
                () -> conversationService.deleteConversation(conversationId));
        verify(conversationRepository).findById(conversationId);
        verify(conversationRepository, never()).delete(any());
    }

    @Test
    void deleteConversation_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        String conversationId = "conversation-id-1";
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(testConversation));
        doThrow(new DataAccessException("Database error") {}).when(conversationRepository).delete(testConversation);

        // Act & Assert
        assertThrows(DataAccessException.class, 
                () -> conversationService.deleteConversation(conversationId));
        verify(conversationRepository).delete(testConversation);
    }

    @Test
    void deleteConversation_WhenUnexpectedError_ShouldThrowException() {
        // Arrange
        String conversationId = "conversation-id-1";
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(testConversation));
        doThrow(new RuntimeException("Unexpected error")).when(conversationRepository).delete(testConversation);

        // Act & Assert
        assertThrows(RuntimeException.class, 
                () -> conversationService.deleteConversation(conversationId));
        verify(conversationRepository).delete(testConversation);
    }

    @Test
    void findConversationById_WithValidId_ShouldReturnConversation() {
        // Arrange
        String conversationId = "conversation-id-1";
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(testConversation));

        // Act
        Conversation result = conversationService.findConversationById(conversationId);

        // Assert
        assertNotNull(result);
        assertEquals(testConversation.getId(), result.getId());
        verify(conversationRepository).findById(conversationId);
    }

    @Test
    void findConversationById_WithInvalidId_ShouldThrowConversationNotFoundException() {
        // Arrange
        String conversationId = "invalid-id";
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ConversationNotFoundException.class, 
                () -> conversationService.findConversationById(conversationId));
        verify(conversationRepository).findById(conversationId);
    }
}
