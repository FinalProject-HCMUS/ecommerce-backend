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
@Schema(description = "Response object containing product image information")
public class ProductImageResponse {

    @Schema(description = "Unique identifier of the product image", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @Schema(description = "URL of the product image", example = "https://example.com/image.jpg")
    private String url;

    @Schema(description = "ID of the product associated with the image", example = "550e8400-e29b-41d4-a716-446655440000")
    private String productId;

    @Schema(description = "Date and time when the product image was created")
    private LocalDateTime createdAt;

    @Schema(description = "User who created the product image", example = "admin")
    private String createdBy;

    @Schema(description = "Date and time when the product image was last updated")
    private LocalDateTime updatedAt;

    @Schema(description = "User who last updated the product image", example = "admin")
    private String updatedBy;
}