package com.hcmus.ecommerce_backend.review.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Schema(description = "Request for creating a new review")
public class CreateReviewRequest {
    
    @NotBlank(message = "Comment is required")
    @Schema(description = "Comment about the product/order", example = "Great quality product, arrived earlier than expected!")
    private String comment;
    
    @NotBlank(message = "Headline is required")
    @Schema(description = "Brief headline or summary of the review", example = "Excellent product!")
    private String headline;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    @Schema(description = "Rating from 1 to 5", example = "5")
    private Integer rating;
    
    @NotBlank(message = "Order ID is required")
    @Schema(description = "ID of the order this review is for", example = "550e8400-e29b-41d4-a716-446655440000")
    private String orderId;
}