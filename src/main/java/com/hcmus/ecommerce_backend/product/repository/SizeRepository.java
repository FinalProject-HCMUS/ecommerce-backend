package com.hcmus.ecommerce_backend.product.repository;

import com.hcmus.ecommerce_backend.product.model.entity.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SizeRepository extends JpaRepository<Size, String> {
    boolean existsByName(String name);

    @Query("SELECT s FROM Size s WHERE " +
            "(:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))) AND " +
            "(:minHeight IS NULL OR s.minHeight >= :minHeight) AND " +
            "(:maxHeight IS NULL OR s.maxHeight <= :maxHeight) AND " +
            "(:minWeight IS NULL OR s.minWeight >= :minWeight) AND " +
            "(:maxWeight IS NULL OR s.maxWeight <= :maxWeight)")
    Page<Size> searchSizes(
            @Param("keyword") String keyword,
            @Param("minHeight") Integer minHeight,
            @Param("maxHeight") Integer maxHeight,
            @Param("minWeight") Integer minWeight,
            @Param("maxWeight") Integer maxWeight,
            Pageable pageable);
}
