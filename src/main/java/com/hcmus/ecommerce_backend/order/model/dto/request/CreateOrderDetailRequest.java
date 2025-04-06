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
@Schema(description = "Request for creating a new order detail")
public class CreateOrderDetailRequest {

    @NotNull(message = "Product cost is required")
    @Positive(message = "Product cost must be positive")
    @Schema(description = "Cost of the product", example = "100.0")
    private Double productCost;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Schema(description = "Quantity of the product", example = "2")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    @Schema(description = "Unit price of the product", example = "50.0")
    private Double unitPrice;

    @NotNull(message = "Total is required")
    @Positive(message = "Total must be positive")
    @Schema(description = "Total cost of the order detail", example = "100.0")
    private Double total;

    @NotBlank(message = "Product ID is required")
    @Schema(description = "ID of the product", example = "550e8400-e29b-41d4-a716-446655440000")
    private String productId;

    @NotBlank(message = "Order ID is required")
    @Schema(description = "ID of the order", example = "550e8400-e29b-41d4-a716-446655440000")
    private String orderId;
}
