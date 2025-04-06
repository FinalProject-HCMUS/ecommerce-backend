package com.hcmus.ecommerce_backend.order.exception;

public class OrderDetailNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Order detail not found";

    public OrderDetailNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
    
    public OrderDetailNotFoundException(String id) {
        super("Order detail not found with id: " + id);
    }    
}
