package com.hcmus.ecommerce_backend.order.service.impl;

import com.hcmus.ecommerce_backend.order.exception.OrderNotFoundException;
import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateOrderRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderResponse;
import com.hcmus.ecommerce_backend.order.model.entity.Order;
import com.hcmus.ecommerce_backend.order.model.enums.Status;
import com.hcmus.ecommerce_backend.order.model.mapper.OrderMapper;
import com.hcmus.ecommerce_backend.order.repository.OrderRepository;
import com.hcmus.ecommerce_backend.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        log.info("OrderServiceImpl | getAllOrders | Retrieving orders with pagination - Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            Page<Order> orderPage = orderRepository.findAll(pageable);
            Page<OrderResponse> orderResponsePage = orderPage.map(orderMapper::toResponse);

            log.info("OrderServiceImpl | getAllOrders | Found {} orders on page {} of {}",
                    orderResponsePage.getNumberOfElements(),
                    orderResponsePage.getNumber() + 1,
                    orderResponsePage.getTotalPages());

            return orderResponsePage;
        } catch (DataAccessException e) {
            log.error("OrderServiceImpl | getAllOrders | Database error retrieving paginated orders: {}",
                    e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderServiceImpl | getAllOrders | Unexpected error retrieving paginated orders: {}",
                    e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Page<OrderResponse> searchOrders(String keyword, Status status, Pageable pageable) {
        log.info("OrderServiceImpl | searchOrders | keyword: {}, status: {}, page: {}, size: {}, sort: {}",
                keyword, status, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            // If both search parameters are null, use the standard findAll method
            Page<Order> orderPage;
            if ((keyword == null || keyword.trim().isEmpty()) && status == null) {
                orderPage = orderRepository.findAll(pageable);
            } else {
                orderPage = orderRepository.searchOrders(
                        keyword == null || keyword.trim().isEmpty() ? null : keyword.trim(),
                        status,
                        pageable);
            }

            Page<OrderResponse> orderResponsePage = orderPage.map(orderMapper::toResponse);

            log.info("OrderServiceImpl | searchOrders | Found {} orders on page {} of {}",
                    orderResponsePage.getNumberOfElements(),
                    orderResponsePage.getNumber() + 1,
                    orderResponsePage.getTotalPages());

            return orderResponsePage;
        } catch (DataAccessException e) {
            log.error("OrderServiceImpl | searchOrders | Database error searching orders: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderServiceImpl | searchOrders | Unexpected error searching orders: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public OrderResponse getOrderById(String id) {
        log.info("OrderServiceImpl | getOrderById | id: {}", id);
        try {
            Order order = findOrderById(id);
            log.info("OrderServiceImpl | getOrderById | Order found with id: {}", order.getId());
            return orderMapper.toResponse(order);
        } catch (OrderNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("OrderServiceImpl | getOrderById | Database error for id {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderServiceImpl | getOrderById | Unexpected error for id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<OrderResponse> getOrdersByCustomerId(String customerId) {
        log.info("OrderServiceImpl | getOrdersByCustomerId | customerId: {}", customerId);
        try {
            List<OrderResponse> orders = orderRepository.findByCustomerId(customerId).stream()
                    .map(orderMapper::toResponse)
                    .collect(Collectors.toList());
            log.info("OrderServiceImpl | getOrdersByCustomerId | Found {} orders for customer {}", orders.size(),
                    customerId);
            return orders;
        } catch (DataAccessException e) {
            log.error("OrderServiceImpl | getOrdersByCustomerId | Database error for customerId {}: {}", customerId,
                    e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("OrderServiceImpl | getOrdersByCustomerId | Unexpected error for customerId {}: {}", customerId,
                    e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("OrderServiceImpl | createOrder | Creating order for customer: {}", request.getCustomerId());
        try {
            Order order = orderMapper.toEntity(request);

            Order savedOrder = orderRepository.save(order);
            log.info("OrderServiceImpl | createOrder | Created order with id: {}", savedOrder.getId());
            return orderMapper.toResponse(savedOrder);
        } catch (DataAccessException e) {
            log.error("OrderServiceImpl | createOrder | Database error creating order for customer {}: {}",
                    request.getCustomerId(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderServiceImpl | createOrder | Unexpected error creating order for customer {}: {}",
                    request.getCustomerId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(String id, UpdateOrderRequest request) {
        log.info("OrderServiceImpl | updateOrder | Updating order with id: {}", id);
        try {
            Order order = findOrderById(id);

            orderMapper.updateEntity(request, order);
            Order updatedOrder = orderRepository.save(order);
            log.info("OrderServiceImpl | updateOrder | Updated order with id: {}", updatedOrder.getId());
            return orderMapper.toResponse(updatedOrder);
        } catch (OrderNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("OrderServiceImpl | updateOrder | Database error updating order with id '{}': {}",
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderServiceImpl | updateOrder | Unexpected error updating order with id '{}': {}",
                    id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteOrder(String id) {
        log.info("OrderServiceImpl | deleteOrder | Deleting order with id: {}", id);
        try {
            // Check existence first in a separate transaction
            if (!doesOrderExistById(id)) {
                log.error("OrderServiceImpl | deleteOrder | Order not found with id: {}", id);
                throw new OrderNotFoundException(id);
            }

            // Then delete in the current transaction
            orderRepository.deleteById(id);
            log.info("OrderServiceImpl | deleteOrder | Deleted order with id: {}", id);
        } catch (OrderNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("OrderServiceImpl | deleteOrder | Database error deleting order with id '{}': {}",
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("OrderServiceImpl | deleteOrder | Unexpected error deleting order with id '{}': {}",
                    id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Helper method to find an order by ID.
     * Uses a separate transaction to avoid issues with the main transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private Order findOrderById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("OrderServiceImpl | findOrderById | Order not found with id: {}", id);
                    return new OrderNotFoundException(id);
                });
    }

    /**
     * Helper method to check if an order exists by ID.
     * Uses a separate transaction to avoid issues with the main transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private boolean doesOrderExistById(String id) {
        return orderRepository.existsById(id);
    }
}