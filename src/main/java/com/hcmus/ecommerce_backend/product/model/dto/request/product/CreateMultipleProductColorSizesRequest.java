package com.hcmus.ecommerce_backend.product.model.dto.request.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create multiple product color sizes at once")
public class CreateMultipleProductColorSizesRequest {

    @NotEmpty(message = "Product color sizes list cannot be empty")
    @Valid
    @Schema(description = "List of product color sizes to create", required = true)
    private List<CreateProductColorSizeRequest> productColorSizes;
}