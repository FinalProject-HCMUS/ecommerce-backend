package com.hcmus.ecommerce_backend.common.exception;

public class PaymentException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "PaymentException";

    public PaymentException() {
        super(DEFAULT_MESSAGE);
    }

    public PaymentException(String message) {
        super(message);
    }
}
