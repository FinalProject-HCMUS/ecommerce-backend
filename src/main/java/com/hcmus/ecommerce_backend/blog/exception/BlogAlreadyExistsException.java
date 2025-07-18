package com.hcmus.ecommerce_backend.blog.exception;

public class BlogAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Blog already exists";

    public BlogAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }
    
}
