package com.hcmus.ecommerce_backend.order.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for creating a order detail during checkout")
public class CheckoutOrderDetailRequest {

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Schema(description = "Quantity of the product", example = "2")
    private Integer quantity;

    @NotBlank(message = "Item ID is required")
    @Schema(description = "ID of the product color size", example = "550e8400-e29b-41d4-a716-446655440000")
    private String itemId; // References Product_Color_Size entity
}