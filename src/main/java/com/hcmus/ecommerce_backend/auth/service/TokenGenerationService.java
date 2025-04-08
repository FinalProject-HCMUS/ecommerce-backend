package com.hcmus.ecommerce_backend.auth.service;


import java.util.Map;

import com.hcmus.ecommerce_backend.auth.model.Token;

public interface TokenGenerationService {
    Token generateToken(Map<String, Object> claims);
    Token generateToken(Map<String, Object> claims, String refreshToken);
}