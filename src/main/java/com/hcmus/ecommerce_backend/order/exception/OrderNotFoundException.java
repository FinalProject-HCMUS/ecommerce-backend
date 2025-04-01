package com.hcmus.ecommerce_backend.order.exception;

public class OrderNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Order not found";

    public OrderNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
    
    public OrderNotFoundException(String id) {
        super("Order not found with id: " + id);
    }
}