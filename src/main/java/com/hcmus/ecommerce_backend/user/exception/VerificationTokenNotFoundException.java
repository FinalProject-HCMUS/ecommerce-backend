package com.hcmus.ecommerce_backend.user.exception;

public class VerificationTokenNotFoundException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Verification token not found";

    public VerificationTokenNotFoundException() {
        super(DEFAULT_MESSAGE);
    }

    public VerificationTokenNotFoundException(String token) {
        super(DEFAULT_MESSAGE + ": " + token);
    }
}