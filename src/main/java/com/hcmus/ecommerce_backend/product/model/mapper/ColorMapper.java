package com.hcmus.ecommerce_backend.product.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.hcmus.ecommerce_backend.product.model.dto.request.CreateColorRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateColorRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ColorResponse;
import com.hcmus.ecommerce_backend.product.model.entity.Color;

@Mapper(componentModel = "spring")
public interface ColorMapper {
    
    ColorResponse toResponse(Color color);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Color toEntity(CreateColorRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(UpdateColorRequest request, @MappingTarget Color color);
}
