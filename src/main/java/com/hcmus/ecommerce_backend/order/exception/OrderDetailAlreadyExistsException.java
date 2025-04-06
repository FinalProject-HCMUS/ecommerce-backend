package com.hcmus.ecommerce_backend.order.exception;

public class OrderDetailAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Order detail already exists";

    public OrderDetailAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }
    
    public OrderDetailAlreadyExistsException(String id) {
        super("Order detail already exists with id: " + id);
    }
    
}
