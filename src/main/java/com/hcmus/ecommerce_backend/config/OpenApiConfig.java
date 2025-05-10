package com.hcmus.ecommerce_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port}")
    private String serverPort;
    
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce API Documentation")
                        .description("RESTful API documentation for the E-Commerce Backend application.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("HCMUS E-Commerce Team")
                                .email("contact@example.com")
                                .url("https://github.com/FinalProject-HCMUS/ecommerce-backend"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("https://localhost:" + serverPort + contextPath)
                                .description("Development Server")
                ));
    }
}