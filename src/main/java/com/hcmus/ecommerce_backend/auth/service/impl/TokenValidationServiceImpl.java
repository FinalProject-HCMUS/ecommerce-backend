package com.hcmus.ecommerce_backend.auth.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hcmus.ecommerce_backend.auth.exception.TokenAlreadyInvalidatedException;
import com.hcmus.ecommerce_backend.auth.model.enums.TokenClaims;
import com.hcmus.ecommerce_backend.auth.service.TokenManagementService;
import com.hcmus.ecommerce_backend.auth.service.TokenValidationService;
import com.hcmus.ecommerce_backend.config.TokenConfigurationParameter;
import com.hcmus.ecommerce_backend.user.exception.UserNotFoundException;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;

import java.util.Date;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenValidationServiceImpl implements TokenValidationService {
    private final TokenConfigurationParameter tokenConfigurationParameter;
    private final TokenManagementService tokenManagementService;
    private final UserRepository userRepository;

    @Override
    public boolean verifyAndValidate(String jwt) {
        try {
            tokenManagementService.checkForInvalidityOfToken(getId(jwt));
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(tokenConfigurationParameter.getPublicKey())
                    .build()
                    .parseClaimsJws(jwt);

            Claims claims = claimsJws.getBody();
            String userId = claims.get(TokenClaims.USER_ID.getValue(), String.class);
            Integer tokenVersion = claims.get(TokenClaims.TOKEN_VERSION.getValue(), Integer.class);

            // Get the current token version from user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

            // Check if token version matches current user token version
            if (tokenVersion == null || tokenVersion < user.getTokenVersion()) {
                throw new TokenAlreadyInvalidatedException();
            }

            // Additional checks (e.g., expiration, issuer, etc.)
            if (claims.getExpiration().before(new Date())) {
                throw new JwtException("Token has expired");
            }

            log.info("Token is valid");
            return true;

        } catch (ExpiredJwtException e) {
            log.error("Token has expired", e);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token has expired", e);
        } catch (JwtException e) {
            log.error("Invalid JWT token", e);
            throw new JwtException("Invalid JWT token");
        } catch (TokenAlreadyInvalidatedException e) {
            log.error("Token is already invalidated", e);
            throw new TokenAlreadyInvalidatedException();
        } catch (Exception e) {
            log.error("Error validating token", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error validating token", e);
        }
    }

    @Override
    public boolean verifyAndValidate(Set<String> jwts) {
        for (String jwt : jwts) {
            if (!verifyAndValidate(jwt)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Jws<Claims> getClaims(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(tokenConfigurationParameter.getPublicKey())
                .build()
                .parseClaimsJws(jwt);
    }

    @Override
    public Claims getPayload(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(tokenConfigurationParameter.getPublicKey())
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }

    @Override
    public String getId(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(tokenConfigurationParameter.getPublicKey())
                .build()
                .parseClaimsJws(jwt)
                .getBody()
                .getId();
    }
}