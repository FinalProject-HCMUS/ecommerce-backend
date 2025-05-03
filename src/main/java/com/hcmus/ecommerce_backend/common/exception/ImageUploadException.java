package com.hcmus.ecommerce_backend.common.exception;

public class ImageUploadException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "ImageUploadException";

    public ImageUploadException() {
        super(DEFAULT_MESSAGE);
    }

    public ImageUploadException(String message) {
        super(message);
    }
}
