package com.hcmus.ecommerce_backend.user.exception;

public class UserNotAuthorizedException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "User is not authorized to perform this action";

    public UserNotAuthorizedException() {
        super(DEFAULT_MESSAGE);
    }

    public UserNotAuthorizedException(String id) {
        super("User is not authorized to perform this action on user with id: " + id);
    }
}