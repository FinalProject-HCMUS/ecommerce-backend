package com.hcmus.ecommerce_backend.order.model.dto.response;

import com.hcmus.ecommerce_backend.order.model.enums.PaymentMethod;
import com.hcmus.ecommerce_backend.order.model.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing order information")
public class OrderResponse {
    
    @Schema(description = "Unique identifier of the order", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    
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
    
    @Schema(description = "Payment method for the order", example = "CREDIT_CARD")
    private PaymentMethod paymentMethod;
    
    @Schema(description = "Shipping cost for the order", example = "10.99")
    private Double shippingCost;
    
    @Schema(description = "Total product cost", example = "99.99")
    private Double productCost;
    
    @Schema(description = "Subtotal before shipping", example = "99.99")
    private Double subTotal;
    
    @Schema(description = "Total order amount", example = "110.98")
    private Double total;
    
    @Schema(description = "ID of the customer", example = "550e8400-e29b-41d4-a716-446655440000")
    private String customerId;

    @Schema(description = "Shipping address", example = "123 Main St, Springfield, USA")
    private String address;

    @Schema(description = "Indicates whether the order has been paid")
    private Boolean isPaid;
    
    @Schema(description = "List of order tracks")
    private List<OrderTrackResponse> orderTracks;
    
    @Schema(description = "Date and time when the order was created")
    private LocalDateTime createdAt;
    
    @Schema(description = "User who created the order", example = "admin")
    private String createdBy;
    
    @Schema(description = "Date and time when the order was last updated")
    private LocalDateTime updatedAt;
    
    @Schema(description = "User who last updated the order", example = "admin")
    private String updatedBy;
}