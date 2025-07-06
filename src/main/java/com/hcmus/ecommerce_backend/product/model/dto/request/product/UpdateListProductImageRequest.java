package com.hcmus.ecommerce_backend.product.model.dto.request.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for processing product images")
@Builder
public class UpdateListProductImageRequest {
    @Schema(description = "ID of the product image", example = "c5504f81-a277-4f1f-a625-2d81025f3c08")
    private String id;

    @Schema(description = "URL of the product image", example = "http://example.com/image.jpg")
    private String url;

    @NotNull(message = "Product ID is required")
    @Schema(description = "ID of the product associated with the image", example = "e70111d9-75cb-44b4-9119-8cadb807ee30", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productId;

    @Schema(description = "Created at timestamp", example = "2024-12-16T14:41:36")
    private String createdAt;

    @Schema(description = "Created by user", example = "admin")
    private String createdBy;

    @Schema(description = "Updated at timestamp", example = "2024-12-24T14:41:36")
    private String updatedAt;

    @Schema(description = "Updated by user", example = "admin")
    private String updatedBy;
}