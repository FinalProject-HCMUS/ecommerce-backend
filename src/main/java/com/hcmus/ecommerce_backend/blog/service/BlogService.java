package com.hcmus.ecommerce_backend.blog.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hcmus.ecommerce_backend.blog.model.dto.request.CreateBlogRequest;
import com.hcmus.ecommerce_backend.blog.model.dto.request.UpdateBlogRequest;
import com.hcmus.ecommerce_backend.blog.model.dto.response.BlogResponse;

public interface BlogService {
    
    Page<BlogResponse> getAllBlogs(Pageable pageable);
    
    BlogResponse getBlogById(String id);
    
    BlogResponse createBlog(CreateBlogRequest request);
    
    BlogResponse updateBlog(String id, UpdateBlogRequest request);
    
    void deleteBlog(String id);
}
