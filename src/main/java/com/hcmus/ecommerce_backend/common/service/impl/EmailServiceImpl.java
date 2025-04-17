package com.hcmus.ecommerce_backend.common.service.impl;

import com.hcmus.ecommerce_backend.common.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.email.shop-name:}")
    private String shopName;
    
    @Value("${app.email.support-email:}")
    private String supportEmail;

    @Override
    public void sendEmailConfirmation(String email, String name, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Confirm Your Account");

            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("confirmationUrl", frontendUrl + "/confirm-email?token=" + token);
            context.setVariable("shopName", shopName);
            context.setVariable("supportEmail", supportEmail);
            
            // Set social media links
            Map<String, String> socialLinks = new HashMap<>();
            socialLinks.put("facebook", "https://facebook.com/" + shopName.toLowerCase());
            socialLinks.put("instagram", "https://instagram.com/" + shopName.toLowerCase());
            socialLinks.put("twitter", "https://twitter.com/" + shopName.toLowerCase());
            context.setVariable("socialLinks", socialLinks);

            String emailContent = templateEngine.process("email/email-confirmation", context);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("Email confirmation sent to {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send email confirmation: {}", e.getMessage(), e);
        }
    }
}