package com.hcmus.ecommerce_backend.product.model.dto.request.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating a new product_color_size")
public class CreateProductColorSizeRequest {
    
    @Schema(description = "Quantity of the product_color_size", example = "10")
    @Min(value = 0, message = "Quantity must be at least 0")
    private int quantity;

    @Schema(description = "ID of the product", example = "123e4567-e89b-12d3-a456-426614174000")
    @NotBlank(message = "Product ID is required")
    private String productId;

    @Schema(description = "ID of the color", example = "456e7890-b12c-34d5-e678-123456789abc")
    @NotBlank(message = "Color ID is required")
    private String colorId;

    @Schema(description = "ID of the size", example = "789e0123-c45d-67f8-9012-3456789abcde")
    @NotBlank(message = "Size ID is required")
    private String sizeId;
}
