package com.hcmus.ecommerce_backend.order.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.hcmus.ecommerce_backend.order.model.entity.OrderDetail;

import feign.Param;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, String> {

    List<OrderDetail> findByOrderId(String orderId);

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
            'productUpdatedBy', p.updated_by,
            'colorId', col.id,
            'colorName', col.name,
            'sizeId', s.id,
            'sizeName', s.name
        ) AS product,
        od.id AS id,
        od.product_cost AS productCost,
        od.quantity AS quantity,
        od.unit_price AS unitPrice,
        od.total AS total,
        od.item_id AS itemId,
        od.order_id AS orderId,
        od.created_at AS createdAt,
        od.created_by AS createdBy,
        od.updated_at AS updatedAt,
        od.updated_by AS updatedBy
    FROM order_detail od
    JOIN product_color_sizes pcs ON od.item_id = pcs.id
    JOIN products p ON pcs.product_id = p.id
    JOIN categories c ON p.category_id = c.id
    JOIN colors col ON pcs.color_id = col.id
    JOIN sizes s ON pcs.size_id = s.id
    WHERE od.order_id = :orderId
    """, nativeQuery = true)
    List<Map<String, Object>> findOrderDetailsWithProductByOrderId(@org.springframework.data.repository.query.Param("orderId") String orderId);
}
