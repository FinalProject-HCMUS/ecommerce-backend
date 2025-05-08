package com.hcmus.ecommerce_backend.product.model.dto.request.size;

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
@Schema(description = "Request to create multiple sizes at once")
public class CreateMultipleSizesRequest {

    @NotEmpty(message = "Sizes list cannot be empty")
    @Valid
    @Schema(description = "List of sizes to create", required = true)
    private List<CreateSizeRequest> sizes;
}
