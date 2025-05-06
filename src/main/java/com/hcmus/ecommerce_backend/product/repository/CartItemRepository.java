package com.hcmus.ecommerce_backend.product.repository;

import com.hcmus.ecommerce_backend.product.model.entity.CartItem;

import feign.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {

    List<CartItem> findByUserId(String userId);

    Optional<CartItem> findByUserIdAndItemId(String userId, String itemId);

    boolean existsByUserIdAndItemId(String userId, String itemId);

    void deleteByUserIdAndItemId(String userId, String itemId);

    @Query(value = """
    SELECT 
        json_build_object(
            'productId', p.id,
            'productName', p.name,
            'productDescription', p.description,
            'productCost', p.cost,
            'productTotal', p.total,
            'productPrice', p.price,
            'productDiscountPercent', p.discount_percent,
            'productEnable', p.enable,
            'productInStock', p.in_stock,
            'productMainImageUrl', p.main_image_url,
            'productAverageRating', p.average_rating,
            'productReviewCount', p.review_count,
            'productCategoryId', p.category_id,
            'productCategoryName', c.name,
            'productCreatedAt', p.created_at,
            'productUpdatedAt', p.updated_at,
            'productCreatedBy', p.created_by,
            'productUpdatedBy', p.updated_by
        ) AS product,
        ci.id AS id,
        co.code AS color,
        s.name AS size,
        ci.quantity AS quantity,
        ci.user_id AS userId,
        ci.item_id AS itemId
    FROM cart_items ci
    JOIN product_color_sizes pcs ON ci.item_id = pcs.id
    JOIN colors co ON pcs.color_id = co.id
    JOIN sizes s ON pcs.size_id = s.id
    JOIN products p ON pcs.product_id = p.id
    JOIN categories c ON p.category_id = c.id
    WHERE ci.user_id = :userId
    """, nativeQuery = true)
    List<Map<String, Object>> findCartItemWithProductByUserId(@Param("userId") String userId);
}