package com.hcmus.ecommerce_backend.product.controller;

import com.hcmus.ecommerce_backend.product.model.dto.request.CreateSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.SizeResponse;
import com.hcmus.ecommerce_backend.product.service.SizeService;
import com.hcmus.ecommerce_backend.common.model.dto.CustomResponse;
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
@RequestMapping("/sizes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Size", description = "Size management APIs")
public class SizeController {

    private final SizeService sizeService;

    @Operation(summary = "Get all sizes", description = "Retrieves a list of all available sizes")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all sizes")
    @GetMapping
    public ResponseEntity<CustomResponse<List<SizeResponse>>> getAllSizes() {
        log.info("SizeController | getAllSizes | Retrieving all sizes");
        List<SizeResponse> sizes = sizeService.getAllSizes();
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
                .body(CustomResponse.successOf(createdSize));
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