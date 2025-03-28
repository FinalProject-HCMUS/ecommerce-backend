package com.hcmus.ecommerce_backend.product.exception;

public class ColorAlreadyExistsException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Color already exists";

    public ColorAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }
    
    public ColorAlreadyExistsException(String name) {
        super("Color already exists with name: " + name);
    }
}

