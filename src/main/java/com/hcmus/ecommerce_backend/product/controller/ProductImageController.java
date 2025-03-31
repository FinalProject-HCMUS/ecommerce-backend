package com.hcmus.ecommerce_backend.product.controller;

import com.hcmus.ecommerce_backend.common.model.dto.CustomResponse;
import com.hcmus.ecommerce_backend.product.model.dto.request.CreateProductImageRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateProductImageRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductImageResponse;
import com.hcmus.ecommerce_backend.product.service.ProductImageService;
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
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Image", description = "Product image management APIs")
public class ProductImageController {

    private final ProductImageService productImageService;

    @Operation(summary = "Get all product images", description = "Retrieves all product images")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all product images")
    @GetMapping
    public ResponseEntity<CustomResponse<List<ProductImageResponse>>> getAllProductImages() {
        log.info("ProductImageController | getAllProductImages | Retrieving all product images");
        List<ProductImageResponse> images = productImageService.getAllProductImages();
        return ResponseEntity.ok(CustomResponse.successOf(images));
    }

    @Operation(summary = "Get product image by ID", description = "Retrieves a specific product image by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product image found"),
        @ApiResponse(responseCode = "404", description = "Product image not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<ProductImageResponse>> getProductImageById(
            @Parameter(description = "ID of the product image to retrieve", required = true)
            @PathVariable String id) {
        log.info("ProductImageController | getProductImageById | id: {}", id);
        ProductImageResponse image = productImageService.getProductImageById(id);
        return ResponseEntity.ok(CustomResponse.successOf(image));
    }

    @Operation(summary = "Create a new product image", description = "Creates a new product image")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product image successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "409", description = "Product image already exists",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CustomResponse<ProductImageResponse>> createProductImage(
            @Parameter(description = "Product image information for creation", required = true)
            @Valid @RequestBody CreateProductImageRequest request) {
        log.info("ProductImageController | createProductImage | request: {}", request);
        ProductImageResponse createdImage = productImageService.createProductImage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.successOf(createdImage));
    }

    @Operation(summary = "Update a product image", description = "Updates an existing product image")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product image successfully updated"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product image not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "409", description = "Product image already exists",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<ProductImageResponse>> updateProductImage(
            @Parameter(description = "ID of the product image to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated product image information", required = true)
            @Valid @RequestBody UpdateProductImageRequest request) {
        log.info("ProductImageController | updateProductImage | id: {}, request: {}", id, request);
        ProductImageResponse updatedImage = productImageService.updateProductImage(id, request);
        return ResponseEntity.ok(CustomResponse.successOf(updatedImage));
    }

    @Operation(summary = "Delete a product image", description = "Deletes a product image by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product image successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Product image not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteProductImage(
            @Parameter(description = "ID of the product image to delete", required = true)
            @PathVariable String id) {
        log.info("ProductImageController | deleteProductImage | id: {}", id);
        productImageService.deleteProductImage(id);
        return ResponseEntity.ok(CustomResponse.SUCCESS);
    }
}