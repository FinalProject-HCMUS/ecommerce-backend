package com.hcmus.ecommerce_backend.product.service;

import com.hcmus.ecommerce_backend.product.model.dto.request.product.CreateProductImageRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.UpdateListProductImageRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.UpdateProductImageRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductImageResponse;

import java.util.List;

public interface ProductImageService {

    List<ProductImageResponse> getAllProductImages();

    ProductImageResponse getProductImageById(String id);

    ProductImageResponse createProductImage(CreateProductImageRequest request);

    ProductImageResponse updateProductImage(String id, UpdateProductImageRequest request);

    void deleteProductImage(String id);

    List<ProductImageResponse> getProductImagesByProductId(String productId);

    void updateListProductImage(List<UpdateListProductImageRequest> productImages);
}
