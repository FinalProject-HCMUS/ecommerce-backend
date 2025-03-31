package com.hcmus.ecommerce_backend.user.exception;

public class UserNotFoundException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "User not found";

    public UserNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
    
    public UserNotFoundException(String id) {
        super("User not found with id: " + id);
    }
}
