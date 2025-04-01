package com.hcmus.ecommerce_backend.user.exception;

public class UserAlreadyExistsException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "User already exists";

    public UserAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }

    public UserAlreadyExistsException(String value, boolean isEmail) {
        super(isEmail 
            ? "User already exists with email: " + value 
            : "User already exists with phone number: " + value);
    }
    
    public UserAlreadyExistsException(String email, String phoneNum) {
        super("User already exists with email: " + email + " and phone number: " + phoneNum);
    }
    
}
