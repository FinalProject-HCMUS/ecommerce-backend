package com.hcmus.ecommerce_backend.unit.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import com.hcmus.ecommerce_backend.common.service.EmailService;
import com.hcmus.ecommerce_backend.message.model.dto.request.CreateConversationRequest;
import com.hcmus.ecommerce_backend.message.model.dto.request.CreateMessageRequest;
import com.hcmus.ecommerce_backend.message.model.dto.response.ConversationResponse;
import com.hcmus.ecommerce_backend.message.service.ConversationService;
import com.hcmus.ecommerce_backend.message.service.MessageService;
import com.hcmus.ecommerce_backend.user.exception.*;
import com.hcmus.ecommerce_backend.user.model.dto.request.*;
import com.hcmus.ecommerce_backend.user.model.dto.response.UserResponse;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.model.entity.VerificationToken;
import com.hcmus.ecommerce_backend.user.model.enums.Role;
import com.hcmus.ecommerce_backend.user.model.mapper.UserMapper;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;
import com.hcmus.ecommerce_backend.user.repository.VerificationTokenRepository;
import com.hcmus.ecommerce_backend.user.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private ConversationService conversationService;

    @Mock
    private MessageService messageService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponse userResponse;
    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;
    private ChangePasswordRequest changePasswordRequest;
    private ResetPasswordRequest resetPasswordRequest;
    private VerificationToken verificationToken;
    private ConversationResponse conversationResponse;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Setup User entity
        user = User.builder()
                .id("user-1")
                .email("test@example.com")
                .phoneNumber("1234567890")
                .firstName("John")
                .lastName("Doe")
                .address("123 Main St")
                .weight(70)
                .height(175)
                .password("encodedPassword")
                .enabled(false)
                .role(Role.USER)
                .tokenVersion(0)
                .build();

        // Setup UserResponse
        userResponse = UserResponse.builder()
                .id("user-1")
                .email("test@example.com")
                .phoneNumber("1234567890")
                .firstName("John")
                .lastName("Doe")
                .address("123 Main St")
                .weight(70)
                .height(175)
                .enabled(false)
                .role(Role.USER)
                .build();

        // Setup CreateUserRequest
        createUserRequest = CreateUserRequest.builder()
                .email("test@example.com")
                .phoneNumber("1234567890")
                .firstName("John")
                .lastName("Doe")
                .address("123 Main St")
                .weight(70)
                .height(175)
                .password("password123")
                .build();

        // Setup UpdateUserRequest
        updateUserRequest = UpdateUserRequest.builder()
                .phoneNumber("9876543210")
                .firstName("Jane")
                .lastName("Smith")
                .address("456 Oak Ave")
                .weight(65)
                .height(165)
                .photo("photo.jpg")
                .enabled(true)
                .build();

        // Setup ChangePasswordRequest
        changePasswordRequest = ChangePasswordRequest.builder()
                .currentPassword("password123")
                .newPassword("newPassword123")
                .confirmPassword("newPassword123")
                .build();

        // Setup ResetPasswordRequest
        resetPasswordRequest = ResetPasswordRequest.builder()
                .newPassword("resetPassword123")
                .confirmPassword("resetPassword123")
                .token("reset-token")
                .build();

        // Setup VerificationToken
        verificationToken = VerificationToken.builder()
                .id("token-1")
                .token("verification-token")
                .expiryDate(LocalDateTime.now().plusHours(24))
                .user(user)
                .build();

        // Setup ConversationResponse
        conversationResponse = ConversationResponse.builder()
                .id("conv-1")
                .customer(userResponse)
                .build();

        pageable = PageRequest.of(0, 10);
    }

    // Tests for getAllUsers method
    @Test
    void getAllUsers_Success() {
        // Given
        List<User> users = Arrays.asList(user);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // When
        Page<UserResponse> result = userService.getAllUsers(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(userResponse, result.getContent().get(0));
        verify(userRepository).findAll(pageable);
        verify(userMapper).toResponse(user);
    }

    @Test
    void getAllUsers_DatabaseError_ReturnsEmptyPage() {
        // Given
        when(userRepository.findAll(pageable)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When
        Page<UserResponse> result = userService.getAllUsers(pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll(pageable);
    }

    @Test
    void getAllUsers_UnexpectedError_ThrowsException() {
        // Given
        when(userRepository.findAll(pageable)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.getAllUsers(pageable));
        verify(userRepository).findAll(pageable);
    }

    // Tests for searchUsers method
    @Test
    void searchUsers_Success() {
        // Given
        String keyword = "john";
        List<User> users = Arrays.asList(user);
        Page<User> userPage = new PageImpl<>(users, pageable, 1);
        when(userRepository.searchUsers(keyword, pageable)).thenReturn(userPage);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // When
        Page<UserResponse> result = userService.searchUsers(keyword, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(userResponse, result.getContent().get(0));
        verify(userRepository).searchUsers(keyword, pageable);
        verify(userMapper).toResponse(user);
    }

    @Test
    void searchUsers_DatabaseError_ReturnsEmptyPage() {
        // Given
        String keyword = "john";
        when(userRepository.searchUsers(keyword, pageable))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When
        Page<UserResponse> result = userService.searchUsers(keyword, pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).searchUsers(keyword, pageable);
    }

    @Test
    void searchUsers_UnexpectedError_ThrowsException() {
        // Given
        String keyword = "john";
        when(userRepository.searchUsers(keyword, pageable)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.searchUsers(keyword, pageable));
        verify(userRepository).searchUsers(keyword, pageable);
    }

    // Tests for getUserById method
    @Test
    void getUserById_Success() {
        // Given
        String id = "user-1";
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // When
        UserResponse result = userService.getUserById(id);

        // Then
        assertNotNull(result);
        assertEquals(userResponse, result);
        verify(userRepository).findById(id);
        verify(userMapper).toResponse(user);
    }

    @Test
    void getUserById_NotFound() {
        // Given
        String id = "non-existent";
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(id));
        verify(userRepository).findById(id);
    }

    @Test
    void getUserById_DatabaseError() {
        // Given
        String id = "user-1";
        when(userRepository.findById(id)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> userService.getUserById(id));
        verify(userRepository).findById(id);
    }

    @Test
    void getUserById_UnexpectedError() {
        // Given
        String id = "user-1";
        when(userRepository.findById(id)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.getUserById(id));
        verify(userRepository).findById(id);
    }

    // Tests for createUser method
    @Test
    void createUser_Success() {
        // Given
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(createUserRequest.getPhoneNumber())).thenReturn(false);
        when(userMapper.toEntity(createUserRequest)).thenReturn(user);
        when(passwordEncoder.encode(createUserRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(verificationToken);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // When
        UserResponse result = userService.createUser(createUserRequest);

        // Then
        assertNotNull(result);
        assertEquals(userResponse, result);
        verify(userRepository).existsByEmail(createUserRequest.getEmail());
        verify(userRepository).existsByPhoneNumber(createUserRequest.getPhoneNumber());
        verify(userMapper).toEntity(createUserRequest);
        verify(passwordEncoder).encode(createUserRequest.getPassword());
        verify(userRepository).save(user);
        verify(verificationTokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendEmailConfirmation(anyString(), anyString(), anyString());
        verify(userMapper).toResponse(user);
    }

    @Test
    void createUser_EmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(createUserRequest));
        verify(userRepository).existsByEmail(createUserRequest.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_PhoneNumberAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(createUserRequest.getPhoneNumber())).thenReturn(true);

        // When & Then
        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(createUserRequest));
        verify(userRepository).existsByEmail(createUserRequest.getEmail());
        verify(userRepository).existsByPhoneNumber(createUserRequest.getPhoneNumber());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_DatabaseError() {
        // Given
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(createUserRequest.getPhoneNumber())).thenReturn(false);
        when(userMapper.toEntity(createUserRequest)).thenReturn(user);
        when(passwordEncoder.encode(createUserRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> userService.createUser(createUserRequest));
        verify(userRepository).save(user);
    }

    // Tests for getCurrentUser method
    @Test
    void getCurrentUser_Success() {
        // Given
        String userId = "user-1";
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString("userId")).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // When
        UserResponse result = userService.getCurrentUser();

        // Then
        assertNotNull(result);
        assertEquals(userResponse, result);
        verify(jwt).getClaimAsString("userId");
        verify(userRepository).findById(userId);
        verify(userMapper).toResponse(user);
    }

    @Test
    void getCurrentUser_NoAuthentication() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // When & Then
        assertThrows(UserNotAuthorizedException.class, () -> userService.getCurrentUser());
    }

    @Test
    void getCurrentUser_InvalidPrincipal() {
        // Given
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not-a-jwt");

        // When & Then
        assertThrows(UserNotAuthorizedException.class, () -> userService.getCurrentUser());
    }

    @Test
    void getCurrentUser_UserNotFound() {
        // Given
        String userId = "non-existent";
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaimAsString("userId")).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.getCurrentUser());
        verify(userRepository).findById(userId);
    }

    // Tests for updateUser method
    @Test
    void updateUser_Success_SameUser() {
        // Given
        String id = "user-1";
        String authenticatedUserId = "user-1";
        setupAuthentication(authenticatedUserId, false);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.existsByPhoneNumber(updateUserRequest.getPhoneNumber())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // When
        UserResponse result = userService.updateUser(id, updateUserRequest);

        // Then
        assertNotNull(result);
        assertEquals(userResponse, result);
        verify(userRepository).findById(id);
        verify(userMapper).updateEntity(updateUserRequest, user);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(user);
    }

    @Test
    void updateUser_Success_Admin() {
        // Given
        String id = "user-1";
        String authenticatedUserId = "admin-1";
        setupAuthentication(authenticatedUserId, true);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.existsByPhoneNumber(updateUserRequest.getPhoneNumber())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // When
        UserResponse result = userService.updateUser(id, updateUserRequest);

        // Then
        assertNotNull(result);
        assertEquals(userResponse, result);
        assertTrue(user.isEnabled()); // Admin can modify enabled field
        verify(userRepository).findById(id);
        verify(userMapper).updateEntity(updateUserRequest, user);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(user);
    }

    @Test
    void updateUser_Unauthorized() {
        // Given
        String id = "user-1";
        String authenticatedUserId = "user-2";
        setupAuthentication(authenticatedUserId, false);

        // When & Then
        assertThrows(UserNotAuthorizedException.class, () -> userService.updateUser(id, updateUserRequest));
    }

    @Test
    void updateUser_PhoneNumberAlreadyExists() {
        // Given
        String id = "user-1";
        String authenticatedUserId = "user-1";
        setupAuthentication(authenticatedUserId, false);

        user.setPhoneNumber("oldPhoneNumber");
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.existsByPhoneNumber(updateUserRequest.getPhoneNumber())).thenReturn(true);

        // When & Then
        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser(id, updateUserRequest));
        verify(userRepository).existsByPhoneNumber(updateUserRequest.getPhoneNumber());
    }

    @Test
    void updateUser_UserNotFound() {
        // Given
        String id = "non-existent";
        String authenticatedUserId = "user-1";
        setupAuthentication(authenticatedUserId, true);

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(id, updateUserRequest));
        verify(userRepository).findById(id);
    }

    // Tests for deleteUser method
    @Test
    void deleteUser_Success() {
        // Given
        String id = "user-1";
        when(userRepository.existsById(id)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> userService.deleteUser(id));

        // Then
        verify(userRepository).existsById(id);
        verify(userRepository).deleteById(id);
    }

    @Test
    void deleteUser_NotFound() {
        // Given
        String id = "non-existent";
        when(userRepository.existsById(id)).thenReturn(false);

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(id));
        verify(userRepository).existsById(id);
        verify(userRepository, never()).deleteById(id);
    }

    @Test
    void deleteUser_DatabaseError() {
        // Given
        String id = "user-1";
        when(userRepository.existsById(id)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("Database error")).when(userRepository).deleteById(id);

        // When & Then
        assertThrows(DataAccessException.class, () -> userService.deleteUser(id));
        verify(userRepository).deleteById(id);
    }

    // Tests for changePassword method
    @Test
    void changePassword_Success() {
        // Given
        String userId = "user-1";
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(changePasswordRequest.getNewPassword())).thenReturn("newEncodedPassword");
        when(userRepository.save(user)).thenReturn(user);

        // When
        assertDoesNotThrow(() -> userService.changePassword(userId, changePasswordRequest));

        // Then
        assertEquals("newEncodedPassword", user.getPassword());
        assertEquals(1, user.getTokenVersion());
        verify(userRepository).findById(userId);
        // verify(passwordEncoder).matches(changePasswordRequest.getCurrentPassword(), user.getPassword());
        verify(passwordEncoder).encode(changePasswordRequest.getNewPassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_PasswordMismatch() {
        // Given
        String userId = "user-1";
        changePasswordRequest.setConfirmPassword("differentPassword");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.changePassword(userId, changePasswordRequest));
    }

    @Test
    void changePassword_IncorrectCurrentPassword() {
        // Given
        String userId = "user-1";
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.changePassword(userId, changePasswordRequest));
        verify(passwordEncoder).matches(changePasswordRequest.getCurrentPassword(), user.getPassword());
    }

    @Test
    void changePassword_UserNotFound() {
        // Given
        String userId = "non-existent";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.changePassword(userId, changePasswordRequest));
        verify(userRepository).findById(userId);
    }

    // Tests for confirmEmail method
    @Test
    void confirmEmail_Success() {
        // Given
        String token = "verification-token";
        when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.of(verificationToken));
        when(userRepository.save(user)).thenReturn(user);
        when(conversationService.createConversation(any(CreateConversationRequest.class)))
                .thenReturn(conversationResponse);

        // When
        boolean result = userService.confirmEmail(token);

        // Then
        assertTrue(result);
        assertTrue(user.isEnabled());
        verify(verificationTokenRepository).findByToken(token);
        verify(userRepository).save(user);
        verify(verificationTokenRepository).delete(verificationToken);
        verify(conversationService).createConversation(any(CreateConversationRequest.class));
        verify(messageService, times(2)).createMessage(any(CreateMessageRequest.class));
    }

    @Test
    void confirmEmail_TokenNotFound() {
        // Given
        String token = "invalid-token";
        when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(VerificationTokenNotFoundException.class, () -> userService.confirmEmail(token));
        verify(verificationTokenRepository).findByToken(token);
    }

    @Test
    void confirmEmail_TokenExpired() {
        // Given
        String token = "expired-token";
        verificationToken.setExpiryDate(LocalDateTime.now().minusHours(1));
        when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.of(verificationToken));

        // When & Then
        assertThrows(VerificationTokenAlreadyExpired.class, () -> userService.confirmEmail(token));
        verify(verificationTokenRepository).findByToken(token);
    }

    @Test
    void confirmEmail_ConversationCreationFails() {
        // Given
        String token = "verification-token";
        when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.of(verificationToken));
        when(userRepository.save(user)).thenReturn(user);
        when(conversationService.createConversation(any(CreateConversationRequest.class)))
                .thenThrow(new RuntimeException("Conversation creation failed"));

        // When
        boolean result = userService.confirmEmail(token);

        // Then
        assertTrue(result); // Should still return true even if conversation creation fails
        assertTrue(user.isEnabled());
        verify(verificationTokenRepository).findByToken(token);
        verify(userRepository).save(user);
        verify(verificationTokenRepository).delete(verificationToken);
        verify(conversationService).createConversation(any(CreateConversationRequest.class));
    }

    // Tests for resendConfirmationEmail method
    @Test
    void resendConfirmationEmail_Success() {
        // Given
        String email = "test@example.com";
        user.setEnabled(false);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(verificationTokenRepository.findByUserId(user.getId())).thenReturn(Optional.of(verificationToken));
        when(verificationTokenRepository.save(verificationToken)).thenReturn(verificationToken);

        // When
        assertDoesNotThrow(() -> userService.resendConfirmationEmail(email));

        // Then
        verify(userRepository).findByEmail(email);
        verify(verificationTokenRepository).findByUserId(user.getId());
        verify(verificationTokenRepository).save(verificationToken);
        verify(emailService).sendEmailConfirmation(anyString(), anyString(), anyString());
    }

    @Test
    void resendConfirmationEmail_UserAlreadyEnabled() {
        // Given
        String email = "test@example.com";
        user.setEnabled(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        assertDoesNotThrow(() -> userService.resendConfirmationEmail(email));

        // Then
        verify(userRepository).findByEmail(email);
        verify(emailService, never()).sendEmailConfirmation(anyString(), anyString(), anyString());
    }

    @Test
    void resendConfirmationEmail_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        assertDoesNotThrow(() -> userService.resendConfirmationEmail(email));

        // Then
        verify(userRepository).findByEmail(email);
        verify(emailService, never()).sendEmailConfirmation(anyString(), anyString(), anyString());
    }

    @Test
    void resendConfirmationEmail_DatabaseError() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.resendConfirmationEmail(email));
        verify(userRepository).findByEmail(email);
    }

    // Tests for sendResetPasswordEmail method
    @Test
    void sendResetPasswordEmail_Success() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(verificationTokenRepository.findByUserId(user.getId())).thenReturn(Optional.of(verificationToken));
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(verificationToken);

        // When
        assertDoesNotThrow(() -> userService.sendResetPasswordEmail(email));

        // Then
        verify(userRepository).findByEmail(email);
        verify(verificationTokenRepository).findByUserId(user.getId());
        verify(verificationTokenRepository).delete(verificationToken);
        verify(verificationTokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendResetPasswordEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendResetPasswordEmail_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        assertDoesNotThrow(() -> userService.sendResetPasswordEmail(email));

        // Then
        verify(userRepository).findByEmail(email);
        verify(emailService, never()).sendResetPasswordEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendResetPasswordEmail_DatabaseError() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.sendResetPasswordEmail(email));
        verify(userRepository).findByEmail(email);
    }

    // Tests for validateResetToken method
    @Test
    void validateResetToken_Success() {
        // Given
        String token = "reset-token";
        when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.of(verificationToken));

        // When
        assertDoesNotThrow(() -> userService.validateResetToken(token));

        // Then
        verify(verificationTokenRepository).findByToken(token);
    }

    @Test
    void validateResetToken_TokenNotFound() {
        // Given
        String token = "invalid-token";
        when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(VerificationTokenNotFoundException.class, () -> userService.validateResetToken(token));
        verify(verificationTokenRepository).findByToken(token);
    }

    @Test
    void validateResetToken_TokenExpired() {
        // Given
        String token = "expired-token";
        verificationToken.setExpiryDate(LocalDateTime.now().minusHours(1));
        when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.of(verificationToken));

        // When & Then
        assertThrows(VerificationTokenAlreadyExpired.class, () -> userService.validateResetToken(token));
        verify(verificationTokenRepository).findByToken(token);
    }

    // Tests for resetPassword method
    @Test
    void resetPassword_Success() {
        // Given
        when(verificationTokenRepository.findByToken(resetPasswordRequest.getToken()))
                .thenReturn(Optional.of(verificationToken));
        when(passwordEncoder.encode(resetPasswordRequest.getNewPassword())).thenReturn("newEncodedPassword");
        when(userRepository.save(user)).thenReturn(user);

        // When
        assertDoesNotThrow(() -> userService.resetPassword(resetPasswordRequest));

        // Then
        assertEquals("newEncodedPassword", user.getPassword());
        assertEquals(1, user.getTokenVersion());
        verify(verificationTokenRepository).findByToken(resetPasswordRequest.getToken());
        verify(passwordEncoder).encode(resetPasswordRequest.getNewPassword());
        verify(userRepository).save(user);
        verify(verificationTokenRepository).delete(verificationToken);
    }

    @Test
    void resetPassword_PasswordMismatch() {
        // Given
        resetPasswordRequest.setConfirmPassword("differentPassword");

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> userService.resetPassword(resetPasswordRequest));
    }

    @Test
    void resetPassword_TokenNotFound() {
        // Given
        when(verificationTokenRepository.findByToken(resetPasswordRequest.getToken())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(VerificationTokenNotFoundException.class, () -> userService.resetPassword(resetPasswordRequest));
        verify(verificationTokenRepository).findByToken(resetPasswordRequest.getToken());
    }

    @Test
    void resetPassword_TokenExpired() {
        // Given
        verificationToken.setExpiryDate(LocalDateTime.now().minusHours(1));
        when(verificationTokenRepository.findByToken(resetPasswordRequest.getToken()))
                .thenReturn(Optional.of(verificationToken));

        // When & Then
        assertThrows(VerificationTokenAlreadyExpired.class, () -> userService.resetPassword(resetPasswordRequest));
        verify(verificationTokenRepository).findByToken(resetPasswordRequest.getToken());
        verify(verificationTokenRepository).delete(verificationToken);
    }

    private void setupAuthentication(String userId, boolean isAdmin) {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        Jwt jwt = mock(Jwt.class);

        doReturn(authentication).when(securityContext).getAuthentication();
        doReturn(jwt).when(authentication).getPrincipal();
        doReturn(userId).when(jwt).getClaimAsString("userId");

        Collection<GrantedAuthority> authorities = isAdmin
                ? Collections.singletonList(new SimpleGrantedAuthority("ADMIN"))
                : Collections.singletonList(new SimpleGrantedAuthority("USER"));

        // Here we use doReturn to avoid the wildcard capture problem
        doReturn(authorities).when(authentication).getAuthorities();

        SecurityContextHolder.setContext(securityContext);
    }

    // Additional edge case tests
    @Test
    void createUser_UnexpectedError() {
        // Given
        when(userRepository.existsByEmail(createUserRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(createUserRequest.getPhoneNumber())).thenReturn(false);
        when(userMapper.toEntity(createUserRequest)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.createUser(createUserRequest));
        verify(userMapper).toEntity(createUserRequest);
    }

    @Test
    void updateUser_SamePhoneNumber() {
        // Given
        String id = "user-1";
        String authenticatedUserId = "user-1";
        setupAuthentication(authenticatedUserId, false);

        user.setPhoneNumber(updateUserRequest.getPhoneNumber());
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // When
        UserResponse result = userService.updateUser(id, updateUserRequest);

        // Then
        assertNotNull(result);
        verify(userRepository, never()).existsByPhoneNumber(any());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_DatabaseError() {
        // Given
        String userId = "user-1";
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(changePasswordRequest.getNewPassword())).thenReturn("newEncodedPassword");
        when(userRepository.save(user)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> userService.changePassword(userId, changePasswordRequest));
        verify(userRepository).save(user);
    }

    @Test
    void resendConfirmationEmail_TokenNotFound() {
        // Given
        String email = "test@example.com";
        user.setEnabled(false);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(verificationTokenRepository.findByUserId(user.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.resendConfirmationEmail(email));
        verify(verificationTokenRepository).findByUserId(user.getId());
    }

    @Test
    void sendResetPasswordEmail_NoExistingToken() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(verificationTokenRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(verificationToken);

        // When
        assertDoesNotThrow(() -> userService.sendResetPasswordEmail(email));

        // Then
        verify(verificationTokenRepository).findByUserId(user.getId());
        verify(verificationTokenRepository, never()).delete(any());
        verify(verificationTokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendResetPasswordEmail(anyString(), anyString(), anyString());
    }

    @Test
    void getAllUsers_EmptyResult() {
        // Given
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        // When
        Page<UserResponse> result = userService.getAllUsers(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(userRepository).findAll(pageable);
    }

    @Test
    void searchUsers_EmptyResult() {
        // Given
        String keyword = "nonexistent";
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(userRepository.searchUsers(keyword, pageable)).thenReturn(emptyPage);

        // When
        Page<UserResponse> result = userService.searchUsers(keyword, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(userRepository).searchUsers(keyword, pageable);
    }

    @Test
    void updateUser_NoAuthentication() {
        // Given
        String id = "user-1";
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // When & Then
        assertThrows(UserNotAuthorizedException.class, () -> userService.updateUser(id, updateUserRequest));
    }

    @Test
    void updateUser_InvalidPrincipal() {
        // Given
        String id = "user-1";
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not-a-jwt");

        // When & Then
        assertThrows(UserNotAuthorizedException.class, () -> userService.updateUser(id, updateUserRequest));
    }

    @Test
    void deleteUser_UnexpectedError() {
        // Given
        String id = "user-1";
        when(userRepository.existsById(id)).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(userRepository).deleteById(id);

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.deleteUser(id));
        verify(userRepository).deleteById(id);
    }

    @Test
    void changePassword_UnexpectedError() {
        // Given
        String userId = "user-1";
        when(userRepository.findById(userId)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.changePassword(userId, changePasswordRequest));
        verify(userRepository).findById(userId);
    }

    @Test
    void resendConfirmationEmail_UnexpectedError() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.resendConfirmationEmail(email));
        verify(userRepository).findByEmail(email);
    }

    @Test
    void sendResetPasswordEmail_UnexpectedError() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.sendResetPasswordEmail(email));
        verify(userRepository).findByEmail(email);
    }

    @Test
    void validateResetToken_UnexpectedError() {
        // Given
        String token = "reset-token";
        when(verificationTokenRepository.findByToken(token)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.validateResetToken(token));
        verify(verificationTokenRepository).findByToken(token);
    }

    @Test
    void resetPassword_UnexpectedError() {
        // Given
        when(verificationTokenRepository.findByToken(resetPasswordRequest.getToken()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.resetPassword(resetPasswordRequest));
        verify(verificationTokenRepository).findByToken(resetPasswordRequest.getToken());
    }
}
