package com.hcmus.ecommerce_backend.category.repository;

import com.hcmus.ecommerce_backend.category.model.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    boolean existsByName(String name);

    Page<Category> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}