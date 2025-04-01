package com.hcmus.ecommerce_backend.message.repository;

import com.hcmus.ecommerce_backend.message.model.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    
    List<Message> findByCustomerId(String customerId);
    
    List<Message> findByAdminId(String adminId);
    
    List<Message> findByCustomerIdAndAdminId(String customerId, String adminId);
}