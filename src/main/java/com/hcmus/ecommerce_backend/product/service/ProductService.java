package com.hcmus.ecommerce_backend.product.service;

import com.hcmus.ecommerce_backend.product.model.dto.request.product.CreateProductRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.UpdateProductRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    Page<ProductResponse> getAllProducts(Pageable pageable, String keysearch, String category,
                                         Double fromprice, Double toprice, String color, String size,
                                         Boolean enabled, Boolean inStock);

    ProductResponse getProductById(String id);

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse updateProduct(String id, UpdateProductRequest request);

    void deleteProduct(String id);

    List<ProductResponse> getTopSellingProducts(int page, int size);

    List<ProductResponse> getTopTrendingProducts(int page, int size);
}