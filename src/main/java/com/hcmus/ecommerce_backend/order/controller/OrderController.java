package com.hcmus.ecommerce_backend.order.controller;

import com.hcmus.ecommerce_backend.common.model.dto.CustomResponse;
import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateOrderRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderResponse;
import com.hcmus.ecommerce_backend.order.model.enums.Status;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.hcmus.ecommerce_backend.common.utils.CreatePageable;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order", description = "Order management APIs")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Get all orders", description = "Retrieves a paginated list of orders with sorting capabilities")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated orders")
    @GetMapping
    public ResponseEntity<CustomResponse<Page<OrderResponse>>> getAllOrders(
            @Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sorting criteria in the format: property(,asc|desc). " +
                    "Default sort order is ascending. " +
                    "Multiple sort criteria are supported.") @RequestParam(required = false) String[] sort) {

        log.info("OrderController | getAllOrders | page: {}, size: {}, sort: {}",
                page, size, sort != null ? String.join(", ", sort) : "unsorted");

        Pageable pageable = CreatePageable.build(page, size, sort);
        Page<OrderResponse> orders = orderService.getAllOrders(pageable);

        return ResponseEntity.ok(CustomResponse.successOf(orders));
    }

    @Operation(summary = "Search orders", description = "Searches orders by keyword and/or status with pagination and sorting")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved search results")
    @GetMapping("/search")
    public ResponseEntity<CustomResponse<Page<OrderResponse>>> searchOrders(
            @Parameter(description = "Keyword to search by name") @RequestParam(required = false) String keyword,

            @Parameter(description = "Filter by order status: NEW, CANCELLED, PROCESSING, PACKAGED, PICKED, SHIPPING, DELIVERED, REFUNDED", schema = @Schema(type = "string", allowableValues = {
                    "NEW", "CANCELLED", "PROCESSING",
                    "PACKAGED", "PICKED", "SHIPPING", "DELIVERED",
                    "REFUNDED" }, description = "Order status values")) @RequestParam(required = false) Status status,

            @Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sorting criteria in the format: property(,asc|desc). " +
                    "Default sort order is ascending. " +
                    "Multiple sort criteria are supported.") @RequestParam(required = false) String[] sort) {

        log.info("OrderController | searchOrders | keyword: {}, status: {}, page: {}, size: {}, sort: {}",
                keyword, status, page, size, sort != null ? String.join(", ", sort) : "unsorted");

        Pageable pageable = CreatePageable.build(page, size, sort);
        Page<OrderResponse> orders = orderService.searchOrders(keyword, status, pageable);

        return ResponseEntity.ok(CustomResponse.successOf(orders));
    }

    @Operation(summary = "Get order by ID", description = "Retrieves a specific order by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<OrderResponse>> getOrderById(
            @Parameter(description = "ID of the order to retrieve", required = true) @PathVariable String id) {
        log.info("OrderController | getOrderById | id: {}", id);
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(CustomResponse.successOf(order));
    }

    @Operation(summary = "Get orders by customer ID", description = "Retrieves orders for a specific customer")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customer orders")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CustomResponse<List<OrderResponse>>> getOrdersByCustomerId(
            @Parameter(description = "ID of the customer", required = true) @PathVariable String customerId) {
        log.info("OrderController | getOrdersByCustomerId | customerId: {}", customerId);
        List<OrderResponse> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(CustomResponse.successOf(orders));
    }

    @Operation(summary = "Create a new order", description = "Creates a new order with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CustomResponse<OrderResponse>> createOrder(
            @Parameter(description = "Order information for creation", required = true) @Valid @RequestBody CreateOrderRequest request) {
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
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<OrderResponse>> updateOrder(
            @Parameter(description = "ID of the order to update", required = true) @PathVariable String id,
            @Parameter(description = "Updated order information", required = true) @Valid @RequestBody UpdateOrderRequest request) {
        log.info("OrderController | updateOrder | id: {}, request: {}", id, request);
        OrderResponse updatedOrder = orderService.updateOrder(id, request);
        return ResponseEntity.ok(CustomResponse.successOf(updatedOrder));
    }

    @Operation(summary = "Delete an order", description = "Deletes an order by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteOrder(
            @Parameter(description = "ID of the order to delete", required = true) @PathVariable String id) {
        log.info("OrderController | deleteOrder | id: {}", id);
        orderService.deleteOrder(id);
        return ResponseEntity.ok(CustomResponse.SUCCESS);
    }
}