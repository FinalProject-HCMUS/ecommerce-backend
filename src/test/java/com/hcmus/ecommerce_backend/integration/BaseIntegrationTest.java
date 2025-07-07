package com.hcmus.ecommerce_backend.integration;

import org.aspectj.weaver.ast.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.ecommerce_backend.config.TestConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:application-test.yml")
@ActiveProfiles("test")
@Transactional
@Import(TestConfig.class)
public abstract class BaseIntegrationTest {
    // Base configuration
}