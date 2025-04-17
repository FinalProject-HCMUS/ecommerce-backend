package com.hcmus.ecommerce_backend.product.exception;

public class MaxProductImagesExceededException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "A product can have a maximum of 3 images.";

    public MaxProductImagesExceededException() {
        super(DEFAULT_MESSAGE);
    }

    public MaxProductImagesExceededException(String productId) {
        super("Product with ID " + productId + " already has the maximum of 3 images.");
    }
}
