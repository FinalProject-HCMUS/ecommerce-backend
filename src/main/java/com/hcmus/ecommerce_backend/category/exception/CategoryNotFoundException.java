package com.hcmus.ecommerce_backend.category.exception;

public class CategoryNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Category not found";

    public CategoryNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
    
    public CategoryNotFoundException(String id) {
        super("Category not found with id: " + id);
    }
}