package com.hcmus.ecommerce_backend.security.filter;

import java.io.IOException;

import com.hcmus.ecommerce_backend.auth.exception.TokenAlreadyInvalidatedException;
import com.hcmus.ecommerce_backend.user.exception.UserNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.hcmus.ecommerce_backend.auth.model.Token;
import com.hcmus.ecommerce_backend.auth.service.TokenManagementService;
import com.hcmus.ecommerce_backend.auth.service.TokenService;
import com.hcmus.ecommerce_backend.auth.service.TokenValidationService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomBearerTokenAuthenticationFilter extends OncePerRequestFilter {
    private final TokenValidationService tokenValidationService;
    private final TokenManagementService tokenManagementService;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(@NonNull final HttpServletRequest httpServletRequest,
            @NonNull final HttpServletResponse httpServletResponse,
            @NonNull final FilterChain filterChain) throws ServletException, IOException {

        log.debug("API Request was secured with Security!");

        final String authorizationHeader = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);

        if (Token.isBearerToken(authorizationHeader)) {
            try {
                final String jwt = Token.getJwt(authorizationHeader);

                tokenValidationService.verifyAndValidate(jwt);

                final String tokenId = tokenValidationService.getId(jwt);

                tokenManagementService.checkForInvalidityOfToken(tokenId);

                final UsernamePasswordAuthenticationToken authentication = tokenService
                        .getAuthentication(jwt);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ExpiredJwtException e) {
                log.warn("Token has expired: {}", e.getMessage());
                // Don't set authentication for expired token
            } catch (TokenAlreadyInvalidatedException e) {
                log.warn("Token has been invalidated: {}", e.getMessage());
                // Don't set authentication for invalidated token
            } catch (UserNotFoundException e) {
                log.warn("User not found: {}", e.getMessage());
                // Don't set authentication for non-existent user
            } catch (JwtException e) {
                log.warn("Invalid JWT token: {}", e.getMessage());
                // Don't set authentication for invalid JWT
            } catch (Exception e) {
                log.error("Error processing authentication token", e);
                // Don't set authentication for any other errors
            }
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);

    }
}
