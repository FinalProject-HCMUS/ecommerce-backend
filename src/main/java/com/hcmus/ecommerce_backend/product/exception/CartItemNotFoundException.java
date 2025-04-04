package com.hcmus.ecommerce_backend.product.exception;

public class CartItemNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Cart item not found";

    public CartItemNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
    
    public CartItemNotFoundException(String id) {
        super("Cart item not found with id: " + id);
    }
}