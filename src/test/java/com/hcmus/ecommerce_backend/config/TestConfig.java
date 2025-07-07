package com.hcmus.ecommerce_backend.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;

import com.hcmus.ecommerce_backend.common.service.EmailService;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return Mockito.mock(JavaMailSender.class);
    }

    @Bean
    @Primary
    public EmailService emailService() {
        return Mockito.mock(EmailService.class);
    }

    // @Bean
    // @Primary
    // public CustomAuthenticationEntryPoint customAuthenticationEntryPoint() {
    //     return Mockito.mock(CustomAuthenticationEntryPoint.class);
    // }

    // @Bean
    // @Primary
    // public CustomBearerTokenAuthenticationFilter customBearerTokenAuthenticationFilter() {
    //     return Mockito.mock(CustomBearerTokenAuthenticationFilter.class);
    // }
}