package com.hcmus.ecommerce_backend.order.exception;

public class OrderAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Order already exists";

    public OrderAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }
    
    public OrderAlreadyExistsException(String id) {
        super("Order already exists with id: " + id);
    }
}