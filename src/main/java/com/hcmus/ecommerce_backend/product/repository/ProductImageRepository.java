package com.hcmus.ecommerce_backend.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hcmus.ecommerce_backend.product.model.entity.ProductImage;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, String> {
    boolean existsByUrlAndProductId(String url, String productId);
    long countByProductId(String productId);
    List<ProductImage> findByProductId(String productId);
}
