package com.hcmus.ecommerce_backend.product.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hcmus.ecommerce_backend.product.model.dto.request.product.CreateProductColorSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.UpdateProductColorSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductColorSizeResponse;

public interface ProductColorSizeService {
    Page<ProductColorSizeResponse> getAllProductColorSizes(Pageable pageable);
    
    ProductColorSizeResponse getProductColorSizeById(String id);

    ProductColorSizeResponse createProductColorSize(CreateProductColorSizeRequest request);

    List<ProductColorSizeResponse> createMultipleProductColorSizes(List<CreateProductColorSizeRequest> requests);

    ProductColorSizeResponse updateProductColorSize(String id, UpdateProductColorSizeRequest request);

    void deleteProductColorSize(String id);

    List<ProductColorSizeResponse> getProductColorSizesByProductId(String productId);
}
