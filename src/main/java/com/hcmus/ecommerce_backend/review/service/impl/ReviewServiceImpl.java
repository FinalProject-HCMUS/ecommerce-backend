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
        private final ReviewMapper reviewMapper;

        @Override
        public Page<ReviewResponse> getAllReviewsPaginated(Pageable pageable) {
            log.info(
                    "ReviewServiceImpl | getAllReviewsPaginated | Retrieving reviews with pagination - Page: {}, Size: {}, Sort: {}",
                    pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
            try {
                Page<Review> reviewPage = reviewRepository.findAll(pageable);
                Page<ReviewResponse> reviewResponsePage = reviewPage.map(reviewMapper::toResponse);

                log.info("ReviewServiceImpl | getAllReviewsPaginated | Found {} reviews on page {} of {}",
                        reviewResponsePage.getNumberOfElements(),
                        reviewResponsePage.getNumber() + 1,
                        reviewResponsePage.getTotalPages());

                return reviewResponsePage;
            } catch (DataAccessException e) {
                log.error("ReviewServiceImpl | getAllReviewsPaginated | Database error retrieving paginated reviews: {}",
                        e.getMessage(), e);
                throw e;
            } catch (Exception e) {
                log.error("ReviewServiceImpl | getAllReviewsPaginated | Unexpected error retrieving paginated reviews: {}",
                        e.getMessage(), e);
                throw e;
            }
        }

        @Override
        public Page<ReviewResponse> searchReviews(String keyword, Integer minRating, Integer maxRating, Pageable pageable) {
            log.info(
                    "ReviewServiceImpl | searchReviews | keyword: {}, minRating: {}, maxRating: {}, page: {}, size: {}, sort: {}",
                    keyword, minRating, maxRating, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

            try {
                // If all search parameters are null, use standard findAll method
                Page<Review> reviewPage;
                if ((keyword == null || keyword.trim().isEmpty()) && minRating == null && maxRating == null) {
                    reviewPage = reviewRepository.findAll(pageable);
                } else {
                    // Process keyword
                    String processedKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();

                    reviewPage = reviewRepository.searchReviews(
                            processedKeyword,
                            minRating,
                            maxRating,
                            pageable);
                }

                Page<ReviewResponse> reviewResponsePage = reviewPage.map(reviewMapper::toResponse);

                log.info("ReviewServiceImpl | searchReviews | Found {} reviews on page {} of {}",
                        reviewResponsePage.getNumberOfElements(),
                        reviewResponsePage.getNumber() + 1,
                        reviewResponsePage.getTotalPages());

                return reviewResponsePage;
            } catch (DataAccessException e) {
                log.error("ReviewServiceImpl | searchReviews | Database error searching reviews: {}", e.getMessage(), e);
                throw e;
            } catch (Exception e) {
                log.error("ReviewServiceImpl | searchReviews | Unexpected error searching reviews: {}", e.getMessage(), e);
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
        public Page<ReviewResponse> getReviewsByOrderDetailIdPaginated(String orderDetailId, Pageable pageable) {
            log.info("ReviewServiceImpl | getReviewsByOrderDetailIdPaginated | orderDetailId: {}, page: {}, size: {}, sort: {}",
                    orderDetailId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
            try {
                Page<Review> reviewPage = reviewRepository.findByOrderDetailId(orderDetailId, pageable);
                Page<ReviewResponse> reviewResponsePage = reviewPage.map(reviewMapper::toResponse);

                log.info(
                        "ReviewServiceImpl | getReviewsByOrderDetailIdPaginated | Found {} reviews on page {} of {} for order detail {}",
                        reviewResponsePage.getNumberOfElements(),
                        reviewResponsePage.getNumber() + 1,
                        reviewResponsePage.getTotalPages(),
                        orderDetailId);

                return reviewResponsePage;
            } catch (DataAccessException e) {
                log.error("ReviewServiceImpl | getReviewsByOrderDetailIdPaginated | Database error for orderDetailId {}: {}",
                        orderDetailId, e.getMessage(), e);
                throw e;
            } catch (Exception e) {
                log.error("ReviewServiceImpl | getReviewsByOrderDetailIdPaginated | Unexpected error for orderDetailId {}: {}",
                        orderDetailId, e.getMessage(), e);
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
                // If all search parameters are null, use standard findByOrderDetailId method
                Page<Review> reviewPage;
                if ((keyword == null || keyword.trim().isEmpty()) && minRating == null && maxRating == null) {
                    reviewPage = reviewRepository.findByOrderDetailId(orderDetailId, pageable);
                } else {
                    // Process keyword
                    String processedKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();

                    reviewPage = reviewRepository.searchReviewsByOrderDetailId(
                            orderDetailId,
                            processedKeyword,
                            minRating,
                            maxRating,
                            pageable);
                }

                Page<ReviewResponse> reviewResponsePage = reviewPage.map(reviewMapper::toResponse);

                log.info("ReviewServiceImpl | searchReviewsByOrderDetailId | Found {} reviews on page {} of {} for order detail {}",
                        reviewResponsePage.getNumberOfElements(),
                        reviewResponsePage.getNumber() + 1,
                        reviewResponsePage.getTotalPages(),
                        orderDetailId);

                return reviewResponsePage;
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
        protected Review findReviewById(String id) {
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
        protected boolean doesReviewExistById(String id) {
            return reviewRepository.existsById(id);
        }
    }