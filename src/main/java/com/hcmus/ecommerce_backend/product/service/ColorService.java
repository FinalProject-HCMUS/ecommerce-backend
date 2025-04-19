package com.hcmus.ecommerce_backend.product.service;

import com.hcmus.ecommerce_backend.product.model.dto.request.CreateColorRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateColorRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ColorResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ColorService {
    
    Page<ColorResponse> getAllColors(Pageable pageable);
    
    ColorResponse getColorById(String id);
    
    ColorResponse createColor(CreateColorRequest request);
    
    ColorResponse updateColor(String id, UpdateColorRequest request);
    
    void deleteColor(String id);
}
