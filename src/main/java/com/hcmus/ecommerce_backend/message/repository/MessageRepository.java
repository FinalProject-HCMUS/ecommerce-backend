package com.hcmus.ecommerce_backend.message.repository;

import com.hcmus.ecommerce_backend.message.model.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    
    List<Message> findByConversationId(String conversationId);
    
    Page<Message> findByConversationId(String conversationId, Pageable pageable);
    
    List<Message> findByUserId(String userId);
    
    @Query("SELECT m FROM Message m WHERE " +
           "(:keyword IS NULL OR " +
           "LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Message> searchMessages(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId AND " +
           "(:keyword IS NULL OR LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Message> searchMessagesByConversation(
            @Param("conversationId") String conversationId, 
            @Param("keyword") String keyword, 
            Pageable pageable);
}