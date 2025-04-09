package com.hcmus.ecommerce_backend.product.service.impl;

import com.hcmus.ecommerce_backend.category.exception.CategoryNotFoundException;
import com.hcmus.ecommerce_backend.category.repository.CategoryRepository;
import com.hcmus.ecommerce_backend.product.exception.ProductAlreadyExistsException;
import com.hcmus.ecommerce_backend.product.exception.ProductNotFoundException;
import com.hcmus.ecommerce_backend.product.model.dto.request.CreateProductRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateProductRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductResponse;
import com.hcmus.ecommerce_backend.product.model.entity.Product;
import com.hcmus.ecommerce_backend.product.model.mapper.ProductMapper;
import com.hcmus.ecommerce_backend.product.repository.ProductRepository;
import com.hcmus.ecommerce_backend.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.query.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    
    @Override
    public List<ProductResponse> getAllProducts() {
        log.info("ProductServiceImpl | getAllProducts | Retrieving all products");
        try {
            List<ProductResponse> products = productRepository.findAll().stream()
                    .map(productMapper::toResponse)
                    .collect(Collectors.toList());
            log.info("ProductServiceImpl | getAllProducts | Found {} products", products.size());
            return products;
        } catch (DataAccessException e) {
            log.error("ProductServiceImpl | getAllProducts | Error retrieving products: {}", e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("ProductServiceImpl | getAllProducts | Unexpected error: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public ProductResponse getProductById(String id) {
        log.info("ProductServiceImpl | getProductById | id: {}", id);
        try {
            Product product = findProductById(id);
            log.info("ProductServiceImpl | getProductById | Product found: {}", product.getName());
            return productMapper.toResponse(product);
        } catch (ProductNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("ProductServiceImpl | getProductById | Database error for id {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ProductServiceImpl | getProductById | Unexpected error for id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("ProductServiceImpl | createProduct | Creating product with name: {}", request.getName());
        try {
            // Check if product with the same name already exists
            checkProductNameExists(request.getName());
            
            // Validate that the category exists
            validateCategory(request.getCategoryId());
            
            Product product = productMapper.toEntity(request);
            
            // Set initial values
            product.setEnable(true);
            product.setInStock(true);
            
            Product savedProduct = productRepository.save(product);
            log.info("ProductServiceImpl | createProduct | Created product with id: {}", savedProduct.getId());
            return productMapper.toResponse(savedProduct);
        } catch (ProductAlreadyExistsException | CategoryNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("ProductServiceImpl | createProduct | Database error creating product '{}': {}", 
                    request.getName(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ProductServiceImpl | createProduct | Unexpected error creating product '{}': {}", 
                    request.getName(), e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public ProductResponse updateProduct(String id, UpdateProductRequest request) {
        log.info("ProductServiceImpl | updateProduct | Updating product with id: {}", id);
        try {
            Product product = findProductById(id);
            
            // Only check name existence if the name is being changed
            if (!product.getName().equals(request.getName())) {
                checkProductNameExists(request.getName());
            }
            
            productMapper.updateEntity(request, product);
            Product updatedProduct = productRepository.save(product);
            log.info("ProductServiceImpl | updateProduct | Updated product with id: {}", updatedProduct.getId());
            return productMapper.toResponse(updatedProduct);
        } catch (ProductNotFoundException | ProductAlreadyExistsException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("ProductServiceImpl | updateProduct | Database error updating product with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ProductServiceImpl | updateProduct | Unexpected error updating product with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void deleteProduct(String id) {
        log.info("ProductServiceImpl | deleteProduct | Deleting product with id: {}", id);
        try {
            if (!doesProductExistById(id)) {
                throw new ProductNotFoundException(id);
            }
            
            productRepository.deleteById(id);
            log.info("ProductServiceImpl | deleteProduct | Deleted product with id: {}", id);
        } catch (ProductNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("ProductServiceImpl | deleteProduct | Database error deleting product with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ProductServiceImpl | deleteProduct | Unexpected error deleting product with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private Product findProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("ProductServiceImpl | findProductById | Product not found with id: {}", id);
                    return new ProductNotFoundException(id);
                });
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private boolean doesProductExistById(String id) {
        return productRepository.existsById(id);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private void checkProductNameExists(String name) {
        if (productRepository.existsByName(name)) {
            log.error("ProductServiceImpl | checkProductNameExists | Product already exists with name: {}", name);
            throw new ProductAlreadyExistsException(name);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private void validateCategory(String categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            log.error("ProductServiceImpl | validateCategory | Category not found with id: {}", categoryId);
            throw new CategoryNotFoundException(categoryId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getTopTrendingProducts(int page, int size) {
        log.info("ProductServiceImpl | getTopTrendingProducts | page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);

        // Query to calculate review count and average rating for each product
        List<Object[]> results = productRepository.findTopTrendingProducts(pageable);

        // Map results to ProductResponse
        return results.stream()
                .map(row -> ProductResponse.builder()
                        .id((String) row[0])
                        .name((String) row[1])
                        .averageRating((Double) row[2]) // Assuming average rating is calculated
                        .reviewCount((Double) row[3])  // Assuming review count is calculated
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getTopSellingProducts(int page, int size) {
        log.info("ProductServiceImpl | getTopSellingProducts | page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);

        // Query to calculate order count for each product
        List<Object[]> results = productRepository.findTopSellingProducts(pageable);

        // Map results to ProductResponse
        return results.stream()
                .map(row -> ProductResponse.builder()
                        .id((String) row[0])
                        .name((String) row[1])
                        .averageRating((Double) row[2]) // Assuming average rating is calculated
                        .reviewCount((Double) row[3])  // Assuming review count is calculated
                        .build())
                .collect(Collectors.toList());
    }
}