package com.hcmus.ecommerce_backend.product.exception;

public class CartItemAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Cart item already exists";

    public CartItemAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }
    
    public CartItemAlreadyExistsException(String id) {
        super("Cart item already exists with id: " + id);
    }
}