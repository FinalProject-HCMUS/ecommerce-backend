package com.hcmus.ecommerce_backend.blog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hcmus.ecommerce_backend.blog.model.entity.Blog;

@Repository
public interface BlogRepository extends JpaRepository<Blog, String> {
    boolean existsByTitleAndUserId(String title, String userId);
}
