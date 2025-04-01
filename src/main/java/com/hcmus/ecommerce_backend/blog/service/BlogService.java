package com.hcmus.ecommerce_backend.blog.service;

import java.util.List;

import com.hcmus.ecommerce_backend.blog.model.dto.request.CreateBlogRequest;
import com.hcmus.ecommerce_backend.blog.model.dto.request.UpdateBlogRequest;
import com.hcmus.ecommerce_backend.blog.model.dto.response.BlogResponse;

public interface BlogService {
    
    List<BlogResponse> getAllBlogs();
    
    BlogResponse getBlogById(String id);
    
    BlogResponse createBlog(CreateBlogRequest request);
    
    BlogResponse updateBlog(String id, UpdateBlogRequest request);
    
    void deleteBlog(String id);
}
