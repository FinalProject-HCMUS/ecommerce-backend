package com.hcmus.ecommerce_backend.product.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hcmus.ecommerce_backend.product.model.dto.request.size.CreateSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.size.UpdateSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.SizeResponse;

import java.util.List;

public interface SizeService {

    Page<SizeResponse> searchSizes(Pageable pageable, String keyword, Integer minHeight,
                                   Integer maxHeight, Integer minWeight, Integer maxWeight);

    SizeResponse getSizeById(String id);

    SizeResponse createSize(CreateSizeRequest request);

    List<SizeResponse> createMultipleSizes(List<CreateSizeRequest> requests);

    SizeResponse updateSize(String id, UpdateSizeRequest request);

    void deleteSize(String id);
}
