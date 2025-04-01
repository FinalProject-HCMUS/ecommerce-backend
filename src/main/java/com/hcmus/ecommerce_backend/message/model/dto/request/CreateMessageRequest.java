package com.hcmus.ecommerce_backend.message.model.dto.request;

import com.hcmus.ecommerce_backend.message.model.enums.RoleChat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for creating a new message")
public class CreateMessageRequest {
    
    @NotBlank(message = "Content is required")
    @Schema(description = "Content of the message", example = "Hello, how can I help you?")
    private String content;
    
    @NotNull(message = "Role chat is required")
    @Schema(description = "Role of the message sender", example = "USER")
    private RoleChat roleChat;
    
    @Schema(description = "ID of the customer", example = "550e8400-e29b-41d4-a716-446655440000")
    private String customerId;
    
    @Schema(description = "ID of the admin", example = "550e8400-e29b-41d4-a716-446655440000")
    private String adminId;
}