package com.hcmus.ecommerce_backend.message.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for updating an existing conversation")
public class UpdateConversationRequest {
    
    @Schema(description = "Whether the admin has read the conversation", example = "true")
    private Boolean isAdminRead;
    
    @Schema(description = "Whether the customer has read the conversation", example = "false")
    private Boolean isCustomerRead;
}