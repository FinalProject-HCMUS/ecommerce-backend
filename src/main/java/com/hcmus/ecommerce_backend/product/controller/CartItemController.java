package com.hcmus.ecommerce_backend.product.controller;

import com.hcmus.ecommerce_backend.common.model.dto.CustomResponse;
import com.hcmus.ecommerce_backend.common.utils.CreatePageable;
import com.hcmus.ecommerce_backend.product.model.dto.request.CreateCartItemRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateCartItemRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.CartItemResponse;
import com.hcmus.ecommerce_backend.product.model.dto.response.CartItemWithProductResponse;
import com.hcmus.ecommerce_backend.product.service.CartItemService;
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
@RequestMapping("/cart-items")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart Items", description = "Cart Items management APIs")
public class CartItemController {
    
    private final CartItemService cartItemService;
    
    @Operation(summary = "Get all cart items", description = "Retrieves a paginated list of cart items")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated cart items")
    @GetMapping
    public ResponseEntity<CustomResponse<Page<CartItemResponse>>> getAllCartItems(
            @Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sorting criteria in the format: property(,asc|desc). " +
                    "Default sort order is ascending. " +
                    "Multiple sort criteria are supported.") @RequestParam(required = false) String[] sort) {
        log.info("CartItemController | getAllCartItems | page: {}, size: {}, sort: {}",
                page, size, sort != null ? String.join(", ", sort) : "unsorted");
        Pageable pageable = CreatePageable.build(page, size, sort);
        Page<CartItemResponse> cartItems = cartItemService.getAllCartItems(pageable);
        return ResponseEntity.ok(CustomResponse.successOf(cartItems));
    }
    
    @Operation(summary = "Get cart item by ID", description = "Retrieves a specific cart item by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart item found"),
        @ApiResponse(responseCode = "404", description = "Cart item not found", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<CartItemResponse>> getCartItemById(
            @Parameter(description = "ID of the cart item to retrieve", required = true)
            @PathVariable String id) {
        log.info("CartItemController | getCartItemById | id: {}", id);
        CartItemResponse cartItem = cartItemService.getCartItemById(id);
        return ResponseEntity.ok(CustomResponse.successOf(cartItem));
    }
    
    @Operation(summary = "Get cart items by user ID", description = "Retrieves cart items for a specific user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user's cart items")
    @GetMapping("/user/{userId}")
    public ResponseEntity<CustomResponse<List<CartItemWithProductResponse>>> getCartItemsByUserId(
            @Parameter(description = "ID of the user", required = true)
            @PathVariable String userId) {
        log.info("CartItemController | getCartItemsByUserId | userId: {}", userId);
        List<CartItemWithProductResponse> cartItems = cartItemService.getCartItemsByUserId(userId);
        return ResponseEntity.ok(CustomResponse.successOf(cartItems));
    }
    
    @Operation(summary = "Create a new cart item", description = "Creates a new cart item with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cart item successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid input data", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CustomResponse<CartItemResponse>> createCartItem(
            @Parameter(description = "Cart item information for creation", required = true) 
            @Valid @RequestBody CreateCartItemRequest request) {
        log.info("CartItemController | createCartItem | request: {}", request);
        CartItemResponse createdCartItem = cartItemService.createCartItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.<CartItemResponse>builder()
                        .httpStatus(HttpStatus.CREATED)
                        .isSuccess(true)
                        .data(createdCartItem)
                        .build());
    }
    
    @Operation(summary = "Update a cart item", description = "Updates an existing cart item with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart item successfully updated"),
        @ApiResponse(responseCode = "400", description = "Invalid input data", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cart item not found", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<CartItemResponse>> updateCartItem(
            @Parameter(description = "ID of the cart item to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated cart item information", required = true)
            @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("CartItemController | updateCartItem | id: {}, request: {}", id, request);
        CartItemResponse updatedCartItem = cartItemService.updateCartItem(id, request);
        return ResponseEntity.ok(CustomResponse.successOf(updatedCartItem));
    }
    
    @Operation(summary = "Delete a cart item", description = "Deletes a cart item by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart item successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Cart item not found", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteCartItem(
            @Parameter(description = "ID of the cart item to delete", required = true)
            @PathVariable String id) {
        log.info("CartItemController | deleteCartItem | id: {}", id);
        cartItemService.deleteCartItem(id);
        return ResponseEntity.ok(CustomResponse.SUCCESS);
    }
    
    @Operation(summary = "Delete a cart item by user and item", description = "Deletes a cart item by user ID and item ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart item successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Cart item not found", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @DeleteMapping("/user/{userId}/item/{itemId}")
    public ResponseEntity<CustomResponse<Void>> deleteCartItemByUserIdAndItemId(
            @Parameter(description = "ID of the user", required = true)
            @PathVariable String userId,
            @Parameter(description = "ID of the item", required = true)
            @PathVariable String itemId) {
        log.info("CartItemController | deleteCartItemByUserIdAndItemId | userId: {}, itemId: {}", userId, itemId);
        cartItemService.deleteCartItemByUserIdAndItemId(userId, itemId);
        return ResponseEntity.ok(CustomResponse.SUCCESS);
    }
}