package com.hcmus.ecommerce_backend.message.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing conversation information")
public class ConversationResponse {
    
    @Schema(description = "Unique identifier of the conversation", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    
    @Schema(description = "ID of the customer in this conversation", example = "550e8400-e29b-41d4-a716-446655440000")
    private String customerId;
    
    @Schema(description = "Whether the admin has read the conversation", example = "true")
    private boolean isAdminRead;
    
    @Schema(description = "Whether the customer has read the conversation", example = "false")
    private boolean isCustomerRead;
    
    @Schema(description = "Messages in this conversation")
    private List<MessageResponse> messages;
    
    @Schema(description = "Date and time when the conversation was created")
    private LocalDateTime createdAt;
    
    @Schema(description = "User who created the conversation", example = "customer@example.com")
    private String createdBy;
    
    @Schema(description = "Date and time when the conversation was last updated")
    private LocalDateTime updatedAt;
    
    @Schema(description = "User who last updated the conversation", example = "admin@example.com")
    private String updatedBy;
}