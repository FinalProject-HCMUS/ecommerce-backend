package com.hcmus.ecommerce_backend.unit.auth.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import com.hcmus.ecommerce_backend.auth.exception.TokenAlreadyInvalidatedException;
import com.hcmus.ecommerce_backend.auth.model.entity.InvalidToken;
import com.hcmus.ecommerce_backend.auth.repository.InvalidTokenRepository;
import com.hcmus.ecommerce_backend.auth.service.impl.TokenManagementServiceImpl;

@ExtendWith(MockitoExtension.class)
public class TokenManagementServiceImplTest {

    @Mock
    private InvalidTokenRepository invalidTokenRepository;

    @InjectMocks
    private TokenManagementServiceImpl tokenManagementService;

    @Captor
    private ArgumentCaptor<Set<InvalidToken>> invalidTokenSetCaptor;

    private String validTokenId;
    private String invalidTokenId;

    @BeforeEach
    void setUp() {
        validTokenId = "valid-token-id";
        invalidTokenId = "invalid-token-id";
    }

    @Test
    void invalidateTokens_SingleToken_Success() {
        // Given
        Set<String> tokenIds = Set.of(validTokenId);

        // When
        tokenManagementService.invalidateTokens(tokenIds);

        // Then
        verify(invalidTokenRepository).saveAll(invalidTokenSetCaptor.capture());
        Set<InvalidToken> capturedTokens = invalidTokenSetCaptor.getValue();
        
        assertEquals(1, capturedTokens.size());
        InvalidToken capturedToken = capturedTokens.iterator().next();
        assertEquals(validTokenId, capturedToken.getTokenId());
    }

    @Test
    void invalidateTokens_MultipleTokens_Success() {
        // Given
        Set<String> tokenIds = Set.of(validTokenId, "another-token", "third-token");

        // When
        tokenManagementService.invalidateTokens(tokenIds);

        // Then
        verify(invalidTokenRepository).saveAll(invalidTokenSetCaptor.capture());
        Set<InvalidToken> capturedTokens = invalidTokenSetCaptor.getValue();
        
        assertEquals(3, capturedTokens.size());
        assertTrue(capturedTokens.stream().anyMatch(token -> token.getTokenId().equals(validTokenId)));
        assertTrue(capturedTokens.stream().anyMatch(token -> token.getTokenId().equals("another-token")));
        assertTrue(capturedTokens.stream().anyMatch(token -> token.getTokenId().equals("third-token")));
    }

    @Test
    void invalidateTokens_EmptySet_Success() {
        // Given
        Set<String> tokenIds = Collections.emptySet();

        // When
        tokenManagementService.invalidateTokens(tokenIds);

        // Then
        verify(invalidTokenRepository).saveAll(invalidTokenSetCaptor.capture());
        Set<InvalidToken> capturedTokens = invalidTokenSetCaptor.getValue();
        
        assertTrue(capturedTokens.isEmpty());
    }

