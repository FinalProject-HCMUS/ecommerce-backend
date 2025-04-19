package com.hcmus.ecommerce_backend.order.repository;

import com.hcmus.ecommerce_backend.order.model.entity.Order;
import com.hcmus.ecommerce_backend.order.model.enums.Status;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    
    List<Order> findByCustomerId(String customerId);
    
    boolean existsByCustomerIdAndId(String customerId, String id);

    @Query("SELECT o FROM Order o WHERE " +
           "(:keyword IS NULL OR " +
           "LOWER(o.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR o.status = :status)")
    Page<Order> searchOrders(@Param("keyword") String keyword, 
                             @Param("status") Status status,
                             Pageable pageable);
}