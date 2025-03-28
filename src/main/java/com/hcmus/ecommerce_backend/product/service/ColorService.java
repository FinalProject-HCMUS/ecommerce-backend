package com.hcmus.ecommerce_backend.product.service;

import com.hcmus.ecommerce_backend.product.model.dto.request.CreateColorRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateColorRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ColorResponse;

import java.util.List;

public interface ColorService {
    
    List<ColorResponse> getAllColors();
    
    ColorResponse getColorById(String id);
    
    ColorResponse createColor(CreateColorRequest request);
    
    ColorResponse updateColor(String id, UpdateColorRequest request);
    
    void deleteColor(String id);
}
