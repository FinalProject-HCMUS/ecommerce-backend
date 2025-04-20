package com.hcmus.ecommerce_backend.message.exception;

public class ConversationNotFoundException extends RuntimeException {
    
    private static final String DEFAULT_MESSAGE = "Conversation not found";
    
    public ConversationNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
    
    public ConversationNotFoundException(String id) {
        super("Conversation not found with id: " + id);
    }
}