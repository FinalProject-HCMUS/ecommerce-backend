package com.hcmus.ecommerce_backend.unit.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.hcmus.ecommerce_backend.auth.exception.TokenAlreadyInvalidatedException;
import com.hcmus.ecommerce_backend.auth.model.enums.TokenClaims;
import com.hcmus.ecommerce_backend.auth.model.enums.TokenType;
import com.hcmus.ecommerce_backend.auth.service.TokenManagementService;
import com.hcmus.ecommerce_backend.auth.service.impl.TokenValidationServiceImpl;
import com.hcmus.ecommerce_backend.config.TokenConfigurationParameter;
import com.hcmus.ecommerce_backend.user.exception.UserNotFoundException;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
public class TokenValidationServiceImplTest {

    @Mock
    private TokenConfigurationParameter tokenConfigurationParameter;

    @Mock
    private TokenManagementService tokenManagementService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TokenValidationServiceImpl tokenValidationService;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private String validToken;
    private String expiredToken;
    private String tokenWithoutUserId;
    private String tokenWithoutVersion;
    private String invalidToken;
    private String userId;
    private User user;
    private String tokenId;

    @BeforeEach
    void setUp() throws Exception {
        // Generate key pair for signing and validating tokens
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();

        // Mock token configuration

        // Setup user
        userId = "user-123";
        user = new User();
        user.setId(userId);
        user.setTokenVersion(1);

        // Create unique token ID
        tokenId = UUID.randomUUID().toString();

        // Create tokens for testing with different claims
        validToken = Jwts.builder()
                .setHeaderParam(TokenClaims.TYP.getValue(), TokenType.BEARER.getValue())
                .setId(tokenId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000)) // 1 minute from now
                .claim(TokenClaims.USER_ID.getValue(), userId)
                .claim(TokenClaims.TOKEN_VERSION.getValue(), 1)
                .signWith(privateKey)
                .compact();

