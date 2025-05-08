package com.hcmus.ecommerce_backend.product.model.dto.request.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating an existing product")
public class UpdateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    @Schema(description = "Name of the product", example = "iPhone 15 Pro Max", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Description of the product", example = "The latest iPhone with revolutionary features")
    private String description;

    @NotNull(message = "Cost is required")
    @Positive(message = "Cost must be greater than 0")
    @Schema(description = "Cost of the product", example = "900.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double cost;

    @NotNull(message = "Total quantity is required")
    @Positive(message = "Total quantity must be greater than 0")
    @Schema(description = "Total quantity in stock", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer total;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    @Schema(description = "Selling price of the product", example = "1099.99", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double price;

    @Schema(description = "Discount percentage", example = "10.0")
    private Double discountPercent;

    @NotNull(message = "Enable status is required")
    @Schema(description = "Whether the product is enabled", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean enable;

    @NotNull(message = "In stock status is required")
    @Schema(description = "Whether the product is in stock", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean inStock;

    @Schema(description = "Main image URL of the product", example = "https://example.com/iphone15.jpg")
    private String mainImageUrl;

    @NotNull(message = "Category ID is required")
    @Schema(description = "ID of the category this product belongs to", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String categoryId;
}