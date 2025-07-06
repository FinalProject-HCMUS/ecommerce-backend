package com.hcmus.ecommerce_backend.auth.service.impl;

import com.hcmus.ecommerce_backend.auth.client.OutboundIdentityClient;
import com.hcmus.ecommerce_backend.auth.client.OutboundUserClient;
import com.hcmus.ecommerce_backend.auth.exception.UserNotActivatedException;
import com.hcmus.ecommerce_backend.auth.model.dto.request.ExchangeTokenRequest;
import com.hcmus.ecommerce_backend.auth.model.dto.response.OutboundUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hcmus.ecommerce_backend.auth.exception.PasswordNotValidException;
import com.hcmus.ecommerce_backend.auth.model.Token;
import com.hcmus.ecommerce_backend.auth.model.dto.request.LoginRequest;
import com.hcmus.ecommerce_backend.auth.model.dto.request.TokenInvalidateRequest;
import com.hcmus.ecommerce_backend.auth.model.dto.request.TokenRefreshRequest;
import com.hcmus.ecommerce_backend.auth.model.dto.response.TokenResponse;
import com.hcmus.ecommerce_backend.auth.model.enums.TokenClaims;
import com.hcmus.ecommerce_backend.auth.model.mapper.TokenMapper;
import com.hcmus.ecommerce_backend.auth.service.AuthenticationService;
import com.hcmus.ecommerce_backend.auth.service.TokenGenerationService;
import com.hcmus.ecommerce_backend.auth.service.TokenManagementService;
import com.hcmus.ecommerce_backend.auth.service.TokenValidationService;
import com.hcmus.ecommerce_backend.user.exception.UserNotFoundException;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final TokenGenerationService tokenGenerationService;
        private final TokenValidationService tokenValidationService;
        private final TokenManagementService tokenManagementService;
        private final TokenMapper tokenMapper;
        private final OutboundIdentityClient outboundIdentityClient;
        private final OutboundUserClient outboundUserClient;
        @NonFinal
        @Value("${outbound.identity.client-id}")
        protected String CLIENT_ID;
        @NonFinal
        @Value("${outbound.identity.client-secret}")
        protected String CLIENT_SECRET;
        @NonFinal
        @Value("${redirect-uri}")
        protected String REDIRECT_URI;
        @NonFinal
        protected String GRANT_TYPE = "authorization_code";

        @Override
        @Cacheable(value = "userTokens", key = "#loginRequest.email")
        public TokenResponse login(LoginRequest loginRequest) {
                final User user = userRepository.findByEmail(loginRequest.getEmail())
                                .orElseThrow(() -> new UserNotFoundException());
                if (Boolean.FALSE.equals(
                                passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))) {
                        throw new PasswordNotValidException();
                }

                if (!user.isEnabled()) {
                        log.warn("Login attempt with unverified account: {}", loginRequest.getEmail());
                        throw new UserNotActivatedException();
                }

                Token token = tokenGenerationService.generateToken(user.getClaims());
                TokenResponse tokenResponse = tokenMapper.toTokenResponse(token);
                return tokenResponse;
        }

        @Override
        @Cacheable(value = "refreshedTokens", key = "#tokenRefreshRequest.refreshToken")
        public TokenResponse refreshToken(TokenRefreshRequest tokenRefreshRequest) {

                tokenValidationService.verifyAndValidate(tokenRefreshRequest.getRefreshToken());

                final String userId = tokenValidationService.getPayload(tokenRefreshRequest.getRefreshToken())
                                .get(TokenClaims.USER_ID.getValue()).toString();

                final User user = userRepository.findById(userId)
                                .orElseThrow(UserNotFoundException::new);

                Token token = tokenGenerationService.generateToken(user.getClaims(),
                                tokenRefreshRequest.getRefreshToken());

                TokenResponse tokenResponse = tokenMapper.toTokenResponse(token);
                return tokenResponse;
        }

        @Override
        public TokenResponse outboundAuthentication(String code) {
                var accessToken = outboundIdentityClient.exchangeToken(
                                ExchangeTokenRequest.builder()
                                                .code(code)
                                                .clientId(CLIENT_ID)
                                                .clientSecret(CLIENT_SECRET)
                                                .redirectUri(REDIRECT_URI)
                                                .grantType(GRANT_TYPE)
                                                .build());
                OutboundUserResponse userResponse = outboundUserClient.getUserInfo("json",
                                accessToken.getAccessToken());
                // Check if the user already exists in the database
                User user = userRepository.findByEmail(userResponse.getEmail())
                                .orElseGet(() -> {
                                        // If the user does not exist, create a new user
                                        User newUser = User.builder()
                                                        .email(userResponse.getEmail())
                                                        .firstName(userResponse.getName())
                                                        .lastName(userResponse.getGivenName())
                                                        .photo(userResponse.getPicture())
                                                        .enabled(true)
                                                        .build();
                                        return userRepository.save(newUser);
                                });
                Token token = tokenGenerationService.generateToken(user.getClaims());
                TokenResponse tokenResponse = tokenMapper.toTokenResponse(token);
                return tokenResponse;
        }

        @Override
        @CacheEvict(value = { "userTokens", "refreshedTokens", "tokenValidation", "tokenPayloads" }, allEntries = true)
        public void logout(TokenInvalidateRequest tokenInvalidateRequest) {
                try {
                        tokenValidationService.verifyAndValidate(Set.of(tokenInvalidateRequest.getAccessToken(),
                                        tokenInvalidateRequest.getRefreshToken()));

                        final String accessTokenId = tokenValidationService
                                        .getPayload(tokenInvalidateRequest.getAccessToken())
                                        .getId();

                        tokenManagementService.checkForInvalidityOfToken(accessTokenId);

                        final String refreshTokenId = tokenValidationService
                                        .getPayload(tokenInvalidateRequest.getRefreshToken()).getId();

                        tokenManagementService.checkForInvalidityOfToken(refreshTokenId);

                        tokenManagementService.invalidateTokens(Set.of(accessTokenId, refreshTokenId));
                } catch (Exception e) {
                        log.error("Error during logout: {}", e.getMessage());
                        throw e; // Re-throw the exception to handle it in the controller
                }

        }
}