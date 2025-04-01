package com.hcmus.ecommerce_backend.review.model.dto.response;

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
@Schema(description = "Response object containing review information")
public class ReviewResponse {
    
    @Schema(description = "Unique identifier of the review", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    
    @Schema(description = "Comment about the product/order", example = "Great quality product, arrived earlier than expected!")
    private String comment;
    
    @Schema(description = "Brief headline or summary of the review", example = "Excellent product!")
    private String headline;
    
    @Schema(description = "Rating from 1 to 5", example = "5")
    private Integer rating;
    
    @Schema(description = "Time when the review was submitted", example = "2023-05-15T15:30:00")
    private LocalDateTime reviewTime;
    
    @Schema(description = "ID of the order this review is for", example = "550e8400-e29b-41d4-a716-446655440000")
    private String orderId;
}