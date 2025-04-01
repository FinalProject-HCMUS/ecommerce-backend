package com.hcmus.ecommerce_backend.message.service;

import com.hcmus.ecommerce_backend.message.model.dto.request.CreateMessageRequest;
import com.hcmus.ecommerce_backend.message.model.dto.response.MessageResponse;

import java.util.List;

public interface MessageService {
    
    List<MessageResponse> getAllMessages();
    
    MessageResponse getMessageById(String id);
    
    List<MessageResponse> getMessagesByCustomerId(String customerId);
    
    List<MessageResponse> getMessagesByAdminId(String adminId);
    
    List<MessageResponse> getMessagesByCustomerIdAndAdminId(String customerId, String adminId);
    
    MessageResponse createMessage(CreateMessageRequest request);
        
    void deleteMessage(String id);
}