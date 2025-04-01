package com.hcmus.ecommerce_backend.product.exception;

public class ProductImageNotFoundException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Product image not found";

    public ProductImageNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public ProductImageNotFoundException(String id) {
        super("Product image not found with id: " + id);
    }
}
