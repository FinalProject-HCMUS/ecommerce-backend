package com.hcmus.ecommerce_backend.review.service.impl;

import com.hcmus.ecommerce_backend.order.exception.OrderDetailNotFoundException;
import com.hcmus.ecommerce_backend.order.model.entity.OrderDetail;
import com.hcmus.ecommerce_backend.order.model.mapper.OrderDetailMapper;
import com.hcmus.ecommerce_backend.order.repository.OrderDetailRepository;
import com.hcmus.ecommerce_backend.product.exception.ProductColorSizeNotFoundException;
import com.hcmus.ecommerce_backend.product.model.entity.Color;
import com.hcmus.ecommerce_backend.product.model.entity.Product;
import com.hcmus.ecommerce_backend.product.model.entity.ProductColorSize;
import com.hcmus.ecommerce_backend.product.model.entity.Size;
import com.hcmus.ecommerce_backend.product.model.mapper.ColorMapper;
import com.hcmus.ecommerce_backend.product.model.mapper.SizeMapper;
import com.hcmus.ecommerce_backend.product.repository.ProductColorSizeRepository;
import com.hcmus.ecommerce_backend.product.repository.ProductRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final OrderDetailRepository orderDetailRepository;
    private final ProductColorSizeRepository productColorSizeRepository;
    private final ProductRepository productRepository;
    private final ReviewMapper reviewMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final ColorMapper colorMapper;
    private final SizeMapper sizeMapper;

    @Override
    public Page<ReviewResponse> searchReviews(String keyword, Integer minRating, Integer maxRating, String productId, Pageable pageable) {
        log.info(
                "ReviewServiceImpl | searchReviews | keyword: {}, minRating: {}, maxRating: {}, productId: {}, page: {}, size: {}, sort: {}",
                keyword, minRating, maxRating, productId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        try {
            // Process keyword
            String processedKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();

            Page<Object[]> results = reviewRepository.findReviewsWithDetails(
                    processedKeyword, minRating, maxRating, null, productId, pageable);

            return results.map(this::mapToFullReviewResponse);
        } catch (DataAccessException e) {
            log.error("ReviewServiceImpl | searchReviews | Database error searching reviews: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ReviewServiceImpl | searchReviews | Unexpected error searching reviews: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Page<ReviewResponse> searchReviewsByOrderDetailId(String orderDetailId, String keyword, Integer minRating,
            Integer maxRating, Pageable pageable) {
        log.info(
                "ReviewServiceImpl | searchReviewsByOrderDetailId | orderDetailId: {}, keyword: {}, minRating: {}, maxRating: {}, page: {}, size: {}, sort: {}",
                orderDetailId, keyword, minRating, maxRating, pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSort());

        try {
            // Process keyword
            String processedKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();

            Page<Object[]> results = reviewRepository.findReviewsWithDetails(
                    processedKeyword, minRating, maxRating, orderDetailId, null, pageable);

            return results.map(this::mapToFullReviewResponse);
        } catch (DataAccessException e) {
            log.error(
                    "ReviewServiceImpl | searchReviewsByOrderDetailId | Database error searching reviews for orderDetailId {}: {}",
                    orderDetailId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(
                    "ReviewServiceImpl | searchReviewsByOrderDetailId | Unexpected error searching reviews for orderDetailId {}: {}",
                    orderDetailId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public ReviewResponse getReviewById(String id) {
        log.info("ReviewServiceImpl | getReviewById | id: {}", id);
        try {
            Object[] result = reviewRepository.findReviewsWithDetails(null, null, null, null, null, Pageable.unpaged())
                    .stream()
                    .filter(row -> ((Review)row[0]).getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new ReviewNotFoundException(id));

            return mapToFullReviewResponse(result);
        } catch (ReviewNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("ReviewServiceImpl | getReviewById | Database error for id {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ReviewServiceImpl | getReviewById | Unexpected error for id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }


    // Helper method to map the join query result to a full ReviewResponse
    private ReviewResponse mapToFullReviewResponse(Object[] row) {
        Review review = (Review) row[0];
        OrderDetail orderDetail = (OrderDetail) row[1];
        ProductColorSize productColorSize = (ProductColorSize) row[2];
        Color color = (Color) row[3];
        Size size = (Size) row[4];

        // Create a base review response from the review entity
        ReviewResponse response = reviewMapper.toResponse(review);

        // Add related entity responses
        response.setOrderDetail(orderDetailMapper.toResponse(orderDetail));
        response.setColor(colorMapper.toResponse(color));
        response.setSize(sizeMapper.toResponse(size));

        return response;
    }

    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {
        log.info("ReviewServiceImpl | createReview | Creating review for order detail: {}", request.getOrderDetailId());
        try {
            // Check if a review for this order detail already exists
            if (reviewRepository.existsByOrderDetailId(request.getOrderDetailId())) {
                log.warn("ReviewServiceImpl | createReview | A review already exists for order detail: {}",
                        request.getOrderDetailId());
                throw new ReviewAlreadyExistsException(request.getOrderDetailId());
            }

            Review review = reviewMapper.toEntity(request);
            Review savedReview = reviewRepository.save(review);
            
            // Update the order detail to mark it as reviewed
            OrderDetail orderDetail = orderDetailRepository.findById(request.getOrderDetailId())
                    .orElseThrow(() -> new OrderDetailNotFoundException(request.getOrderDetailId()));
            orderDetail.setReviewed(true);
            orderDetailRepository.save(orderDetail);

            // Update product review count and average rating
            ProductColorSize productColorSize = productColorSizeRepository.findById(orderDetail.getItemId())
                    .orElseThrow(() -> new ProductColorSizeNotFoundException(orderDetail.getItemId()));
            Product product = productColorSize.getProduct();
            product.setReviewCount(product.getReviewCount() + 1);
            if (product.getReviewCount() > 0) {
                double newAverageRating = ((product.getAverageRating() * (product.getReviewCount() - 1)) + review.getRating()) / product.getReviewCount();
                product.setAverageRating(newAverageRating);
            } else {
                product.setAverageRating(0.0); // Nếu không còn review, đặt averageRating về 0
            }
            productRepository.save(product);

            log.info("ReviewServiceImpl | createReview | Created review with id: {} for order detail: {}",
                    savedReview.getId(), savedReview.getOrderDetailId());
            return reviewMapper.toResponse(savedReview);
        } catch (ReviewAlreadyExistsException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("ReviewServiceImpl | createReview | Database error creating review for order detail {}: {}",
                    request.getOrderDetailId(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ReviewServiceImpl | createReview | Unexpected error creating review for order detail {}: {}",
                    request.getOrderDetailId(), e.getMessage(), e);
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
            
            Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException(id));

            // Update the order detail to mark it as reviewed
            OrderDetail orderDetail = orderDetailRepository.findById(review.getOrderDetailId())
                    .orElseThrow(() -> new OrderDetailNotFoundException(review.getOrderDetailId()));
            orderDetail.setReviewed(false);
            orderDetailRepository.save(orderDetail);

            // Update product review count and average rating
            ProductColorSize productColorSize = productColorSizeRepository.findById(orderDetail.getItemId())
                    .orElseThrow(() -> new ProductColorSizeNotFoundException(orderDetail.getItemId()));
            Product product = productColorSize.getProduct();
            product.setReviewCount(product.getReviewCount() - 1);
            if (product.getReviewCount() > 0) {
                double newAverageRating = ((product.getAverageRating() * (product.getReviewCount() + 1)) - review.getRating()) / product.getReviewCount();
                product.setAverageRating(newAverageRating);
            } else {
                product.setAverageRating(0.0); 
            }
            productRepository.save(product);

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
            List<ReviewResponse> reviews = reviewRepository
                    .findByRatingBetweenOrderByCreatedAtDesc(minRating, maxRating)
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
    public Review findReviewById(String id) {
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
    public boolean doesReviewExistById(String id) {
        return reviewRepository.existsById(id);
    }
}