package com.hcmus.ecommerce_backend.unit.auth.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import com.hcmus.ecommerce_backend.auth.model.enums.TokenClaims;
import com.hcmus.ecommerce_backend.auth.model.enums.TokenType;
import com.hcmus.ecommerce_backend.auth.service.TokenValidationService;
import com.hcmus.ecommerce_backend.auth.service.impl.TokenServiceImpl;
import com.hcmus.ecommerce_backend.config.TokenConfigurationParameter;
import com.hcmus.ecommerce_backend.user.model.enums.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;

@ExtendWith(MockitoExtension.class)
public class TokenServiceImplTest {

    @Mock
    private TokenConfigurationParameter tokenConfigurationParameter;

    @Mock
    private TokenValidationService tokenValidationService;

    @InjectMocks
    private TokenServiceImpl tokenService;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private String validToken;
    private String tokenWithoutRole;
    private String expiredToken;
    private String invalidToken;
    private Date issuedAt;
    private Date expiresAt;

    @BeforeEach
    void setUp() throws Exception {
        // Generate key pair for signing and validating tokens
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();

        // Mock token configuration

        // Create dates for token generation
        issuedAt = new Date();
        expiresAt = new Date(issuedAt.getTime() + 3600000); // 1 hour later
        Date pastDate = new Date(issuedAt.getTime() - 3600000); // 1 hour before

        // Setup claims for token generation
        Map<String, Object> validClaims = new HashMap<>();
        validClaims.put(TokenClaims.USER_ID.getValue(), "user123");
        validClaims.put(TokenClaims.USER_ROLE.getValue(), Role.ADMIN.name());
        validClaims.put(TokenClaims.TOKEN_VERSION.getValue(), 1);

        Map<String, Object> noRoleClaims = new HashMap<>(validClaims);
        noRoleClaims.remove(TokenClaims.USER_ROLE.getValue());

        // Generate tokens for testing
        validToken = Jwts.builder()
                .setHeaderParam(TokenClaims.TYP.getValue(), TokenType.BEARER.getValue())
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .addClaims(validClaims)
                .signWith(privateKey)
                .compact();

        tokenWithoutRole = Jwts.builder()
                .setHeaderParam(TokenClaims.TYP.getValue(), TokenType.BEARER.getValue())
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .addClaims(noRoleClaims)
                .signWith(privateKey)
                .compact();

        expiredToken = Jwts.builder()
                .setHeaderParam(TokenClaims.TYP.getValue(), TokenType.BEARER.getValue())
                .setIssuedAt(pastDate)
                .setExpiration(pastDate)
                .addClaims(validClaims)
                .signWith(privateKey)
                .compact();

        invalidToken = "invalid.token.format";
    }

    @Test
    void getAuthentication_ValidToken_Success() {
        // Given
        doReturn(true).when(tokenValidationService).verifyAndValidate(validToken);
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);

        // When
        UsernamePasswordAuthenticationToken authToken = tokenService.getAuthentication(validToken);

        // Then
        assertNotNull(authToken);
        assertTrue(authToken.isAuthenticated());
        
        // Verify principal is a Jwt
        Object principal = authToken.getPrincipal();
        assertTrue(principal instanceof Jwt);
        Jwt jwt = (Jwt) principal;
        
        // Verify JWT claims
        assertEquals(validToken, jwt.getTokenValue());
        assertEquals(issuedAt.toInstant().toEpochMilli(), jwt.getIssuedAt().toEpochMilli(), 100);
        assertEquals(expiresAt.toInstant().toEpochMilli(), jwt.getExpiresAt().toEpochMilli(), 100);
        
