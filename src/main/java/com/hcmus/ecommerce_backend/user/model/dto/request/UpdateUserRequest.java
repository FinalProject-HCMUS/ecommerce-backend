package com.hcmus.ecommerce_backend.user.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request object for updating an existing user")
public class UpdateUserRequest {

    @NotBlank(message = "Phone number is required")
    @Schema(description = "Phone number of the user", example = "1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNumber;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Schema(description = "First name of the user", example = "John", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Schema(description = "Last name of the user", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @Schema(description = "Address of the user", example = "123 Main St, City, Country")
    private String address;

    @NotNull(message = "Weight is required")
    @Schema(description = "Weight of the user in kilograms", example = "70", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer weight;

    @NotNull(message = "Height is required")
    @Schema(description = "Height of the user in centimeters", example = "175", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer height;

    @Schema(description = "Photo URL of the user", example = "https://example.com/photo.jpg")
    private String photo;

}
