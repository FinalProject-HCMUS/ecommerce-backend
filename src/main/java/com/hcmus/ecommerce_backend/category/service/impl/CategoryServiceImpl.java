package com.hcmus.ecommerce_backend.category.service.impl;

import com.hcmus.ecommerce_backend.category.exception.CategoryAlreadyExistsException;
import com.hcmus.ecommerce_backend.category.exception.CategoryNotFoundException;
import com.hcmus.ecommerce_backend.category.model.dto.request.CreateCategoryRequest;
import com.hcmus.ecommerce_backend.category.model.dto.request.UpdateCategoryRequest;
import com.hcmus.ecommerce_backend.category.model.dto.response.CategoryResponse;
import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.category.model.mapper.CategoryMapper;
import com.hcmus.ecommerce_backend.category.repository.CategoryRepository;
import com.hcmus.ecommerce_backend.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    
    @Override
    public List<CategoryResponse> getAllCategories() {
        log.info("CategoryServiceImpl | getAllCategories | Retrieving all categories");
        try {
            List<CategoryResponse> categories = categoryRepository.findAll().stream()
                    .map(categoryMapper::toResponse)
                    .collect(Collectors.toList());
            log.info("CategoryServiceImpl | getAllCategories | Found {} categories", categories.size());
            return categories;
        } catch (DataAccessException e) {
            log.error("CategoryServiceImpl | getAllCategories | Error retrieving categories: {}", e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("CategoryServiceImpl | getAllCategories | Unexpected error: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public CategoryResponse getCategoryById(String id) {
        log.info("CategoryServiceImpl | getCategoryById | id: {}", id);
        try {
            Category category = findCategoryById(id);
            log.info("CategoryServiceImpl | getCategoryById | Category found: {}", category.getName());
            return categoryMapper.toResponse(category);
        } catch (CategoryNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("CategoryServiceImpl | getCategoryById | Database error for id {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("CategoryServiceImpl | getCategoryById | Unexpected error for id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.info("CategoryServiceImpl | createCategory | Creating category with name: {}", request.getName());
        try {
            // Check if category with the same name already exists
            checkCategoryNameExists(request.getName());
            
            Category category = categoryMapper.toEntity(request);
            
            Category savedCategory = categoryRepository.save(category);
            log.info("CategoryServiceImpl | createCategory | Created category with id: {}", savedCategory.getId());
            return categoryMapper.toResponse(savedCategory);
        } catch (CategoryAlreadyExistsException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("CategoryServiceImpl | createCategory | Database error creating category '{}': {}", 
                    request.getName(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("CategoryServiceImpl | createCategory | Unexpected error creating category '{}': {}", 
                    request.getName(), e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public CategoryResponse updateCategory(String id, UpdateCategoryRequest request) {
        log.info("CategoryServiceImpl | updateCategory | Updating category with id: {}", id);
        try {
            Category category = findCategoryById(id);
            
            // Only check name existence if the name is being changed
            if (!category.getName().equals(request.getName())) {
                checkCategoryNameExists(request.getName());
            }
            
            categoryMapper.updateEntity(request, category);
            Category updatedCategory = categoryRepository.save(category);
            log.info("CategoryServiceImpl | updateCategory | Updated category with id: {}", updatedCategory.getId());
            return categoryMapper.toResponse(updatedCategory);
        } catch (CategoryNotFoundException | CategoryAlreadyExistsException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("CategoryServiceImpl | updateCategory | Database error updating category with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("CategoryServiceImpl | updateCategory | Unexpected error updating category with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void deleteCategory(String id) {
        log.info("CategoryServiceImpl | deleteCategory | Deleting category with id: {}", id);
        try {
            // Check existence first in a separate transaction
            if (!doesCategoryExistById(id)) {
                log.error("CategoryServiceImpl | deleteCategory | Category not found with id: {}", id);
                throw new CategoryNotFoundException(id);
            }
            
            // Then delete in the current transaction
            categoryRepository.deleteById(id);
            log.info("CategoryServiceImpl | deleteCategory | Deleted category with id: {}", id);
        } catch (CategoryNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("CategoryServiceImpl | deleteCategory | Database error deleting category with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("CategoryServiceImpl | deleteCategory | Unexpected error deleting category with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Helper method to find a category by ID.
     * Uses a separate transaction to avoid issues with the main transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private Category findCategoryById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("CategoryServiceImpl | findCategoryById | Category not found with id: {}", id);
                    return new CategoryNotFoundException(id);
                });
    }
    
    /**
     * Helper method to check if a category exists by ID.
     * Uses a separate transaction to avoid issues with the main transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private boolean doesCategoryExistById(String id) {
        return categoryRepository.existsById(id);
    }
    
    /**
     * Helper method to check if a category name already exists.
     * Throws CategoryAlreadyExistsException if the name exists.
     * Uses a separate transaction to avoid issues with the main transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private void checkCategoryNameExists(String name) {
        if (categoryRepository.existsByName(name)) {
            log.error("CategoryServiceImpl | checkCategoryNameExists | Category already exists with name: {}", name);
            throw new CategoryAlreadyExistsException(name);
        }
    }
}