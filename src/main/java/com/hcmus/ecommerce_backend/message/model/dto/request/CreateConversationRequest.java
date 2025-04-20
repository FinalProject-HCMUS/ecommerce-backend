package com.hcmus.ecommerce_backend.message.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for creating a new conversation")
public class CreateConversationRequest {
    
    @NotBlank(message = "Customer ID is required")
    @Schema(description = "ID of the customer starting the conversation", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private String customerId;
}