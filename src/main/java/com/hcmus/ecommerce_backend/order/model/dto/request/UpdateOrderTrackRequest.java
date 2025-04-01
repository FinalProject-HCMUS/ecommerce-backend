package com.hcmus.ecommerce_backend.order.model.dto.request;

import com.hcmus.ecommerce_backend.order.model.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for updating an existing order track")
public class UpdateOrderTrackRequest {
    
    @Schema(description = "Notes about the order tracking", example = "Package delivered to customer")
    private String notes;
    
    @Schema(description = "Status of the order at this tracking point", example = "SHIPPING")
    private Status status;
    
    @Schema(description = "Time when the status was updated", example = "2023-05-12T14:30:00")
    private LocalDateTime updatedTime;
    
    @Schema(description = "ID of the order this tracking belongs to", example = "550e8400-e29b-41d4-a716-446655440000")
    private String orderId;
}