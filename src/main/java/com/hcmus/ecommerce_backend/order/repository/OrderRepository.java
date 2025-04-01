package com.hcmus.ecommerce_backend.order.repository;

import com.hcmus.ecommerce_backend.order.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    
    List<Order> findByCustomerId(String customerId);
    
    boolean existsByCustomerIdAndId(String customerId, String id);
}