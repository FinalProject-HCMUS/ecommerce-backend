package com.hcmus.ecommerce_backend.message.service.impl;

import com.hcmus.ecommerce_backend.message.exception.MessageNotFoundException;
import com.hcmus.ecommerce_backend.message.model.dto.request.CreateMessageRequest;
import com.hcmus.ecommerce_backend.message.model.dto.response.MessageResponse;
import com.hcmus.ecommerce_backend.message.model.entity.Message;
import com.hcmus.ecommerce_backend.message.model.mapper.MessageMapper;
import com.hcmus.ecommerce_backend.message.repository.MessageRepository;
import com.hcmus.ecommerce_backend.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {
    
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    
    @Override
    public List<MessageResponse> getAllMessages() {
        log.info("MessageServiceImpl | getAllMessages | Retrieving all messages");
        try {
            List<MessageResponse> messages = messageRepository.findAll().stream()
                    .map(messageMapper::toResponse)
                    .collect(Collectors.toList());
            log.info("MessageServiceImpl | getAllMessages | Found {} messages", messages.size());
            return messages;
        } catch (DataAccessException e) {
            log.error("MessageServiceImpl | getAllMessages | Error retrieving messages: {}", e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("MessageServiceImpl | getAllMessages | Unexpected error: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public MessageResponse getMessageById(String id) {
        log.info("MessageServiceImpl | getMessageById | id: {}", id);
        try {
            Message message = findMessageById(id);
            log.info("MessageServiceImpl | getMessageById | Message found with id: {}", message.getId());
            return messageMapper.toResponse(message);
        } catch (MessageNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("MessageServiceImpl | getMessageById | Database error for id {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("MessageServiceImpl | getMessageById | Unexpected error for id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public List<MessageResponse> getMessagesByCustomerId(String customerId) {
        log.info("MessageServiceImpl | getMessagesByCustomerId | customerId: {}", customerId);
        try {
            List<MessageResponse> messages = messageRepository.findByCustomerId(customerId).stream()
                    .map(messageMapper::toResponse)
                    .collect(Collectors.toList());
            log.info("MessageServiceImpl | getMessagesByCustomerId | Found {} messages for customer {}", 
                    messages.size(), customerId);
            return messages;
        } catch (DataAccessException e) {
            log.error("MessageServiceImpl | getMessagesByCustomerId | Database error for customerId {}: {}", 
                    customerId, e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("MessageServiceImpl | getMessagesByCustomerId | Unexpected error for customerId {}: {}", 
                    customerId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public List<MessageResponse> getMessagesByAdminId(String adminId) {
        log.info("MessageServiceImpl | getMessagesByAdminId | adminId: {}", adminId);
        try {
            List<MessageResponse> messages = messageRepository.findByAdminId(adminId).stream()
                    .map(messageMapper::toResponse)
                    .collect(Collectors.toList());
            log.info("MessageServiceImpl | getMessagesByAdminId | Found {} messages for admin {}", 
                    messages.size(), adminId);
            return messages;
        } catch (DataAccessException e) {
            log.error("MessageServiceImpl | getMessagesByAdminId | Database error for adminId {}: {}", 
                    adminId, e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("MessageServiceImpl | getMessagesByAdminId | Unexpected error for adminId {}: {}", 
                    adminId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public List<MessageResponse> getMessagesByCustomerIdAndAdminId(String customerId, String adminId) {
        log.info("MessageServiceImpl | getMessagesByCustomerIdAndAdminId | customerId: {}, adminId: {}", customerId, adminId);
        try {
            List<MessageResponse> messages = messageRepository.findByCustomerIdAndAdminId(customerId, adminId).stream()
                    .map(messageMapper::toResponse)
                    .collect(Collectors.toList());
            log.info("MessageServiceImpl | getMessagesByCustomerIdAndAdminId | Found {} messages for customer {} and admin {}", 
                    messages.size(), customerId, adminId);
            return messages;
        } catch (DataAccessException e) {
            log.error("MessageServiceImpl | getMessagesByCustomerIdAndAdminId | Database error for customerId {} and adminId {}: {}", 
                    customerId, adminId, e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("MessageServiceImpl | getMessagesByCustomerIdAndAdminId | Unexpected error for customerId {} and adminId {}: {}", 
                    customerId, adminId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public MessageResponse createMessage(CreateMessageRequest request) {
        log.info("MessageServiceImpl | createMessage | Creating message from {} role", request.getRoleChat());
        try {
            Message message = messageMapper.toEntity(request);
            
            Message savedMessage = messageRepository.save(message);
            log.info("MessageServiceImpl | createMessage | Created message with id: {}", savedMessage.getId());
            return messageMapper.toResponse(savedMessage);
        } catch (DataAccessException e) {
            log.error("MessageServiceImpl | createMessage | Database error creating message: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("MessageServiceImpl | createMessage | Unexpected error creating message: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void deleteMessage(String id) {
        log.info("MessageServiceImpl | deleteMessage | Deleting message with id: {}", id);
        try {
            // Check existence first in a separate transaction
            if (!doesMessageExistById(id)) {
                log.error("MessageServiceImpl | deleteMessage | Message not found with id: {}", id);
                throw new MessageNotFoundException(id);
            }
            
            // Then delete in the current transaction
            messageRepository.deleteById(id);
            log.info("MessageServiceImpl | deleteMessage | Deleted message with id: {}", id);
        } catch (MessageNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("MessageServiceImpl | deleteMessage | Database error deleting message with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("MessageServiceImpl | deleteMessage | Unexpected error deleting message with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Helper method to find a message by ID.
     * Uses a separate transaction to avoid issues with the main transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private Message findMessageById(String id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("MessageServiceImpl | findMessageById | Message not found with id: {}", id);
                    return new MessageNotFoundException(id);
                });
    }
    
    /**
     * Helper method to check if a message exists by ID.
     * Uses a separate transaction to avoid issues with the main transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private boolean doesMessageExistById(String id) {
        return messageRepository.existsById(id);
    }
}