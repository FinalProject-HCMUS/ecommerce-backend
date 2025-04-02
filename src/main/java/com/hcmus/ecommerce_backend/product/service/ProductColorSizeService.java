package com.hcmus.ecommerce_backend.product.service;

import java.util.List;

import com.hcmus.ecommerce_backend.product.model.dto.request.CreateProductColorSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateProductColorSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductColorSizeResponse;

public interface ProductColorSizeService {
    List<ProductColorSizeResponse> getAllProductColorSizes();
    
    ProductColorSizeResponse getProductColorSizeById(String id);

    ProductColorSizeResponse createProductColorSize(CreateProductColorSizeRequest request);

    ProductColorSizeResponse updateProductColorSize(String id, UpdateProductColorSizeRequest request);

    void deleteProductColorSize(String id);
}
