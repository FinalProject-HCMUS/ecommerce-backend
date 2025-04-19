package com.hcmus.ecommerce_backend.order.service;

import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateOrderRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderResponse;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    Page<OrderResponse> getAllOrders(Pageable pageable);

    OrderResponse getOrderById(String id);

    List<OrderResponse> getOrdersByCustomerId(String customerId);

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse updateOrder(String id, UpdateOrderRequest request);

    void deleteOrder(String id);
}