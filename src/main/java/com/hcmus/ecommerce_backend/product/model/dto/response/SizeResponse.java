package com.hcmus.ecommerce_backend.product.model.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing size information")
public class SizeResponse {
    @Schema(description = "Unique identifier of the size", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @Schema(description = "Name of the size", example = "M")
    private String name;

    @Schema(description = "Minimum height for this size in cm", example = "150")
    private int minHeight;

    @Schema(description = "Maximum height for this size in cm", example = "180")
    private int maxHeight;

    @Schema(description = "Minimum weight for this size in kg", example = "50")
    private int minWeight;

    @Schema(description = "Maximum weight for this size in kg", example = "80")
    private int maxWeight;

    @Schema(description = "Date and time when the color was created")
    private LocalDateTime createdAt;
    
    @Schema(description = "User who created the color", example = "admin")
    private String createdBy;
    
    @Schema(description = "Date and time when the color was last updated")
    private LocalDateTime updatedAt;
    
    @Schema(description = "User who last updated the color", example = "admin")
    private String updatedBy;
}
