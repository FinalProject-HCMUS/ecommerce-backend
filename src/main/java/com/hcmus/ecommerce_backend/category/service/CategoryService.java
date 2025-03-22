package com.hcmus.ecommerce_backend.category.service;

import com.hcmus.ecommerce_backend.category.model.dto.request.CreateCategoryRequest;
import com.hcmus.ecommerce_backend.category.model.dto.request.UpdateCategoryRequest;
import com.hcmus.ecommerce_backend.category.model.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    
    List<CategoryResponse> getAllCategories();
    
    CategoryResponse getCategoryById(String id);
    
    CategoryResponse createCategory(CreateCategoryRequest request);
    
    CategoryResponse updateCategory(String id, UpdateCategoryRequest request);
    
    void deleteCategory(String id);
}