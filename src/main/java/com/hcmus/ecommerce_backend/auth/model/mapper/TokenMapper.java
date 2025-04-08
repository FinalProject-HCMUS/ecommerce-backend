package com.hcmus.ecommerce_backend.auth.model.mapper;

import org.mapstruct.Mapper;

import com.hcmus.ecommerce_backend.auth.model.Token;
import com.hcmus.ecommerce_backend.auth.model.dto.response.TokenResponse;

@Mapper(componentModel = "spring")
public interface TokenMapper {
    TokenResponse toTokenResponse(Token token);
}
