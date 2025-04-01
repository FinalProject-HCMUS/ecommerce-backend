package com.hcmus.ecommerce_backend.message.model.dto.response;

import com.hcmus.ecommerce_backend.message.model.enums.RoleChat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing message information")
public class MessageResponse {
    
    @Schema(description = "Unique identifier of the message", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    
    @Schema(description = "Content of the message", example = "Hello, how can I help you?")
    private String content;
    
    @Schema(description = "Role of the message sender", example = "ADMIN")
    private RoleChat roleChat;
    
    @Schema(description = "ID of the customer", example = "550e8400-e29b-41d4-a716-446655440000")
    private String customerId;
    
    @Schema(description = "ID of the admin", example = "550e8400-e29b-41d4-a716-446655440000")
    private String adminId;
    
    @Schema(description = "Date and time when the message was created")
    private LocalDateTime createdAt;
    
    @Schema(description = "User who created the message", example = "admin")
    private String createdBy;
    
    @Schema(description = "Date and time when the message was last updated")
    private LocalDateTime updatedAt;
    
    @Schema(description = "User who last updated the message", example = "admin")
    private String updatedBy;
}