package com.hcmus.ecommerce_backend.order.controller;

import com.hcmus.ecommerce_backend.common.model.dto.CustomResponse;
import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderTrackRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateOrderTrackRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderTrackResponse;
import com.hcmus.ecommerce_backend.order.model.enums.Status;
import com.hcmus.ecommerce_backend.order.service.OrderTrackService;
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
@RequestMapping("/order-tracks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Track", description = "Order Track management APIs")
public class OrderTrackController {
    
    private final OrderTrackService orderTrackService;
    
    @Operation(summary = "Get all order tracks", description = "Retrieves a list of all available order tracks")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all order tracks")
    @GetMapping
    public ResponseEntity<CustomResponse<List<OrderTrackResponse>>> getAllOrderTracks() {
        log.info("OrderTrackController | getAllOrderTracks | Retrieving all order tracks");
        List<OrderTrackResponse> orderTracks = orderTrackService.getAllOrderTracks();
        return ResponseEntity.ok(CustomResponse.successOf(orderTracks));
    }
    
    @Operation(summary = "Get order track by ID", description = "Retrieves a specific order track by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order track found"),
        @ApiResponse(responseCode = "404", description = "Order track not found", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<OrderTrackResponse>> getOrderTrackById(
            @Parameter(description = "ID of the order track to retrieve", required = true)
            @PathVariable String id) {
        log.info("OrderTrackController | getOrderTrackById | id: {}", id);
        OrderTrackResponse orderTrack = orderTrackService.getOrderTrackById(id);
        return ResponseEntity.ok(CustomResponse.successOf(orderTrack));
    }
    
    @Operation(summary = "Get order tracks by order ID", description = "Retrieves order tracks for a specific order")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved order tracks")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<CustomResponse<List<OrderTrackResponse>>> getOrderTracksByOrderId(
            @Parameter(description = "ID of the order", required = true)
            @PathVariable String orderId) {
        log.info("OrderTrackController | getOrderTracksByOrderId | orderId: {}", orderId);
        List<OrderTrackResponse> orderTracks = orderTrackService.getOrderTracksByOrderId(orderId);
        return ResponseEntity.ok(CustomResponse.successOf(orderTracks));
    }
    
    @Operation(summary = "Get order tracks by status", description = "Retrieves order tracks with a specific status")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved order tracks by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<CustomResponse<List<OrderTrackResponse>>> getOrderTracksByStatus(
            @Parameter(description = "Status of the order tracks to retrieve", required = true)
            @PathVariable Status status) {
        log.info("OrderTrackController | getOrderTracksByStatus | status: {}", status);
        List<OrderTrackResponse> orderTracks = orderTrackService.getOrderTracksByStatus(status);
        return ResponseEntity.ok(CustomResponse.successOf(orderTracks));
    }
    
    @Operation(summary = "Create a new order track", description = "Creates a new order track with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order track successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid input data", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "404", description = "Order not found", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CustomResponse<OrderTrackResponse>> createOrderTrack(
            @Parameter(description = "Order track information for creation", required = true) 
            @Valid @RequestBody CreateOrderTrackRequest request) {
        log.info("OrderTrackController | createOrderTrack | request: {}", request);
        OrderTrackResponse createdOrderTrack = orderTrackService.createOrderTrack(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.<OrderTrackResponse>builder()
                        .httpStatus(HttpStatus.CREATED)
                        .isSuccess(true)
                        .data(createdOrderTrack)
                        .build());
    }
    
    @Operation(summary = "Update an order track", description = "Updates an existing order track with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order track successfully updated"),
        @ApiResponse(responseCode = "400", description = "Invalid input data", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "404", description = "Order track not found", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<OrderTrackResponse>> updateOrderTrack(
            @Parameter(description = "ID of the order track to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated order track information", required = true)
            @Valid @RequestBody UpdateOrderTrackRequest request) {
        log.info("OrderTrackController | updateOrderTrack | id: {}, request: {}", id, request);
        OrderTrackResponse updatedOrderTrack = orderTrackService.updateOrderTrack(id, request);
        return ResponseEntity.ok(CustomResponse.successOf(updatedOrderTrack));
    }
    
    @Operation(summary = "Delete an order track", description = "Deletes an order track by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order track successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Order track not found", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteOrderTrack(
            @Parameter(description = "ID of the order track to delete", required = true)
            @PathVariable String id) {
        log.info("OrderTrackController | deleteOrderTrack | id: {}", id);
        orderTrackService.deleteOrderTrack(id);
        return ResponseEntity.ok(CustomResponse.SUCCESS);
    }
}