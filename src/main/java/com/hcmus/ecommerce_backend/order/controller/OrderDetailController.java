package com.hcmus.ecommerce_backend.order.controller;

import com.hcmus.ecommerce_backend.common.model.dto.CustomResponse;
import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderDetailRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateOrderDetailRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderDetailResponse;
import com.hcmus.ecommerce_backend.order.service.OrderDetailService;
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
@RequestMapping("/order-details")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Detail", description = "Order detail management APIs")
public class OrderDetailController {

    private final OrderDetailService orderDetailService;

    @Operation(summary = "Get all order details", description = "Retrieves a list of all available order details")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all order details")
    @GetMapping
    public ResponseEntity<CustomResponse<List<OrderDetailResponse>>> getAllOrderDetails() {
        log.info("OrderDetailController | getAllOrderDetails | Retrieving all order details");
        List<OrderDetailResponse> orderDetails = orderDetailService.getAllOrderDetails();
        return ResponseEntity.ok(CustomResponse.successOf(orderDetails));
    }

    @Operation(summary = "Get order detail by ID", description = "Retrieves a specific order detail by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order detail found"),
        @ApiResponse(responseCode = "404", description = "Order detail not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = CustomResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<OrderDetailResponse>> getOrderDetailById(
            @Parameter(description = "ID of the order detail to retrieve", required = true)
            @PathVariable String id) {
        log.info("OrderDetailController | getOrderDetailById | id: {}", id);
        OrderDetailResponse orderDetail = orderDetailService.getOrderDetailById(id);
        return ResponseEntity.ok(CustomResponse.successOf(orderDetail));
    }

    @Operation(summary = "Create a new order detail", description = "Creates a new order detail with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order detail successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = CustomResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CustomResponse<OrderDetailResponse>> createOrderDetail(
            @Parameter(description = "Order detail information for creation", required = true)
            @Valid @RequestBody CreateOrderDetailRequest request) {
        log.info("OrderDetailController | createOrderDetail | request: {}", request);
        OrderDetailResponse createdOrderDetail = orderDetailService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.successOf(createdOrderDetail));
    }

    @Operation(summary = "Update an order detail", description = "Updates an existing order detail with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order detail successfully updated"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "404", description = "Order detail not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = CustomResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<OrderDetailResponse>> updateOrderDetail(
            @Parameter(description = "ID of the order detail to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated order detail information", required = true)
            @Valid @RequestBody UpdateOrderDetailRequest request) {
        log.info("OrderDetailController | updateOrderDetail | id: {}, request: {}", id, request);
        OrderDetailResponse updatedOrderDetail = orderDetailService.updateOrder(id, request);
        return ResponseEntity.ok(CustomResponse.successOf(updatedOrderDetail));
    }

    @Operation(summary = "Delete an order detail", description = "Deletes an order detail by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order detail successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Order detail not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = CustomResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteOrderDetail(
            @Parameter(description = "ID of the order detail to delete", required = true)
            @PathVariable String id) {
        log.info("OrderDetailController | deleteOrderDetail | id: {}", id);
        orderDetailService.deleteOrderDetail(id);
        return ResponseEntity.ok(CustomResponse.SUCCESS);
    }
}
