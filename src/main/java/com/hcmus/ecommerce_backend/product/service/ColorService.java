package com.hcmus.ecommerce_backend.product.service;

import com.hcmus.ecommerce_backend.product.model.dto.request.color.CreateColorRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.color.UpdateColorRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ColorResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ColorService {
    
    Page<ColorResponse> getAllColors(Pageable pageable);

    Page<ColorResponse> searchColors(Pageable pageable, String keyword);
    
    ColorResponse getColorById(String id);
    
    ColorResponse createColor(CreateColorRequest request);

    List<ColorResponse> createMultipleColors(List<CreateColorRequest> requests);
    
    ColorResponse updateColor(String id, UpdateColorRequest request);
    
    void deleteColor(String id);
}
