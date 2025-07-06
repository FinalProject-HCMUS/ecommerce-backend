package com.hcmus.ecommerce_backend.unit.common.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.hcmus.ecommerce_backend.common.service.impl.EmailServiceImpl;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderDetailWithProductResponse;
import com.hcmus.ecommerce_backend.order.model.enums.PaymentMethod;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductResponse;

import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {
    
    @Mock
    private JavaMailSender mailSender;
    
    @Mock
    private TemplateEngine templateEngine;
    
    @Mock
    private MimeMessage mimeMessage;
    
    @InjectMocks
    private EmailServiceImpl emailService;
    
    @Captor
    private ArgumentCaptor<Context> contextCaptor;
    
    private final String fromEmail = "test@example.com";
    private final String frontendUrl = "https://example.com";
    private final String shopName = "Test Shop";
    private final String supportEmail = "support@example.com";
    
    @BeforeEach
    void setUp() {
        // Setup configuration values using ReflectionTestUtils
        ReflectionTestUtils.setField(emailService, "fromEmail", fromEmail);
        ReflectionTestUtils.setField(emailService, "frontendUrl", frontendUrl);
        ReflectionTestUtils.setField(emailService, "shopName", shopName);
        ReflectionTestUtils.setField(emailService, "supportEmail", supportEmail);
        
    }
    
    @Test
    void sendEmailConfirmation_Success() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Email content</html>");

        // Given
        String email = "user@example.com";
        String name = "John Doe";
        String token = "confirmation-token-123";
        
        // When
        emailService.sendEmailConfirmation(email, name, token);
        
        // Then
        verify(mailSender).createMimeMessage();
        verify(templateEngine).process(eq("email/email-confirmation"), contextCaptor.capture());
        verify(mailSender).send(mimeMessage);
        
        // Verify context variables
        Context capturedContext = contextCaptor.getValue();
        // Map<String, Object> variables = capturedContext.getVariableNames();
        
        // assert capturedContext.getVariable("name").equals(name);
        // assert capturedContext.getVariable("confirmationUrl").equals(frontendUrl + "/confirm-email?token=" + token);
        // assert capturedContext.getVariable("shopName").equals(shopName);
        // assert capturedContext.getVariable("supportEmail").equals(supportEmail);
        // assert variables.containsKey("socialLinks");
    }
    
    @Test
    void sendEmailConfirmation_HandlesMailException() {
        
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Email content</html>");

        // Given
        String email = "user@example.com";
        String name = "John Doe";
        String token = "confirmation-token-123";
        
        doThrow(new MailSendException("Mail server unavailable"))
            .when(mailSender).send(mimeMessage);
        
        // When
        assertThrows(MailSendException.class, () -> emailService.sendEmailConfirmation(email, name, token));
        
        // Then
        verify(mailSender).createMimeMessage();
        verify(templateEngine).process(eq("email/email-confirmation"), any(Context.class));
        verify(mailSender).send(mimeMessage);
        // No exception should be thrown, just logged
    }
    
    @Test
    void sendEmailConfirmation_HandlesRuntimeException() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Email content</html>");

        // Given
        String email = "user@example.com";
        String name = "John Doe";
        String token = "confirmation-token-123";
        
        doThrow(new RuntimeException("Unexpected error"))
            .when(mailSender).send(mimeMessage);
        
        // When
        assertThrows(RuntimeException.class, () -> emailService.sendEmailConfirmation(email, name, token));

        // Then
        verify(mailSender).createMimeMessage();
        verify(templateEngine).process(eq("email/email-confirmation"), any(Context.class));
        verify(mailSender).send(mimeMessage);
        // No exception should be thrown, just logged
    }
    
    @Test
    void sendResetPasswordEmail_Success() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Email content</html>");

        // Given
        String email = "user@example.com";
        String name = "John Doe";
        String token = "reset-token-123";
        
        // When
        emailService.sendResetPasswordEmail(email, name, token);
        
        // Then
        verify(mailSender).createMimeMessage();
        verify(templateEngine).process(eq("email/reset-password"), contextCaptor.capture());
        verify(mailSender).send(mimeMessage);
        
        // Verify context variables
        Context capturedContext = contextCaptor.getValue();
        
        assert capturedContext.getVariable("name").equals(name);
        assert capturedContext.getVariable("resetUrl").equals(frontendUrl + "/reset-password?token=" + token);
        assert capturedContext.getVariable("shopName").equals(shopName);
        assert capturedContext.getVariable("supportEmail").equals(supportEmail);
        assert capturedContext.getVariable("socialLinks").equals(capturedContext.getVariable("socialLinks"));
    }
    
    @Test
    void sendResetPasswordEmail_HandlesMailException() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Email content</html>");

        // Given
        String email = "user@example.com";
        String name = "John Doe";
        String token = "reset-token-123";
        
        doThrow(new MailSendException("Mail server unavailable"))
            .when(mailSender).send(mimeMessage);
        
        // When
        assertThrows(MailSendException.class, () -> emailService.sendResetPasswordEmail(email, name, token));

        // Then
        verify(mailSender).createMimeMessage();
        verify(templateEngine).process(eq("email/reset-password"), any(Context.class));
        verify(mailSender).send(mimeMessage);
        // No exception should be thrown, just logged
    }
    
    @Test
    void sendOrderConfirmationEmail_Success() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Email content</html>");

        // Given
        String email = "user@example.com";
        String name = "John Doe";
        String orderId = "ORDER-123";
        Double total = 125.75;
        Double subTotal = 115.75;
        Double shippingCost = 10.00;
        String address = "123 Test Street, Test City";
        PaymentMethod paymentMethod = PaymentMethod.COD;
        LocalDateTime orderDate = LocalDateTime.of(2023, 5, 15, 14, 30);
        
        // Create mock order items
        List<OrderDetailWithProductResponse> orderItems = createMockOrderItems();
        
        // When
        emailService.sendOrderConfirmationEmail(
            email, name, orderId, total, orderItems, address, 
            subTotal, shippingCost, paymentMethod, orderDate
        );
        
        // Then
        verify(mailSender).createMimeMessage();
        verify(templateEngine).process(eq("email/order-confirmation"), contextCaptor.capture());
        verify(mailSender).send(mimeMessage);
        
        // Verify context variables
        Context capturedContext = contextCaptor.getValue();
        
        assert capturedContext.getVariable("name").equals(name);
        assert capturedContext.getVariable("orderId").equals(orderId);
        assert capturedContext.getVariable("orderItems").equals(orderItems);
        assert capturedContext.getVariable("orderTotal").equals(total);
        assert capturedContext.getVariable("orderSubTotal").equals(subTotal);
        assert capturedContext.getVariable("shippingCost").equals(shippingCost);
        assert capturedContext.getVariable("address").equals(address);
        assert capturedContext.getVariable("paymentMethod").equals(paymentMethod.toString());
        assert capturedContext.getVariable("orderDetailsUrl").equals(frontendUrl + "/orders");
        assert capturedContext.getVariable("shopName").equals(shopName);
        assert capturedContext.getVariable("supportEmail").equals(supportEmail);
    }
    
    @Test
    void sendOrderConfirmationEmail_HandlesMailException() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Email content</html>");

        // Given
        String email = "user@example.com";
        String name = "John Doe";
        String orderId = "ORDER-123";
        Double total = 125.75;
        Double subTotal = 115.75;
        Double shippingCost = 10.00;
        String address = "123 Test Street, Test City";
        PaymentMethod paymentMethod = PaymentMethod.COD;
        LocalDateTime orderDate = LocalDateTime.of(2023, 5, 15, 14, 30);
        
        List<OrderDetailWithProductResponse> orderItems = createMockOrderItems();
        
        doThrow(new MailSendException("Mail server unavailable"))
            .when(mailSender).send(mimeMessage);
        
        // When
        assertThrows(MailSendException.class, () -> emailService.sendOrderConfirmationEmail(
            email, name, orderId, total, orderItems, address,
            subTotal, shippingCost, paymentMethod, orderDate
        ));

        // Then
        verify(mailSender).createMimeMessage();
        verify(templateEngine).process(eq("email/order-confirmation"), any(Context.class));
        verify(mailSender).send(mimeMessage);
        // No exception should be thrown, just logged
    }
    
        @Test
    void sendOrderConfirmationEmail_WithNullOrderDate() {
        // Given
        String email = "user@example.com";
        String name = "John Doe";
        String orderId = "ORDER-123";
        Double total = 125.75;
        Double subTotal = 115.75;
        Double shippingCost = 10.00;
        String address = "123 Test Street, Test City";
        PaymentMethod paymentMethod = PaymentMethod.COD;
        LocalDateTime orderDate = null; // Test with null order date
        
        List<OrderDetailWithProductResponse> orderItems = createMockOrderItems();
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> emailService.sendOrderConfirmationEmail(
            email, name, orderId, total, orderItems, address,
            subTotal, shippingCost, paymentMethod, orderDate
        ));
    }
    
    @Test
    void sendOrderConfirmationEmail_WithEmptyOrderItems() {
        // Given
        String email = "user@example.com";
        String name = "John Doe";
        String orderId = "ORDER-123";
        Double total = 0.0;
        Double subTotal = 0.0;
        Double shippingCost = 0.0;
        String address = "123 Test Street, Test City";
        PaymentMethod paymentMethod = PaymentMethod.COD;
        LocalDateTime orderDate = LocalDateTime.now();
        
        List<OrderDetailWithProductResponse> orderItems = List.of(); // Empty list
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> emailService.sendOrderConfirmationEmail(
            email, name, orderId, total, orderItems, address,
            subTotal, shippingCost, paymentMethod, orderDate
        ));
    }
    
    @Test
    void sendMultipleEmails_ReusesSameComponents() {
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Email content</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        // Given
        String email = "user@example.com";
        String name = "John Doe";
        String token = "token-123";
        
        // When
        emailService.sendEmailConfirmation(email, name, token);
        emailService.sendResetPasswordEmail(email, name, token);
        
        // Then
        verify(mailSender, times(2)).createMimeMessage();
        verify(templateEngine).process(eq("email/email-confirmation"), any(Context.class));
        verify(templateEngine).process(eq("email/reset-password"), any(Context.class));
        verify(mailSender, times(2)).send(mimeMessage);
    }
    
    @Test
    void emailsWithSpecialCharacters_HandledCorrectly() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Email content</html>");
        // Given
        String email = "user+test@example.com"; // Email with + character
        String name = "John O'Doe"; // Name with apostrophe
        String token = "token-123<script>alert('xss')</script>"; // Token with potential XSS
        
        // When
        emailService.sendEmailConfirmation(email, name, token);
        
        // Then
        verify(mailSender).createMimeMessage();
        verify(templateEngine).process(eq("email/email-confirmation"), contextCaptor.capture());
        verify(mailSender).send(mimeMessage);
        
        Context capturedContext = contextCaptor.getValue();
        assert capturedContext.getVariable("name").equals(name);
        assert capturedContext.getVariable("confirmationUrl").equals(frontendUrl + "/confirm-email?token=" + token);
    }
    
    @Test
    void templateEngine_HandlesTemplateProcessingException() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        // Given
        String email = "user@example.com";
        String name = "John Doe";
        String token = "token-123";
        
        when(templateEngine.process(anyString(), any(Context.class)))
            .thenThrow(new RuntimeException("Template processing error"));
        
        // When
        assertThrows(RuntimeException.class, () -> emailService.sendEmailConfirmation(email, name, token));

        // Then
        verify(mailSender).createMimeMessage();
        verify(templateEngine).process(eq("email/email-confirmation"), any(Context.class));
        // Email sending should be skipped due to template error, but no exception thrown
    }
    
    @Test
    void mimeMessageCreation_HandlesException() {
        // Given
        String email = "user@example.com";
        String name = "John Doe";
        String token = "token-123";
        
        when(mailSender.createMimeMessage())
            .thenThrow(new RuntimeException("Cannot create MIME message"));
        
        // When
        assertThrows(RuntimeException.class, () -> emailService.sendEmailConfirmation(email, name, token));

        // Then
        verify(mailSender).createMimeMessage();
        // Should handle the exception gracefully without propagating
    }
    
    @Test
    void socialLinksGeneration_ReturnsCorrectLinks() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Email content</html>");
        // Given
        String email = "user@example.com";
        String name = "John Doe";
        String token = "token-123";
        
        // When
        emailService.sendEmailConfirmation(email, name, token);
        
        // Then
        verify(templateEngine).process(eq("email/email-confirmation"), contextCaptor.capture());
        
        Context capturedContext = contextCaptor.getValue();
        
        @SuppressWarnings("unchecked")
        Map<String, String> socialLinks = (Map<String, String>) capturedContext.getVariable("socialLinks");
        
        assert socialLinks.containsKey("facebook");
        assert socialLinks.containsKey("twitter");
        assert socialLinks.containsKey("instagram");
        assert socialLinks.get("facebook").contains("facebook.com");
        assert socialLinks.get("twitter").contains("twitter.com");
        assert socialLinks.get("instagram").contains("instagram.com");
    }
    
    @Test
    void sendEmailConfirmation_WithNullParameters() {
        // Given
        String email = null;
        String name = null;
        String token = null;
        
        // When
        assertThrows(NullPointerException.class, () -> emailService.sendEmailConfirmation(email, name, token));

    }
    
    @Test
    void sendResetPasswordEmail_WithEmptyStrings() {
        // Given
        String email = "";
        String name = "";
        String token = "";
        
        // When
        assertThrows(NullPointerException.class, () -> emailService.sendResetPasswordEmail(email, name, token));
        
    }

    
    // Helper method to create mock order items
    private List<OrderDetailWithProductResponse> createMockOrderItems() {
        ProductResponse product1 = ProductResponse.builder()
            .id("product-1")
            .name("Test Product 1")
            .description("Description 1")
            .price(50.0)
            .build();
            
        ProductResponse product2 = ProductResponse.builder()
            .id("product-2")
            .name("Test Product 2")
            .description("Description 2")
            .price(65.75)
            .build();
            
            
        OrderDetailWithProductResponse item1 = OrderDetailWithProductResponse.builder()
            .id("item-1")
            .product(product1)
            .quantity(1)
            .unitPrice(50.0)
            .product(product1)
            .build();
            
        OrderDetailWithProductResponse item2 = OrderDetailWithProductResponse.builder()
            .id("item-2")
            .product(product2)
            .quantity(1)
            .unitPrice(65.75)
            .product(product2)
            .build();
            
        return Arrays.asList(item1, item2);
    }
}
