package com.hcmus.ecommerce_backend.message.controller;

import com.hcmus.ecommerce_backend.common.model.dto.CustomResponse;
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
    
    @Operation(summary = "Get all messages", description = "Retrieves a list of all available messages")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all messages")
    @GetMapping
    public ResponseEntity<CustomResponse<List<MessageResponse>>> getAllMessages() {
        log.info("MessageController | getAllMessages | Retrieving all messages");
        List<MessageResponse> messages = messageService.getAllMessages();
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
            @Parameter(description = "ID of the message to retrieve", required = true)
            @PathVariable String id) {
        log.info("MessageController | getMessageById | id: {}", id);
        MessageResponse message = messageService.getMessageById(id);
        return ResponseEntity.ok(CustomResponse.successOf(message));
    }
    
    @Operation(summary = "Get messages by customer ID", description = "Retrieves messages for a specific customer")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customer messages")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CustomResponse<List<MessageResponse>>> getMessagesByCustomerId(
            @Parameter(description = "ID of the customer", required = true)
            @PathVariable String customerId) {
        log.info("MessageController | getMessagesByCustomerId | customerId: {}", customerId);
        List<MessageResponse> messages = messageService.getMessagesByCustomerId(customerId);
        return ResponseEntity.ok(CustomResponse.successOf(messages));
    }
    
    @Operation(summary = "Get messages by admin ID", description = "Retrieves messages for a specific admin")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved admin messages")
    @GetMapping("/admin/{adminId}")
    public ResponseEntity<CustomResponse<List<MessageResponse>>> getMessagesByAdminId(
            @Parameter(description = "ID of the admin", required = true)
            @PathVariable String adminId) {
        log.info("MessageController | getMessagesByAdminId | adminId: {}", adminId);
        List<MessageResponse> messages = messageService.getMessagesByAdminId(adminId);
        return ResponseEntity.ok(CustomResponse.successOf(messages));
    }
    
    @Operation(summary = "Get messages by customer ID and admin ID", description = "Retrieves messages between a specific customer and admin")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customer-admin messages")
    @GetMapping("/customer/{customerId}/admin/{adminId}")
    public ResponseEntity<CustomResponse<List<MessageResponse>>> getMessagesByCustomerIdAndAdminId(
            @Parameter(description = "ID of the customer", required = true)
            @PathVariable String customerId,
            @Parameter(description = "ID of the admin", required = true)
            @PathVariable String adminId) {
        log.info("MessageController | getMessagesByCustomerIdAndAdminId | customerId: {}, adminId: {}", customerId, adminId);
        List<MessageResponse> messages = messageService.getMessagesByCustomerIdAndAdminId(customerId, adminId);
        return ResponseEntity.ok(CustomResponse.successOf(messages));
    }
    
    @Operation(summary = "Create a new message", description = "Creates a new message with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Message successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid input data", 
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
            @Parameter(description = "ID of the message to delete", required = true)
            @PathVariable String id) {
        log.info("MessageController | deleteMessage | id: {}", id);
        messageService.deleteMessage(id);
        return ResponseEntity.ok(CustomResponse.SUCCESS);
    }
}