package com.hcmus.ecommerce_backend.blog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.hcmus.ecommerce_backend.blog.model.entity.Blog;

@Repository
public interface BlogRepository extends JpaRepository<Blog, String>, JpaSpecificationExecutor<Blog> {
    boolean existsByTitleAndUserId(String title, String userId);
}
