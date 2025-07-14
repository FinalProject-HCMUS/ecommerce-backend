package com.hcmus.ecommerce_backend.integration.auth.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmus.ecommerce_backend.auth.controller.AuthController;
import com.hcmus.ecommerce_backend.auth.exception.PasswordNotValidException;
import com.hcmus.ecommerce_backend.auth.exception.TokenAlreadyInvalidatedException;
import com.hcmus.ecommerce_backend.auth.exception.UserNotActivatedException;
import com.hcmus.ecommerce_backend.auth.model.dto.request.LoginRequest;
import com.hcmus.ecommerce_backend.auth.model.dto.request.TokenInvalidateRequest;
import com.hcmus.ecommerce_backend.auth.model.dto.request.TokenRefreshRequest;
import com.hcmus.ecommerce_backend.auth.model.dto.response.TokenResponse;
import com.hcmus.ecommerce_backend.auth.service.AuthenticationService;
import com.hcmus.ecommerce_backend.auth.service.TokenService;
import com.hcmus.ecommerce_backend.auth.service.TokenValidationService;
import com.hcmus.ecommerce_backend.user.exception.UserNotFoundException;

import io.jsonwebtoken.JwtException;

import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
public class AuthControllerIntegrationTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private TokenService tokenService;

    @Mock
    private TokenValidationService tokenValidationService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private LoginRequest validLoginRequest;
    private TokenRefreshRequest validTokenRefreshRequest;
    private TokenInvalidateRequest validTokenInvalidateRequest;
    private TokenResponse validTokenResponse;
    private UsernamePasswordAuthenticationToken validAuthentication;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        setupTestData();
    }

    private void setupTestData() {
        // Setup valid login request
        validLoginRequest = LoginRequest.builder()
                .email("test@gmail.com")
                .password("123456")
                .build();

        // Setup valid token refresh request
        validTokenRefreshRequest = TokenRefreshRequest.builder()
                .refreshToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .build();

        // Setup valid token invalidate request
        validTokenInvalidateRequest = TokenInvalidateRequest.builder()
                .accessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .refreshToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .build();

        // Setup valid token response
        validTokenResponse = TokenResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .accessTokenExpiresAt(System.currentTimeMillis() / 1000 + 3600)
                .refreshToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
                .build();

        // Setup valid authentication
        Jwt jwt = new Jwt(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("typ", "JWT", "alg", "HS256"),
                Map.of("sub", "1234567890", "name", "John Doe", "iat", 1516239022)
        );

        validAuthentication = new UsernamePasswordAuthenticationToken(
                jwt, 
                null, 
                Arrays.asList(new SimpleGrantedAuthority("USER"))
        );
    }

    @Test
    void login_WithUserNotActivated_ShouldReturnForbidden() throws Exception {
        // Given
        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new UserNotActivatedException());

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isForbidden());

        verify(authenticationService).login(any(LoginRequest.class));
    }

    @Test
    void login_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        // Given
        LoginRequest invalidEmailRequest = LoginRequest.builder()
                .email("invalid-email")
                .password("123456")
                .build();

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).login(any(LoginRequest.class));
    }

    @Test
    void login_WithShortPassword_ShouldReturnBadRequest() throws Exception {
        // Given
        LoginRequest shortPasswordRequest = LoginRequest.builder()
                .email("test@gmail.com")
                .password("123")
                .build();

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shortPasswordRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).login(any(LoginRequest.class));
    }

    @Test
    void login_WithNullEmail_ShouldReturnBadRequest() throws Exception {
        // Given
        LoginRequest nullEmailRequest = LoginRequest.builder()
                .email(null)
                .password("123456")
                .build();

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullEmailRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).login(any(LoginRequest.class));
    }

    @Test
    void login_WithEmptyPassword_ShouldReturnBadRequest() throws Exception {
        // Given
        LoginRequest emptyPasswordRequest = LoginRequest.builder()
                .email("test@gmail.com")
                .password("")
                .build();

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyPasswordRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).login(any(LoginRequest.class));
    }

    @Test
    void login_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).login(any(LoginRequest.class));
    }

    @Test
    void refreshToken_WithNullToken_ShouldReturnBadRequest() throws Exception {
        // Given
        TokenRefreshRequest nullTokenRequest = TokenRefreshRequest.builder()
                .refreshToken(null)
                .build();

        // When & Then
        mockMvc.perform(post("/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullTokenRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).refreshToken(any(TokenRefreshRequest.class));
    }

    @Test
    void refreshToken_WithEmptyToken_ShouldReturnBadRequest() throws Exception {
        // Given
        TokenRefreshRequest emptyTokenRequest = TokenRefreshRequest.builder()
                .refreshToken("")
                .build();

        // When & Then
        mockMvc.perform(post("/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyTokenRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).refreshToken(any(TokenRefreshRequest.class));
    }

    @Test
    void logout_WithNullAccessToken_ShouldReturnBadRequest() throws Exception {
        // Given
        TokenInvalidateRequest nullAccessTokenRequest = TokenInvalidateRequest.builder()
                .accessToken(null)
                .refreshToken("valid-refresh-token")
                .build();

        // When & Then
        mockMvc.perform(post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullAccessTokenRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).logout(any(TokenInvalidateRequest.class));
    }

    @Test
    void logout_WithNullRefreshToken_ShouldReturnBadRequest() throws Exception {
        // Given
        TokenInvalidateRequest nullRefreshTokenRequest = TokenInvalidateRequest.builder()
                .accessToken("valid-access-token")
                .refreshToken(null)
                .build();

        // When & Then
        mockMvc.perform(post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullRefreshTokenRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).logout(any(TokenInvalidateRequest.class));
    }

    @Test
    void logout_WithEmptyTokens_ShouldReturnBadRequest() throws Exception {
        // Given
        TokenInvalidateRequest emptyTokensRequest = TokenInvalidateRequest.builder()
                .accessToken("")
                .refreshToken("")
                .build();

        // When & Then
        mockMvc.perform(post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyTokensRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).logout(any(TokenInvalidateRequest.class));
    }

    @Test
    void validateToken_WithMissingAuthorizationHeader_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/auth/validate-token"))
                .andExpect(status().isBadRequest());

        verify(tokenValidationService, never()).verifyAndValidate(anyString());
    }

    // Test GET /auth/authenticate - Get authentication
    @Test
    void getAuthentication_WithValidToken_ShouldReturnAuthentication() throws Exception {
        // Given
        when(tokenService.getAuthentication(anyString())).thenReturn(validAuthentication);

        // When & Then
        mockMvc.perform(get("/auth/authenticate")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validTokenResponse.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.principal").exists())
                .andExpect(jsonPath("$.authorities").exists());

        verify(tokenService).getAuthentication(validTokenResponse.getAccessToken());
    }

    @Test
    void getAuthentication_WithMissingAuthorizationHeader_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/auth/authenticate"))
                .andExpect(status().isBadRequest());

        verify(tokenService, never()).getAuthentication(anyString());
    }

    @Test
    void outboundAuthenticate_WithMissingCode_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/auth/outbound/authentication"))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).outboundAuthentication(anyString());
    }
}