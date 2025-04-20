package com.hcmus.ecommerce_backend.message.controller;

import com.hcmus.ecommerce_backend.common.model.dto.CustomResponse;
import com.hcmus.ecommerce_backend.common.utils.CreatePageable;
import com.hcmus.ecommerce_backend.message.model.dto.request.CreateConversationRequest;
import com.hcmus.ecommerce_backend.message.model.dto.request.UpdateConversationRequest;
import com.hcmus.ecommerce_backend.message.model.dto.response.ConversationResponse;
import com.hcmus.ecommerce_backend.message.service.ConversationService;
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
@RequestMapping("/conversations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Conversation", description = "Conversation management APIs")
public class ConversationController {
    
    private final ConversationService conversationService;
    
    @Operation(summary = "Get all conversations with pagination", description = "Retrieves a paginated list of conversations with sorting capabilities")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated conversations")
    @GetMapping
    public ResponseEntity<CustomResponse<Page<ConversationResponse>>> getAllConversationsPaginated(
            @Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "10") int size,
            
            @Parameter(description = "Sorting criteria in the format: property(,asc|desc). " +
                    "Default sort order is ascending. " +
                    "Multiple sort criteria are supported.") @RequestParam(required = false) String[] sort) {
        
        log.info("ConversationController | getAllConversationsPaginated | page: {}, size: {}, sort: {}",
                page, size, sort != null ? String.join(", ", sort) : "unsorted");
        
        Pageable pageable = CreatePageable.build(page, size, sort);
        Page<ConversationResponse> conversations = conversationService.getAllConversationsPaginated(pageable);
        
        return ResponseEntity.ok(CustomResponse.successOf(conversations));
    }
    
    @Operation(summary = "Search conversations", description = "Search conversations by keyword with pagination and sorting")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved search results")
    @GetMapping("/search")
    public ResponseEntity<CustomResponse<Page<ConversationResponse>>> searchConversations(
            @Parameter(description = "Keyword to search by customer ID") @RequestParam(required = false) String keyword,
            
            @Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "10") int size,
            
            @Parameter(description = "Sorting criteria in the format: property(,asc|desc). " +
                    "Default sort order is ascending. " +
                    "Multiple sort criteria are supported.") @RequestParam(required = false) String[] sort) {
        
        log.info("ConversationController | searchConversations | keyword: {}, page: {}, size: {}, sort: {}",
                keyword, page, size, sort != null ? String.join(", ", sort) : "unsorted");
        
        Pageable pageable = CreatePageable.build(page, size, sort);
        Page<ConversationResponse> conversations = conversationService.searchConversations(keyword, pageable);
        
        return ResponseEntity.ok(CustomResponse.successOf(conversations));
    }
    
    @Operation(summary = "Get conversation by ID", description = "Retrieves a specific conversation by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversation found"),
            @ApiResponse(responseCode = "404", description = "Conversation not found", 
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                    schema = @Schema(implementation = CustomResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<ConversationResponse>> getConversationById(
            @Parameter(description = "ID of the conversation to retrieve", required = true) @PathVariable String id) {
        
        log.info("ConversationController | getConversationById | id: {}", id);
        ConversationResponse conversation = conversationService.getConversationById(id);
        
        return ResponseEntity.ok(CustomResponse.successOf(conversation));
    }
    
    @Operation(summary = "Get conversations by customer ID", description = "Retrieves all conversations for a specific customer")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customer's conversations")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CustomResponse<List<ConversationResponse>>> getConversationsByCustomerId(
            @Parameter(description = "ID of the customer", required = true) @PathVariable String customerId) {
        
        log.info("ConversationController | getConversationsByCustomerId | customerId: {}", customerId);
        List<ConversationResponse> conversations = conversationService.getConversationsByCustomerId(customerId);
        
        return ResponseEntity.ok(CustomResponse.successOf(conversations));
    }
    
    @Operation(summary = "Create a new conversation", description = "Creates a new conversation with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conversation successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", 
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                    schema = @Schema(implementation = CustomResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conversation already exists for the customer", 
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                    schema = @Schema(implementation = CustomResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CustomResponse<ConversationResponse>> createConversation(
            @Parameter(description = "Conversation information for creation", required = true) 
            @Valid @RequestBody CreateConversationRequest request) {
        
        log.info("ConversationController | createConversation | request: {}", request);
        ConversationResponse createdConversation = conversationService.createConversation(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.<ConversationResponse>builder()
                        .httpStatus(HttpStatus.CREATED)
                        .isSuccess(true)
                        .data(createdConversation)
                        .build());
    }
    
    @Operation(summary = "Update a conversation", description = "Updates an existing conversation with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversation successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", 
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                    schema = @Schema(implementation = CustomResponse.class))),
            @ApiResponse(responseCode = "404", description = "Conversation not found", 
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                    schema = @Schema(implementation = CustomResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<ConversationResponse>> updateConversation(
            @Parameter(description = "ID of the conversation to update", required = true) 
            @PathVariable String id,
            
            @Parameter(description = "Updated conversation information", required = true) 
            @Valid @RequestBody UpdateConversationRequest request) {
        
        log.info("ConversationController | updateConversation | id: {}, request: {}", id, request);
        ConversationResponse updatedConversation = conversationService.updateConversation(id, request);
        
        return ResponseEntity.ok(CustomResponse.successOf(updatedConversation));
    }
    
    @Operation(summary = "Delete a conversation", description = "Deletes a conversation by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conversation successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Conversation not found", 
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                    schema = @Schema(implementation = CustomResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteConversation(
            @Parameter(description = "ID of the conversation to delete", required = true) @PathVariable String id) {
        
        log.info("ConversationController | deleteConversation | id: {}", id);
        conversationService.deleteConversation(id);
        
        return ResponseEntity.ok(CustomResponse.SUCCESS);
    }
}