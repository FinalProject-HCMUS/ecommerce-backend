package com.hcmus.ecommerce_backend.order.model.mapper;

import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateOrderRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderResponse;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderTrackResponse;
import com.hcmus.ecommerce_backend.order.model.entity.Order;
import com.hcmus.ecommerce_backend.order.model.entity.OrderTrack;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    
    @Mapping(target = "orderTracks", source = "orderTracks")
    OrderResponse toResponse(Order order);
    
    OrderTrackResponse toOrderTrackResponse(OrderTrack orderTrack);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderTracks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    Order toEntity(CreateOrderRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderTracks", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    void updateEntity(UpdateOrderRequest request, @MappingTarget Order order);
}