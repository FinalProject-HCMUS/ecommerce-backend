package com.hcmus.ecommerce_backend.message.repository;

import com.hcmus.ecommerce_backend.message.model.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    
    Page<Conversation> findAll(Pageable pageable);
    
    List<Conversation> findByCustomerId(String customerId);
    
    Optional<Conversation> findByCustomerIdAndId(String customerId, String id);
    
    @Query("SELECT c FROM Conversation c WHERE " +
          "(:keyword IS NULL OR " +
          "LOWER(c.customer.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
          "LOWER(c.customer.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
          "LOWER(c.customer.id) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Conversation> searchConversations(@Param("keyword") String keyword, Pageable pageable);
    
    boolean existsByCustomerId(String customerId);
}