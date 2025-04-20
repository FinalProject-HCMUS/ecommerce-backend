package com.hcmus.ecommerce_backend.message.exception;

public class ConversationAlreadyExistsException extends RuntimeException {
    
    private static final String DEFAULT_MESSAGE = "Conversation already exists";
    
    public ConversationAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }
    
    public ConversationAlreadyExistsException(String customerId) {
        super("Conversation already exists for customer: " + customerId);
    }
}