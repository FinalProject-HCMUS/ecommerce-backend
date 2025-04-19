package com.hcmus.ecommerce_backend.order.model.dto.response;

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
@Schema(description = "Response object containing order track information")
public class OrderTrackResponse {
    
    @Schema(description = "Unique identifier of the order track", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    
    @Schema(description = "Notes about the order tracking", example = "Package shipped from warehouse")
    private String notes;
    
    @Schema(description = "Status of the order at this tracking point", example = "SHIPPED")
    private Status status;

    @Schema(description = "Date and time when the order track was created")
    private LocalDateTime createdAt;

    @Schema(description = "User who created the order track", example = "admin")
    private String createdBy;

    @Schema(description = "Date and time when the order track was last updated")
    private LocalDateTime updatedAt;

    @Schema(description = "User who last updated the order track", example = "admin")
    private String updatedBy;
}