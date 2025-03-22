package com.hcmus.ecommerce_backend.category.exception;

public class CategoryAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Category already exists";

    public CategoryAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }
    
    public CategoryAlreadyExistsException(String name) {
        super("Category already exists with name: " + name);
    }
}