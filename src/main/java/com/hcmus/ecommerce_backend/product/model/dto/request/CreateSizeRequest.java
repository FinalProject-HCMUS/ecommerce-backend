package com.hcmus.ecommerce_backend.product.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating a new size")
public class CreateSizeRequest {
    
    @NotBlank(message = "Size name is required")
    @Size(min = 1, max = 50, message = "Size name must be between 1 and 50 characters")
    @Schema(description = "Name of the size", example = "M", required = true)
    private String name;

    @NotNull(message = "Minimum height is required")
    @Min(value = 0, message = "Minimum height must be greater than or equal to 0")
    @Schema(description = "Minimum height for this size in cm", example = "150", required = true)
    private int minHeight;

    @NotNull(message = "Maximum height is required")
    @Min(value = 0, message = "Maximum height must be greater than or equal to 0")
    @Schema(description = "Maximum height for this size in cm", example = "180", required = true)
    private int maxHeight;

    @NotNull(message = "Minimum weight is required")
    @Min(value = 0, message = "Minimum weight must be greater than or equal to 0")
    @Schema(description = "Minimum weight for this size in kg", example = "50", required = true)
    private int minWeight;

    @NotNull(message = "Maximum weight is required")
    @Min(value = 0, message = "Maximum weight must be greater than or equal to 0")
    @Schema(description = "Maximum weight for this size in kg", example = "80", required = true)
    private int maxWeight;
}
