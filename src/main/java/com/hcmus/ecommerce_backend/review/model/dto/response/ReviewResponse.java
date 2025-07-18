package com.hcmus.ecommerce_backend.review.model.dto.response;

import com.hcmus.ecommerce_backend.order.model.dto.response.OrderDetailResponse;
import com.hcmus.ecommerce_backend.product.model.dto.response.ColorResponse;
import com.hcmus.ecommerce_backend.product.model.dto.response.SizeResponse;
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
@Schema(description = "Response object containing review information with related entities")
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
    private LocalDateTime createdAt;

    @Schema(description = "Time when the review was last updated", example = "2023-05-16T10:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "ID of the order detail this review is for", example = "550e8400-e29b-41d4-a716-446655440000")
    private String orderDetailId;

    @Schema(description = "Name of the user who wrote the review", example = "John Doe")
    private String userName;

    @Schema(description = "Order detail information")
    private OrderDetailResponse orderDetail;

    @Schema(description = "Color information")
    private ColorResponse color;

    @Schema(description = "Size information")
    private SizeResponse size;
}