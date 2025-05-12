package com.hcmus.ecommerce_backend.product.controller;

import com.hcmus.ecommerce_backend.product.model.dto.request.size.CreateMultipleSizesRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.size.CreateSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.size.UpdateSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.SizeResponse;
import com.hcmus.ecommerce_backend.product.service.SizeService;
import com.hcmus.ecommerce_backend.common.model.dto.CustomResponse;
import com.hcmus.ecommerce_backend.common.utils.CreatePageable;

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
@RequestMapping("/sizes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Size", description = "Size management APIs")
public class SizeController {

    private final SizeService sizeService;

    @Operation(summary = "Get all sizes", description = "Retrieves a paginated list of sizes with filtering and sorting capabilities")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated sizes")
    @GetMapping
    public ResponseEntity<CustomResponse<Page<SizeResponse>>> getAllSizes(
            @Parameter(description = "Zero-based page index (0..N)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "The size of the page to be returned")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sorting criteria in the format: property(,asc|desc). " +
                    "Default sort order is ascending. " +
                    "Multiple sort criteria are supported.")
            @RequestParam(required = false) String[] sort,
            @Parameter(description = "Keyword to search in size name")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Filter by minimum height (cm)")
            @RequestParam(required = false) Integer minHeight,
            @Parameter(description = "Filter by maximum height (cm)")
            @RequestParam(required = false) Integer maxHeight,
            @Parameter(description = "Filter by minimum weight (kg)")
            @RequestParam(required = false) Integer minWeight,
            @Parameter(description = "Filter by maximum weight (kg)")
            @RequestParam(required = false) Integer maxWeight) {

        log.info("SizeController | getAllSizes | page: {}, size: {}, sort: {}, keyword: {}, " +
                "minHeight: {}, maxHeight: {}, minWeight: {}, maxWeight: {}",
                page, size, sort != null ? String.join(", ", sort) : "unsorted",
                keyword, minHeight, maxHeight, minWeight, maxWeight);

        Pageable pageable = CreatePageable.build(page, size, sort);
        Page<SizeResponse> sizes = sizeService.searchSizes(pageable, keyword,
                                                      minHeight, maxHeight,
                                                      minWeight, maxWeight);
        return ResponseEntity.ok(CustomResponse.successOf(sizes));
    }

    @Operation(summary = "Get size by ID", description = "Retrieves a specific size by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Size found"),
        @ApiResponse(responseCode = "404", description = "Size not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<SizeResponse>> getSizeById(
            @Parameter(description = "ID of the size to retrieve", required = true)
            @PathVariable String id) {
        log.info("SizeController | getSizeById | id: {}", id);
        SizeResponse size = sizeService.getSizeById(id);
        return ResponseEntity.ok(CustomResponse.successOf(size));
    }

    @Operation(summary = "Create a new size", description = "Creates a new size with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Size successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "409", description = "Size already exists",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CustomResponse<SizeResponse>> createSize(
            @Parameter(description = "Size information for creation", required = true)
            @Valid @RequestBody CreateSizeRequest request) {
        log.info("SizeController | createSize | request: {}", request);
        SizeResponse createdSize = sizeService.createSize(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.createdOf(createdSize));
    }

    @Operation(summary = "Create multiple sizes", description = "Creates multiple sizes at once with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Sizes successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "409", description = "One or more sizes already exist",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @PostMapping("/batch")
    public ResponseEntity<CustomResponse<List<SizeResponse>>> createMultipleSizes(
            @Parameter(description = "List of sizes for creation", required = true)
            @Valid @RequestBody CreateMultipleSizesRequest request) {
        log.info("SizeController | createMultipleSizes | request with {} sizes", request.getSizes().size());
        List<SizeResponse> createdSizes = sizeService.createMultipleSizes(request.getSizes());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.createdOf(createdSizes));
    }

    @Operation(summary = "Update a size", description = "Updates an existing size with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Size successfully updated"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "404", description = "Size not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "409", description = "Size name already exists",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<SizeResponse>> updateSize(
            @Parameter(description = "ID of the size to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated size information", required = true)
            @Valid @RequestBody UpdateSizeRequest request) {
        log.info("SizeController | updateSize | id: {}, request: {}", id, request);
        SizeResponse updatedSize = sizeService.updateSize(id, request);
        return ResponseEntity.ok(CustomResponse.successOf(updatedSize));
    }

    @Operation(summary = "Delete a size", description = "Deletes a size by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Size successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Size not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteSize(
            @Parameter(description = "ID of the size to delete", required = true)
            @PathVariable String id) {
        log.info("SizeController | deleteSize | id: {}", id);
        sizeService.deleteSize(id);
        return ResponseEntity.ok(CustomResponse.SUCCESS);
    }
}