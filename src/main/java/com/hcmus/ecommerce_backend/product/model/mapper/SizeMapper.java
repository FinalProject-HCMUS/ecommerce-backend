package com.hcmus.ecommerce_backend.product.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.hcmus.ecommerce_backend.product.model.dto.request.size.CreateSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.size.UpdateSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.SizeResponse;
import com.hcmus.ecommerce_backend.product.model.entity.Size;

@Mapper(componentModel = "spring")
public interface SizeMapper {
    
    SizeResponse toResponse(Size size);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Size toEntity(CreateSizeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(UpdateSizeRequest request, @MappingTarget Size size);
}
