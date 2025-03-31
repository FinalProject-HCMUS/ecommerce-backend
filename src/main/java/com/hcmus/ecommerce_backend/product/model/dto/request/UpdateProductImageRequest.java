package com.hcmus.ecommerce_backend.product.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating an existing image")
public class UpdateProductImageRequest {
    
    @NotBlank(message = "Image URL is required")
    @Schema(description = "URL of the product image", example = "https://example.com/image.jpg", required = true)
    private String url;

    @NotBlank(message = "Product ID is required")
    @Schema(description = "ID of the product this image belongs to", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productId;
}