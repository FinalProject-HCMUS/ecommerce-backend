package com.hcmus.ecommerce_backend.message.exception;

public class MessageAlreadyExistsException extends RuntimeException {
    
    private static final String DEFAULT_MESSAGE = "Message already exists";
    
    public MessageAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }
    
    public MessageAlreadyExistsException(String id) {
        super("Message already exists with id: " + id);
    }
}