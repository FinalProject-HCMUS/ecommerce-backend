package com.hcmus.ecommerce_backend.order.repository;

import com.hcmus.ecommerce_backend.order.model.entity.CartItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItems, String> {
    
    List<CartItems> findByUserId(String userId);
    
    List<CartItems> findByOrderId(String orderId);
    
    Optional<CartItems> findByUserIdAndItemId(String userId, String itemId);
    
    boolean existsByUserIdAndItemId(String userId, String itemId);
    
    void deleteByUserIdAndItemId(String userId, String itemId);
}