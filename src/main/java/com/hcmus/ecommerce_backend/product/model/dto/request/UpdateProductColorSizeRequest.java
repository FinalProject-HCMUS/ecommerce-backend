package com.hcmus.ecommerce_backend.product.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating an existing product_color_size")
public class UpdateProductColorSizeRequest {
    
    @Schema(description = "New quantity of the product_color_size", example = "20")
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
