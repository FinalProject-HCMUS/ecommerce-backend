package com.hcmus.ecommerce_backend.common.exception;

public class KeyNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public KeyNotFoundException(String key) {
        super("Key not found: " + key);
    }

    public KeyNotFoundException(String key, Throwable cause) {
        super("Key not found: " + key, cause);
    }
    
}
