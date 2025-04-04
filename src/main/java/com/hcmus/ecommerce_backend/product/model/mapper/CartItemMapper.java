package com.hcmus.ecommerce_backend.product.model.mapper;

import com.hcmus.ecommerce_backend.product.model.dto.request.CreateCartItemRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateCartItemRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.CartItemResponse;
import com.hcmus.ecommerce_backend.product.model.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    CartItemResponse toResponse(CartItem cartItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CartItem toEntity(CreateCartItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(UpdateCartItemRequest request, @MappingTarget CartItem cartItem);
}
