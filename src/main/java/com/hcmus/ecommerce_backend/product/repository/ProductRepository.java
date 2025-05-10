package com.hcmus.ecommerce_backend.product.repository;

import com.hcmus.ecommerce_backend.product.model.entity.Product;

import feign.Param;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {

    boolean existsByName(String name);

    @Query("SELECT p " +
            "FROM Product p " +
            "ORDER BY p.reviewCount DESC, p.averageRating DESC")
    List<Product> findTopTrendingProducts(Pageable pageable);

    @Query("SELECT p " +
            "FROM Product p " +
            "JOIN ProductColorSize pcs ON pcs.product = p " +
            "JOIN OrderDetail od ON od.itemId = pcs.id " +
            "GROUP BY p " +
            "ORDER BY SUM(od.quantity) DESC")
    List<Product> findTopSellingProducts(Pageable pageable);
    
    @Query(value = """
        SELECT p.id AS productId, p.name AS name, p.main_image_url AS imageUrl, 
               p.price AS price, SUM(od.quantity) AS quantitySold, 
               (SUM(od.quantity) * p.price) AS revenue
        FROM products p
        JOIN order_detail od ON p.id = od.product_id
        JOIN orders o ON od.order_id = o.id
        WHERE 
            o.delivery_date IS NOT NULL AND
            (
                (:type = 'month' AND TO_CHAR(o.delivery_date, 'MM-YYYY') = :date) OR
                (:type = 'year' AND TO_CHAR(o.delivery_date, 'YYYY') = :date)
            )
        GROUP BY p.id, p.name, p.main_image_url, p.price
        ORDER BY quantitySold DESC
        LIMIT 10
    """, nativeQuery = true)
    List<Map<String, Object>> findBestSellersByTypeAndDate(@Param("type") String type, @Param("date") String date);

    @Query(value = """
        SELECT SUM(p.price * od.quantity)
        FROM order_detail od
        JOIN product_color_sizes pcs ON od.item_id = pcs.id
        JOIN products p ON pcs.product_id = p.id
        WHERE EXTRACT(DAY FROM od.created_at) = :day
        AND TO_CHAR(od.created_at, 'MM-YYYY') = :monthYear
    """, nativeQuery = true)
    Double sumPriceQuantityByDayAndMonth(@Param("day") int day, @Param("monthYear") String monthYear);

    @Query(value = """
        SELECT SUM(p.price * od.quantity)
        FROM order_detail od
        JOIN product_color_sizes pcs ON od.item_id = pcs.id
        JOIN products p ON pcs.product_id = p.id
        WHERE EXTRACT(YEAR FROM od.created_at) = :year
    """, nativeQuery = true)
    Double sumPriceQuantityByYear(@Param("year") int year);

    @Query(value = """
        SELECT SUM(p.price * od.quantity)
        FROM order_detail od
        JOIN product_color_sizes pcs ON od.item_id = pcs.id
        JOIN products p ON pcs.product_id = p.id
        WHERE EXTRACT(MONTH FROM od.created_at) = :month
        AND EXTRACT(YEAR FROM od.created_at) = :year
    """, nativeQuery = true)
    Double sumPriceQuantityByMonthAndYear(@Param("month") int month, @Param("year") int year);
}