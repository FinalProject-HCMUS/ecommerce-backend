package com.hcmus.ecommerce_backend.review.service;

import com.hcmus.ecommerce_backend.review.model.dto.request.CreateReviewRequest;
import com.hcmus.ecommerce_backend.review.model.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {
    
    List<ReviewResponse> getAllReviews();
    
    ReviewResponse getReviewById(String id);
    
    List<ReviewResponse> getReviewsByOrderId(String orderId);
    
    ReviewResponse createReview(CreateReviewRequest request);
    
    void deleteReview(String id);
    
    List<ReviewResponse> getReviewsByRatingRange(Integer minRating, Integer maxRating);
}