package com.hcmus.ecommerce_backend.auth.model.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Data Transfer Object (DTO) for exchange token responses.
 * This class is used to encapsulate the data returned from the authorization server
 * when exchanging an authorization code for an access token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ExchangeTokenResponse {
    private String accessToken; // The access token issued by the authorization server
    private Long expiresIn; // The lifetime in seconds of the access token
    private String refreshToken; // The refresh token which can be used to obtain new access tokens
    private String scope; // The scope of the access token
    private String tokenType; // The type of the token issued (e.g., Bearer)
}
