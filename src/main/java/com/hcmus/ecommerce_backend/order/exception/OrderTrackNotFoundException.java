package com.hcmus.ecommerce_backend.order.exception;

public class OrderTrackNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Order track not found";

    public OrderTrackNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
    
    public OrderTrackNotFoundException(String id) {
        super("Order track not found with id: " + id);
    }
}