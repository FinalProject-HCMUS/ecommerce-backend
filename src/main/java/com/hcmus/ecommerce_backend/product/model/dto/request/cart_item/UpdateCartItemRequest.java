package com.hcmus.ecommerce_backend.product.model.dto.request.cart_item;

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
@Schema(description = "Request for updating an existing cart item")
public class UpdateCartItemRequest {
    
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Quantity of the product", example = "3")
    private Integer quantity;
    
    @Schema(description = "ID of the item (product)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String itemId;
    
    @Schema(description = "ID of the user", example = "550e8400-e29b-41d4-a716-446655440000")
    private String userId;
}
