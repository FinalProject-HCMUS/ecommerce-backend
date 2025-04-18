package com.hcmus.ecommerce_backend.common.service;


public interface EmailService {
    void sendEmailConfirmation(String email, String name, String token);

    void sendResetPasswordEmail(String email, String name, String token);
}