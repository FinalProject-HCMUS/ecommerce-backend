package com.hcmus.ecommerce_backend.product.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hcmus.ecommerce_backend.product.model.entity.ProductColorSize;

@Repository
public interface ProductColorSizeRepository extends JpaRepository<ProductColorSize, String> {
    boolean existsByProductIdAndColorIdAndSizeId(String productId, String colorId, String sizeId);

    List<ProductColorSize> findByProductId(String productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pcs FROM ProductColorSize pcs WHERE pcs.id = :id")
    Optional<ProductColorSize> findByIdWithLock(@Param("id") String id);
}
