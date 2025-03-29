package com.hcmus.ecommerce_backend.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hcmus.ecommerce_backend.product.model.entity.Size;

@Repository
public interface SizeRepository extends JpaRepository<Size, String> {
    boolean existsByName(String name);
}
