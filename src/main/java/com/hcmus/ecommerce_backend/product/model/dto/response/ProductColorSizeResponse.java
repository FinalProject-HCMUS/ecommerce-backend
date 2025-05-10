package com.hcmus.ecommerce_backend.product.model.dto.response;

    import java.time.LocalDateTime;

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
    @Schema(description = "Response object containing product color size information")
    public class ProductColorSizeResponse {
        @Schema(description = "Unique identifier of the product color size", example = "550e8400-e29b-41d4-a716-446655440000")
        private String id;

        @Schema(description = "Quantity of the product color size", example = "10")
        @Min(value = 0, message = "Quantity must be at least 0")
        private int quantity;

        @Schema(description = "Product information")
        private ProductResponse product;

        @Schema(description = "Color information")
        private ColorResponse color;

        @Schema(description = "Size information")
        private SizeResponse size;

        @Schema(description = "Date and time when the product color size was created")
        private LocalDateTime createdAt;

        @Schema(description = "User who created the product color size", example = "admin")
        private String createdBy;

        @Schema(description = "Date and time when the product color size was last updated")
        private LocalDateTime updatedAt;

        @Schema(description = "User who last updated the product color size", example = "admin")
        private String updatedBy;
    }