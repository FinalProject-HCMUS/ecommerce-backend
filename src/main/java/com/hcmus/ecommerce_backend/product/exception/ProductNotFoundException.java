package com.hcmus.ecommerce_backend.product.exception;

public class ProductNotFoundException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Product not found";

    public ProductNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
    
    public ProductNotFoundException(String id) {
        super("Category not found with id: " + id);
    }
}