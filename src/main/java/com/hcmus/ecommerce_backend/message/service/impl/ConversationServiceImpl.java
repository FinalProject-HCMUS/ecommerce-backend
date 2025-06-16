package com.hcmus.ecommerce_backend.message.service.impl;

import com.hcmus.ecommerce_backend.message.exception.ConversationNotFoundException;
import com.hcmus.ecommerce_backend.message.model.dto.request.CreateConversationRequest;
import com.hcmus.ecommerce_backend.message.model.dto.request.UpdateConversationRequest;
import com.hcmus.ecommerce_backend.message.model.dto.response.ConversationResponse;
import com.hcmus.ecommerce_backend.message.model.entity.Conversation;
import com.hcmus.ecommerce_backend.message.model.mapper.ConversationMapper;
import com.hcmus.ecommerce_backend.message.repository.ConversationRepository;
import com.hcmus.ecommerce_backend.message.service.ConversationService;
import com.hcmus.ecommerce_backend.user.exception.UserNotFoundException;
import com.hcmus.ecommerce_backend.user.model.entity.User;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationMapper conversationMapper;
    private final UserRepository userRepository;

    @Override
    public Page<ConversationResponse> getAllConversationsPaginated(Pageable pageable) {
        log.info("ConversationServiceImpl | getAllConversationsPaginated | Retrieving conversations with pagination - Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            Page<Conversation> conversationPage = conversationRepository.findAllSortedByLatestMessage(pageable);
            Page<ConversationResponse> conversationResponsePage = conversationPage.map(conversationMapper::toResponse);

            log.info("ConversationServiceImpl | getAllConversationsPaginated | Found {} conversations on page {} of {}",
                    conversationResponsePage.getNumberOfElements(),
                    conversationResponsePage.getNumber() + 1,
                    conversationResponsePage.getTotalPages());

            return conversationResponsePage;
        } catch (DataAccessException e) {
            log.error("ConversationServiceImpl | getAllConversationsPaginated | Database error retrieving paginated conversations: {}",
                    e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ConversationServiceImpl | getAllConversationsPaginated | Unexpected error retrieving paginated conversations: {}",
                    e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Page<ConversationResponse> searchConversations(String keyword, Pageable pageable) {
        log.info("ConversationServiceImpl | searchConversations | keyword: {}, page: {}, size: {}, sort: {}",
                keyword, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            // If search parameter is null, use standard findAll method
            Page<Conversation> conversationPage;
            if (keyword == null || keyword.trim()
                    .isEmpty()) {
                conversationPage = conversationRepository.findAll(pageable);
            } else {
                // Process keyword
                String processedKeyword = keyword.trim();
                conversationPage = conversationRepository.searchConversationsSortedByLatestMessage(processedKeyword, pageable);
            }

            Page<ConversationResponse> conversationResponsePage = conversationPage.map(conversationMapper::toResponse);

            log.info("ConversationServiceImpl | searchConversations | Found {} conversations on page {} of {}",
                    conversationResponsePage.getNumberOfElements(),
                    conversationResponsePage.getNumber() + 1,
                    conversationResponsePage.getTotalPages());

            return conversationResponsePage;
        } catch (DataAccessException e) {
            log.error("ConversationServiceImpl | searchConversations | Database error searching conversations: {}",
                    e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ConversationServiceImpl | searchConversations | Unexpected error searching conversations: {}",
                    e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public ConversationResponse getConversationById(String id) {
        log.info("ConversationServiceImpl | getConversationById | id: {}", id);
        try {
            Conversation conversation = findConversationById(id);
            return conversationMapper.toResponse(conversation);
        } catch (ConversationNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("ConversationServiceImpl | getConversationById | Database error retrieving conversation {}: {}",
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ConversationServiceImpl | getConversationById | Unexpected error retrieving conversation {}: {}",
                    id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<ConversationResponse> getConversationsByCustomerId(String customerId) {
        log.info("ConversationServiceImpl | getConversationsByCustomerId | customerId: {}", customerId);
        try {
            List<Conversation> conversations = conversationRepository.findByCustomerId(customerId);
            return conversations.stream()
                    .map(conversationMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            log.error("ConversationServiceImpl | getConversationsByCustomerId | Database error retrieving conversations for customer {}: {}",
                    customerId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ConversationServiceImpl | getConversationsByCustomerId | Unexpected error retrieving conversations for customer {}: {}",
                    customerId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ConversationResponse createConversation(CreateConversationRequest request) {
        log.info("ConversationServiceImpl | createConversation | Creating conversation for customer: {}",
                request.getCustomerId());
        try {
            // First fetch the customer user to ensure it exists
            User customer = userRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> {
                        log.warn("ConversationServiceImpl | createConversation | User not found with id: {}",
                                request.getCustomerId());
                        return new UserNotFoundException("User not found with id: " + request.getCustomerId());
                    });

            Conversation conversation = Conversation.builder()
                    .customer(customer)  // Set the customer properly
                    .isAdminRead(false)
                    .isCustomerRead(true)
                    .build();

            Conversation savedConversation = conversationRepository.save(conversation);

            // Make sure savedConversation.getCustomer() is not null before accessing getId()
            if (savedConversation.getCustomer() == null) {
                log.error("ConversationServiceImpl | createConversation | Customer is null in saved conversation");
                throw new IllegalStateException("Customer is null in saved conversation");
            }

            log.info("ConversationServiceImpl | createConversation | Created conversation with id: {} for customer: {}",
                    savedConversation.getId(), savedConversation.getCustomer()
                            .getId());

            return conversationMapper.toResponse(savedConversation);
        } catch (UserNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("ConversationServiceImpl | createConversation | Database error creating conversation for customer {}: {}",
                    request.getCustomerId(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ConversationServiceImpl | createConversation | Unexpected error creating conversation for customer {}: {}",
                    request.getCustomerId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ConversationResponse updateConversation(String id, UpdateConversationRequest request) {
        log.info("ConversationServiceImpl | updateConversation | Updating conversation with id: {}", id);
        try {
            Conversation conversation = findConversationById(id);

            // Update only the fields that are provided in the request
            conversationMapper.updateEntity(request, conversation);

            // Apply conditional updates
            if (request.getIsAdminRead() != null) {
                conversation.setAdminRead(request.getIsAdminRead());
            }
            if (request.getIsCustomerRead() != null) {
                conversation.setCustomerRead(request.getIsCustomerRead());
            }

            Conversation updatedConversation = conversationRepository.save(conversation);
            log.info("ConversationServiceImpl | updateConversation | Updated conversation with id: {}", id);
            return conversationMapper.toResponse(updatedConversation);
        } catch (ConversationNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("ConversationServiceImpl | updateConversation | Database error updating conversation with id '{}': {}",
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ConversationServiceImpl | updateConversation | Unexpected error updating conversation with id '{}': {}",
                    id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteConversation(String id) {
        log.info("ConversationServiceImpl | deleteConversation | Deleting conversation with id: {}", id);
        try {
            Conversation conversation = findConversationById(id);
            conversationRepository.delete(conversation);
            log.info("ConversationServiceImpl | deleteConversation | Deleted conversation with id: {}", id);
        } catch (ConversationNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("ConversationServiceImpl | deleteConversation | Database error deleting conversation with id '{}': {}",
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ConversationServiceImpl | deleteConversation | Unexpected error deleting conversation with id '{}': {}",
                    id, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    protected Conversation findConversationById(String id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("ConversationServiceImpl | findConversationById | Conversation not found with id: {}", id);
                    return new ConversationNotFoundException(id);
                });
    }
}