package com.hcmus.ecommerce_backend.order.service.impl;

import com.hcmus.ecommerce_backend.order.exception.OrderNotFoundException;
import com.hcmus.ecommerce_backend.order.exception.OrderTrackNotFoundException;
import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderTrackRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateOrderTrackRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderTrackResponse;
import com.hcmus.ecommerce_backend.order.model.entity.Order;
import com.hcmus.ecommerce_backend.order.model.entity.OrderTrack;
import com.hcmus.ecommerce_backend.order.model.enums.Status;
import com.hcmus.ecommerce_backend.order.model.mapper.OrderTrackMapper;
import com.hcmus.ecommerce_backend.order.repository.OrderRepository;
import com.hcmus.ecommerce_backend.order.repository.OrderTrackRepository;
import com.hcmus.ecommerce_backend.order.service.OrderTrackService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderTrackServiceImpl implements OrderTrackService {
    
    private final OrderTrackRepository orderTrackRepository;
    private final OrderRepository orderRepository;
    private final OrderTrackMapper orderTrackMapper;
    
    @Override
    public List<OrderTrackResponse> getAllOrderTracks() {
        log.info("OrderTrackServiceImpl | getAllOrderTracks | Retrieving all order tracks");
        try {
            List<OrderTrackResponse> orderTracks = orderTrackRepository.findAll().stream()
                    .map(orderTrackMapper::toResponse)
                    .collect(Collectors.toList());
            log.info("OrderTrackServiceImpl | getAllOrderTracks | Found {} order tracks", orderTracks.size());
            return orderTracks;
        } catch (DataAccessException e) {
            log.error("OrderTrackServiceImpl | getAllOrderTracks | Error retrieving order tracks: {}", e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("OrderTrackServiceImpl | getAllOrderTracks | Unexpected error: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public OrderTrackResponse getOrderTrackById(String id) {
        log.info("OrderTrackServiceImpl | getOrderTrackById | id: {}", id);
        try {
            OrderTrack orderTrack = findOrderTrackById(id);
            log.info("OrderTrackServiceImpl | getOrderTrackById | Order track found with id: {}", orderTrack.getId());
            return orderTrackMapper.toResponse(orderTrack);
        } catch (OrderTrackNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("OrderTrackServiceImpl | getOrderTrackById | Database error for id {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderTrackServiceImpl | getOrderTrackById | Unexpected error for id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public List<OrderTrackResponse> getOrderTracksByOrderId(String orderId) {
        log.info("OrderTrackServiceImpl | getOrderTracksByOrderId | orderId: {}", orderId);
        try {
            List<OrderTrackResponse> orderTracks = orderTrackRepository.findByOrderIdOrderByUpdatedAtDesc(orderId).stream()
                    .map(orderTrackMapper::toResponse)
                    .collect(Collectors.toList());
            log.info("OrderTrackServiceImpl | getOrderTracksByOrderId | Found {} order tracks for order {}", 
                    orderTracks.size(), orderId);
            return orderTracks;
        } catch (DataAccessException e) {
            log.error("OrderTrackServiceImpl | getOrderTracksByOrderId | Database error for orderId {}: {}", 
                    orderId, e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("OrderTrackServiceImpl | getOrderTracksByOrderId | Unexpected error for orderId {}: {}", 
                    orderId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public List<OrderTrackResponse> getOrderTracksByStatus(Status status) {
        log.info("OrderTrackServiceImpl | getOrderTracksByStatus | status: {}", status);
        try {
            List<OrderTrackResponse> orderTracks = orderTrackRepository.findByStatusOrderByUpdatedAtDesc(status).stream()
                    .map(orderTrackMapper::toResponse)
                    .collect(Collectors.toList());
            log.info("OrderTrackServiceImpl | getOrderTracksByStatus | Found {} order tracks with status {}", 
                    orderTracks.size(), status);
            return orderTracks;
        } catch (DataAccessException e) {
            log.error("OrderTrackServiceImpl | getOrderTracksByStatus | Database error for status {}: {}", 
                    status, e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("OrderTrackServiceImpl | getOrderTracksByStatus | Unexpected error for status {}: {}", 
                    status, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public OrderTrackResponse createOrderTrack(CreateOrderTrackRequest request) {
        log.info("OrderTrackServiceImpl | createOrderTrack | Creating order track for order: {}", request.getOrderId());
        try {
            // Verify that the order exists
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new OrderNotFoundException(request.getOrderId()));
            
            OrderTrack orderTrack = orderTrackMapper.toEntity(request);
            
            // Update the order status if tracking will change it
            if (order.getStatus() != request.getStatus()) {
                log.info("OrderTrackServiceImpl | createOrderTrack | Updating order status from {} to {}", 
                        order.getStatus(), request.getStatus());
                order.setStatus(request.getStatus());
                orderRepository.save(order);
            }
            
            OrderTrack savedOrderTrack = orderTrackRepository.save(orderTrack);
            log.info("OrderTrackServiceImpl | createOrderTrack | Created order track with id: {}", savedOrderTrack.getId());
            return orderTrackMapper.toResponse(savedOrderTrack);
        } catch (OrderNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("OrderTrackServiceImpl | createOrderTrack | Database error creating order track for order '{}': {}", 
                    request.getOrderId(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderTrackServiceImpl | createOrderTrack | Unexpected error creating order track for order '{}': {}", 
                    request.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public OrderTrackResponse updateOrderTrack(String id, UpdateOrderTrackRequest request) {
        log.info("OrderTrackServiceImpl | updateOrderTrack | Updating order track with id: {}", id);
        try {
            OrderTrack orderTrack = findOrderTrackById(id);
            
            // If order ID is being changed, verify the new order exists
            if (request.getOrderId() != null && !request.getOrderId().equals(orderTrack.getOrder().getId())) {
                orderRepository.findById(request.getOrderId())
                        .orElseThrow(() -> new OrderNotFoundException(request.getOrderId()));
            }
            
            // Remember old status to check for changes
            Status oldStatus = orderTrack.getStatus();
            
            orderTrackMapper.updateEntity(request, orderTrack);
            OrderTrack updatedOrderTrack = orderTrackRepository.save(orderTrack);
            
            // Update the order status if tracking status changed and it's the latest track
            if (request.getStatus() != null && request.getStatus() != oldStatus) {
                Order order = orderTrack.getOrder();
                log.info("OrderTrackServiceImpl | updateOrderTrack | Updating order status from {} to {}", 
                        order.getStatus(), request.getStatus());
                order.setStatus(request.getStatus());
                orderRepository.save(order);
            }
            
            log.info("OrderTrackServiceImpl | updateOrderTrack | Updated order track with id: {}", updatedOrderTrack.getId());
            return orderTrackMapper.toResponse(updatedOrderTrack);
        } catch (OrderTrackNotFoundException | OrderNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("OrderTrackServiceImpl | updateOrderTrack | Database error updating order track with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderTrackServiceImpl | updateOrderTrack | Unexpected error updating order track with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void deleteOrderTrack(String id) {
        log.info("OrderTrackServiceImpl | deleteOrderTrack | Deleting order track with id: {}", id);
        try {
            // Check existence first in a separate transaction
            if (!doesOrderTrackExistById(id)) {
                log.error("OrderTrackServiceImpl | deleteOrderTrack | Order track not found with id: {}", id);
                throw new OrderTrackNotFoundException(id);
            }
            
            // Then delete in the current transaction
            orderTrackRepository.deleteById(id);
            log.info("OrderTrackServiceImpl | deleteOrderTrack | Deleted order track with id: {}", id);
        } catch (OrderTrackNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("OrderTrackServiceImpl | deleteOrderTrack | Database error deleting order track with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderTrackServiceImpl | deleteOrderTrack | Unexpected error deleting order track with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Helper method to find an order track by ID.
     * Uses a separate transaction to avoid issues with the main transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private OrderTrack findOrderTrackById(String id) {
        return orderTrackRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("OrderTrackServiceImpl | findOrderTrackById | Order track not found with id: {}", id);
                    return new OrderTrackNotFoundException(id);
                });
    }
    
    /**
     * Helper method to check if an order track exists by ID.
     * Uses a separate transaction to avoid issues with the main transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    protected boolean doesOrderTrackExistById(String id) {
        return orderTrackRepository.existsById(id);
    }
}