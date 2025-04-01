package com.hcmus.ecommerce_backend.review.exception;

public class ReviewAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Review already exists";

    public ReviewAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }
    
    public ReviewAlreadyExistsException(String orderId) {
        super("Review already exists for order with id: " + orderId);
    }
}