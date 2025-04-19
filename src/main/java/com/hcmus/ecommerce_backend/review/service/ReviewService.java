package com.hcmus.ecommerce_backend.review.service;

import com.hcmus.ecommerce_backend.review.model.dto.request.CreateReviewRequest;
import com.hcmus.ecommerce_backend.review.model.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {
    
    Page<ReviewResponse> getAllReviewsPaginated(Pageable pageable);

    Page<ReviewResponse> searchReviews(String keyword, Integer minRating, Integer maxRating, Pageable pageable);
    
    ReviewResponse getReviewById(String id);
    
    Page<ReviewResponse> getReviewsByOrderIdPaginated(String orderId, Pageable pageable);
    
    ReviewResponse createReview(CreateReviewRequest request);
    
    void deleteReview(String id);
    
    List<ReviewResponse> getReviewsByRatingRange(Integer minRating, Integer maxRating);

    Page<ReviewResponse> searchReviewsByOrderId(String orderId, String keyword, Integer minRating, Integer maxRating, Pageable pageable);
}