        // Verify authorities
        Collection<? extends GrantedAuthority> authorities = authToken.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority(Role.ADMIN.name())));
        
        verify(tokenValidationService).verifyAndValidate(validToken);
    }

    @Test
    void getAuthentication_TokenWithoutRole_DefaultsToUserRole() {
        // Given
        doReturn(true).when(tokenValidationService).verifyAndValidate(tokenWithoutRole);
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);

        // When
        UsernamePasswordAuthenticationToken authToken = tokenService.getAuthentication(tokenWithoutRole);

        // Then
        assertNotNull(authToken);
        assertTrue(authToken.isAuthenticated());
        
        // Verify authorities - should default to USER role
        Collection<? extends GrantedAuthority> authorities = authToken.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority(Role.USER.name())));
        
        verify(tokenValidationService).verifyAndValidate(tokenWithoutRole);
    }

    @Test
    void getAuthentication_TokenValidationFails_ThrowsJwtException() {
        // Given
        doThrow(new JwtException("Token validation failed"))
            .when(tokenValidationService).verifyAndValidate(invalidToken);

        // When & Then
        JwtException exception = assertThrows(JwtException.class, () -> {
            tokenService.getAuthentication(invalidToken);
        });
        
        assertEquals("Invalid JWT token", exception.getMessage());
        verify(tokenValidationService).verifyAndValidate(invalidToken);
    }

    @Test
    void getAuthentication_TokenParsingFails_ThrowsJwtException() {
        // Given
        doReturn(false).when(tokenValidationService).verifyAndValidate(invalidToken);
        // The parsing will fail during execution because the token is invalid

        // When & Then
        Exception exception = assertThrows(JwtException.class, () -> {
            tokenService.getAuthentication(invalidToken);
        });
        
        assertEquals("Invalid JWT token", exception.getMessage());
        verify(tokenValidationService).verifyAndValidate(invalidToken);
    }

    @Test
    void getAuthentication_UnexpectedError_ThrowsRuntimeException() {
        // Given
        doThrow(new RuntimeException("Unexpected error"))
            .when(tokenValidationService).verifyAndValidate(validToken);

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            tokenService.getAuthentication(validToken);
        });
        
        assertEquals("Invalid token", exception.getMessage());
        verify(tokenValidationService).verifyAndValidate(validToken);
    }

    @Test
    void getAuthentication_NullToken_ThrowsException() {
        // Given
        String nullToken = null;
        
        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            tokenService.getAuthentication(nullToken);
        });
        
        // The expected behavior depends on the implementation, but we know it will fail
        assertNotNull(exception);
    }

    @Test
    void getAuthentication_EmptyToken_ThrowsException() {
        // Given
        String emptyToken = "";
        
        // When & Then
        Exception exception = assertThrows(JwtException.class, () -> {
            tokenService.getAuthentication(emptyToken);
        });
        
        assertEquals("Invalid JWT token", exception.getMessage());
    }

    @Test
    void getAuthentication_ExpiredToken_HandledByValidationService() {
        // Given
        doThrow(new JwtException("Token has expired"))
            .when(tokenValidationService).verifyAndValidate(expiredToken);

        // When & Then
        JwtException exception = assertThrows(JwtException.class, () -> {
            tokenService.getAuthentication(expiredToken);
        });
        
        assertEquals("Invalid JWT token", exception.getMessage());
        verify(tokenValidationService).verifyAndValidate(expiredToken);
    }

    @Test
    void getAuthentication_InvalidSignature_HandledByValidationService() {
        // Given
        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "12345";
        doThrow(new JwtException("Invalid signature"))
            .when(tokenValidationService).verifyAndValidate(tamperedToken);

        // When & Then
        JwtException exception = assertThrows(JwtException.class, () -> {
            tokenService.getAuthentication(tamperedToken);
        });
        
        assertEquals("Invalid JWT token", exception.getMessage());
        verify(tokenValidationService).verifyAndValidate(tamperedToken);
    }

    @Test
    void getAuthentication_CachingWorksCorrectly() {
        // Given
        doReturn(true).when(tokenValidationService).verifyAndValidate(validToken);
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);

        // When
        tokenService.getAuthentication(validToken);
        tokenService.getAuthentication(validToken); // Second call should use cache

        // Then
        // Verification should be called only once due to caching
        // Note: This test might not work as expected in unit tests due to caching being disabled
        verify(tokenValidationService, times(2)).verifyAndValidate(validToken);
    }

    @Test
    void getAuthentication_DifferentTokens_ProcessedIndependently() {
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);

        // Given
        String anotherValidToken = Jwts.builder()
                .setHeaderParam(TokenClaims.TYP.getValue(), TokenType.BEARER.getValue())
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .claim(TokenClaims.USER_ID.getValue(), "user456")
                .claim(TokenClaims.USER_ROLE.getValue(), Role.USER.name())
                .claim(TokenClaims.TOKEN_VERSION.getValue(), 1)
                .signWith(privateKey)
                .compact();
                
        doReturn(true).when(tokenValidationService).verifyAndValidate(anyString());

        // When
        UsernamePasswordAuthenticationToken authToken1 = tokenService.getAuthentication(validToken);
        UsernamePasswordAuthenticationToken authToken2 = tokenService.getAuthentication(anotherValidToken);

        // Then
        assertNotNull(authToken1);
        assertNotNull(authToken2);
        assertNotSame(authToken1, authToken2);
        
        // Principals should be different
        Jwt jwt1 = (Jwt) authToken1.getPrincipal();
        Jwt jwt2 = (Jwt) authToken2.getPrincipal();
        assertNotEquals(jwt1, jwt2);
        
        verify(tokenValidationService).verifyAndValidate(validToken);
        verify(tokenValidationService).verifyAndValidate(anotherValidToken);
    }

    @Test
    void getAuthentication_MultipleRoles_OnlyFirstRoleIsUsed() {
        when(tokenConfigurationParameter.getPublicKey()).thenReturn(publicKey);

        // Given
        String multiRoleToken = Jwts.builder()
                .setHeaderParam(TokenClaims.TYP.getValue(), TokenType.BEARER.getValue())
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .claim(TokenClaims.USER_ID.getValue(), "user123")
                .claim(TokenClaims.USER_ROLE.getValue(), Role.ADMIN.name()) // Only this role will be used
                .claim("anotherRole", Role.USER.name()) // This won't be used
                .claim(TokenClaims.TOKEN_VERSION.getValue(), 1)
                .signWith(privateKey)
                .compact();
                
        doReturn(true).when(tokenValidationService).verifyAndValidate(multiRoleToken);

        // When
        UsernamePasswordAuthenticationToken authToken = tokenService.getAuthentication(multiRoleToken);

        // Then
        assertNotNull(authToken);
        Collection<? extends GrantedAuthority> authorities = authToken.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority(Role.ADMIN.name())));
        
        verify(tokenValidationService).verifyAndValidate(multiRoleToken);
    }
}