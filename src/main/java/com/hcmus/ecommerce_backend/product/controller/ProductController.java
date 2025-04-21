package com.hcmus.ecommerce_backend.product.controller;

import com.hcmus.ecommerce_backend.common.model.dto.CustomResponse;
import com.hcmus.ecommerce_backend.common.utils.CreatePageable;
import com.hcmus.ecommerce_backend.product.model.dto.request.CreateProductRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateProductRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductResponse;
import com.hcmus.ecommerce_backend.product.service.ProductService;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product", description = "Product management APIs")
public class ProductController {
    
    private final ProductService productService;
    
    @Operation(summary = "Get all products with filters", description = "Retrieves a paginated list of products with filters and sorting capabilities")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered products")
    @GetMapping
    public ResponseEntity<CustomResponse<Page<ProductResponse>>> getAllProducts(
            @Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "10") int perpage,
            @Parameter(description = "Sorting criteria in the format: property(,asc|desc). " +
                    "Default sort order is ascending. " +
                    "Multiple sort criteria are supported.") @RequestParam(required = false) String[] sort,
            @Parameter(description = "Keyword to search in product name or description") @RequestParam(required = false) String keysearch,
            @Parameter(description = "Filter by category ID (UUID)") @RequestParam(required = false) String category,
            @Parameter(description = "Minimum price (inclusive)") @RequestParam(required = false) Double fromprice,
            @Parameter(description = "Maximum price (inclusive)") @RequestParam(required = false) Double toprice,
            @Parameter(description = "Filter by color") @RequestParam(required = false) String color,
            @Parameter(description = "Filter by size (e.g., S, M, L, XL)") @RequestParam(required = false) String size) {
    
        log.info("ProductController | getAllProducts | page: {}, perpage: {}, sort: {}, filters: keysearch={}, category={}, fromprice={}, toprice={}, color={}, size={}",
                page, perpage, sort != null ? String.join(", ", sort) : "unsorted", keysearch, category, fromprice, toprice, color, size);
    
        Pageable pageable = CreatePageable.build(page, perpage, sort);
        Page<ProductResponse> products = productService.getAllProducts(pageable, keysearch, category, fromprice, toprice, color, size);
    
        return ResponseEntity.ok(CustomResponse.successOf(products));
    }

    @Operation(summary = "Get product by ID", description = "Retrieves a specific product by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "404", description = "Product not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<ProductResponse>> getProductById(
            @Parameter(description = "ID of the product to retrieve", required = true)
            @PathVariable String id) {
        log.info("ProductController | getProductById | id: {}", id);
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(CustomResponse.successOf(product));
    }
    
    @Operation(summary = "Create a new product", description = "Creates a new product with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "409", description = "Product already exists",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CustomResponse<ProductResponse>> createProduct(
            @Parameter(description = "Product information for creation", required = true)
            @Valid @RequestBody CreateProductRequest request) {
        log.info("ProductController | createProduct | request: {}", request);
        ProductResponse createdProduct = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.<ProductResponse>builder()
                        .httpStatus(HttpStatus.CREATED)
                        .isSuccess(true)
                        .data(createdProduct)
                        .build());
    }
    
    @Operation(summary = "Update a product", description = "Updates an existing product with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product successfully updated"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "409", description = "Product name already exists",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<ProductResponse>> updateProduct(
            @Parameter(description = "ID of the product to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated product information", required = true)
            @Valid @RequestBody UpdateProductRequest request) {
        log.info("ProductController | updateProduct | id: {}, request: {}", id, request);
        ProductResponse updatedProduct = productService.updateProduct(id, request);
        return ResponseEntity.ok(CustomResponse.successOf(updatedProduct));
    }

    @Operation(summary = "Delete a product", description = "Deletes a product by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Product not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteProduct(
            @Parameter(description = "ID of the product to delete", required = true)
            @PathVariable String id) {
        log.info("ProductController | deleteProduct | id: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.ok(CustomResponse.SUCCESS);
    }

    @Operation(summary = "Get top products", description = "Retrieves top-selling and top-trending products with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved top products"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @GetMapping("/top-products")
    public ResponseEntity<CustomResponse<Map<String, Object>>> getTopProducts(
        @Parameter(description = "Page number for pagination", example = "0") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size for pagination", example = "10") @RequestParam(defaultValue = "10") int size) {
        log.info("ProductController | getTopProducts | page: {}, size: {}", page, size);

        // Top-selling products
        List<ProductResponse> topSelling = productService.getTopSellingProducts(page, size);

        // Top-trending products
        List<ProductResponse> topTrending = productService.getTopTrendingProducts(page, size);

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("topProducts", Arrays.asList(
                Map.of(
                        "title", "TOP SELLING",
                        "data", topSelling
                ),
                Map.of(
                        "title", "TOP TRENDING",
                        "data", topTrending
                )
        ));
        response.put("pagination", Map.of(
                "totalItems", topSelling.size(),
                "totalPages", (int) Math.ceil((double) topSelling.size() / size),
                "currentPage", page
        ));

        return ResponseEntity.ok(CustomResponse.successOf(response));
    }
}