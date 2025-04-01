package com.hcmus.ecommerce_backend.user.model.dto.request;

import com.hcmus.ecommerce_backend.user.model.enums.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "Request object for creating a new user")
public class CreateUserRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Email of the user", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Schema(description = "Phone number of the user", example = "1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNum;

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

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Schema(description = "Password of the user", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotNull(message = "Enabled status is required")
    @Schema(description = "Whether the user account is enabled", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean enabled;

    @Schema(description = "Photo URL of the user", example = "https://example.com/photo.jpg")
    private String photo;

    @NotNull(message = "Role is required")
    @Schema(description = "Role of the user", example = "USER", requiredMode = Schema.RequiredMode.REQUIRED)
    private Role role;
}
