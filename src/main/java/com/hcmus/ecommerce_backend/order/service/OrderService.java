package com.hcmus.ecommerce_backend.order.service;

import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateOrderRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    
    List<OrderResponse> getAllOrders();
    
    OrderResponse getOrderById(String id);
    
    List<OrderResponse> getOrdersByCustomerId(String customerId);
    
    OrderResponse createOrder(CreateOrderRequest request);
    
    OrderResponse updateOrder(String id, UpdateOrderRequest request);
    
    void deleteOrder(String id);
}