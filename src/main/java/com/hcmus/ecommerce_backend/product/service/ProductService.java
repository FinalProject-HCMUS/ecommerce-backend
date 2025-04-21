package com.hcmus.ecommerce_backend.product.service;

import com.hcmus.ecommerce_backend.product.model.dto.request.CreateProductRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateProductRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductResponse;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    
    Page<ProductResponse> getAllProducts(Pageable pageable, String keysearch, String category, Double fromprice, Double toprice, String color, String size);
    
    ProductResponse getProductById(String id);
    
    ProductResponse createProduct(CreateProductRequest request);
    
    ProductResponse updateProduct(String id, UpdateProductRequest request);
    
    void deleteProduct(String id);

    List<ProductResponse> getTopSellingProducts(int page, int size);

    List<ProductResponse> getTopTrendingProducts(int page, int size);
}