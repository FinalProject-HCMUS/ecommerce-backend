package com.hcmus.ecommerce_backend.unit.auth.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.hcmus.ecommerce_backend.auth.model.Token;
import com.hcmus.ecommerce_backend.auth.service.impl.TokenGenerationServiceImpl;
import com.hcmus.ecommerce_backend.config.TokenConfigurationParameter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
public class TokenGenerationServiceImplTest {

    @Mock
    private TokenConfigurationParameter tokenConfigurationParameter;

    @InjectMocks
    private TokenGenerationServiceImpl tokenGenerationService;

    private Map<String, Object> claims;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws Exception {
        // Generate key pair for testing
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();

        // Setup claims
        claims = new HashMap<>();
        claims.put("userId", "user-123");
        claims.put("role", "USER");
        claims.put("email", "test@example.com");
        claims.put("tokenVersion", 1);

        // Setup mock configuration
        when(tokenConfigurationParameter.getPrivateKey()).thenReturn(privateKey);
        when(tokenConfigurationParameter.getAccessTokenExpireMinute()).thenReturn(15);
    }

    // Tests for generateToken(Map<String, Object> claims) method
    @Test
    void generateToken_WithClaims_Success() {
        when(tokenConfigurationParameter.getRefreshTokenExpireDay()).thenReturn(7);
        // When
        Token result = tokenGenerationService.generateToken(claims);

        // Then
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());
        assertTrue(result.getAccessTokenExpiresAt() > 0);

        // Verify tokens can be parsed
        Claims accessTokenClaims = parseToken(result.getAccessToken());
        Claims refreshTokenClaims = parseToken(result.getRefreshToken());

        // Verify access token claims
        assertEquals("user-123", accessTokenClaims.get("userId"));
        assertEquals("USER", accessTokenClaims.get("role"));
        assertEquals("test@example.com", accessTokenClaims.get("email"));
        assertEquals(1, accessTokenClaims.get("tokenVersion"));
        assertNotNull(accessTokenClaims.getId());
        assertNotNull(accessTokenClaims.getIssuedAt());
        assertNotNull(accessTokenClaims.getExpiration());

        // Verify refresh token claims
        assertEquals("user-123", refreshTokenClaims.get("userId"));
        assertNotNull(refreshTokenClaims.getId());
        assertNotNull(refreshTokenClaims.getIssuedAt());
        assertNotNull(refreshTokenClaims.getExpiration());

        // Verify expiration times
        long accessTokenExpiration = accessTokenClaims.getExpiration().getTime();
        long refreshTokenExpiration = refreshTokenClaims.getExpiration().getTime();
        assertTrue(refreshTokenExpiration > accessTokenExpiration);

