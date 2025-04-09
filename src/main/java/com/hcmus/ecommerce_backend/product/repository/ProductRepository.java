package com.hcmus.ecommerce_backend.product.repository;

import com.hcmus.ecommerce_backend.product.model.entity.Product;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    boolean existsByName(String name);

    @Query("SELECT p.id, p.name, AVG(r.rating) AS averageRating, COUNT(r.id) AS reviewCount " +
        "FROM ProductColorSize pcs " +
        "JOIN pcs.product p " +
        "JOIN OrderDetail od ON od.productId = p.id " +
        "JOIN Review r ON od.order.id = r.orderId " +
        "GROUP BY p.id, p.name " +
        "ORDER BY reviewCount DESC, averageRating DESC")
    List<Object[]> findTopTrendingProducts(Pageable pageable);

    @Query("SELECT p.id, p.name, SUM(od.quantity) AS totalOrders " +
        "FROM ProductColorSize pcs " +
        "JOIN pcs.product p " +
        "JOIN OrderDetail od ON od.productId = p.id " +
        "GROUP BY p.id, p.name " +
        "ORDER BY totalOrders DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);
}
