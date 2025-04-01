package com.hcmus.ecommerce_backend.message.exception;

public class MessageNotFoundException extends RuntimeException {
    
    private static final String DEFAULT_MESSAGE = "Message not found";
    
    public MessageNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
    
    public MessageNotFoundException(String id) {
        super("Message not found with id: " + id);
    }
}