package com.hcmus.ecommerce_backend.product.exception;

public class SizeNotFoundException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Size not found";

    public SizeNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
    
    public SizeNotFoundException(String id) {
        super("Size not found with id: " + id);
    }
}
