package com.hcmus.ecommerce_backend.review.repository;

import com.hcmus.ecommerce_backend.review.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    
    List<Review> findByOrderId(String orderId);
    
    boolean existsByOrderId(String orderId);
    
    Optional<Review> findByOrderIdAndRatingGreaterThan(String orderId, Integer rating);
    
    List<Review> findByRatingBetweenOrderByReviewTimeDesc(Integer minRating, Integer maxRating);
}