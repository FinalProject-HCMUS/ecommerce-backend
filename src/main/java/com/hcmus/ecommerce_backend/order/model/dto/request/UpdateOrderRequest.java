package com.hcmus.ecommerce_backend.order.model.dto.request;

import com.hcmus.ecommerce_backend.order.model.enums.PaymentMethod;
import com.hcmus.ecommerce_backend.order.model.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for updating an existing order")
public class UpdateOrderRequest {
    
    @Schema(description = "First name of the customer", example = "John")
    private String firstName;
    
    @Schema(description = "Last name of the customer", example = "Doe")
    private String lastName;
    
    @Schema(description = "Phone number of the customer", example = "+1234567890")
    private String phoneNumber;
    
    @Schema(description = "Current status of the order", example = "PROCESSING")
    private Status status;
    
    @Schema(description = "Expected delivery date", example = "2023-05-15T15:30:00")
    private LocalDateTime deliveryDate;
    
    @Schema(description = "Payment method for the order", example = "VNPAY")
    private PaymentMethod paymentMethod;
    
    @PositiveOrZero(message = "Shipping cost must be zero or positive")
    @Schema(description = "Shipping cost for the order", example = "10.99")
    private Double shippingCost;
    
    @PositiveOrZero(message = "Product cost must be zero or positive")
    @Schema(description = "Total product cost", example = "99.99")
    private Double productCost;
    
    @PositiveOrZero(message = "Sub total must be zero or positive")
    @Schema(description = "Subtotal before shipping", example = "99.99")
    private Double subTotal;
    
    @PositiveOrZero(message = "Total must be zero or positive")
    @Schema(description = "Total order amount", example = "110.98")
    private Double total;
    
    @Schema(description = "ID of the customer", example = "550e8400-e29b-41d4-a716-446655440000")
    private String customerId;
}