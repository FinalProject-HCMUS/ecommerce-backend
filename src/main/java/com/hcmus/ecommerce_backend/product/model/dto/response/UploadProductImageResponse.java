package com.hcmus.ecommerce_backend.product.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for product image upload")
public class UploadProductImageResponse {
    
    @Schema(description = "ID of the created product image", 
            example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    
    @Schema(description = "URL of the uploaded product image", 
            example = "https://example.com/image.jpg")
    private String url;
    
    @Schema(description = "ID of the product this image belongs to", 
            example = "550e8400-e29b-41d4-a716-446655440000")
    private String productId;
    
    @Schema(description = "Whether this is the main product image", 
            example = "true")
    private Boolean isMain;
}