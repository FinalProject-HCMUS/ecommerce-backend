package com.hcmus.ecommerce_backend.auth.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public interface TokenService {
    UsernamePasswordAuthenticationToken getAuthentication(String token);
}