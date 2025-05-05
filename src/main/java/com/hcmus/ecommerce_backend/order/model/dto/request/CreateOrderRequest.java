package com.hcmus.ecommerce_backend.order.model.dto.request;

import com.hcmus.ecommerce_backend.order.model.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Request for creating a new order")
public class CreateOrderRequest {
    
    @NotBlank(message = "First name is required")
    @Schema(description = "First name of the customer", example = "John")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Schema(description = "Last name of the customer", example = "Doe")
    private String lastName;
    
    @NotBlank(message = "Phone number is required")
    @Schema(description = "Phone number of the customer", example = "+1234567890")
    private String phoneNumber;
    
    @Schema(description = "Expected delivery date", example = "2023-05-15T15:30:00")
    private LocalDateTime deliveryDate;
    
    @NotNull(message = "Payment method is required")
    @Schema(description = "Payment method for the order", example = "COD")
    private PaymentMethod paymentMethod;
    
    @NotNull(message = "Shipping cost is required")
    @PositiveOrZero(message = "Shipping cost must be zero or positive")
    @Schema(description = "Shipping cost for the order", example = "10.99")
    private Double shippingCost;
    
    @NotNull(message = "Product cost is required")
    @PositiveOrZero(message = "Product cost must be zero or positive")
    @Schema(description = "Total product cost", example = "99.99")
    private Double productCost;
    
    @NotNull(message = "Sub total is required")
    @PositiveOrZero(message = "Sub total must be zero or positive")
    @Schema(description = "Subtotal before shipping", example = "99.99")
    private Double subTotal;
    
    @NotNull(message = "Total is required")
    @PositiveOrZero(message = "Total must be zero or positive")
    @Schema(description = "Total order amount", example = "110.98")
    private Double total;
    
    @NotBlank(message = "Customer ID is required")
    @Schema(description = "ID of the customer", example = "550e8400-e29b-41d4-a716-446655440000")
    private String customerId;

    @NotBlank(message = "Address is required")
    @Schema(description = "Shipping address for the order", example = "123 Main St, Springfield, USA")
    private String address;
}