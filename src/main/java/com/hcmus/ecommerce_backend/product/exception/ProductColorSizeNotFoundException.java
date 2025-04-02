package com.hcmus.ecommerce_backend.product.exception;

public class ProductColorSizeNotFoundException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Color not found";

    public ProductColorSizeNotFoundException() {
        super(DEFAULT_MESSAGE);
    }
    
    public ProductColorSizeNotFoundException(String productId, String colorId, String sizeId) {
        super("Product color size not found with product id: " + productId + ", color id: " + colorId + ", size id: " + sizeId);
    }

    public ProductColorSizeNotFoundException(String id) {
        super("Product color size not found with id: " + id);
    }
    
}
