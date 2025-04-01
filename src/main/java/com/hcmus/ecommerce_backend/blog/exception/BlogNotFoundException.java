package com.hcmus.ecommerce_backend.blog.exception;

public class BlogNotFoundException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Blog not found";

    public BlogNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
    
    public BlogNotFoundException(String id) {
        super("Blog not found with id: " + id);
    }
}
