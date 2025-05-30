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

    Page<Review> findByOrderDetailId(String orderDetailId, Pageable pageable);

    boolean existsByOrderDetailId(String orderDetailId);

    Optional<Review> findByOrderDetailIdAndRatingGreaterThan(String orderDetailId, Integer rating);

    List<Review> findByRatingBetweenOrderByCreatedAtDesc(Integer minRating, Integer maxRating);

    @Query("SELECT r FROM Review r WHERE " +
            "(:keyword IS NULL OR " +
            "LOWER(r.comment) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR " +
            "LOWER(r.headline) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))) " +
            "AND (:minRating IS NULL OR r.rating >= :minRating) " +
            "AND (:maxRating IS NULL OR r.rating <= :maxRating)")
    Page<Review> searchReviews(
            @Param("keyword") String keyword,
            @Param("minRating") Integer minRating,
            @Param("maxRating") Integer maxRating,
            Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.orderDetailId = :orderDetailId AND " +
            "(:keyword IS NULL OR " +
            "LOWER(r.comment) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR " +
            "LOWER(r.headline) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))) " +
            "AND (:minRating IS NULL OR r.rating >= :minRating) " +
            "AND (:maxRating IS NULL OR r.rating <= :maxRating)")
    Page<Review> searchReviewsByOrderDetailId(
            @Param("orderDetailId") String orderDetailId,
            @Param("keyword") String keyword,
            @Param("minRating") Integer minRating,
            @Param("maxRating") Integer maxRating,
            Pageable pageable);

    @Query("SELECT r, od, pcs, c, s FROM Review r " +
                    "JOIN OrderDetail od ON r.orderDetailId = od.id " +
                    "JOIN ProductColorSize pcs ON od.itemId = pcs.id " +
                    "JOIN Color c ON pcs.color.id = c.id " +
                    "JOIN Size s ON pcs.size.id = s.id " +
                    "WHERE " +
                    "(:keyword IS NULL OR " +
                    "LOWER(r.comment) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR " +
                    "LOWER(r.headline) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))) " +
                    "AND (:minRating IS NULL OR r.rating >= :minRating) " +
                    "AND (:maxRating IS NULL OR r.rating <= :maxRating) " +
                    "AND (:orderDetailId IS NULL OR r.orderDetailId = :orderDetailId) " +
                    "AND (:productId IS NULL OR pcs.product.id = :productId)")
            Page<Object[]> findReviewsWithDetails(
                    @Param("keyword") String keyword,
                    @Param("minRating") Integer minRating,
                    @Param("maxRating") Integer maxRating,
                    @Param("orderDetailId") String orderDetailId,
                    @Param("productId") String productId,
                    Pageable pageable);
}