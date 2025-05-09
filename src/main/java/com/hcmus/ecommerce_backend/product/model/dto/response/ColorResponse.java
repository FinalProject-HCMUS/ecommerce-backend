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
@Schema(description = "Response object containing color information")
public class ColorResponse {
    
    @Schema(description = "Unique identifier of the color", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @Schema(description = "Name of the color", example = "Red")
    private String name;

    @Schema(description = "Hexadecimal code representing the color", example = "#FF0000")
    private String code;

    @Schema(description = "Date and time when the color was created")
    private LocalDateTime createdAt;
    
    @Schema(description = "User who created the color", example = "admin")
    private String createdBy;
    
    @Schema(description = "Date and time when the color was last updated")
    private LocalDateTime updatedAt;
    
    @Schema(description = "User who last updated the color", example = "admin")
    private String updatedBy;

}
