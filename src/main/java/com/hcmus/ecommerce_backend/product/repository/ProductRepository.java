package com.hcmus.ecommerce_backend.product.repository;

import com.hcmus.ecommerce_backend.product.model.entity.Product;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {
    
    boolean existsByName(String name);

    @Query("SELECT p " +
        "FROM Product p " +
        "ORDER BY p.reviewCount DESC, p.averageRating DESC")
    List<Product> findTopTrendingProducts(Pageable pageable);

    @Query("SELECT p " +
        "FROM Product p " +
        "JOIN OrderDetail od ON od.productId = p.id " +
        "GROUP BY p " +
        "ORDER BY SUM(od.quantity) DESC")
    List<Product> findTopSellingProducts(Pageable pageable);
}
