package com.hcmus.ecommerce_backend.user.model.dto.response;

import com.hcmus.ecommerce_backend.user.model.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing user information")
public class UserResponse {

    @Schema(description = "Unique identifier of the user", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @Schema(description = "Email of the user", example = "user@example.com")
    private String email;

    @Schema(description = "Phone number of the user", example = "1234567890")
    private String phoneNum;

    @Schema(description = "First name of the user", example = "John")
    private String firstName;

    @Schema(description = "Last name of the user", example = "Doe")
    private String lastName;

    @Schema(description = "Address of the user", example = "123 Main St, City, Country")
    private String address;

    @Schema(description = "Weight of the user in kilograms", example = "70")
    private Integer weight;

    @Schema(description = "Height of the user in centimeters", example = "175")
    private Integer height;

    @Schema(description = "Whether the user account is enabled", example = "true")
    private Boolean enabled;

    @Schema(description = "Photo URL of the user", example = "https://example.com/photo.jpg")
    private String photo;

    @Schema(description = "Role of the user", example = "USER")
    private Role role;

    @Schema(description = "Date and time when the user was created")
    private String createdAt;

    @Schema(description = "Date and time when the user was last updated")
    private String updatedAt;

    @Schema(description = "User who created this user", example = "admin")
    private String createdBy;

    @Schema(description = "User who last updated this user", example = "admin")
    private String updatedBy;
}