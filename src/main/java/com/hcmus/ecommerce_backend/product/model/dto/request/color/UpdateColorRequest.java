package com.hcmus.ecommerce_backend.product.model.dto.request.color;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating an existing color")
public class UpdateColorRequest {

    @NotBlank(message = "Color name is required")
    @Size(min = 2, max = 50, message = "Color name must be between 2 and 50 characters")
    @Schema(description = "Name of the color", example = "Red", required = true)
    private String name;

    @NotBlank(message = "Color code is required")
    @Size(min = 7, max = 7, message = "Color code must be exactly 7 characters (e.g., #FFFFFF)")
    @Schema(description = "Hex code of the color", example = "#FF5733", required = true)
    private String code;
}
