package com.hcmus.ecommerce_backend.category.controller;

import com.hcmus.ecommerce_backend.category.model.dto.request.CreateCategoryRequest;
import com.hcmus.ecommerce_backend.category.model.dto.request.UpdateCategoryRequest;
import com.hcmus.ecommerce_backend.category.model.dto.response.CategoryResponse;
import com.hcmus.ecommerce_backend.category.service.CategoryService;
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

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Category", description = "Category management APIs")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @Operation(summary = "Get all categories", description = "Retrieves a paginated list of categories with filtering and sorting capabilities")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated categories")
    @GetMapping
    public ResponseEntity<CustomResponse<Page<CategoryResponse>>> getAllCategories(
            @Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sorting criteria in the format: property(,asc|desc). " +
                    "Default sort order is ascending. " +
                    "Multiple sort criteria are supported.") @RequestParam(required = false) String[] sort,
            @Parameter(description = "Keyword to search in category name") @RequestParam(required = false) String keyword) {

        log.info("CategoryController | getAllCategories | page: {}, size: {}, sort: {}, keyword: {}",
                page, size, sort != null ? String.join(", ", sort) : "unsorted", keyword);

        Pageable pageable = CreatePageable.build(page, size, sort);
        Page<CategoryResponse> categories = categoryService.searchCategories(pageable, keyword != null ? keyword.trim() : null);
        return ResponseEntity.ok(CustomResponse.successOf(categories));
    }

    @Operation(summary = "Get all categories without pagination", description = "Retrieves a list of all categories without pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all categories")
    @GetMapping("/all")
    public ResponseEntity<CustomResponse<List<CategoryResponse>>> getAllCategoriesWithoutPagination() {
        log.info("CategoryController | getAllCategoriesWithoutPagination | Retrieving all categories");
        List<CategoryResponse> categories = categoryService.getAllCategoriesWithoutPagination();
        return ResponseEntity.ok(CustomResponse.successOf(categories));
    }
    
    @Operation(summary = "Get category by ID", description = "Retrieves a specific category by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category found"),
        @ApiResponse(responseCode = "404", description = "Category not found", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<CategoryResponse>> getCategoryById(
            @Parameter(description = "ID of the category to retrieve", required = true)
            @PathVariable String id) {
        log.info("CategoryController | getCategoryById | id: {}", id);
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(CustomResponse.successOf(category));
    }
    
    @Operation(summary = "Create a new category", description = "Creates a new category with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Category successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid input data", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "409", description = "Category already exists", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CustomResponse<CategoryResponse>> createCategory(
            @Parameter(description = "Category information for creation", required = true) 
            @Valid @RequestBody CreateCategoryRequest request) {
        log.info("CategoryController | createCategory | request: {}", request);
        CategoryResponse createdCategory = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.<CategoryResponse>builder()
                        .httpStatus(HttpStatus.CREATED)
                        .isSuccess(true)
                        .data(createdCategory)
                        .build());
    }
    
    @Operation(summary = "Update a category", description = "Updates an existing category with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category successfully updated"),
        @ApiResponse(responseCode = "400", description = "Invalid input data", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "404", description = "Category not found", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "409", description = "Category name already exists", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<CategoryResponse>> updateCategory(
            @Parameter(description = "ID of the category to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated category information", required = true)
            @Valid @RequestBody UpdateCategoryRequest request) {
        log.info("CategoryController | updateCategory | id: {}, request: {}", id, request);
        CategoryResponse updatedCategory = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(CustomResponse.successOf(updatedCategory));
    }
    
    @Operation(summary = "Delete a category", description = "Deletes a category by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Category not found", 
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, 
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteCategory(
            @Parameter(description = "ID of the category to delete", required = true)
            @PathVariable String id) {
        log.info("CategoryController | deleteCategory | id: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(CustomResponse.SUCCESS);
    }
}