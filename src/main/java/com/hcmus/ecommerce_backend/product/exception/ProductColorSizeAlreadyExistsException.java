package com.hcmus.ecommerce_backend.product.exception;

public class ProductColorSizeAlreadyExistsException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Product color size already exists";

    public ProductColorSizeAlreadyExistsException() {
        super(DEFAULT_MESSAGE);
    }

    public ProductColorSizeAlreadyExistsException(String productId, String colorId, String sizeId) {
        super("Product color size already exists with product id: " + productId + ", color id: " + colorId + ", size id: " + sizeId);
    }
    
}
