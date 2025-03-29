package com.hcmus.ecommerce_backend.product.service;

import java.util.List;

import com.hcmus.ecommerce_backend.product.model.dto.request.CreateSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.SizeResponse;

public interface SizeService {
    
    List<SizeResponse> getAllSizes();

    SizeResponse getSizeById(String id);

    SizeResponse createSize(CreateSizeRequest request);

    SizeResponse updateSize(String id, UpdateSizeRequest request);

    void deleteSize(String id);
}
