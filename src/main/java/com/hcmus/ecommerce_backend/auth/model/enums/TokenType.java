package com.hcmus.ecommerce_backend.auth.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum TokenType {

    BEARER("Bearer");

    private final String value;

}