    @Test
    void invalidateTokens_DatabaseError_ThrowsException() {
        // Given
        Set<String> tokenIds = Set.of(validTokenId);
        when(invalidTokenRepository.saveAll(any())).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> tokenManagementService.invalidateTokens(tokenIds));
        verify(invalidTokenRepository).saveAll(any());
    }

    @Test
    void invalidateTokens_NullSet_ThrowsException() {
        // When & Then
        assertThrows(NullPointerException.class, () -> tokenManagementService.invalidateTokens(null));
        verify(invalidTokenRepository, never()).saveAll(any());
    }

    @Test
    void checkForInvalidityOfToken_ValidToken_ReturnsFalse() {
        // Given
        when(invalidTokenRepository.findByTokenId(validTokenId)).thenReturn(Optional.empty());

        // When
        boolean result = tokenManagementService.checkForInvalidityOfToken(validTokenId);

        // Then
        assertFalse(result);
        verify(invalidTokenRepository).findByTokenId(validTokenId);
    }

    @Test
    void checkForInvalidityOfToken_InvalidToken_ThrowsException() {
        // Given
        InvalidToken invalidToken = InvalidToken.builder().tokenId(invalidTokenId).build();
        when(invalidTokenRepository.findByTokenId(invalidTokenId)).thenReturn(Optional.of(invalidToken));

        // When & Then
        assertThrows(TokenAlreadyInvalidatedException.class, 
                () -> tokenManagementService.checkForInvalidityOfToken(invalidTokenId));
        verify(invalidTokenRepository).findByTokenId(invalidTokenId);
    }

    @Test
    void checkForInvalidityOfToken_DatabaseError_ThrowsException() {
        // Given
        when(invalidTokenRepository.findByTokenId(validTokenId))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> tokenManagementService.checkForInvalidityOfToken(validTokenId));
        verify(invalidTokenRepository).findByTokenId(validTokenId);
    }

    @Test
    void checkForInvalidityOfToken_EmptyTokenId_ReturnsValidity() {
        // Given
        String emptyTokenId = "";
        when(invalidTokenRepository.findByTokenId(emptyTokenId)).thenReturn(Optional.empty());

        // When
        boolean result = tokenManagementService.checkForInvalidityOfToken(emptyTokenId);

        // Then
        assertFalse(result);
        verify(invalidTokenRepository).findByTokenId(emptyTokenId);
    }

    @Test
    void invalidateTokens_DuplicateTokenIds_SavesDistinct() {
        // Given
        Set<String> tokenIds = new HashSet<>(Arrays.asList(validTokenId, validTokenId, "another-token"));

        // When
        tokenManagementService.invalidateTokens(tokenIds);

        // Then
        verify(invalidTokenRepository).saveAll(invalidTokenSetCaptor.capture());
        Set<InvalidToken> capturedTokens = invalidTokenSetCaptor.getValue();
        
        assertEquals(2, capturedTokens.size());
        assertTrue(capturedTokens.stream().anyMatch(token -> token.getTokenId().equals(validTokenId)));
        assertTrue(capturedTokens.stream().anyMatch(token -> token.getTokenId().equals("another-token")));
    }

    @Test
    void invalidateTokens_TokensWithSpecialChars_Success() {
        // Given
        String specialCharsToken = "token-with-special-chars-!@#$%^&*()_+";
        Set<String> tokenIds = Set.of(specialCharsToken);

        // When
        tokenManagementService.invalidateTokens(tokenIds);

        // Then
        verify(invalidTokenRepository).saveAll(invalidTokenSetCaptor.capture());
        Set<InvalidToken> capturedTokens = invalidTokenSetCaptor.getValue();
        
        assertEquals(1, capturedTokens.size());
        InvalidToken capturedToken = capturedTokens.iterator().next();
        assertEquals(specialCharsToken, capturedToken.getTokenId());
    }

    @Test
    void invalidateTokens_LargeSetOfTokens_Success() {
        // Given
        Set<String> tokenIds = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            tokenIds.add("token-" + i);
        }

        // When
        tokenManagementService.invalidateTokens(tokenIds);

        // Then
        verify(invalidTokenRepository).saveAll(invalidTokenSetCaptor.capture());
        Set<InvalidToken> capturedTokens = invalidTokenSetCaptor.getValue();
        
        assertEquals(1000, capturedTokens.size());
        for (int i = 0; i < 1000; i++) {
            String tokenId = "token-" + i;
            assertTrue(capturedTokens.stream().anyMatch(token -> token.getTokenId().equals(tokenId)));
        }
    }


    @Test
    void invalidateTokens_NonEmptyTokenWithWhitespace_Success() {
        // Given
        String whitespaceToken = "  token with spaces  ";
        Set<String> tokenIds = Set.of(whitespaceToken);

        // When
        tokenManagementService.invalidateTokens(tokenIds);

        // Then
        verify(invalidTokenRepository).saveAll(invalidTokenSetCaptor.capture());
        Set<InvalidToken> capturedTokens = invalidTokenSetCaptor.getValue();
        
        assertEquals(1, capturedTokens.size());
        InvalidToken capturedToken = capturedTokens.iterator().next();
        assertEquals(whitespaceToken, capturedToken.getTokenId());
    }
}
