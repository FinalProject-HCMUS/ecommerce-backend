package com.hcmus.ecommerce_backend.message.model.dto.request;

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
    @Schema(description = "Content of the message", example = "Hello, I have a question about my order.", required = true)
    private String content;
    
    @NotBlank(message = "User ID is required")
    @Schema(description = "ID of the user sending the message", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private String userId;
    
    @NotNull(message = "Conversation ID is required")
    @Schema(description = "ID of the conversation this message belongs to", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    private String conversationId;

    @Schema(description = "Type of the message", example = "TEXT", defaultValue = "TEXT")
    private String messageType = "TEXT";

    @Schema(description = "URL of the image content if message type is IMAGE", example = "https://storage.example.com/images/abc123.jpg")
    private String contentUrl;
}