package com.hcmus.ecommerce_backend.order.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderDetailRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateOrderDetailRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderDetailResponse;
import com.hcmus.ecommerce_backend.order.model.entity.Order;
import com.hcmus.ecommerce_backend.order.model.entity.OrderDetail;

@Mapper(componentModel = "spring")
public interface OrderDetailMapper {

    @Mapping(target = "orderId", source = "order", qualifiedByName = "orderToOrderId")
    OrderDetailResponse toResponse(OrderDetail orderDetail);
    
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    OrderDetail toEntity(CreateOrderDetailRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "order", source = "orderId", qualifiedByName = "orderIdToOrder")
    void updateEntity(UpdateOrderDetailRequest request, @MappingTarget OrderDetail orderDetail);

    @Named("orderIdToOrder")
    default Order orderIdToOrder(String orderId) {
        Order order = new Order();
        order.setId(orderId);
        return order;
    }

    @Named("orderToOrderId")
    default String orderToOrderId(Order order) {
        return order.getId();
    }
}
