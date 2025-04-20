package com.hcmus.ecommerce_backend.message.service;

import com.hcmus.ecommerce_backend.message.model.dto.request.CreateConversationRequest;
import com.hcmus.ecommerce_backend.message.model.dto.request.UpdateConversationRequest;
import com.hcmus.ecommerce_backend.message.model.dto.response.ConversationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ConversationService {
    
    Page<ConversationResponse> getAllConversationsPaginated(Pageable pageable);
    
    Page<ConversationResponse> searchConversations(String keyword, Pageable pageable);
    
    ConversationResponse getConversationById(String id);
    
    List<ConversationResponse> getConversationsByCustomerId(String customerId);
    
    ConversationResponse createConversation(CreateConversationRequest request);
    
    ConversationResponse updateConversation(String id, UpdateConversationRequest request);
    
    void deleteConversation(String id);
}