package com.hcmus.ecommerce_backend.product.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hcmus.ecommerce_backend.product.model.dto.request.CreateSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.SizeResponse;

public interface SizeService {
    
    Page<SizeResponse> getAllSizes(Pageable pageable);

    SizeResponse getSizeById(String id);

    SizeResponse createSize(CreateSizeRequest request);

    SizeResponse updateSize(String id, UpdateSizeRequest request);

    void deleteSize(String id);
}
