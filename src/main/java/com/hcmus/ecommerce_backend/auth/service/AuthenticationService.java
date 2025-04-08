package com.hcmus.ecommerce_backend.auth.service;

import com.hcmus.ecommerce_backend.auth.model.dto.request.LoginRequest;
import com.hcmus.ecommerce_backend.auth.model.dto.request.TokenInvalidateRequest;
import com.hcmus.ecommerce_backend.auth.model.dto.request.TokenRefreshRequest;
import com.hcmus.ecommerce_backend.auth.model.dto.response.TokenResponse;

public interface AuthenticationService {
    TokenResponse login(LoginRequest loginRequest);
    TokenResponse refreshToken(TokenRefreshRequest tokenRefreshRequest);
    void logout(TokenInvalidateRequest tokenInvalidateRequest);
}