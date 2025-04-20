package com.hcmus.ecommerce_backend.message.service;

import com.hcmus.ecommerce_backend.message.model.dto.request.CreateMessageRequest;
import com.hcmus.ecommerce_backend.message.model.dto.response.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MessageService {
    
    Page<MessageResponse> getAllMessagesPaginated(Pageable pageable);
    
    Page<MessageResponse> searchMessages(String keyword, Pageable pageable);
    
    MessageResponse getMessageById(String id);
    
    List<MessageResponse> getMessagesByConversationId(String conversationId);
    
    Page<MessageResponse> getMessagesByConversationIdPaginated(String conversationId, Pageable pageable);
    
    Page<MessageResponse> searchMessagesByConversation(String conversationId, String keyword, Pageable pageable);
    
    List<MessageResponse> getMessagesByUserId(String userId);
    
    MessageResponse createMessage(CreateMessageRequest request);
    
    void deleteMessage(String id);
}