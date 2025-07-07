package com.hcmus.ecommerce_backend.category.repository;

import com.hcmus.ecommerce_backend.category.model.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    boolean existsByName(String name);

    @Query("SELECT c FROM Category c " +
            "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Category> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

}