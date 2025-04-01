package com.hcmus.ecommerce_backend.product.exception;

public class ProductImageAlreadyExistsException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Product image already exists";

    public ProductImageAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }

    public ProductImageAlreadyExistsException(String url, String productId) {
        super("Product image already exists with url: " + url + " and product id: " + productId);
    }
}
