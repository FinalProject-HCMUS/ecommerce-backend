package com.hcmus.ecommerce_backend.message.controller;

import com.hcmus.ecommerce_backend.common.model.dto.CustomResponse;
import com.hcmus.ecommerce_backend.common.utils.CreatePageable;
import com.hcmus.ecommerce_backend.message.model.dto.request.CreateMessageRequest;
import com.hcmus.ecommerce_backend.message.model.dto.response.MessageResponse;
import com.hcmus.ecommerce_backend.message.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Message", description = "Message management APIs")
public class MessageController {
    
    private final MessageService messageService;
    
    @Operation(summary = "Get all messages with pagination", description = "Retrieves a paginated list of messages with sorting capabilities")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated messages")
    @GetMapping
    public ResponseEntity<CustomResponse<Page<MessageResponse>>> getAllMessagesPaginated(
            @Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "10") int size,
            
            @Parameter(description = "Sorting criteria in the format: property(,asc|desc). " +
                    "Default sort order is ascending. " +
                    "Multiple sort criteria are supported.") @RequestParam(required = false) String[] sort) {
        
        log.info("MessageController | getAllMessagesPaginated | page: {}, size: {}, sort: {}",
                page, size, sort != null ? String.join(", ", sort) : "unsorted");
        
        Pageable pageable = CreatePageable.build(page, size, sort);
        Page<MessageResponse> messages = messageService.getAllMessagesPaginated(pageable);
        
        return ResponseEntity.ok(CustomResponse.successOf(messages));
    }
    
    @Operation(summary = "Search messages", description = "Search messages by content with pagination and sorting")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved search results")
    @GetMapping("/search")
    public ResponseEntity<CustomResponse<Page<MessageResponse>>> searchMessages(
            @Parameter(description = "Keyword to search in message content") @RequestParam(required = false) String keyword,
            
            @Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "10") int size,
            
            @Parameter(description = "Sorting criteria in the format: property(,asc|desc). " +
                    "Default sort order is ascending. " +
                    "Multiple sort criteria are supported.") @RequestParam(required = false) String[] sort) {
        
        log.info("MessageController | searchMessages | keyword: {}, page: {}, size: {}, sort: {}",
                keyword, page, size, sort != null ? String.join(", ", sort) : "unsorted");
        
        Pageable pageable = CreatePageable.build(page, size, sort);
        Page<MessageResponse> messages = messageService.searchMessages(keyword, pageable);
        
        return ResponseEntity.ok(CustomResponse.successOf(messages));
    }
    
    @Operation(summary = "Get message by ID", description = "Retrieves a specific message by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message found"),
            @ApiResponse(responseCode = "404", description = "Message not found", 
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                    schema = @Schema(implementation = CustomResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<MessageResponse>> getMessageById(
            @Parameter(description = "ID of the message to retrieve", required = true) @PathVariable String id) {
        
        log.info("MessageController | getMessageById | id: {}", id);
        MessageResponse message = messageService.getMessageById(id);
        
        return ResponseEntity.ok(CustomResponse.successOf(message));
    }
    
    @Operation(summary = "Get messages by conversation ID", description = "Retrieves all messages for a specific conversation")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved conversation's messages")
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<CustomResponse<List<MessageResponse>>> getMessagesByConversationId(
            @Parameter(description = "ID of the conversation", required = true) @PathVariable String conversationId) {
        
        log.info("MessageController | getMessagesByConversationId | conversationId: {}", conversationId);
        List<MessageResponse> messages = messageService.getMessagesByConversationId(conversationId);
        
        return ResponseEntity.ok(CustomResponse.successOf(messages));
    }
    
    @Operation(summary = "Get messages by conversation ID with pagination", description = "Retrieves a paginated list of messages for a specific conversation")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated conversation messages")
    @GetMapping("/conversation/{conversationId}/paginated")
    public ResponseEntity<CustomResponse<Page<MessageResponse>>> getMessagesByConversationIdPaginated(
            @Parameter(description = "ID of the conversation", required = true) @PathVariable String conversationId,
            
            @Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "10") int size,
            
            @Parameter(description = "Sorting criteria in the format: property(,asc|desc). " +
                    "Default sort order is ascending. " +
                    "Multiple sort criteria are supported.") @RequestParam(required = false) String[] sort) {
        
        log.info("MessageController | getMessagesByConversationIdPaginated | conversationId: {}, page: {}, size: {}, sort: {}",
                conversationId, page, size, sort != null ? String.join(", ", sort) : "unsorted");
        
        Pageable pageable = CreatePageable.build(page, size, sort);
        Page<MessageResponse> messages = messageService.getMessagesByConversationIdPaginated(conversationId, pageable);
        
        return ResponseEntity.ok(CustomResponse.successOf(messages));
    }
    
    @Operation(summary = "Search messages by conversation", description = "Search messages within a conversation by content with pagination and sorting")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved search results")
    @GetMapping("/conversation/{conversationId}/search")
    public ResponseEntity<CustomResponse<Page<MessageResponse>>> searchMessagesByConversation(
            @Parameter(description = "ID of the conversation", required = true) @PathVariable String conversationId,
            
            @Parameter(description = "Keyword to search in message content") @RequestParam(required = false) String keyword,
            
            @Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "10") int size,
            
            @Parameter(description = "Sorting criteria in the format: property(,asc|desc). " +
                    "Default sort order is ascending. " +
                    "Multiple sort criteria are supported.") @RequestParam(required = false) String[] sort) {
        
        log.info("MessageController | searchMessagesByConversation | conversationId: {}, keyword: {}, page: {}, size: {}, sort: {}",
                conversationId, keyword, page, size, sort != null ? String.join(", ", sort) : "unsorted");
        
        Pageable pageable = CreatePageable.build(page, size, sort);
        Page<MessageResponse> messages = messageService.searchMessagesByConversation(conversationId, keyword, pageable);
        
        return ResponseEntity.ok(CustomResponse.successOf(messages));
    }
    
    @Operation(summary = "Get messages by user ID", description = "Retrieves all messages sent by a specific user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user's messages")
    @GetMapping("/user/{userId}")
    public ResponseEntity<CustomResponse<List<MessageResponse>>> getMessagesByUserId(
            @Parameter(description = "ID of the user", required = true) @PathVariable String userId) {
        
        log.info("MessageController | getMessagesByUserId | userId: {}", userId);
        List<MessageResponse> messages = messageService.getMessagesByUserId(userId);
        
        return ResponseEntity.ok(CustomResponse.successOf(messages));
    }
    
    @Operation(summary = "Create a new message", description = "Creates a new message with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Message successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", 
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                    schema = @Schema(implementation = CustomResponse.class))),
            @ApiResponse(responseCode = "404", description = "Conversation not found", 
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                    schema = @Schema(implementation = CustomResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CustomResponse<MessageResponse>> createMessage(
            @Parameter(description = "Message information for creation", required = true) 
            @Valid @RequestBody CreateMessageRequest request) {
        
        log.info("MessageController | createMessage | request: {}", request);
        MessageResponse createdMessage = messageService.createMessage(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.<MessageResponse>builder()
                        .httpStatus(HttpStatus.CREATED)
                        .isSuccess(true)
                        .data(createdMessage)
                        .build());
    }
    
    @Operation(summary = "Delete a message", description = "Deletes a message by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Message not found", 
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                    schema = @Schema(implementation = CustomResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteMessage(
            @Parameter(description = "ID of the message to delete", required = true) @PathVariable String id) {
        
        log.info("MessageController | deleteMessage | id: {}", id);
        messageService.deleteMessage(id);
        
        return ResponseEntity.ok(CustomResponse.SUCCESS);
    }

    @Operation(summary = "Mark a message as read", description = "Updates a message's read status")
    @PutMapping("/{id}/read")
    public ResponseEntity<CustomResponse<MessageResponse>> markMessageAsRead(
            @Parameter(description = "ID of the message to mark as read", required = true) @PathVariable String id) {

        log.info("MessageController | markMessageAsRead | id: {}", id);
        MessageResponse message = messageService.markMessageAsRead(id);

        return ResponseEntity.ok(CustomResponse.successOf(message));
    }
}