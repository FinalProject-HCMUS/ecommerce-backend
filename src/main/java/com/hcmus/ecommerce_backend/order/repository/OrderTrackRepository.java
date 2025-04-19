package com.hcmus.ecommerce_backend.order.repository;

import com.hcmus.ecommerce_backend.order.model.entity.OrderTrack;
import com.hcmus.ecommerce_backend.order.model.enums.Status;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderTrackRepository extends JpaRepository<OrderTrack, String> {
    
    List<OrderTrack> findByOrderId(String orderId);
    
    List<OrderTrack> findByStatusOrderByUpdatedAtDesc(Status status);
    
    List<OrderTrack> findByOrderIdOrderByUpdatedAtDesc(String orderId);
}