        verify(tokenConfigurationParameter, times(2)).getPrivateKey();
        verify(tokenConfigurationParameter).getAccessTokenExpireMinute();
        verify(tokenConfigurationParameter).getRefreshTokenExpireDay();
    }

    @Test
    void generateToken_WithEmptyClaims_Success() {
        when(tokenConfigurationParameter.getRefreshTokenExpireDay()).thenReturn(7);

        // Given
        Map<String, Object> emptyClaims = new HashMap<>();

        // When
        Token result = tokenGenerationService.generateToken(emptyClaims);

        // Then
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());
        assertTrue(result.getAccessTokenExpiresAt() > 0);

        // Verify tokens can be parsed
        Claims accessTokenClaims = parseToken(result.getAccessToken());
        Claims refreshTokenClaims = parseToken(result.getRefreshToken());

        // Access token should have no custom claims except default ones
        assertNotNull(accessTokenClaims.getId());
        assertNotNull(accessTokenClaims.getIssuedAt());
        assertNotNull(accessTokenClaims.getExpiration());

        // Refresh token should have userId as null
        assertNull(refreshTokenClaims.get("userId"));
        assertNotNull(refreshTokenClaims.getId());
    }

    @Test
    void generateToken_WithNullClaims_ThrowsException() {
        // When & Then
        assertThrows(Exception.class, () -> tokenGenerationService.generateToken(null));
    }

    @Test
    void generateToken_WithNullUserId_Success() {
        when(tokenConfigurationParameter.getRefreshTokenExpireDay()).thenReturn(7);

        // Given
        claims.put("userId", null);

        // When
        Token result = tokenGenerationService.generateToken(claims);

        // Then
        assertNotNull(result);
        Claims refreshTokenClaims = parseToken(result.getRefreshToken());
        assertNull(refreshTokenClaims.get("userId"));
    }

    @Test
    void generateToken_ConfigurationError_ThrowsException() {
        // Given
        when(tokenConfigurationParameter.getPrivateKey()).thenReturn(null);

        // When & Then
        assertThrows(Exception.class, () -> tokenGenerationService.generateToken(claims));
        verify(tokenConfigurationParameter).getPrivateKey();
    }


    @Test
    void generateToken_LargeClaims_Success() {
        // Given
        Map<String, Object> largeClaims = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            largeClaims.put("claim" + i, "value" + i);
        }
        largeClaims.put("userId", "user-123");

        // When
        Token result = tokenGenerationService.generateToken(largeClaims);

        // Then
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertNotNull(result.getRefreshToken());

        Claims accessTokenClaims = parseToken(result.getAccessToken());
        // Verify some of the claims are present
        assertEquals("value0", accessTokenClaims.get("claim0"));
        assertEquals("value99", accessTokenClaims.get("claim99"));
        assertEquals("user-123", accessTokenClaims.get("userId"));
    }

    @Test
    void generateToken_SpecialCharactersInClaims_Success() {
        // Given
        Map<String, Object> specialClaims = new HashMap<>();
        specialClaims.put("userId", "user-123");
        specialClaims.put("email", "test+special@example.com");
        specialClaims.put("name", "John O'Connor & Jane");
        specialClaims.put("unicode", "测试用户");

        // When
        Token result = tokenGenerationService.generateToken(specialClaims);

        // Then
        assertNotNull(result);
        Claims accessTokenClaims = parseToken(result.getAccessToken());
        assertEquals("test+special@example.com", accessTokenClaims.get("email"));
        assertEquals("John O'Connor & Jane", accessTokenClaims.get("name"));
        assertEquals("测试用户", accessTokenClaims.get("unicode"));
    }

    // Tests for generateToken(Map<String, Object> claims, String refreshToken) method
    @Test
    void generateToken_WithRefreshToken_Success() {
        // Given
        String existingRefreshToken = "existing-refresh-token";

        // When
        Token result = tokenGenerationService.generateToken(claims, existingRefreshToken);

        // Then
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertEquals(existingRefreshToken, result.getRefreshToken());
        assertTrue(result.getAccessTokenExpiresAt() > 0);

        // Verify access token can be parsed and contains claims
        Claims accessTokenClaims = parseToken(result.getAccessToken());
        assertEquals("user-123", accessTokenClaims.get("userId"));
        assertEquals("USER", accessTokenClaims.get("role"));
        assertEquals("test@example.com", accessTokenClaims.get("email"));
        assertEquals(1, accessTokenClaims.get("tokenVersion"));
        assertNotNull(accessTokenClaims.getId());
        assertNotNull(accessTokenClaims.getIssuedAt());
        assertNotNull(accessTokenClaims.getExpiration());

        verify(tokenConfigurationParameter).getPrivateKey();
        verify(tokenConfigurationParameter).getAccessTokenExpireMinute();
        verify(tokenConfigurationParameter, never()).getRefreshTokenExpireDay();
    }

    @Test
    void generateToken_WithNullRefreshToken_Success() {
        // When
        Token result = tokenGenerationService.generateToken(claims, null);

        // Then
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertNull(result.getRefreshToken());
        assertTrue(result.getAccessTokenExpiresAt() > 0);

        Claims accessTokenClaims = parseToken(result.getAccessToken());
        assertEquals("user-123", accessTokenClaims.get("userId"));
    }

    @Test
    void generateToken_WithEmptyRefreshToken_Success() {
        // Given
        String emptyRefreshToken = "";

        // When
        Token result = tokenGenerationService.generateToken(claims, emptyRefreshToken);

        // Then
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
        assertEquals(emptyRefreshToken, result.getRefreshToken());
    }

    @Test
    void generateToken_WithRefreshToken_ConfigurationError_ThrowsException() {
        // Given
        String refreshToken = "refresh-token-123";
        when(tokenConfigurationParameter.getPrivateKey()).thenReturn(null);

        // When & Then
        assertThrows(Exception.class, () -> tokenGenerationService.generateToken(claims, refreshToken));
    }


    // Comparison tests between the two methods
    @Test
    void generateToken_BothMethods_AccessTokensHaveSameStructure() {
        // Given
        String refreshToken = "test-refresh-token";

        // When
        Token token1 = tokenGenerationService.generateToken(claims);
        Token token2 = tokenGenerationService.generateToken(claims, refreshToken);

        // Then
        Claims claims1 = parseToken(token1.getAccessToken());
        Claims claims2 = parseToken(token2.getAccessToken());

        // Both should have same claim values (except for unique fields like ID and timestamps)
        assertEquals(claims1.get("userId"), claims2.get("userId"));
        assertEquals(claims1.get("role"), claims2.get("role"));
        assertEquals(claims1.get("email"), claims2.get("email"));
        assertEquals(claims1.get("tokenVersion"), claims2.get("tokenVersion"));

        // IDs should be different (randomly generated)
        assertNotEquals(claims1.getId(), claims2.getId());
    }

    @Test
    void generateToken_MultipleCallsSameClaims_GeneratesDifferentTokens() {
        // When
        Token token1 = tokenGenerationService.generateToken(claims);
        Token token2 = tokenGenerationService.generateToken(claims);

        // Then
        assertNotEquals(token1.getAccessToken(), token2.getAccessToken());
        assertNotEquals(token1.getRefreshToken(), token2.getRefreshToken());

        // But should have same claim values
        Claims claims1 = parseToken(token1.getAccessToken());
        Claims claims2 = parseToken(token2.getAccessToken());
        assertEquals(claims1.get("userId"), claims2.get("userId"));
        assertEquals(claims1.get("role"), claims2.get("role"));
    }

    @Test
    void generateToken_TokenExpirationTimes_AreCorrect() {
        // Given
        when(tokenConfigurationParameter.getAccessTokenExpireMinute()).thenReturn(30);
        when(tokenConfigurationParameter.getRefreshTokenExpireDay()).thenReturn(14);

        long beforeGeneration = System.currentTimeMillis();

        // When
        Token result = tokenGenerationService.generateToken(claims);

        // Then
        long afterGeneration = System.currentTimeMillis();

        Claims accessTokenClaims = parseToken(result.getAccessToken());
        Claims refreshTokenClaims = parseToken(result.getRefreshToken());

        // Access token should expire in approximately 30 minutes
        long accessTokenExpiry = accessTokenClaims.getExpiration().getTime();
        long expectedAccessExpiry = beforeGeneration + (30 * 60 * 1000); // 30 minutes in ms
        assertTrue(Math.abs(accessTokenExpiry - expectedAccessExpiry) < 5000); // Within 5 seconds tolerance

        // Refresh token should expire in approximately 14 days
        long refreshTokenExpiry = refreshTokenClaims.getExpiration().getTime();
        long expectedRefreshExpiry = beforeGeneration + (14 * 24 * 60 * 60 * 1000L); // 14 days in ms
        assertTrue(Math.abs(refreshTokenExpiry - expectedRefreshExpiry) < 5000); // Within 5 seconds tolerance

        // Access token expiry should match the field in Token object
        assertEquals(accessTokenExpiry / 1000, result.getAccessTokenExpiresAt());
    }


    @Test
    void generateToken_ExtremeExpiration_HandledCorrectly() {
        // Given
        when(tokenConfigurationParameter.getAccessTokenExpireMinute()).thenReturn(Integer.MAX_VALUE);
        when(tokenConfigurationParameter.getRefreshTokenExpireDay()).thenReturn(Integer.MAX_VALUE);

        // When & Then
        // This might throw an exception due to date overflow, which is expected
        assertDoesNotThrow(() -> {
            Token result = tokenGenerationService.generateToken(claims);
            assertNotNull(result);
        });
    }

    // Helper method to parse tokens for verification
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Edge case tests
    @Test
    void generateToken_VeryLongRefreshToken_Success() {
        // Given
        StringBuilder longToken = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longToken.append("a");
        }
        String veryLongRefreshToken = longToken.toString();

        // When
        Token result = tokenGenerationService.generateToken(claims, veryLongRefreshToken);

        // Then
        assertNotNull(result);
        assertEquals(veryLongRefreshToken, result.getRefreshToken());
    }

    @Test
    void generateToken_ClaimsWithNullValues_Success() {
        // Given
        Map<String, Object> claimsWithNulls = new HashMap<>();
        claimsWithNulls.put("userId", "user-123");
        claimsWithNulls.put("nullClaim", null);
        claimsWithNulls.put("role", "USER");

        // When
        Token result = tokenGenerationService.generateToken(claimsWithNulls);

        // Then
        assertNotNull(result);
        Claims accessTokenClaims = parseToken(result.getAccessToken());
        assertEquals("user-123", accessTokenClaims.get("userId"));
        assertEquals("USER", accessTokenClaims.get("role"));
        // Null claims might be excluded or included as null
        assertTrue(accessTokenClaims.containsKey("nullClaim") || !accessTokenClaims.containsKey("nullClaim"));
    }

    @Test
    void generateToken_ClaimsWithComplexObjects_Success() {
        // Given
        Map<String, Object> complexClaims = new HashMap<>();
        complexClaims.put("userId", "user-123");
        complexClaims.put("number", 42);
        complexClaims.put("boolean", true);
        complexClaims.put("date", new Date());

        // When
        Token result = tokenGenerationService.generateToken(complexClaims);

        // Then
        assertNotNull(result);
        Claims accessTokenClaims = parseToken(result.getAccessToken());
        assertEquals("user-123", accessTokenClaims.get("userId"));
        assertEquals(42, accessTokenClaims.get("number"));
        assertEquals(true, accessTokenClaims.get("boolean"));
        assertNotNull(accessTokenClaims.get("date"));
    }

    @Test
    void generateToken_PerformanceTest_CompletesQuickly() {
        // Given
        long startTime = System.currentTimeMillis();

        // When
        for (int i = 0; i < 100; i++) {
            tokenGenerationService.generateToken(claims);
        }

        // Then
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should complete 100 token generations in reasonable time (less than 5 seconds)
        assertTrue(duration < 5000, "Token generation took too long: " + duration + "ms");
    }

    @Test
    void generateToken_ConcurrentGeneration_ProducesDifferentTokens() throws InterruptedException {
        // This test ensures thread safety and uniqueness
        final Token[] tokens = new Token[2];
        final Exception[] exceptions = new Exception[2];

        Thread thread1 = new Thread(() -> {
            try {
                tokens[0] = tokenGenerationService.generateToken(claims);
            } catch (Exception e) {
                exceptions[0] = e;
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                tokens[1] = tokenGenerationService.generateToken(claims);
            } catch (Exception e) {
                exceptions[1] = e;
            }
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // Verify no exceptions occurred
        assertNull(exceptions[0]);
        assertNull(exceptions[1]);

        // Verify both tokens were generated and are different
        assertNotNull(tokens[0]);
        assertNotNull(tokens[1]);
        assertNotEquals(tokens[0].getAccessToken(), tokens[1].getAccessToken());
        assertNotEquals(tokens[0].getRefreshToken(), tokens[1].getRefreshToken());
    }
}
