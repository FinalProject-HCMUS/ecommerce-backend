package com.hcmus.ecommerce_backend.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hcmus.ecommerce_backend.product.model.entity.ProductColorSize;

@Repository
public interface ProductColorSizeRepository extends JpaRepository<ProductColorSize, String> {
    boolean existsByProductIdAndColorIdAndSizeId(String productId, String colorId, String sizeId);
}
