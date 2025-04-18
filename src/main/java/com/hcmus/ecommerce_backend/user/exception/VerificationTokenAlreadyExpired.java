package com.hcmus.ecommerce_backend.user.exception;

public class VerificationTokenAlreadyExpired extends RuntimeException{
    
    public VerificationTokenAlreadyExpired() {
        super("Verification token has already expired");
    }

    public VerificationTokenAlreadyExpired(String token) {
        super("Verification token " + token + " has already expired");
    }
}
