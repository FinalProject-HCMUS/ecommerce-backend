package com.hcmus.ecommerce_backend.order.model.dto.response;

import java.time.LocalDateTime;

import com.hcmus.ecommerce_backend.product.model.dto.response.ProductResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing order detail information")
public class OrderDetailWithProductResponse {

    @Schema(description = "Cost of the product", example = "100.0")
    private Double productCost;

    @Schema(description = "Quantity of the product", example = "2")
    private Integer quantity;

    @Schema(description = "Unit price of the product", example = "50.0")
    private Double unitPrice;

    @Schema(description = "Total cost of the order detail", example = "100.0")
    private Double total;

    @Schema(description = "Product information associated with the order detail", implementation = ProductResponse.class)
    private ProductResponse product;

    @Schema(description = "ID of the order", example = "550e8400-e29b-41d4-a716-446655440000")
    private String orderId;

    @Schema(description = "ID of the item (ProductColorSize)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String itemId;

    @Schema(description = "Date and time when the order detail was created")
    private LocalDateTime createdAt;
    
    @Schema(description = "User who created the order detail", example = "admin")
    private String createdBy;
    
    @Schema(description = "Date and time when the order detail was last updated")
    private LocalDateTime updatedAt;
    
    @Schema(description = "User who last updated the order detail", example = "admin")
    private String updatedBy;
}
