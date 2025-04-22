package com.hcmus.ecommerce_backend.auth.model.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Data Transfer Object (DTO) for outbound user responses.
 * This class is used to encapsulate the user information returned from an external authentication provider.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OutboundUserResponse {
    String id; // The unique identifier of the user
    String email; // The email address of the user
    boolean verifiedEmail; // Indicates whether the user's email address has been verified
    String name; // The full name of the user
    String givenName; // The given name (first name) of the user
    String familyName; // The family name (last name) of the user
    String picture; // The URL of the user's profile picture
    String locale; // The locale of the user (e.g., en-US)
}
