package com.hcmus.ecommerce_backend.auth.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

        @Override
        @Cacheable(value = "userTokens", key = "#loginRequest.email")
        public TokenResponse login(LoginRequest loginRequest) {
                final User user = userRepository.findByEmail(loginRequest.getEmail())
                                .orElseThrow(() -> new UserNotFoundException(
                                                "Can't find with given email: " + loginRequest.getEmail()));
                if (Boolean.FALSE.equals(
                                passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))) {
                        throw new PasswordNotValidException();
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
            @CacheEvict(value = {"userTokens", "refreshedTokens", "tokenValidation", "tokenPayloads"}, allEntries = true)
        public void logout(TokenInvalidateRequest tokenInvalidateRequest) {
                tokenValidationService.verifyAndValidate(Set.of(tokenInvalidateRequest.getAccessToken(),
                                tokenInvalidateRequest.getRefreshToken()));

                final String accessTokenId = tokenValidationService.getPayload(tokenInvalidateRequest.getAccessToken())
                                .getId();

                tokenManagementService.checkForInvalidityOfToken(accessTokenId);

                final String refreshTokenId = tokenValidationService
                                .getPayload(tokenInvalidateRequest.getRefreshToken()).getId();

                tokenManagementService.checkForInvalidityOfToken(refreshTokenId);

                tokenManagementService.invalidateTokens(Set.of(accessTokenId, refreshTokenId));
        }
}