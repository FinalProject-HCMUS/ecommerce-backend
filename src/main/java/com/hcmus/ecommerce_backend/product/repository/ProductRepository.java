package com.hcmus.ecommerce_backend.product.repository;

import com.hcmus.ecommerce_backend.product.model.entity.Product;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    // boolean existsByName(String name);

    // @Query("SELECT p.id, p.name, AVG(r.rating) AS averageRating, COUNT(r.id) AS reviewCount " +
    //     "FROM Product p " +
    //     "JOIN Product_Color_Size pcs ON p.id = pcs.product_id " +
    //     "JOIN Order_Detail od ON pcs.id = od.product_id " +
    //     "JOIN Review r ON od.order_id = r.order_id " +
    //     "GROUP BY p.id, p.name " +
    //     "ORDER BY reviewCount DESC, averageRating DESC")
    // List<Object[]> findTopTrendingProducts(Pageable pageable);

    // @Query("SELECT p.id, p.name, SUM(od.quantity) AS totalOrders " +
    //     "FROM Product p " +
    //     "JOIN Product_Color_Size pcs ON p.id = pcs.product_id " +
    //     "JOIN Order_Detail od ON pcs.id = od.product_id " +
    //     "GROUP BY p.id, p.name " +
    //     "ORDER BY totalOrders DESC")
    // List<Object[]> findTopSellingProducts(Pageable pageable);

    boolean existsByName(String name);

    @Query("SELECT p.id, p.name, p.averageRating, p.reviewCount FROM Product p ORDER BY p.reviewCount DESC, p.averageRating DESC")
    List<Object[]> findTopTrendingProducts(Pageable pageable);

    @Query("SELECT p.id, p.name, p.averageRating, p.reviewCount FROM Product p ORDER BY p.reviewCount DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);
}
