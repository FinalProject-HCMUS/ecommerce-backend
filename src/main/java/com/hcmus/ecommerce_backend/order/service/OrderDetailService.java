package com.hcmus.ecommerce_backend.order.service;

import java.util.List;

import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderDetailRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateOrderDetailRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderDetailResponse;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderDetailWithProductResponse;

public interface OrderDetailService {
        
    List<OrderDetailResponse> getAllOrderDetails();
    
    OrderDetailResponse getOrderDetailById(String id);
    
    OrderDetailResponse createOrder(CreateOrderDetailRequest request);
    
    OrderDetailResponse updateOrder(String id, UpdateOrderDetailRequest request);
    
    void deleteOrderDetail(String id);

    List<OrderDetailWithProductResponse> getOrderDetailsWithProductByOrderId(String orderId);    

    List<OrderDetailResponse> addOrderDetails(List<CreateOrderDetailRequest> requests);
}
