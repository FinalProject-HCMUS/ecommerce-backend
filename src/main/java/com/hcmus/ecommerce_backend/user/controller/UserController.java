package com.hcmus.ecommerce_backend.user.controller;

import com.hcmus.ecommerce_backend.common.model.dto.CustomResponse;
import com.hcmus.ecommerce_backend.user.model.dto.request.ChangePasswordRequest;
import com.hcmus.ecommerce_backend.user.model.dto.request.CreateUserRequest;
import com.hcmus.ecommerce_backend.user.model.dto.request.ResetPasswordRequest;
import com.hcmus.ecommerce_backend.user.model.dto.request.UpdateUserRequest;
import com.hcmus.ecommerce_backend.user.model.dto.response.UserResponse;
import com.hcmus.ecommerce_backend.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hcmus.ecommerce_backend.common.utils.CreatePageable;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "User management APIs")
public class UserController {

        private final UserService userService;

        @Operation(summary = "Get all users", description = "Retrieves a paginated list of users with sorting capabilities")
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated users")
        @GetMapping
        public ResponseEntity<CustomResponse<Page<UserResponse>>> getAllUsers(
                        @Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "10") int size,

                        @Parameter(description = "Sorting criteria in the format: property(,asc|desc). " +
                                        "Default sort order is ascending. " +
                                        "Multiple sort criteria are supported.") @RequestParam(required = false) String[] sort) {
                log.info("UserController | getPaginatedUsers | page: {}, size: {}, sort: {}",
                                page, size, sort != null ? String.join(", ", sort) : "unsorted");
                Pageable pageable = CreatePageable.build(page, size, sort);
                Page<UserResponse> users = userService.getAllUsers(pageable);
                return ResponseEntity.ok(CustomResponse.successOf(users));
        }

        @Operation(summary = "Get user by ID", description = "Retrieves a specific user by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User found"),
                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class)))
        })
        @GetMapping("/{id}")
        public ResponseEntity<CustomResponse<UserResponse>> getUserById(
                        @Parameter(description = "ID of the user to retrieve", required = true) @PathVariable String id) {
                log.info("UserController | getUserById | id: {}", id);
                UserResponse user = userService.getUserById(id);
                return ResponseEntity.ok(CustomResponse.successOf(user));
        }

        @Operation(summary = "Get current user profile", description = "Retrieves user profile based on the authentication token")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile"),
            @ApiResponse(responseCode = "401", description = "Not authenticated", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class)))
        })
        @GetMapping("/me")
        public ResponseEntity<CustomResponse<UserResponse>> getCurrentUser() {
            log.info("UserController | getCurrentUser | Getting current user profile");
            UserResponse user = userService.getCurrentUser();
            return ResponseEntity.ok(CustomResponse.successOf(user));
        }

        @Operation(summary = "Create a new user", description = "Creates a new user with the provided information")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "User successfully created"),
                        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class))),
                        @ApiResponse(responseCode = "409", description = "User already exists", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class)))
        })
        @PostMapping
        public ResponseEntity<CustomResponse<UserResponse>> createUser(
                        @Parameter(description = "User information for creation", required = true) @Valid @RequestBody CreateUserRequest request) {
                log.info("UserController | createUser | request: {}", request);
                UserResponse createdUser = userService.createUser(request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(CustomResponse.successOf(createdUser));
        }

        @Operation(summary = "Update a user", description = "Updates an existing user with the provided information")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User successfully updated"),
                        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class))),
                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class))),
                        @ApiResponse(responseCode = "409", description = "User email or phone number already exists", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class)))
        })
        @PutMapping("/{id}")
        public ResponseEntity<CustomResponse<UserResponse>> updateUser(
                        @Parameter(description = "ID of the user to update", required = true) @PathVariable String id,
                        @Parameter(description = "Updated user information", required = true) @Valid @RequestBody UpdateUserRequest request) {
                log.info("UserController | updateUser | id: {}, request: {}", id, request);
                UserResponse updatedUser = userService.updateUser(id, request);
                return ResponseEntity.ok(CustomResponse.successOf(updatedUser));
        }

        @Operation(summary = "Delete a user", description = "Deletes a user by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User successfully deleted"),
                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class)))
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<CustomResponse<Void>> deleteUser(
                        @Parameter(description = "ID of the user to delete", required = true) @PathVariable String id) {
                log.info("UserController | deleteUser | id: {}", id);
                userService.deleteUser(id);
                return ResponseEntity.ok(CustomResponse.SUCCESS);
        }

        @Operation(summary = "Change user password", description = "Changes the password for a user and invalidates all existing tokens")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Password successfully changed"),
                        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class))),
                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class)))
        })
        @PostMapping("/{id}/change-password")
        public ResponseEntity<CustomResponse<Void>> changePassword(
                        @Parameter(description = "ID of the user", required = true) @PathVariable String id,
                        @Parameter(description = "Password change details", required = true) @Valid @RequestBody ChangePasswordRequest request) {
                log.info("UserController | changePassword | id: {}", id);
                userService.changePassword(id, request);
                return ResponseEntity.ok(CustomResponse.SUCCESS);
        }

        @Operation(summary = "Confirm email", description = "Confirms the user's email address using a token")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Email successfully confirmed"),
                        @ApiResponse(responseCode = "400", description = "Invalid token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class))),
                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class)))
        })
        @PostMapping("/confirm-email")
        public CustomResponse<Void> confirmEmail(@RequestParam String token) {
                log.info("UserController | confirmEmail | token: {}", token);
                userService.confirmEmail(token);
                return CustomResponse.SUCCESS;
        }

        @Operation(summary = "Resend confirmation email", description = "Generates a new confirmation token and resends the confirmation email")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Confirmation email sent successfully"),
                        @ApiResponse(responseCode = "400", description = "Email already confirmed", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class))),
                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class)))
        })
        @PostMapping("/resend-confirmation")
        public ResponseEntity<CustomResponse<Void>> resendConfirmationEmail(
                        @Parameter(description = "Email address", required = true) @RequestParam String email) {
                log.info("UserController | resendConfirmationEmail | email: {}", email);
                userService.resendConfirmationEmail(email);
                return ResponseEntity.ok(CustomResponse.SUCCESS);
        }

        @Operation(summary = "Request password reset", description = "Sends a password reset email to the user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Password reset email sent successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class)))
        })
        @PostMapping("/request-password-reset")
        public ResponseEntity<CustomResponse<Void>> requestPasswordReset(
                        @Parameter(description = "Email address", required = true) @RequestParam String email) {
                log.info("UserController | requestPasswordReset | email: {}", email);
                userService.sendResetPasswordEmail(email);
                return ResponseEntity.ok(CustomResponse.SUCCESS);
        }

        @Operation(summary = "Reset password", description = "Resets the user's password using a token")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Password successfully reset"),
                        @ApiResponse(responseCode = "400", description = "Invalid token or password mismatch", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class))),
                        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class)))
        })
        @PostMapping("/reset-password")
        public ResponseEntity<CustomResponse<Void>> resetPassword(
                        @Parameter(description = "Password reset details", required = true) @Valid @RequestBody ResetPasswordRequest request) {
                log.info("UserController | resetPassword | request: {}", request);
                userService.resetPassword(request);
                return ResponseEntity.ok(CustomResponse.SUCCESS);
        }

        @Operation(summary = "Validate password reset token", description = "Checks if a password reset token is valid and not expired")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Token is valid and not expired"),
                        @ApiResponse(responseCode = "400", description = "Invalid or expired token", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class)))
        })
        @GetMapping("/validate-reset-token")
        public ResponseEntity<CustomResponse<Void>> validateResetToken(
                        @Parameter(description = "Reset token to validate", required = true) @RequestParam String token) {
                log.info("UserController | validateResetToken | Validating reset token");
                userService.validateResetToken(token);
                return ResponseEntity.ok(CustomResponse.SUCCESS);
        }
}