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
@Schema(description = "Response object containing product information")
public class ProductResponse {
    
    @Schema(description = "Unique identifier of the product", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @Schema(description = "Name of the product", example = "iPhone 12")
    private String name;

    @Schema(description = "Description of the product", example = "The latest iPhone model")
    private String description;

    @Schema(description = "Cost of the product", example = "800")
    private double cost;

    @Schema(description = "Total quantity of the product", example = "100")
    private int total;

    @Schema(description = "Price of the product", example = "1000")
    private double price;

    @Schema(description = "Discount percentage of the product", example = "0.1 ~ 10%")
    private double discountPercent;

    @Schema(description = "Whether the product is enabled", example = "true")
    private boolean enable;
    
    @Schema(description = "Whether the product is in stock", example = "true")
    private boolean inStock;

    @Schema(description = "URL of the main image of the product", example = "https://example.com/image.jpg")
    private String mainImageUrl;

    @Schema(description = "Average rating of the product", example = "4.5")
    private double averageRating;

    @Schema(description = "Number of reviews of the product", example = "100")
    private double reviewCount;

    @Schema(description = "ID of the category this product belongs to", example = "550e8400-e29b-41d4-a716-446655440000")
    private String categoryId;

    @Schema(description = "Name of the category this product belongs to", example = "Electronics")
    private String categoryName;

    @Schema(description = "Date and time when the product was created")
    private String createdAt;

    @Schema(description = "Date and time when the product was last updated")
    private String updatedAt;

    @Schema(description = "User who created the product", example = "admin")
    private String createdBy;

    @Schema(description = "User who last updated the product", example = "admin")
    private String updatedBy;
}
