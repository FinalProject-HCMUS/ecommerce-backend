package com.hcmus.ecommerce_backend.category.service;

import com.hcmus.ecommerce_backend.category.model.dto.request.CreateCategoryRequest;
import com.hcmus.ecommerce_backend.category.model.dto.request.UpdateCategoryRequest;
import com.hcmus.ecommerce_backend.category.model.dto.response.CategoryResponse;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    
    Page<CategoryResponse> searchCategories(Pageable pageable, String keyword);
    
    CategoryResponse getCategoryById(String id);
    
    CategoryResponse createCategory(CreateCategoryRequest request);
    
    CategoryResponse updateCategory(String id, UpdateCategoryRequest request);
    
    void deleteCategory(String id);

    List<CategoryResponse> getAllCategoriesWithoutPagination();
}