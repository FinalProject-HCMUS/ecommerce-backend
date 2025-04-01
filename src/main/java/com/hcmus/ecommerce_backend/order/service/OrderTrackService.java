package com.hcmus.ecommerce_backend.order.service;

import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderTrackRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateOrderTrackRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderTrackResponse;
import com.hcmus.ecommerce_backend.order.model.enums.Status;

import java.util.List;

public interface OrderTrackService {
    
    List<OrderTrackResponse> getAllOrderTracks();
    
    OrderTrackResponse getOrderTrackById(String id);
    
    List<OrderTrackResponse> getOrderTracksByOrderId(String orderId);
    
    List<OrderTrackResponse> getOrderTracksByStatus(Status status);
    
    OrderTrackResponse createOrderTrack(CreateOrderTrackRequest request);
    
    OrderTrackResponse updateOrderTrack(String id, UpdateOrderTrackRequest request);
    
    void deleteOrderTrack(String id);
}