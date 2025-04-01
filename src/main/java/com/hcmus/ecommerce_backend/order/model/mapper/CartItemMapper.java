package com.hcmus.ecommerce_backend.order.model.mapper;

import com.hcmus.ecommerce_backend.order.model.dto.request.CreateCartItemRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateCartItemRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.CartItemResponse;
import com.hcmus.ecommerce_backend.order.model.entity.CartItems;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    
    CartItemResponse toResponse(CartItems cartItem);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CartItems toEntity(CreateCartItemRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(UpdateCartItemRequest request, @MappingTarget CartItems cartItem);
}