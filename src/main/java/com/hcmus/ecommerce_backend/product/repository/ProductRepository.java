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

    @Query("SELECT p.id, p.name, p.description, p.cost, p.total, p.price, p.discountPercent, p.enable, " +
        "p.inStock, p.mainImageUrl, p.averageRating, p.reviewCount " +
        "FROM Product p " +
        "ORDER BY p.reviewCount DESC, p.averageRating DESC")
    List<Object[]> findTopTrendingProducts(Pageable pageable);

    @Query("SELECT p.id, p.name, p.description, p.cost, p.total, p.price, p.discountPercent, p.enable, " +
       "p.inStock, p.mainImageUrl, p.averageRating, p.reviewCount, SUM(od.quantity) AS totalOrders " +
       "FROM ProductColorSize pcs " +
       "JOIN pcs.product p " +
       "JOIN OrderDetail od ON od.productId = p.id " +
       "GROUP BY p.id, p.name, p.description, p.cost, p.total, p.price, p.discountPercent, p.enable, " +
       "p.inStock, p.mainImageUrl, p.averageRating, p.reviewCount " +
       "ORDER BY totalOrders DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);
}
