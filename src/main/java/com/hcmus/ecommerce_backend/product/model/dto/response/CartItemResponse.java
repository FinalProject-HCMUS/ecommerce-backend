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
@Schema(description = "Response object containing cart item information")
public class CartItemResponse {

    @Schema(description = "Unique identifier of the cart item", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @Schema(description = "Quantity of the product", example = "2")
    private Integer quantity;

    @Schema(description = "ID of the user", example = "550e8400-e29b-41d4-a716-446655440000")
    private String userId;

    @Schema(description = "ID of the item - product color size", example = "550e8400-e29b-41d4-a716-446655440000")
    private String itemId;
}