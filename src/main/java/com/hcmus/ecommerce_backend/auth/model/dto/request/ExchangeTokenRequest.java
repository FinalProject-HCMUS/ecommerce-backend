package com.hcmus.ecommerce_backend.auth.model.dto.request;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Data Transfer Object (DTO) for exchange token requests.
 * This class is used to encapsulate the data required for exchanging an authorization code for an access token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ExchangeTokenRequest {
    private String code; // The authorization code received from the authorization server
    private String clientId; // The client ID of the application making the request
    private String clientSecret; // The client secret of the application making the request
    private String redirectUri; // The redirect URI to which the authorization server will send the user after authorization
    private String grantType; // The type of grant being requested (e.g., authorization_code)
}