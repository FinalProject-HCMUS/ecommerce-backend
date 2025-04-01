package com.hcmus.ecommerce_backend.order.exception;

public class OrderTrackAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Order track already exists";

    public OrderTrackAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }
    
    public OrderTrackAlreadyExistsException(String id) {
        super("Order track already exists with id: " + id);
    }
}