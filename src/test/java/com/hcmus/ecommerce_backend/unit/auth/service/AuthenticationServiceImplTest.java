package com.hcmus.ecommerce_backend.unit.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import com.hcmus.ecommerce_backend.auth.client.OutboundIdentityClient;
import com.hcmus.ecommerce_backend.auth.client.OutboundUserClient;
import com.hcmus.ecommerce_backend.auth.exception.PasswordNotValidException;
import com.hcmus.ecommerce_backend.auth.exception.TokenAlreadyInvalidatedException;
import com.hcmus.ecommerce_backend.auth.exception.UserNotActivatedException;
import com.hcmus.ecommerce_backend.auth.model.Token;
import com.hcmus.ecommerce_backend.auth.model.dto.request.ExchangeTokenRequest;
import com.hcmus.ecommerce_backend.auth.model.dto.request.LoginRequest;
import com.hcmus.ecommerce_backend.auth.model.dto.request.TokenInvalidateRequest;
import com.hcmus.ecommerce_backend.auth.model.dto.request.TokenRefreshRequest;
import com.hcmus.ecommerce_backend.auth.model.dto.response.ExchangeTokenResponse;
import com.hcmus.ecommerce_backend.auth.model.dto.response.OutboundUserResponse;
import com.hcmus.ecommerce_backend.auth.model.dto.response.TokenResponse;
import com.hcmus.ecommerce_backend.auth.model.enums.TokenClaims;
import com.hcmus.ecommerce_backend.auth.model.mapper.TokenMapper;
import com.hcmus.ecommerce_backend.auth.service.TokenGenerationService;
import com.hcmus.ecommerce_backend.auth.service.TokenManagementService;
import com.hcmus.ecommerce_backend.auth.service.TokenValidationService;
import com.hcmus.ecommerce_backend.auth.service.impl.AuthenticationServiceImpl;
import com.hcmus.ecommerce_backend.user.exception.UserNotFoundException;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.model.enums.Role;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenGenerationService tokenGenerationService;

    @Mock
    private TokenValidationService tokenValidationService;

    @Mock
    private TokenManagementService tokenManagementService;

    @Mock
    private TokenMapper tokenMapper;

    @Mock
    private OutboundIdentityClient outboundIdentityClient;

    @Mock
    private OutboundUserClient outboundUserClient;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User user;
    private LoginRequest loginRequest;
    private TokenRefreshRequest tokenRefreshRequest;
    private TokenInvalidateRequest tokenInvalidateRequest;
    private Token token;
    private TokenResponse tokenResponse;
    private ExchangeTokenResponse outboundTokenResponse;
    private OutboundUserResponse outboundUserResponse;
    private Claims claims;

    @BeforeEach
    void setUp() {
        // Setup configuration values using reflection
        ReflectionTestUtils.setField(authenticationService, "CLIENT_ID", "test-client-id");
        ReflectionTestUtils.setField(authenticationService, "CLIENT_SECRET", "test-client-secret");
        ReflectionTestUtils.setField(authenticationService, "REDIRECT_URI", "http://localhost:3000/callback");
        ReflectionTestUtils.setField(authenticationService, "GRANT_TYPE", "authorization_code");

        // Setup User entity
        user = User.builder()
                .id("user-1")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .enabled(true)
                .role(Role.USER)
                .tokenVersion(0)
                .build();

        // Setup LoginRequest
        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        // Setup TokenRefreshRequest
        tokenRefreshRequest = TokenRefreshRequest.builder()
                .refreshToken("refresh-token-123")
                .build();

        // Setup TokenInvalidateRequest
        tokenInvalidateRequest = TokenInvalidateRequest.builder()
                .accessToken("access-token-123")
                .refreshToken("refresh-token-123")
                .build();

        // Setup Token
        token = Token.builder()
                .accessToken("access-token-123")
                .refreshToken("refresh-token-123")
                .build();

        // Setup TokenResponse
        tokenResponse = TokenResponse.builder()
                .accessToken("access-token-123")
                .refreshToken("refresh-token-123")
                .build();

        // Setup OutboundTokenResponse
        outboundTokenResponse = ExchangeTokenResponse.builder().accessToken("outbound-access-token")
                .refreshToken("outbound-refresh-token")
                .expiresIn(Long.parseLong("3600")) // 1 hour
                .scope("read write")
                .build();

        // Setup OutboundUserResponse
        outboundUserResponse = OutboundUserResponse.builder()
                .email("oauth@example.com")
                .name("OAuth User")
                .givenName("User")
                .picture("https://example.com/photo.jpg")
                .build();

        // Setup Claims
        claims = new DefaultClaims();
        claims.put(TokenClaims.USER_ID.getValue(), "user-1");
        claims.setId("token-id-123");
    }

    // Tests for login method
    @Test
    void login_Success() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(tokenGenerationService.generateToken(any(Map.class))).thenReturn(token);
        when(tokenMapper.toTokenResponse(token)).thenReturn(tokenResponse);

        // When
        TokenResponse result = authenticationService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals(tokenResponse, result);
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
        verify(tokenGenerationService).generateToken(any(Map.class));
        verify(tokenMapper).toTokenResponse(token);
    }

    @Test
    void login_UserNotFound() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> authenticationService.login(loginRequest));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_InvalidPassword() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

        // When & Then
        assertThrows(PasswordNotValidException.class, () -> authenticationService.login(loginRequest));
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
        verify(tokenGenerationService, never()).generateToken(any());
    }

    @Test
    void login_UserNotEnabled() {
        // Given
        user.setEnabled(false);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);

        // When & Then
        assertThrows(UserNotActivatedException.class, () -> authenticationService.login(loginRequest));
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
        verify(tokenGenerationService, never()).generateToken(any());
    }

    @Test
    void login_DatabaseError() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> authenticationService.login(loginRequest));
        verify(userRepository).findByEmail(loginRequest.getEmail());
    }

    @Test
    void login_TokenGenerationError() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(tokenGenerationService.generateToken(any(Map.class)))
                .thenThrow(new RuntimeException("Token generation failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> authenticationService.login(loginRequest));
        verify(tokenGenerationService).generateToken(any(Map.class));
    }

    @Test
    void login_TokenMappingError() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(tokenGenerationService.generateToken(any(Map.class))).thenReturn(token);
        when(tokenMapper.toTokenResponse(token)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> authenticationService.login(loginRequest));
        verify(tokenMapper).toTokenResponse(token);
    }

    // Tests for refreshToken method
    @Test
    void refreshToken_Success() {
        // Given
        when(tokenValidationService.getPayload(tokenRefreshRequest.getRefreshToken())).thenReturn(claims);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(tokenGenerationService.generateToken(any(Map.class), eq(tokenRefreshRequest.getRefreshToken())))
                .thenReturn(token);
        when(tokenMapper.toTokenResponse(token)).thenReturn(tokenResponse);

        // When
        TokenResponse result = authenticationService.refreshToken(tokenRefreshRequest);

        // Then
        assertNotNull(result);
        assertEquals(tokenResponse, result);
        verify(tokenValidationService).verifyAndValidate(tokenRefreshRequest.getRefreshToken());
        verify(tokenValidationService).getPayload(tokenRefreshRequest.getRefreshToken());
        verify(userRepository).findById("user-1");
        verify(tokenGenerationService).generateToken(any(Map.class), eq(tokenRefreshRequest.getRefreshToken()));
        verify(tokenMapper).toTokenResponse(token);
    }

    @Test
    void refreshToken_InvalidToken() {
        // Given
        doThrow(new RuntimeException("Invalid token")).when(tokenValidationService)
                .verifyAndValidate(tokenRefreshRequest.getRefreshToken());

        // When & Then
        assertThrows(RuntimeException.class, () -> authenticationService.refreshToken(tokenRefreshRequest));
        verify(tokenValidationService).verifyAndValidate(tokenRefreshRequest.getRefreshToken());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void refreshToken_UserNotFound() {
        // Given
        when(tokenValidationService.getPayload(tokenRefreshRequest.getRefreshToken())).thenReturn(claims);
        when(userRepository.findById("user-1")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> authenticationService.refreshToken(tokenRefreshRequest));
        verify(tokenValidationService).verifyAndValidate(tokenRefreshRequest.getRefreshToken());
        verify(tokenValidationService).getPayload(tokenRefreshRequest.getRefreshToken());
        verify(userRepository).findById("user-1");
        verify(tokenGenerationService, never()).generateToken(any(), any());
    }

    @Test
    void refreshToken_TokenValidationError() {
        // Given
        doThrow(new RuntimeException("Token validation failed")).when(tokenValidationService)
                .verifyAndValidate(tokenRefreshRequest.getRefreshToken());

        // When & Then
        assertThrows(RuntimeException.class, () -> authenticationService.refreshToken(tokenRefreshRequest));
        verify(tokenValidationService).verifyAndValidate(tokenRefreshRequest.getRefreshToken());
    }

    @Test
    void refreshToken_PayloadExtractionError() {
        // Given
        when(tokenValidationService.getPayload(tokenRefreshRequest.getRefreshToken()))
                .thenThrow(new RuntimeException("Payload extraction failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> authenticationService.refreshToken(tokenRefreshRequest));
        verify(tokenValidationService).verifyAndValidate(tokenRefreshRequest.getRefreshToken());
        verify(tokenValidationService).getPayload(tokenRefreshRequest.getRefreshToken());
    }

    @Test
    void refreshToken_DatabaseError() {
        // Given
        when(tokenValidationService.getPayload(tokenRefreshRequest.getRefreshToken())).thenReturn(claims);
        when(userRepository.findById("user-1")).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> authenticationService.refreshToken(tokenRefreshRequest));
        verify(userRepository).findById("user-1");
    }

    @Test
    void refreshToken_TokenGenerationError() {
        // Given
        when(tokenValidationService.getPayload(tokenRefreshRequest.getRefreshToken())).thenReturn(claims);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(tokenGenerationService.generateToken(any(Map.class), eq(tokenRefreshRequest.getRefreshToken())))
                .thenThrow(new RuntimeException("Token generation failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> authenticationService.refreshToken(tokenRefreshRequest));
        verify(tokenGenerationService).generateToken(any(Map.class), eq(tokenRefreshRequest.getRefreshToken()));
    }

    // Tests for outboundAuthentication method
    @Test
    void outboundAuthentication_ExistingUser_Success() {
        // Given
        String code = "auth-code-123";
        doReturn(outboundTokenResponse)
                .when(outboundIdentityClient)
                .exchangeToken(any(ExchangeTokenRequest.class));
        when(outboundUserClient.getUserInfo("json", outboundTokenResponse.getAccessToken()))
                .thenReturn(outboundUserResponse);
        when(userRepository.findByEmail(outboundUserResponse.getEmail())).thenReturn(Optional.of(user));
        when(tokenGenerationService.generateToken(any(Map.class))).thenReturn(token);
        when(tokenMapper.toTokenResponse(token)).thenReturn(tokenResponse);

        // When
        TokenResponse result = authenticationService.outboundAuthentication(code);

        // Then
        assertNotNull(result);
        assertEquals(tokenResponse, result);
        verify(outboundIdentityClient).exchangeToken(any(ExchangeTokenRequest.class));
        verify(outboundUserClient).getUserInfo("json", outboundTokenResponse.getAccessToken());
        verify(userRepository).findByEmail(outboundUserResponse.getEmail());
        verify(userRepository, never()).save(any()); // Existing user, no save needed
        verify(tokenGenerationService).generateToken(any(Map.class));
        verify(tokenMapper).toTokenResponse(token);
    }

    @Test
    void outboundAuthentication_NewUser_Success() {
        // Given
        String code = "auth-code-123";
        User newUser = User.builder()
                .email(outboundUserResponse.getEmail())
                .firstName(outboundUserResponse.getName())
                .lastName(outboundUserResponse.getGivenName())
                .photo(outboundUserResponse.getPicture())
                .enabled(true)
                .build();

        doReturn(outboundTokenResponse)
                .when(outboundIdentityClient)
                .exchangeToken(any(ExchangeTokenRequest.class));
        when(outboundUserClient.getUserInfo("json", outboundTokenResponse.getAccessToken()))
                .thenReturn(outboundUserResponse);
        when(userRepository.findByEmail(outboundUserResponse.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(tokenGenerationService.generateToken(any(Map.class))).thenReturn(token);
        when(tokenMapper.toTokenResponse(token)).thenReturn(tokenResponse);

        // When
        TokenResponse result = authenticationService.outboundAuthentication(code);

        // Then
        assertNotNull(result);
        assertEquals(tokenResponse, result);
        verify(outboundIdentityClient).exchangeToken(any(ExchangeTokenRequest.class));
        verify(outboundUserClient).getUserInfo("json", outboundTokenResponse.getAccessToken());
        verify(userRepository).findByEmail(outboundUserResponse.getEmail());
        verify(userRepository).save(any(User.class)); // New user, save needed
        verify(tokenGenerationService).generateToken(any(Map.class));
        verify(tokenMapper).toTokenResponse(token);
    }

    @Test
    void outboundAuthentication_TokenExchangeError() {
        // Given
        String code = "invalid-code";
        when(outboundIdentityClient.exchangeToken(any(ExchangeTokenRequest.class)))
                .thenThrow(new RuntimeException("Token exchange failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> authenticationService.outboundAuthentication(code));
        verify(outboundIdentityClient).exchangeToken(any(ExchangeTokenRequest.class));
        verify(outboundUserClient, never()).getUserInfo(any(), any());
    }

    @Test
    void outboundAuthentication_UserInfoRetrievalError() {
        // Given
        String code = "auth-code-123";
        doReturn(outboundTokenResponse)
                .when(outboundIdentityClient)
                .exchangeToken(any(ExchangeTokenRequest.class));
        when(outboundUserClient.getUserInfo("json", outboundTokenResponse.getAccessToken()))
                .thenThrow(new RuntimeException("User info retrieval failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> authenticationService.outboundAuthentication(code));
        verify(outboundIdentityClient).exchangeToken(any(ExchangeTokenRequest.class));
        verify(outboundUserClient).getUserInfo("json", outboundTokenResponse.getAccessToken());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void outboundAuthentication_DatabaseError() {
        // Given
        String code = "auth-code-123";
        doReturn(outboundTokenResponse)
                .when(outboundIdentityClient)
                .exchangeToken(any(ExchangeTokenRequest.class));
        when(outboundUserClient.getUserInfo("json", outboundTokenResponse.getAccessToken()))
                .thenReturn(outboundUserResponse);
        when(userRepository.findByEmail(outboundUserResponse.getEmail()))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> authenticationService.outboundAuthentication(code));
        verify(userRepository).findByEmail(outboundUserResponse.getEmail());
    }

    @Test
    void outboundAuthentication_NewUserSaveError() {
        // Given
        String code = "auth-code-123";
        doReturn(outboundTokenResponse)
                .when(outboundIdentityClient)
                .exchangeToken(any(ExchangeTokenRequest.class));
        when(outboundUserClient.getUserInfo("json", outboundTokenResponse.getAccessToken()))
                .thenReturn(outboundUserResponse);
        when(userRepository.findByEmail(outboundUserResponse.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("Save failed"));

        // When & Then
        assertThrows(DataAccessException.class, () -> authenticationService.outboundAuthentication(code));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void outboundAuthentication_TokenGenerationError() {
        // Given
        String code = "auth-code-123";
        doReturn(outboundTokenResponse)
                .when(outboundIdentityClient)
                .exchangeToken(any(ExchangeTokenRequest.class));
        when(outboundUserClient.getUserInfo("json", outboundTokenResponse.getAccessToken()))
                .thenReturn(outboundUserResponse);
        when(userRepository.findByEmail(outboundUserResponse.getEmail())).thenReturn(Optional.of(user));
        when(tokenGenerationService.generateToken(any(Map.class)))
                .thenThrow(new RuntimeException("Token generation failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> authenticationService.outboundAuthentication(code));
        verify(tokenGenerationService).generateToken(any(Map.class));
    }

    // Tests for logout method
    @Test
    void logout_Success() {
        // Given
        Claims accessTokenClaims = new DefaultClaims();
        accessTokenClaims.setId("access-token-id");
        Claims refreshTokenClaims = new DefaultClaims();
        refreshTokenClaims.setId("refresh-token-id");

        when(tokenValidationService.getPayload(tokenInvalidateRequest.getAccessToken())).thenReturn(accessTokenClaims);
        when(tokenValidationService.getPayload(tokenInvalidateRequest.getRefreshToken()))
                .thenReturn(refreshTokenClaims);

        // When
        assertDoesNotThrow(() -> authenticationService.logout(tokenInvalidateRequest));

        // Then
        verify(tokenValidationService).verifyAndValidate(Set.of(
                tokenInvalidateRequest.getAccessToken(),
                tokenInvalidateRequest.getRefreshToken()));
        verify(tokenValidationService).getPayload(tokenInvalidateRequest.getAccessToken());
        verify(tokenValidationService).getPayload(tokenInvalidateRequest.getRefreshToken());
        verify(tokenManagementService).checkForInvalidityOfToken("access-token-id");
        verify(tokenManagementService).checkForInvalidityOfToken("refresh-token-id");
        verify(tokenManagementService).invalidateTokens(Set.of("access-token-id", "refresh-token-id"));
    }

    @Test
    void logout_InvalidTokens() {
        // Given
        doThrow(new RuntimeException("Invalid tokens")).when(tokenValidationService)
                .verifyAndValidate(
                        Set.of(tokenInvalidateRequest.getAccessToken(), tokenInvalidateRequest.getRefreshToken()));

        // When & Then
        assertThrows(RuntimeException.class, () -> authenticationService.logout(tokenInvalidateRequest));
        verify(tokenValidationService).verifyAndValidate(Set.of(
                tokenInvalidateRequest.getAccessToken(),
                tokenInvalidateRequest.getRefreshToken()));
        verify(tokenManagementService, never()).checkForInvalidityOfToken(any());
    }

    @Test
    void logout_AccessTokenAlreadyInvalidated() {
        // Given
        Claims accessTokenClaims = new DefaultClaims();
        accessTokenClaims.setId("access-token-id");
        Claims refreshTokenClaims = new DefaultClaims();
        refreshTokenClaims.setId("refresh-token-id");

        when(tokenValidationService.getPayload(tokenInvalidateRequest.getAccessToken())).thenReturn(accessTokenClaims);
        // when(tokenValidationService.getPayload(tokenInvalidateRequest.getRefreshToken()))
        // .thenReturn(refreshTokenClaims);
        doThrow(new TokenAlreadyInvalidatedException("Token already invalidated")).when(tokenManagementService)
                .checkForInvalidityOfToken("access-token-id");

        // When & Then
        assertThrows(TokenAlreadyInvalidatedException.class,
                () -> authenticationService.logout(tokenInvalidateRequest));
        verify(tokenManagementService).checkForInvalidityOfToken("access-token-id");
        verify(tokenManagementService, never()).checkForInvalidityOfToken("refresh-token-id");
        verify(tokenManagementService, never()).invalidateTokens(any());
    }

    @Test
    void logout_RefreshTokenAlreadyInvalidated() {
        // Given
        Claims accessTokenClaims = new DefaultClaims();
        accessTokenClaims.setId("access-token-id");
        Claims refreshTokenClaims = new DefaultClaims();
        refreshTokenClaims.setId("refresh-token-id");

        when(tokenValidationService.getPayload(tokenInvalidateRequest.getAccessToken())).thenReturn(accessTokenClaims);
        when(tokenValidationService.getPayload(tokenInvalidateRequest.getRefreshToken()))
                .thenReturn(refreshTokenClaims);

        // Stub both method calls - access token check passes, refresh token check fails
        doReturn(true).when(tokenManagementService).checkForInvalidityOfToken("access-token-id");
        doThrow(new TokenAlreadyInvalidatedException("Token already invalidated"))
                .when(tokenManagementService)
                .checkForInvalidityOfToken("refresh-token-id");

        // When & Then
        assertThrows(TokenAlreadyInvalidatedException.class,
                () -> authenticationService.logout(tokenInvalidateRequest));
        verify(tokenManagementService).checkForInvalidityOfToken("access-token-id");
        verify(tokenManagementService).checkForInvalidityOfToken("refresh-token-id");
        verify(tokenManagementService, never()).invalidateTokens(any());
    }

    @Test
    void logout_PayloadExtractionError() {
        // Given
        when(tokenValidationService.getPayload(tokenInvalidateRequest.getAccessToken()))
                .thenThrow(new RuntimeException("Payload extraction failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> authenticationService.logout(tokenInvalidateRequest));
        verify(tokenValidationService).verifyAndValidate(Set.of(
                tokenInvalidateRequest.getAccessToken(),
                tokenInvalidateRequest.getRefreshToken()));
        verify(tokenValidationService).getPayload(tokenInvalidateRequest.getAccessToken());
        verify(tokenManagementService, never()).checkForInvalidityOfToken(any());
    }

    @Test
    void logout_TokenInvalidationError() {
        // Given
        Claims accessTokenClaims = new DefaultClaims();
        accessTokenClaims.setId("access-token-id");
        Claims refreshTokenClaims = new DefaultClaims();
        refreshTokenClaims.setId("refresh-token-id");

        when(tokenValidationService.getPayload(tokenInvalidateRequest.getAccessToken())).thenReturn(accessTokenClaims);
        when(tokenValidationService.getPayload(tokenInvalidateRequest.getRefreshToken()))
                .thenReturn(refreshTokenClaims);
        doThrow(new RuntimeException("Invalidation failed")).when(tokenManagementService)
                .invalidateTokens(Set.of("access-token-id", "refresh-token-id"));

        // When & Then
        assertThrows(RuntimeException.class, () -> authenticationService.logout(tokenInvalidateRequest));
        verify(tokenManagementService).invalidateTokens(Set.of("access-token-id", "refresh-token-id"));
    }

    // Edge case tests
    @Test
    void login_NullEmail() {
        // Given
        loginRequest.setEmail(null);

        // When & Then
        assertThrows(Exception.class, () -> authenticationService.login(loginRequest));
    }

    @Test
    void login_EmptyEmail() {
        // Given
        loginRequest.setEmail("");
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> authenticationService.login(loginRequest));
    }

    @Test
    void login_NullPassword() {
        // Given
        loginRequest.setPassword(null);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(null, user.getPassword())).thenReturn(false);

        // When & Then
        assertThrows(PasswordNotValidException.class, () -> authenticationService.login(loginRequest));
    }

    @Test
    void refreshToken_NullRefreshToken() {
        // Given
        tokenRefreshRequest.setRefreshToken(null);
        doThrow(new RuntimeException("Token is null"))
                .when(tokenValidationService)
                .verifyAndValidate((String) null);

        // When & Then
        assertThrows(RuntimeException.class, () -> authenticationService.refreshToken(tokenRefreshRequest));
    }

    @Test
    void outboundAuthentication_NullCode() {
        // When & Then
        assertThrows(Exception.class, () -> authenticationService.outboundAuthentication(null));
    }

    @Test
    void outboundAuthentication_EmptyCode() {
        // When & Then
        assertThrows(Exception.class, () -> authenticationService.outboundAuthentication(""));
    }


    @Test
    void outboundAuthentication_ExchangeTokenRequestValidation() {
        // Given
        String code = "auth-code-123";
        doReturn(outboundTokenResponse)
                .when(outboundIdentityClient)
                .exchangeToken(any(ExchangeTokenRequest.class));
        when(outboundUserClient.getUserInfo("json", outboundTokenResponse.getAccessToken()))
                .thenReturn(outboundUserResponse);
        when(userRepository.findByEmail(outboundUserResponse.getEmail())).thenReturn(Optional.of(user));
        when(tokenGenerationService.generateToken(any(Map.class))).thenReturn(token);
        when(tokenMapper.toTokenResponse(token)).thenReturn(tokenResponse);

        // When
        authenticationService.outboundAuthentication(code);

        // Then
        verify(outboundIdentityClient).exchangeToken(argThat(request -> request.getCode().equals(code) &&
                request.getClientId().equals("test-client-id") &&
                request.getClientSecret().equals("test-client-secret") &&
                request.getRedirectUri().equals("http://localhost:3000/callback") &&
                request.getGrantType().equals("authorization_code")));
    }

    @Test
    void refreshToken_NullUserId() {
        // Given
        Claims claimsWithNullUserId = new DefaultClaims();
        claimsWithNullUserId.put(TokenClaims.USER_ID.getValue(), null);
        when(tokenValidationService.getPayload(tokenRefreshRequest.getRefreshToken())).thenReturn(claimsWithNullUserId);

        // When & Then
        assertThrows(Exception.class, () -> authenticationService.refreshToken(tokenRefreshRequest));
    }

    @Test
    void refreshToken_MissingUserIdClaim() {
        // Given
        Claims claimsWithoutUserId = new DefaultClaims();
        when(tokenValidationService.getPayload(tokenRefreshRequest.getRefreshToken())).thenReturn(claimsWithoutUserId);

        // When & Then
        assertThrows(Exception.class, () -> authenticationService.refreshToken(tokenRefreshRequest));
    }

    @Test
    void login_PasswordEncoderReturnsNull() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

        // When & Then
        assertThrows(PasswordNotValidException.class, () -> authenticationService.login(loginRequest));
    }

    @Test
    void outboundAuthentication_NewUserWithNullFields() {
        // Given
        String code = "auth-code-123";
        OutboundUserResponse userResponseWithNulls = OutboundUserResponse.builder()
                .email("oauth@example.com")
                .name(null)
                .givenName(null)
                .picture(null)
                .build();

        User newUser = User.builder()
                .email(userResponseWithNulls.getEmail())
                .firstName(null)
                .lastName(null)
                .photo(null)
                .enabled(true)
                .build();

        doReturn(outboundTokenResponse)
                .when(outboundIdentityClient)
                .exchangeToken(any(ExchangeTokenRequest.class));
        when(outboundUserClient.getUserInfo("json", outboundTokenResponse.getAccessToken()))
                .thenReturn(userResponseWithNulls);
        when(userRepository.findByEmail(userResponseWithNulls.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(tokenGenerationService.generateToken(any(Map.class))).thenReturn(token);
        when(tokenMapper.toTokenResponse(token)).thenReturn(tokenResponse);

        // When
        TokenResponse result = authenticationService.outboundAuthentication(code);

        // Then
        assertNotNull(result);
        verify(userRepository).save(argThat(savedUser -> savedUser.getEmail().equals("oauth@example.com") &&
                savedUser.getFirstName() == null &&
                savedUser.getLastName() == null &&
                savedUser.getPhoto() == null &&
                savedUser.isEnabled()));
    }

    @Test
    void outboundAuthentication_TokenResponseMappingError() {
        // Given
        String code = "auth-code-123";
        doReturn(outboundTokenResponse)
                .when(outboundIdentityClient)
                .exchangeToken(any(ExchangeTokenRequest.class));
        when(outboundUserClient.getUserInfo("json", outboundTokenResponse.getAccessToken()))
                .thenReturn(outboundUserResponse);
        when(userRepository.findByEmail(outboundUserResponse.getEmail())).thenReturn(Optional.of(user));
        when(tokenGenerationService.generateToken(any(Map.class))).thenReturn(token);
        when(tokenMapper.toTokenResponse(token)).thenThrow(new RuntimeException("Token response mapping failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> authenticationService.outboundAuthentication(code));
        verify(tokenMapper).toTokenResponse(token);
    }
}
