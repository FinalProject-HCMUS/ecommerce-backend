package com.hcmus.ecommerce_backend.product.exception;

public class ProductAlreadyExistsException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Product already exists";

    public ProductAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }
    
    public ProductAlreadyExistsException(String name) {
        super("Product already exists with name: " + name);
    }
}
