package com.hcmus.ecommerce_backend.order.model.dto.request;

import com.hcmus.ecommerce_backend.order.model.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for creating a new order track")
public class CreateOrderTrackRequest {
    
    @Schema(description = "Notes about the order tracking", example = "Package has been shipped")
    private String notes;
    
    @NotNull(message = "Status is required")
    @Schema(description = "Status of the order at this tracking point", example = "SHIPPED", required = true)
    private Status status;
    
    @Builder.Default
    @Schema(description = "Time when the status was updated", example = "2023-05-12T14:30:00")
    private LocalDateTime updatedTime = LocalDateTime.now();
    
    @NotBlank(message = "Order ID is required")
    @Schema(description = "ID of the order this tracking belongs to", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private String orderId;
}