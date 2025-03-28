package com.hcmus.ecommerce_backend.product.exception;

public class ColorNotFoundException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Color not found";

    public ColorNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
    
    public ColorNotFoundException(String id) {
        super("Color not found with id: " + id);
    }
}
