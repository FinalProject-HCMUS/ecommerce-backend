package com.hcmus.ecommerce_backend.product.service;

import com.hcmus.ecommerce_backend.product.model.dto.request.CreateProductRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateProductRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    
    List<ProductResponse> getAllProducts();
    
    ProductResponse getProductById(String id);
    
    ProductResponse createProduct(CreateProductRequest request);
    
    ProductResponse updateProduct(String id, UpdateProductRequest request);
    
    void deleteProduct(String id);
}