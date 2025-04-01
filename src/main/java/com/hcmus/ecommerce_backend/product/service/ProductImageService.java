package com.hcmus.ecommerce_backend.product.service;

import com.hcmus.ecommerce_backend.product.model.dto.request.CreateProductImageRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateProductImageRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductImageResponse;

import java.util.List;

public interface ProductImageService {

    List<ProductImageResponse> getAllProductImages();

    ProductImageResponse getProductImageById(String id);

    ProductImageResponse createProductImage(CreateProductImageRequest request);

    ProductImageResponse updateProductImage(String id, UpdateProductImageRequest request);

    void deleteProductImage(String id);
}
