package com.hcmus.ecommerce_backend.review.repository;

    import com.hcmus.ecommerce_backend.review.model.entity.Review;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;
    import org.springframework.stereotype.Repository;

    import java.util.List;
    import java.util.Optional;

    @Repository
    public interface ReviewRepository extends JpaRepository<Review, String> {

        List<Review> findByOrderDetailId(String orderDetailId);

        Page<Review> findByOrderDetailId(String orderDetailId, Pageable pageable);

        boolean existsByOrderDetailId(String orderDetailId);

        Optional<Review> findByOrderDetailIdAndRatingGreaterThan(String orderDetailId, Integer rating);

        List<Review> findByRatingBetweenOrderByCreatedAtDesc(Integer minRating, Integer maxRating);

        @Query("SELECT r FROM Review r WHERE " +
                "(:keyword IS NULL OR " +
                "LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                "LOWER(r.headline) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                "AND (:minRating IS NULL OR r.rating >= :minRating) " +
                "AND (:maxRating IS NULL OR r.rating <= :maxRating)")
        Page<Review> searchReviews(
                @Param("keyword") String keyword,
                @Param("minRating") Integer minRating,
                @Param("maxRating") Integer maxRating,
                Pageable pageable);

        @Query("SELECT r FROM Review r WHERE r.orderDetailId = :orderDetailId AND " +
                "(:keyword IS NULL OR " +
                "LOWER(r.comment) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                "LOWER(r.headline) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                "AND (:minRating IS NULL OR r.rating >= :minRating) " +
                "AND (:maxRating IS NULL OR r.rating <= :maxRating)")
        Page<Review> searchReviewsByOrderDetailId(
                @Param("orderDetailId") String orderDetailId,
                @Param("keyword") String keyword,
                @Param("minRating") Integer minRating,
                @Param("maxRating") Integer maxRating,
                Pageable pageable);
    }