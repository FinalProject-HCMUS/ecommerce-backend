package com.hcmus.ecommerce_backend.blog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.hcmus.ecommerce_backend.blog.model.entity.Blog;

@Repository
public interface BlogRepository extends JpaRepository<Blog, String>, JpaSpecificationExecutor<Blog> {
    boolean existsByTitleAndUserId(String title, String userId);
    List<Blog> findByUserId(String userId);
}
