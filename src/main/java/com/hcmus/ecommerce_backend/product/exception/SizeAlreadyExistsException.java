package com.hcmus.ecommerce_backend.product.exception;

public class SizeAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Size already exists";

    public SizeAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }
    
    public SizeAlreadyExistsException(String name) {
        super("Size already exists with name: " + name);
    }
}
