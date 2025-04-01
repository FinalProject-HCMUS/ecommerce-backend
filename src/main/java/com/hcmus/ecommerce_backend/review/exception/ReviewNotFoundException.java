package com.hcmus.ecommerce_backend.review.exception;

public class ReviewNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Review not found";

    public ReviewNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
    
    public ReviewNotFoundException(String id) {
        super("Review not found with id: " + id);
    }
}