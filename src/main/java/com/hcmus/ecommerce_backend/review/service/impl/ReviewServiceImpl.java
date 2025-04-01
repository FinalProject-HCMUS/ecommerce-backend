package com.hcmus.ecommerce_backend.review.service.impl;

import com.hcmus.ecommerce_backend.review.exception.ReviewAlreadyExistsException;
import com.hcmus.ecommerce_backend.review.exception.ReviewNotFoundException;
import com.hcmus.ecommerce_backend.review.model.dto.request.CreateReviewRequest;
import com.hcmus.ecommerce_backend.review.model.dto.response.ReviewResponse;
import com.hcmus.ecommerce_backend.review.model.entity.Review;
import com.hcmus.ecommerce_backend.review.model.mapper.ReviewMapper;
import com.hcmus.ecommerce_backend.review.repository.ReviewRepository;
import com.hcmus.ecommerce_backend.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    
    @Override
    public List<ReviewResponse> getAllReviews() {
        log.info("ReviewServiceImpl | getAllReviews | Retrieving all reviews");
        try {
            List<ReviewResponse> reviews = reviewRepository.findAll().stream()
                    .map(reviewMapper::toResponse)
                    .collect(Collectors.toList());
            log.info("ReviewServiceImpl | getAllReviews | Found {} reviews", reviews.size());
            return reviews;
        } catch (DataAccessException e) {
            log.error("ReviewServiceImpl | getAllReviews | Error retrieving reviews: {}", e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("ReviewServiceImpl | getAllReviews | Unexpected error: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public ReviewResponse getReviewById(String id) {
        log.info("ReviewServiceImpl | getReviewById | id: {}", id);
        try {
            Review review = findReviewById(id);
            log.info("ReviewServiceImpl | getReviewById | Review found with id: {}", review.getId());
            return reviewMapper.toResponse(review);
        } catch (ReviewNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("ReviewServiceImpl | getReviewById | Database error for id {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ReviewServiceImpl | getReviewById | Unexpected error for id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public List<ReviewResponse> getReviewsByOrderId(String orderId) {
        log.info("ReviewServiceImpl | getReviewsByOrderId | orderId: {}", orderId);
        try {
            List<ReviewResponse> reviews = reviewRepository.findByOrderId(orderId).stream()
                    .map(reviewMapper::toResponse)
                    .collect(Collectors.toList());
            log.info("ReviewServiceImpl | getReviewsByOrderId | Found {} reviews for order {}", reviews.size(), orderId);
            return reviews;
        } catch (DataAccessException e) {
            log.error("ReviewServiceImpl | getReviewsByOrderId | Database error for orderId {}: {}", orderId, e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("ReviewServiceImpl | getReviewsByOrderId | Unexpected error for orderId {}: {}", orderId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {
        log.info("ReviewServiceImpl | createReview | Creating review for order: {}", request.getOrderId());
        try {
            // Check if a review for this order already exists
            if (reviewRepository.existsByOrderId(request.getOrderId())) {
                log.warn("ReviewServiceImpl | createReview | A review already exists for order: {}", request.getOrderId());
                throw new ReviewAlreadyExistsException(request.getOrderId());
            }
            
            Review review = reviewMapper.toEntity(request);
            Review savedReview = reviewRepository.save(review);
            log.info("ReviewServiceImpl | createReview | Created review with id: {} for order: {}", 
                    savedReview.getId(), savedReview.getOrderId());
            return reviewMapper.toResponse(savedReview);
        } catch (ReviewAlreadyExistsException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("ReviewServiceImpl | createReview | Database error creating review for order {}: {}", 
                    request.getOrderId(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ReviewServiceImpl | createReview | Unexpected error creating review for order {}: {}", 
                    request.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void deleteReview(String id) {
        log.info("ReviewServiceImpl | deleteReview | Deleting review with id: {}", id);
        try {
            // Check existence first in a separate transaction
            if (!doesReviewExistById(id)) {
                log.error("ReviewServiceImpl | deleteReview | Review not found with id: {}", id);
                throw new ReviewNotFoundException(id);
            }
            
            // Then delete in the current transaction
            reviewRepository.deleteById(id);
            log.info("ReviewServiceImpl | deleteReview | Deleted review with id: {}", id);
        } catch (ReviewNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("ReviewServiceImpl | deleteReview | Database error deleting review with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ReviewServiceImpl | deleteReview | Unexpected error deleting review with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public List<ReviewResponse> getReviewsByRatingRange(Integer minRating, Integer maxRating) {
        log.info("ReviewServiceImpl | getReviewsByRatingRange | minRating: {}, maxRating: {}", minRating, maxRating);
        try {
            List<ReviewResponse> reviews = reviewRepository.findByRatingBetweenOrderByCreatedAtDesc(minRating, maxRating)
                    .stream()
                    .map(reviewMapper::toResponse)
                    .collect(Collectors.toList());
            log.info("ReviewServiceImpl | getReviewsByRatingRange | Found {} reviews with rating between {} and {}", 
                    reviews.size(), minRating, maxRating);
            return reviews;
        } catch (DataAccessException e) {
            log.error("ReviewServiceImpl | getReviewsByRatingRange | Database error for rating range {} to {}: {}", 
                    minRating, maxRating, e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("ReviewServiceImpl | getReviewsByRatingRange | Unexpected error for rating range {} to {}: {}", 
                    minRating, maxRating, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Helper method to find a review by ID.
     * Uses a separate transaction to avoid issues with the main transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private Review findReviewById(String id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("ReviewServiceImpl | findReviewById | Review not found with id: {}", id);
                    return new ReviewNotFoundException(id);
                });
    }
    
    /**
     * Helper method to check if a review exists by ID.
     * Uses a separate transaction to avoid issues with the main transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private boolean doesReviewExistById(String id) {
        return reviewRepository.existsById(id);
    }
}