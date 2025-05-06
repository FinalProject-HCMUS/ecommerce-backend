package com.hcmus.ecommerce_backend.order.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.hcmus.ecommerce_backend.order.exception.OrderDetailNotFoundException;
import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderDetailRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateOrderDetailRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderDetailResponse;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderDetailWithProductResponse;
import com.hcmus.ecommerce_backend.order.model.entity.OrderDetail;
import com.hcmus.ecommerce_backend.order.model.mapper.OrderDetailMapper;
import com.hcmus.ecommerce_backend.order.repository.OrderDetailRepository;
import com.hcmus.ecommerce_backend.order.service.OrderDetailService;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductResponse;
import com.hcmus.ecommerce_backend.product.model.entity.Product;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDetailServiceImpl implements OrderDetailService {

    private final OrderDetailRepository orderDetailRepository;
    private final OrderDetailMapper orderDetailMapper;

    @Override
    public List<OrderDetailResponse> getAllOrderDetails() {
        log.info("OrderDetailServiceImpl | getAllOrderDetails | Retrieving all order details");
        try {
            List<OrderDetailResponse> orderDetails = orderDetailRepository.findAll().stream()
                    .map(orderDetailMapper::toResponse)
                    .collect(Collectors.toList());
            log.info("OrderDetailServiceImpl | getAllOrderDetails | Found {} order details", orderDetails.size());
            return orderDetails;
        } catch (DataAccessException e) {
            log.error("OrderDetailServiceImpl | getAllOrderDetails | Database error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderDetailServiceImpl | getAllOrderDetails | Unexpected error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public OrderDetailResponse getOrderDetailById(String id) {
        log.info("OrderDetailServiceImpl | getOrderDetailById | id: {}", id);
        try {
            OrderDetail orderDetail = findOrderDetailById(id);
            log.info("OrderDetailServiceImpl | getOrderDetailById | Order detail found with id: {}", id);
            return orderDetailMapper.toResponse(orderDetail);
        } catch (OrderDetailNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("OrderDetailServiceImpl | getOrderDetailById | Database error for id {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderDetailServiceImpl | getOrderDetailById | Unexpected error for id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public OrderDetailResponse createOrder(CreateOrderDetailRequest request) {
        log.info("OrderDetailServiceImpl | createOrder | Creating order detail");
        try {
            OrderDetail orderDetail = orderDetailMapper.toEntity(request);
            OrderDetail savedOrderDetail = orderDetailRepository.save(orderDetail);
            log.info("OrderDetailServiceImpl | createOrder | Created order detail with id: {}", savedOrderDetail.getId());
            return orderDetailMapper.toResponse(savedOrderDetail);
        } catch (DataAccessException e) {
            log.error("OrderDetailServiceImpl | createOrder | Database error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderDetailServiceImpl | createOrder | Unexpected error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public OrderDetailResponse updateOrder(String id, UpdateOrderDetailRequest request) {
        log.info("OrderDetailServiceImpl | updateOrder | Updating order detail with id: {}", id);
        try {
            OrderDetail orderDetail = findOrderDetailById(id);
            orderDetailMapper.updateEntity(request, orderDetail);
            OrderDetail updatedOrderDetail = orderDetailRepository.save(orderDetail);
            log.info("OrderDetailServiceImpl | updateOrder | Updated order detail with id: {}", updatedOrderDetail.getId());
            return orderDetailMapper.toResponse(updatedOrderDetail);
        } catch (OrderDetailNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("OrderDetailServiceImpl | updateOrder | Database error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderDetailServiceImpl | updateOrder | Unexpected error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteOrderDetail(String id) {
        log.info("OrderDetailServiceImpl | deleteOrderDetail | Deleting order detail with id: {}", id);
        try {
            if (!orderDetailRepository.existsById(id)) {
                log.error("OrderDetailServiceImpl | deleteOrderDetail | Order detail not found with id: {}", id);
                throw new OrderDetailNotFoundException(id);
            }
            orderDetailRepository.deleteById(id);
            log.info("OrderDetailServiceImpl | deleteOrderDetail | Deleted order detail with id: {}", id);
        } catch (OrderDetailNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("OrderDetailServiceImpl | deleteOrderDetail | Database error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderDetailServiceImpl | deleteOrderDetail | Unexpected error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<OrderDetailWithProductResponse> getOrderDetailsWithProductByOrderId(String orderId) {
        log.info("OrderDetailServiceImpl | getOrderDetailsWithProductByOrderId | orderId: {}", orderId);
        try {
            List<Map<String, Object>> orderDetails = orderDetailRepository.findOrderDetailsWithProductByOrderId(orderId);
            if (orderDetails.isEmpty()) {
                log.error("OrderDetailServiceImpl | getOrderDetailsWithProductByOrderId | No order details found for orderId: {}", orderId);
                throw new OrderDetailNotFoundException("No order details found for order ID: " + orderId);
            }
            return orderDetails.stream()
                    .map(orderDetailMapper::mapToOrderDetailWithProductResponse)
                    .collect(Collectors.toList());
        } catch (OrderDetailNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("OrderDetailServiceImpl | getOrderDetailsWithProductByOrderId | Database error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderDetailServiceImpl | getOrderDetailsWithProductByOrderId | Unexpected error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public List<OrderDetailResponse> addOrderDetails(List<CreateOrderDetailRequest> requests) {
        log.info("OrderDetailServiceImpl | addOrderDetails | Adding {} order details", requests.size());
        try {
            List<OrderDetail> orderDetails = requests.stream()
                    .map(orderDetailMapper::toEntity)
                    .collect(Collectors.toList());
            List<OrderDetail> savedOrderDetails = orderDetailRepository.saveAll(orderDetails);
            log.info("OrderDetailServiceImpl | addOrderDetails | Successfully added {} order details", savedOrderDetails.size());
            return savedOrderDetails.stream()
                    .map(orderDetailMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            log.error("OrderDetailServiceImpl | addOrderDetails | Database error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderDetailServiceImpl | addOrderDetails | Unexpected error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Helper method to find an order detail by ID.
     */
    @Transactional
    private OrderDetail findOrderDetailById(String id) {
        return orderDetailRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("OrderDetailServiceImpl | findOrderDetailById | Order detail not found with id: {}", id);
                    return new OrderDetailNotFoundException(id);
                });
    }
}