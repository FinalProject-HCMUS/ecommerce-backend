package com.hcmus.ecommerce_backend.auth.controller;

import com.hcmus.ecommerce_backend.auth.model.dto.request.LoginRequest;
import com.hcmus.ecommerce_backend.auth.model.dto.request.TokenInvalidateRequest;
import com.hcmus.ecommerce_backend.auth.model.dto.request.TokenRefreshRequest;
import com.hcmus.ecommerce_backend.auth.model.dto.response.TokenResponse;
import com.hcmus.ecommerce_backend.auth.service.AuthenticationService;
import com.hcmus.ecommerce_backend.auth.service.TokenService;
import com.hcmus.ecommerce_backend.auth.service.TokenValidationService;
import com.hcmus.ecommerce_backend.common.model.dto.CustomResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication APIs for user login, logout, token refresh and validation")
public class AuthController {

  private final AuthenticationService authenticationService;
  private final TokenService tokenService;
  private final TokenValidationService tokenValidationService;

  @Operation(summary = "Login a user", description = "Authenticates a user with their credentials and returns access and refresh tokens")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User logged in successfully"),
      @ApiResponse(responseCode = "401", description = "Invalid credentials",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CustomResponse.class)))
  })
  @PostMapping("/login")
  public ResponseEntity<CustomResponse<TokenResponse>> login(
          @Parameter(description = "User login credentials", required = true)
          @Valid @RequestBody LoginRequest loginRequest) {
    log.info("AuthController | login | email: {}", loginRequest.getEmail());
    final TokenResponse tokenResponse = authenticationService.login(loginRequest);
    return ResponseEntity.ok(CustomResponse.successOf(tokenResponse));
  }

  @Operation(summary = "Refresh a token", description = "Generates new access token using a valid refresh token")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
      @ApiResponse(responseCode = "401", description = "Invalid refresh token",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CustomResponse.class)))
  })
  @PostMapping("/refresh-token")
  public ResponseEntity<CustomResponse<TokenResponse>> refreshToken(
          @Parameter(description = "Refresh token request", required = true)
          @Valid @RequestBody TokenRefreshRequest tokenRefreshRequest) {
    log.info("AuthController | refreshToken");
    final TokenResponse tokenResponse = authenticationService.refreshToken(tokenRefreshRequest);
    return ResponseEntity.ok(CustomResponse.successOf(tokenResponse));
  }

  @Operation(summary = "Logout a user", description = "Invalidates the user's tokens to prevent further use")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User logged out successfully"),
      @ApiResponse(responseCode = "401", description = "Invalid token",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CustomResponse.class)))
  })
  @PostMapping("/logout")
  public ResponseEntity<CustomResponse<Void>> logout(
          @Parameter(description = "Token invalidation request", required = true)
          @Valid @RequestBody TokenInvalidateRequest tokenInvalidateRequest) {
    log.info("AuthController | logout");
    authenticationService.logout(tokenInvalidateRequest);
    return ResponseEntity.ok(CustomResponse.SUCCESS); 
  }

  @Operation(summary = "Validate a token", description = "Checks if a provided token is valid and not expired or invalidated")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Token is valid"),
      @ApiResponse(responseCode = "401", description = "Invalid token",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CustomResponse.class)))
  })
  @PostMapping("/validate-token")
  public ResponseEntity<CustomResponse<Void>> validateToken(
          @Parameter(name = "Authorization", description = "Bearer token", required = true, in = ParameterIn.HEADER)
          @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
    log.info("AuthController | validateToken");
    String token = extractTokenFromHeader(authorizationHeader);
    tokenValidationService.verifyAndValidate(token);
    return ResponseEntity.ok(CustomResponse.SUCCESS);
  }

  @Operation(summary = "Get authentication details", description = "Extracts and returns the authentication details from a valid JWT token")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Authentication details retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Invalid token",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CustomResponse.class)))
  })
  @GetMapping("/authenticate")
  public ResponseEntity<UsernamePasswordAuthenticationToken> getAuthentication(
          @Parameter(name = "Authorization", description = "Bearer token", required = true, in = ParameterIn.HEADER)
          @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
    log.info("AuthController | authenticate");
    String token = extractTokenFromHeader(authorizationHeader);
    UsernamePasswordAuthenticationToken authentication = tokenService.getAuthentication(token);
    log.info("AuthController | authenticate | authentication: {}", authentication);
    return ResponseEntity.ok(authentication);
  }

  /**
   * Helper method to extract JWT token from Authorization header
   */
  private String extractTokenFromHeader(String authorizationHeader) {
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      return authorizationHeader.substring(7);
    }
    throw new IllegalArgumentException("Invalid Authorization header format. Must be 'Bearer [token]'");
  }
}