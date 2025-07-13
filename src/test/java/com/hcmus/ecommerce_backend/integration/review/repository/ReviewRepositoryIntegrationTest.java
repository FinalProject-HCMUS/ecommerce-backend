package com.hcmus.ecommerce_backend.integration.review.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import com.hcmus.ecommerce_backend.review.model.entity.Review;
import com.hcmus.ecommerce_backend.review.repository.ReviewRepository;

@DataJpaTest
@ActiveProfiles("test")
class ReviewRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReviewRepository reviewRepository;

    private Review testReview1;
    private Review testReview2;
    private Review testReview3;

    @BeforeEach
    void setUp() {
        // Create test reviews
        testReview1 = Review.builder()
                .comment("Great product! Very satisfied with the quality.")
                .headline("Excellent quality")
                .rating(5)
                .orderDetailId("order-detail-1")
                .userName("John Doe")
                .build();
        testReview1.setCreatedAt(LocalDateTime.now().minusDays(3));
        testReview1.setUpdatedAt(LocalDateTime.now().minusDays(3));

        testReview2 = Review.builder()
                .comment("Good product but delivery was slow.")
                .headline("Good but slow delivery")
                .rating(3)
                .orderDetailId("order-detail-2")
                .userName("Jane Smith")
                .build();
        testReview2.setCreatedAt(LocalDateTime.now().minusDays(2));
        testReview2.setUpdatedAt(LocalDateTime.now().minusDays(2));

        testReview3 = Review.builder()
                .comment("Average product, nothing special.")
                .headline("Average quality")
                .rating(2)
                .orderDetailId("order-detail-3")
                .userName("Bob Johnson")
                .build();
        testReview3.setCreatedAt(LocalDateTime.now().minusDays(1));
        testReview3.setUpdatedAt(LocalDateTime.now().minusDays(1));
    }

    // Test JpaRepository inherited methods
    @Test
    void save_WithValidReview_ShouldPersistReview() {
        // Act
        Review savedReview = reviewRepository.save(testReview1);

        // Assert
        assertNotNull(savedReview.getId());
        assertEquals(testReview1.getComment(), savedReview.getComment());
        assertEquals(testReview1.getHeadline(), savedReview.getHeadline());
        assertEquals(testReview1.getRating(), savedReview.getRating());
        assertEquals(testReview1.getOrderDetailId(), savedReview.getOrderDetailId());
        assertEquals(testReview1.getUserName(), savedReview.getUserName());
        assertNotNull(savedReview.getCreatedAt());
        assertNotNull(savedReview.getUpdatedAt());

        // Verify persistence
        entityManager.flush();
        Review foundReview = entityManager.find(Review.class, savedReview.getId());
        assertNotNull(foundReview);
        assertEquals(savedReview.getComment(), foundReview.getComment());
    }

    @Test
    void findById_WithExistingId_ShouldReturnReview() {
        // Arrange
        Review savedReview = entityManager.persistAndFlush(testReview1);

        // Act
        Optional<Review> foundReview = reviewRepository.findById(savedReview.getId());

        // Assert
        assertTrue(foundReview.isPresent());
        assertEquals(savedReview.getId(), foundReview.get().getId());
        assertEquals(savedReview.getComment(), foundReview.get().getComment());
        assertEquals(savedReview.getRating(), foundReview.get().getRating());
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // Act
        Optional<Review> foundReview = reviewRepository.findById("non-existing-id");

        // Assert
        assertFalse(foundReview.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllReviews() {
        // Arrange
        entityManager.persistAndFlush(testReview1);
        entityManager.persistAndFlush(testReview2);
        entityManager.persistAndFlush(testReview3);

        // Act
        List<Review> reviews = reviewRepository.findAll();

        // Assert
        assertEquals(3, reviews.size());
        assertTrue(reviews.stream().anyMatch(r -> r.getComment().equals(testReview1.getComment())));
        assertTrue(reviews.stream().anyMatch(r -> r.getComment().equals(testReview2.getComment())));
        assertTrue(reviews.stream().anyMatch(r -> r.getComment().equals(testReview3.getComment())));
    }

    @Test
    void findAll_WithPageable_ShouldReturnPagedResults() {
        // Arrange
        entityManager.persistAndFlush(testReview1);
        entityManager.persistAndFlush(testReview2);
        entityManager.persistAndFlush(testReview3);

        Pageable pageable = PageRequest.of(0, 2, Sort.by("createdAt").descending());

        // Act
        Page<Review> reviews = reviewRepository.findAll(pageable);

        // Assert
        assertEquals(2, reviews.getContent().size());
        assertEquals(3, reviews.getTotalElements());
        assertEquals(2, reviews.getTotalPages());
        assertEquals(0, reviews.getNumber());
        assertTrue(reviews.hasNext());
    }

    @Test
    void delete_WithExistingReview_ShouldRemoveReview() {
        // Arrange
        Review savedReview = entityManager.persistAndFlush(testReview1);

        // Act
        reviewRepository.delete(savedReview);
        entityManager.flush();

        // Assert
        Review deletedReview = entityManager.find(Review.class, savedReview.getId());
        assertNull(deletedReview);
        assertFalse(reviewRepository.existsById(savedReview.getId()));
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // Arrange
        entityManager.persistAndFlush(testReview1);
        entityManager.persistAndFlush(testReview2);

        // Act
        long count = reviewRepository.count();

        // Assert
        assertEquals(2, count);
    }

    @Test
    void existsById_WithExistingId_ShouldReturnTrue() {
        // Arrange
        Review savedReview = entityManager.persistAndFlush(testReview1);

        // Act
        boolean exists = reviewRepository.existsById(savedReview.getId());

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsById_WithNonExistingId_ShouldReturnFalse() {
        // Act
        boolean exists = reviewRepository.existsById("non-existing-id");

        // Assert
        assertFalse(exists);
    }

    // Test custom query methods
    @Test
    void findByOrderDetailId_WithExistingOrderDetailId_ShouldReturnReviews() {
        // Arrange
        entityManager.persistAndFlush(testReview1);
        entityManager.persistAndFlush(testReview2);
        
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Review> reviews = reviewRepository.findByOrderDetailId("order-detail-1", pageable);

        // Assert
        assertEquals(1, reviews.getContent().size());
        assertEquals(testReview1.getComment(), reviews.getContent().get(0).getComment());
    }

    @Test
    void findByOrderDetailId_WithNonExistingOrderDetailId_ShouldReturnEmpty() {
        // Arrange
        entityManager.persistAndFlush(testReview1);
        
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Review> reviews = reviewRepository.findByOrderDetailId("non-existing-order-detail", pageable);

        // Assert
        assertTrue(reviews.isEmpty());
    }

    @Test
    void existsByOrderDetailId_WithExistingOrderDetailId_ShouldReturnTrue() {
        // Arrange
        entityManager.persistAndFlush(testReview1);

        // Act
        boolean exists = reviewRepository.existsByOrderDetailId("order-detail-1");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByOrderDetailId_WithNonExistingOrderDetailId_ShouldReturnFalse() {
        // Act
        boolean exists = reviewRepository.existsByOrderDetailId("non-existing-order-detail");

        // Assert
        assertFalse(exists);
    }

    @Test
    void findByOrderDetailIdAndRatingGreaterThan_WithMatchingCriteria_ShouldReturnReview() {
        // Arrange
        entityManager.persistAndFlush(testReview1); // rating 5
        entityManager.persistAndFlush(testReview2); // rating 3

        // Act
        Optional<Review> review = reviewRepository.findByOrderDetailIdAndRatingGreaterThan("order-detail-1", 4);

        // Assert
        assertTrue(review.isPresent());
        assertEquals(testReview1.getComment(), review.get().getComment());
        assertEquals(5, review.get().getRating());
    }

    @Test
    void findByOrderDetailIdAndRatingGreaterThan_WithNonMatchingCriteria_ShouldReturnEmpty() {
        // Arrange
        entityManager.persistAndFlush(testReview2); // rating 3

        // Act
        Optional<Review> review = reviewRepository.findByOrderDetailIdAndRatingGreaterThan("order-detail-2", 4);

        // Assert
        assertFalse(review.isPresent());
    }

    @Test
    void findByRatingBetweenOrderByCreatedAtDesc_WithValidRange_ShouldReturnFilteredReviews() {
        // Arrange
        entityManager.persistAndFlush(testReview1); // rating 5
        entityManager.persistAndFlush(testReview2); // rating 3
        entityManager.persistAndFlush(testReview3); // rating 2

        // Act
        List<Review> reviews = reviewRepository.findByRatingBetweenOrderByCreatedAtDesc(2, 4);

        // Assert
        assertEquals(2, reviews.size());
        assertTrue(reviews.stream().anyMatch(r -> r.getRating() == 3));
        assertTrue(reviews.stream().anyMatch(r -> r.getRating() == 2));
        assertFalse(reviews.stream().anyMatch(r -> r.getRating() == 5));
        
        // Check ordering (desc by createdAt)
        assertTrue(reviews.get(0).getCreatedAt().isAfter(reviews.get(1).getCreatedAt()));
    }

    @Test
    void findByRatingBetweenOrderByCreatedAtDesc_WithNoMatchingRatings_ShouldReturnEmptyList() {
        // Arrange
        entityManager.persistAndFlush(testReview1); // rating 5
        entityManager.persistAndFlush(testReview2); // rating 3

        // Act
        List<Review> reviews = reviewRepository.findByRatingBetweenOrderByCreatedAtDesc(1, 1);

        // Assert
        assertTrue(reviews.isEmpty());
    }

    @Test
    void searchReviews_WithKeyword_ShouldReturnMatchingReviews() {
        // Arrange
        entityManager.persistAndFlush(testReview1); // comment contains "Great"
        entityManager.persistAndFlush(testReview2); // comment contains "Good"
        entityManager.persistAndFlush(testReview3); // comment contains "Average"

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Review> reviews = reviewRepository.searchReviews("Great", null, null, pageable);

        // Assert
        assertEquals(1, reviews.getContent().size());
        assertEquals(testReview1.getComment(), reviews.getContent().get(0).getComment());
    }

    @Test
    void searchReviews_WithRatingRange_ShouldReturnMatchingReviews() {
        // Arrange
        entityManager.persistAndFlush(testReview1); // rating 5
        entityManager.persistAndFlush(testReview2); // rating 3
        entityManager.persistAndFlush(testReview3); // rating 2

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Review> reviews = reviewRepository.searchReviews(null, 3, 5, pageable);

        // Assert
        assertEquals(2, reviews.getContent().size());
        assertTrue(reviews.getContent().stream().anyMatch(r -> r.getRating() == 5));
        assertTrue(reviews.getContent().stream().anyMatch(r -> r.getRating() == 3));
        assertFalse(reviews.getContent().stream().anyMatch(r -> r.getRating() == 2));
    }

    @Test
    void searchReviews_WithKeywordAndRatingRange_ShouldReturnMatchingReviews() {
        // Arrange
        entityManager.persistAndFlush(testReview1); // rating 5, comment "Great"
        entityManager.persistAndFlush(testReview2); // rating 3, comment "Good"
        entityManager.persistAndFlush(testReview3); // rating 2, comment "Average"

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Review> reviews = reviewRepository.searchReviews("Good", 3, 5, pageable);

        // Assert
        assertEquals(1, reviews.getContent().size());
        assertEquals(testReview2.getComment(), reviews.getContent().get(0).getComment());
        assertEquals(3, reviews.getContent().get(0).getRating());
    }

    @Test
    void searchReviews_WithNoMatches_ShouldReturnEmptyPage() {
        // Arrange
        entityManager.persistAndFlush(testReview1);
        entityManager.persistAndFlush(testReview2);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Review> reviews = reviewRepository.searchReviews("non-existing-keyword", null, null, pageable);

        // Assert
        assertTrue(reviews.isEmpty());
    }

    @Test
    void searchReviewsByOrderDetailId_WithValidOrderDetailId_ShouldReturnFilteredReviews() {
        // Arrange
        entityManager.persistAndFlush(testReview1); // order-detail-1
        entityManager.persistAndFlush(testReview2); // order-detail-2

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Review> reviews = reviewRepository.searchReviewsByOrderDetailId("order-detail-1", null, null, null, pageable);

        // Assert
        assertEquals(1, reviews.getContent().size());
        assertEquals(testReview1.getComment(), reviews.getContent().get(0).getComment());
    }

    @Test
    void searchReviewsByOrderDetailId_WithKeywordAndRating_ShouldReturnMatchingReviews() {
        // Arrange
        Review additionalReview = Review.builder()
                .comment("Great product from order detail 1")
                .headline("Great from order 1")
                .rating(4)
                .orderDetailId("order-detail-1")
                .userName("Test User")
                .build();
        additionalReview.setCreatedAt(LocalDateTime.now());
        additionalReview.setUpdatedAt(LocalDateTime.now());

        entityManager.persistAndFlush(testReview1); // order-detail-1, rating 5, comment "Great"
        entityManager.persistAndFlush(additionalReview); // order-detail-1, rating 4, comment "Great"
        entityManager.persistAndFlush(testReview2); // order-detail-2

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Review> reviews = reviewRepository.searchReviewsByOrderDetailId("order-detail-1", "Great", 4, 5, pageable);

        // Assert
        assertEquals(2, reviews.getContent().size());
        assertTrue(reviews.getContent().stream().allMatch(r -> r.getOrderDetailId().equals("order-detail-1")));
        assertTrue(reviews.getContent().stream().allMatch(r -> r.getComment().toLowerCase().contains("great")));
        assertTrue(reviews.getContent().stream().allMatch(r -> r.getRating() >= 4 && r.getRating() <= 5));
    }

    // Edge cases and error handling
    @Test
    void findAll_WithSortingByRatingDesc_ShouldReturnSortedReviews() {
        // Arrange
        entityManager.persistAndFlush(testReview1); // rating 5
        entityManager.persistAndFlush(testReview2); // rating 3
        entityManager.persistAndFlush(testReview3); // rating 2

        Sort sort = Sort.by("rating").descending();

        // Act
        List<Review> reviews = reviewRepository.findAll(sort);

        // Assert
        assertEquals(3, reviews.size());
        assertEquals(5, reviews.get(0).getRating());
        assertEquals(3, reviews.get(1).getRating());
        assertEquals(2, reviews.get(2).getRating());
    }

    @Test
    void findAll_WithSortingByCreatedAtAsc_ShouldReturnSortedReviews() {
        // Arrange
        entityManager.persistAndFlush(testReview1); // 3 days ago
        entityManager.persistAndFlush(testReview2); // 2 days ago
        entityManager.persistAndFlush(testReview3); // 1 day ago

        Sort sort = Sort.by("createdAt").ascending();

        // Act
        List<Review> reviews = reviewRepository.findAll(sort);

        // Assert
        assertEquals(3, reviews.size());
        assertTrue(reviews.get(0).getCreatedAt().isBefore(reviews.get(1).getCreatedAt()));
        assertTrue(reviews.get(1).getCreatedAt().isBefore(reviews.get(2).getCreatedAt()));
    }

    @Test
    void searchReviews_WithPagination_ShouldReturnCorrectPage() {
        // Arrange
        entityManager.persistAndFlush(testReview1);
        entityManager.persistAndFlush(testReview2);
        entityManager.persistAndFlush(testReview3);

        Pageable pageable = PageRequest.of(1, 2, Sort.by("createdAt").descending());

        // Act
        Page<Review> reviews = reviewRepository.searchReviews(null, null, null, pageable);

        // Assert
        assertEquals(1, reviews.getContent().size()); // Only 1 review on page 1 (0-based index)
        assertEquals(3, reviews.getTotalElements());
        assertEquals(2, reviews.getTotalPages());
        assertEquals(1, reviews.getNumber());
        assertFalse(reviews.hasNext());
        assertTrue(reviews.hasPrevious());
    }
}