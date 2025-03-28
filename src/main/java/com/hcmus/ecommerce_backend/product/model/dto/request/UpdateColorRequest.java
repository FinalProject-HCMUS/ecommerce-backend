package com.hcmus.ecommerce_backend.product.model.dto.request;

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
}
