package com.hcmus.ecommerce_backend.user.exception;

public class EmailAlreadyConfirmedException extends RuntimeException {
    
    public EmailAlreadyConfirmedException() {
        super("Email address is already confirmed");
    }
    
    public EmailAlreadyConfirmedException(String email) {
        super("Email address " + email + " is already confirmed");
    }
}