package com.hcmus.ecommerce_backend.order.controller;

import com.hcmus.ecommerce_backend.common.model.dto.CustomResponse;
import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateOrderRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderResponse;
import com.hcmus.ecommerce_backend.order.service.OrderService;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order", description = "Order management APIs")
public class OrderController {
    
    private final OrderService orderService;
    
    @Operation(summary = "Get all orders", description = "Retrieves a list of all available orders")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all orders")
    @GetMapping
    public ResponseEntity<CustomResponse<List<OrderResponse>>> getAllOrders() {
        log.info("OrderController | getAllOrders | Retrieving all orders");
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(CustomResponse.successOf(orders));
    }
    
    @Operation(summary = "Get order by ID", description = "Retrieves a specific order by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order found"),
        @ApiResponse(responseCode = "404", description = "Order not found", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<OrderResponse>> getOrderById(
            @Parameter(description = "ID of the order to retrieve", required = true)
            @PathVariable String id) {
        log.info("OrderController | getOrderById | id: {}", id);
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(CustomResponse.successOf(order));
    }
    
    @Operation(summary = "Get orders by customer ID", description = "Retrieves orders for a specific customer")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customer orders")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CustomResponse<List<OrderResponse>>> getOrdersByCustomerId(
            @Parameter(description = "ID of the customer", required = true)
            @PathVariable String customerId) {
        log.info("OrderController | getOrdersByCustomerId | customerId: {}", customerId);
        List<OrderResponse> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(CustomResponse.successOf(orders));
    }
    
    @Operation(summary = "Create a new order", description = "Creates a new order with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid input data", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CustomResponse<OrderResponse>> createOrder(
            @Parameter(description = "Order information for creation", required = true) 
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("OrderController | createOrder | request: {}", request);
        OrderResponse createdOrder = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.<OrderResponse>builder()
                        .httpStatus(HttpStatus.CREATED)
                        .isSuccess(true)
                        .data(createdOrder)
                        .build());
    }
    
    @Operation(summary = "Update an order", description = "Updates an existing order with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order successfully updated"),
        @ApiResponse(responseCode = "400", description = "Invalid input data", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "404", description = "Order not found", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<OrderResponse>> updateOrder(
            @Parameter(description = "ID of the order to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated order information", required = true)
            @Valid @RequestBody UpdateOrderRequest request) {
        log.info("OrderController | updateOrder | id: {}, request: {}", id, request);
        OrderResponse updatedOrder = orderService.updateOrder(id, request);
        return ResponseEntity.ok(CustomResponse.successOf(updatedOrder));
    }
    
    @Operation(summary = "Delete an order", description = "Deletes an order by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Order not found", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteOrder(
            @Parameter(description = "ID of the order to delete", required = true)
            @PathVariable String id) {
        log.info("OrderController | deleteOrder | id: {}", id);
        orderService.deleteOrder(id);
        return ResponseEntity.ok(CustomResponse.SUCCESS);
    }
}