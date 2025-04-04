package com.hcmus.ecommerce_backend.product.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for creating a new cart item")
public class CreateCartItemRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Quantity of the product", example = "2")
    private Integer quantity;

    @NotBlank(message = "User ID is required")
    @Schema(description = "ID of the user", example = "550e8400-e29b-41d4-a716-446655440000")
    private String userId;

    @NotBlank(message = "Item ID is required")
    @Schema(description = "ID of the product color size", example = "550e8400-e29b-41d4-a716-446655440000")
    private String itemId;
}