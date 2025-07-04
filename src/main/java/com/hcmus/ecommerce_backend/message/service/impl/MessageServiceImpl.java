package com.hcmus.ecommerce_backend.message.service.impl;

import com.hcmus.ecommerce_backend.message.exception.ConversationNotFoundException;
import com.hcmus.ecommerce_backend.message.exception.MessageNotFoundException;
import com.hcmus.ecommerce_backend.message.model.dto.request.CreateMessageRequest;
import com.hcmus.ecommerce_backend.message.model.dto.response.MessageResponse;
import com.hcmus.ecommerce_backend.message.model.entity.Conversation;
import com.hcmus.ecommerce_backend.message.model.entity.Message;
import com.hcmus.ecommerce_backend.message.model.mapper.MessageMapper;
import com.hcmus.ecommerce_backend.message.repository.ConversationRepository;
import com.hcmus.ecommerce_backend.message.repository.MessageRepository;
import com.hcmus.ecommerce_backend.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {
    
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final MessageMapper messageMapper;

    @Override
    public Page<MessageResponse> getAllMessagesPaginated(Pageable pageable) {
        log.info("MessageServiceImpl | getAllMessagesPaginated | Retrieving messages with pagination - Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            Page<Message> messagePage = messageRepository.findAll(pageable);
            Page<MessageResponse> messageResponsePage = messagePage.map(messageMapper::toResponse);
            
            log.info("MessageServiceImpl | getAllMessagesPaginated | Found {} messages on page {} of {}",
                    messageResponsePage.getNumberOfElements(),
                    messageResponsePage.getNumber() + 1,
                    messageResponsePage.getTotalPages());
            
            return messageResponsePage;
        } catch (DataAccessException e) {
            log.error("MessageServiceImpl | getAllMessagesPaginated | Database error retrieving paginated messages: {}",
                    e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("MessageServiceImpl | getAllMessagesPaginated | Unexpected error retrieving paginated messages: {}",
                    e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public Page<MessageResponse> searchMessages(String keyword, Pageable pageable) {
        log.info("MessageServiceImpl | searchMessages | keyword: {}, page: {}, size: {}, sort: {}",
                keyword, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            Page<Message> messagePage;
            if (keyword == null || keyword.trim().isEmpty()) {
                messagePage = messageRepository.findAll(pageable);
            } else {
                messagePage = messageRepository.searchMessages(keyword.trim(), pageable);
            }
            
            Page<MessageResponse> messageResponsePage = messagePage.map(messageMapper::toResponse);
            
            log.info("MessageServiceImpl | searchMessages | Found {} messages on page {} of {}",
                    messageResponsePage.getNumberOfElements(),
                    messageResponsePage.getNumber() + 1,
                    messageResponsePage.getTotalPages());
            
            return messageResponsePage;
        } catch (DataAccessException e) {
            log.error("MessageServiceImpl | searchMessages | Database error searching messages: {}",
                    e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("MessageServiceImpl | searchMessages | Unexpected error searching messages: {}",
                    e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public MessageResponse markMessageAsRead(String id) {
        log.info("MessageServiceImpl | markMessageAsRead | id: {}", id);
        try {
            Message message = findMessageById(id);
            message.setRead(true);
            message.setReadAt(LocalDateTime.now());
            Message savedMessage = messageRepository.save(message);
            return messageMapper.toResponse(savedMessage);
        } catch (MessageNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("MessageServiceImpl | markMessageAsRead | Error marking message as read: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public MessageResponse getMessageById(String id) {
        log.info("MessageServiceImpl | getMessageById | id: {}", id);
        try {
            Message message = findMessageById(id);
            return messageMapper.toResponse(message);
        } catch (MessageNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("MessageServiceImpl | getMessageById | Database error retrieving message {}: {}",
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("MessageServiceImpl | getMessageById | Unexpected error retrieving message {}: {}",
                    id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public List<MessageResponse> getMessagesByConversationId(String conversationId) {
        log.info("MessageServiceImpl | getMessagesByConversationId | conversationId: {}", conversationId);
        try {
            List<Message> messages = messageRepository.findByConversationId(conversationId);
            return messages.stream()
                    .map(messageMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            log.error("MessageServiceImpl | getMessagesByConversationId | Database error retrieving messages for conversation {}: {}",
                    conversationId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("MessageServiceImpl | getMessagesByConversationId | Unexpected error retrieving messages for conversation {}: {}",
                    conversationId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public Page<MessageResponse> getMessagesByConversationIdPaginated(String conversationId, Pageable pageable) {
        log.info("MessageServiceImpl | getMessagesByConversationIdPaginated | conversationId: {}, page: {}, size: {}, sort: {}",
                conversationId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            Page<Message> messagePage = messageRepository.findByConversationId(conversationId, pageable);
            Page<MessageResponse> messageResponsePage = messagePage.map(messageMapper::toResponse);
            
            log.info("MessageServiceImpl | getMessagesByConversationIdPaginated | Found {} messages on page {} of {} for conversation {}",
                    messageResponsePage.getNumberOfElements(),
                    messageResponsePage.getNumber() + 1,
                    messageResponsePage.getTotalPages(),
                    conversationId);
            
            return messageResponsePage;
        } catch (DataAccessException e) {
            log.error("MessageServiceImpl | getMessagesByConversationIdPaginated | Database error retrieving paginated messages for conversation {}: {}",
                    conversationId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("MessageServiceImpl | getMessagesByConversationIdPaginated | Unexpected error retrieving paginated messages for conversation {}: {}",
                    conversationId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public Page<MessageResponse> searchMessagesByConversation(String conversationId, String keyword, Pageable pageable) {
        log.info("MessageServiceImpl | searchMessagesByConversation | conversationId: {}, keyword: {}, page: {}, size: {}, sort: {}",
                conversationId, keyword, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            Page<Message> messagePage;
            if (keyword == null || keyword.trim().isEmpty()) {
                messagePage = messageRepository.findByConversationId(conversationId, pageable);
            } else {
                messagePage = messageRepository.searchMessagesByConversation(conversationId, keyword.trim(), pageable);
            }
            
            Page<MessageResponse> messageResponsePage = messagePage.map(messageMapper::toResponse);
            
            log.info("MessageServiceImpl | searchMessagesByConversation | Found {} messages on page {} of {} for conversation {}",
                    messageResponsePage.getNumberOfElements(),
                    messageResponsePage.getNumber() + 1,
                    messageResponsePage.getTotalPages(),
                    conversationId);
            
            return messageResponsePage;
        } catch (DataAccessException e) {
            log.error("MessageServiceImpl | searchMessagesByConversation | Database error searching messages for conversation {}: {}",
                    conversationId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("MessageServiceImpl | searchMessagesByConversation | Unexpected error searching messages for conversation {}: {}",
                    conversationId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public List<MessageResponse> getMessagesByUserId(String userId) {
        log.info("MessageServiceImpl | getMessagesByUserId | userId: {}", userId);
        try {
            List<Message> messages = messageRepository.findByUserId(userId);
            return messages.stream()
                    .map(messageMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            log.error("MessageServiceImpl | getMessagesByUserId | Database error retrieving messages for user {}: {}",
                    userId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("MessageServiceImpl | getMessagesByUserId | Unexpected error retrieving messages for user {}: {}",
                    userId, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public MessageResponse createMessage(CreateMessageRequest request) {
        log.info("MessageServiceImpl | createMessage | Creating message for conversation: {}", request.getConversationId());
        try {
            // Check if conversation exists
            Conversation conversation = conversationRepository.findById(request.getConversationId())
                    .orElseThrow(() -> {
                        log.warn("MessageServiceImpl | createMessage | Conversation not found with id: {}", request.getConversationId());
                        return new ConversationNotFoundException(request.getConversationId());
                    });

            Message message = messageMapper.toEntity(request);
            message.setConversation(conversation);

            Message savedMessage = messageRepository.save(message);

            // Update conversation read status
            if (conversation.getCustomer().getId().equals(request.getUserId())) {
                conversation.setCustomerRead(true);
                conversation.setAdminRead(false);
            } else {
                conversation.setAdminRead(true);
                conversation.setCustomerRead(false);
            }
            conversationRepository.save(conversation);

            log.info("MessageServiceImpl | createMessage | Created message with id: {} in conversation: {}",
                    savedMessage.getId(), savedMessage.getConversation().getId());

            return messageMapper.toResponse(savedMessage);
        } catch (ConversationNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("MessageServiceImpl | createMessage | Database error creating message for conversation {}: {}",
                    request.getConversationId(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("MessageServiceImpl | createMessage | Unexpected error creating message for conversation {}: {}",
                    request.getConversationId(), e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void deleteMessage(String id) {
        log.info("MessageServiceImpl | deleteMessage | Deleting message with id: {}", id);
        try {
            Message message = findMessageById(id);
            messageRepository.delete(message);
            log.info("MessageServiceImpl | deleteMessage | Deleted message with id: {}", id);
        } catch (MessageNotFoundException e) {
            throw e;
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
    
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Message findMessageById(String id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("MessageServiceImpl | findMessageById | Message not found with id: {}", id);
                    return new MessageNotFoundException(id);
                });
    }
}