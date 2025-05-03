package com.hcmus.ecommerce_backend.category.model.dto.response;

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
@Schema(description = "Response object containing category information")
public class CategoryResponse {
    
    @Schema(description = "Unique identifier of the category", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    
    @Schema(description = "Name of the category", example = "Electronics")
    private String name;
    
    @Schema(description = "Description of the category", example = "Electronic devices and gadgets")
    private String description;

    @Schema(description = "Stock quantity of the category", example = "100")
    private int stock;
    
    @Schema(description = "Date and time when the category was created")
    private LocalDateTime createdAt;
    
    @Schema(description = "User who created the category", example = "admin")
    private String createdBy;
    
    @Schema(description = "Date and time when the category was last updated")
    private LocalDateTime updatedAt;
    
    @Schema(description = "User who last updated the category", example = "admin")
    private String updatedBy;
}