package com.hcmus.ecommerce_backend.product.controller;

import com.hcmus.ecommerce_backend.common.model.dto.CustomResponse;
import com.hcmus.ecommerce_backend.common.utils.CreatePageable;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.CreateProductColorSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.UpdateProductColorSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductColorSizeResponse;
import com.hcmus.ecommerce_backend.product.service.ProductColorSizeService;
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

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product-color-sizes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Color Size", description = "APIs for managing product color and size combinations")
public class ProductColorSizeController {

    private final ProductColorSizeService productColorSizeService;

    @Operation(summary = "Get all product color sizes", description = "Retrieves a paginated list of product color and size combinations")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated product color sizes")
    @GetMapping
    public ResponseEntity<CustomResponse<Page<ProductColorSizeResponse>>> getAllProductColorSizes(
            @Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sorting criteria in the format: property(,asc|desc). " +
                    "Default sort order is ascending. " +
                    "Multiple sort criteria are supported.") @RequestParam(required = false) String[] sort) {
        log.info("ProductColorSizeController | getAllProductColorSizes | page: {}, size: {}, sort: {}",
                page, size, sort != null ? String.join(", ", sort) : "unsorted");
        Pageable pageable = CreatePageable.build(page, size, sort);
        Page<ProductColorSizeResponse> productColorSizes = productColorSizeService.getAllProductColorSizes(pageable);
        return ResponseEntity.ok(CustomResponse.successOf(productColorSizes));
    }

    @Operation(summary = "Get product color size by ID", description = "Retrieves a specific product color and size combination by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product color size found"),
        @ApiResponse(responseCode = "404", description = "Product color size not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<ProductColorSizeResponse>> getProductColorSizeById(
            @Parameter(description = "ID of the product color size to retrieve", required = true)
            @PathVariable String id) {
        log.info("ProductColorSizeController | getProductColorSizeById | id: {}", id);
        ProductColorSizeResponse productColorSize = productColorSizeService.getProductColorSizeById(id);
        return ResponseEntity.ok(CustomResponse.successOf(productColorSize));
    }

    @Operation(summary = "Create a new product color size", description = "Creates a new product color and size combination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product color size successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "409", description = "Product color size already exists",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CustomResponse<ProductColorSizeResponse>> createProductColorSize(
            @Parameter(description = "Product color size information for creation", required = true)
            @Valid @RequestBody CreateProductColorSizeRequest request) {
        log.info("ProductColorSizeController | createProductColorSize | request: {}", request);
        ProductColorSizeResponse createdProductColorSize = productColorSizeService.createProductColorSize(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.successOf(createdProductColorSize));
    }

    @Operation(summary = "Update a product color size", description = "Updates an existing product color and size combination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product color size successfully updated"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product color size not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "409", description = "Product color size already exists",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<ProductColorSizeResponse>> updateProductColorSize(
            @Parameter(description = "ID of the product color size to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated product color size information", required = true)
            @Valid @RequestBody UpdateProductColorSizeRequest request) {
        log.info("ProductColorSizeController | updateProductColorSize | id: {}, request: {}", id, request);
        ProductColorSizeResponse updatedProductColorSize = productColorSizeService.updateProductColorSize(id, request);
        return ResponseEntity.ok(CustomResponse.successOf(updatedProductColorSize));
    }

    @Operation(summary = "Delete a product color size", description = "Deletes a product color and size combination by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product color size successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Product color size not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteProductColorSize(
            @Parameter(description = "ID of the product color size to delete", required = true)
            @PathVariable String id) {
        log.info("ProductColorSizeController | deleteProductColorSize | id: {}", id);
        productColorSizeService.deleteProductColorSize(id);
        return ResponseEntity.ok(CustomResponse.SUCCESS);
    }

    @Operation(summary = "Get product color sizes by product ID", description = "Retrieves a list of product color and size combinations for a specific product ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved product color sizes"),
        @ApiResponse(responseCode = "404", description = "Product color sizes not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<CustomResponse<List<ProductColorSizeResponse>>> getProductColorSizesByProductId(
            @Parameter(description = "ID of the product to retrieve color and size combinations for", required = true)
            @PathVariable String productId) {
        log.info("ProductColorSizeController | getProductColorSizesByProductId | productId: {}", productId);
        List<ProductColorSizeResponse> productColorSizes = productColorSizeService.getProductColorSizesByProductId(productId);
        return ResponseEntity.ok(CustomResponse.successOf(productColorSizes));
    }
}