        expiredToken = Jwts.builder()
                .setHeaderParam(TokenClaims.TYP.getValue(), TokenType.BEARER.getValue())
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date(System.currentTimeMillis() - 120000)) // 2 minutes ago
                .setExpiration(new Date(System.currentTimeMillis() - 60000)) // 1 minute ago
                .claim(TokenClaims.USER_ID.getValue(), userId)
                .claim(TokenClaims.TOKEN_VERSION.getValue(), 1)
                .signWith(privateKey)
                .compact();

        tokenWithoutUserId = Jwts.builder()
                .setHeaderParam(TokenClaims.TYP.getValue(), TokenType.BEARER.getValue())
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .claim(TokenClaims.TOKEN_VERSION.getValue(), 1)
                .signWith(privateKey)
                .compact();

        tokenWithoutVersion = Jwts.builder()
                .setHeaderParam(TokenClaims.TYP.getValue(), TokenType.BEARER.getValue())
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .claim(TokenClaims.USER_ID.getValue(), userId)
                .signWith(privateKey)
                .compact();

        invalidToken = "invalid.token.format";
    }

    @Test
    void verifyAndValidate_ValidToken_ReturnsTrue() {
        // Given
        doReturn(false).when(tokenManagementService).checkForInvalidityOfToken(anyString());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);

        // When
        boolean result = tokenValidationService.verifyAndValidate(validToken);

        // Then
        assertTrue(result);
        verify(tokenManagementService).checkForInvalidityOfToken(tokenId);
        verify(userRepository).findById(userId);
    }

    @Test
    void verifyAndValidate_NullToken_ThrowsRuntimeException() {
        // When & Then

        verify(tokenManagementService, never()).checkForInvalidityOfToken(anyString());
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tokenValidationService.verifyAndValidate((String) null));

        assertEquals("Tokens cannot be null or empty", exception.getMessage());
    }

    @Test
    void verifyAndValidate_EmptyToken_ThrowsRuntimeException() {
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> tokenValidationService.verifyAndValidate(""));

        assertEquals("Tokens cannot be null or empty", exception.getMessage());
        verify(tokenManagementService, never()).checkForInvalidityOfToken(anyString());

    }

    @Test
    void verifyAndValidate_InvalidToken_ThrowsJwtException() {
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);

        // When & Then
        JwtException exception = assertThrows(JwtException.class,
                () -> tokenValidationService.verifyAndValidate(invalidToken));

        assertEquals("Invalid JWT token", exception.getMessage());
    }

    @Test
    void verifyAndValidate_TokenInBlacklist_ThrowsTokenAlreadyInvalidatedException() {
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);

        // Given
        doThrow(TokenAlreadyInvalidatedException.class).when(tokenManagementService)
                .checkForInvalidityOfToken(anyString());

        // When & Then
        assertThrows(TokenAlreadyInvalidatedException.class,
                () -> tokenValidationService.verifyAndValidate(validToken));

        verify(tokenManagementService).checkForInvalidityOfToken(tokenId);
        verify(userRepository, never()).findById(anyString());
    }

    @Test
    void verifyAndValidate_ExpiredToken_ThrowsResponseStatusException() {
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);
        expiredToken = Jwts.builder()
                .setHeaderParam(TokenClaims.TYP.getValue(), TokenType.BEARER.getValue())
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date(System.currentTimeMillis() - 120000)) // 2 minutes ago
                .setExpiration(new Date(System.currentTimeMillis() - 60000)) // 1 minute ago
                .claim(TokenClaims.USER_ID.getValue(), userId)
                .claim(TokenClaims.TOKEN_VERSION.getValue(), 1)
                .signWith(privateKey)
                .compact();
        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> tokenValidationService.verifyAndValidate(expiredToken));
        assertTrue(null != exception.getReason() && exception.getReason().contains("Token has expired"));
    }

    @Test
    void verifyAndValidate_UserNotFound_ThrowsUserNotFoundException() {
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);
        // Given
        doReturn(false).when(tokenManagementService).checkForInvalidityOfToken(anyString());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class,
                () -> tokenValidationService.verifyAndValidate(validToken));

        verify(tokenManagementService).checkForInvalidityOfToken(anyString());
        verify(userRepository).findById(userId);
    }

    @Test
    void verifyAndValidate_TokenVersionLowerThanUserVersion_ThrowsTokenAlreadyInvalidatedException() {

        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);
        // Given
        User userWithHigherVersion = new User();
        userWithHigherVersion.setId(userId);
        userWithHigherVersion.setTokenVersion(2); // Higher than the token's version (1)

        doReturn(false).when(tokenManagementService).checkForInvalidityOfToken(anyString());
        when(userRepository.findById(userId)).thenReturn(Optional.of(userWithHigherVersion));

        // When & Then
        assertThrows(TokenAlreadyInvalidatedException.class,
                () -> tokenValidationService.verifyAndValidate(validToken));

        verify(tokenManagementService).checkForInvalidityOfToken(anyString());
        verify(userRepository).findById(userId);
    }

    @Test
    void verifyAndValidate_TokenWithoutVersion_ThrowsTokenAlreadyInvalidatedException() {
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);
        // Given
        doReturn(false).when(tokenManagementService).checkForInvalidityOfToken(anyString());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(TokenAlreadyInvalidatedException.class,
                () -> tokenValidationService.verifyAndValidate(tokenWithoutVersion));

        verify(tokenManagementService).checkForInvalidityOfToken(anyString());
        verify(userRepository).findById(userId);
    }

    @Test
    void verifyAndValidate_TokenWithoutUserId_ThrowsUserNotFoundException() {
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);

        // Given
        String tokenId = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(tokenWithoutUserId)
                .getBody()
                .getId();

        doReturn(false).when(tokenManagementService).checkForInvalidityOfToken(tokenId);

        // When & Then
        assertThrows(UserNotFoundException.class,
                () -> tokenValidationService.verifyAndValidate(tokenWithoutUserId));

        verify(tokenManagementService).checkForInvalidityOfToken(tokenId);
    }

    @Test
    void verifyAndValidate_MultipleTokens_AllValid_ReturnsTrue() {
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);
        // Given
        String validToken2 = Jwts.builder()
                .setHeaderParam(TokenClaims.TYP.getValue(), TokenType.BEARER.getValue())
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .claim(TokenClaims.USER_ID.getValue(), userId)
                .claim(TokenClaims.TOKEN_VERSION.getValue(), 1)
                .signWith(privateKey)
                .compact();

        Set<String> tokens = Set.of(validToken, validToken2);

        doReturn(false).when(tokenManagementService).checkForInvalidityOfToken(anyString());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        boolean result = tokenValidationService.verifyAndValidate(tokens);

        // Then
        assertTrue(result);
        verify(tokenManagementService, times(2)).checkForInvalidityOfToken(anyString());
        verify(userRepository, times(2)).findById(userId);
    }

    @Test
    void getClaims_ValidToken_ReturnsClaims() {
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);

        // When
        Jws<Claims> claims = tokenValidationService.getClaims(validToken);

        // Then
        assertNotNull(claims);
        assertEquals(userId, claims.getBody().get(TokenClaims.USER_ID.getValue()));
        assertEquals(1, claims.getBody().get(TokenClaims.TOKEN_VERSION.getValue()));
        assertEquals(tokenId, claims.getBody().getId());
    }

    @Test
    void getClaims_InvalidToken_ThrowsJwtException() {
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);

        // When & Then
        assertThrows(JwtException.class, () -> tokenValidationService.getClaims(invalidToken));
    }

    @Test
    void getPayload_ValidToken_ReturnsPayload() {
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);

        // When
        Claims payload = tokenValidationService.getPayload(validToken);

        // Then
        assertNotNull(payload);
        assertEquals(userId, payload.get(TokenClaims.USER_ID.getValue()));
        assertEquals(1, payload.get(TokenClaims.TOKEN_VERSION.getValue()));
        assertEquals(tokenId, payload.getId());
    }

    @Test
    void getPayload_InvalidToken_ThrowsJwtException() {
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);

        // When & Then
        assertThrows(JwtException.class, () -> tokenValidationService.getPayload(invalidToken));
    }

    @Test
    void getId_ValidToken_ReturnsTokenId() {
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);

        // When
        String retrievedId = tokenValidationService.getId(validToken);

        // Then
        assertEquals(tokenId, retrievedId);
    }

    @Test
    void getId_InvalidToken_ThrowsJwtException() {
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);
        // When & Then
        assertThrows(JwtException.class, () -> tokenValidationService.getId(invalidToken));
    }

    @Test
    void verifyAndValidate_UnexpectedError_ThrowsResponseStatusException() {
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);
        // Given
        doReturn(false).when(tokenManagementService).checkForInvalidityOfToken(anyString());
        when(userRepository.findById(anyString())).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> tokenValidationService.verifyAndValidate(validToken));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Error validating token"));

        verify(tokenManagementService).checkForInvalidityOfToken(anyString());
        verify(userRepository).findById(userId);
    }

    @Test
    void verifyAndValidate_TokenManagementError_PropagatesException() {
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);
        // Given
        doThrow(new RuntimeException("Token management error"))
                .when(tokenManagementService).checkForInvalidityOfToken(anyString());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> tokenValidationService.verifyAndValidate(validToken));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Error validating token"));

        verify(tokenManagementService).checkForInvalidityOfToken(anyString());
        verify(userRepository, never()).findById(anyString());
    }

    @Test
    void verifyAndValidate_JwtParsingError_ThrowsJwtException() {
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);
        
        // Given
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(null); // This will cause parsing to fail

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> tokenValidationService.verifyAndValidate(validToken));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());

        // verify(tokenManagementService).checkForInvalidityOfToken(anyString());
    }
}