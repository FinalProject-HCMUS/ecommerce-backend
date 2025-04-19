package com.hcmus.ecommerce_backend.blog.controller;
import com.hcmus.ecommerce_backend.blog.model.dto.request.CreateBlogRequest;
import com.hcmus.ecommerce_backend.blog.model.dto.request.UpdateBlogRequest;
import com.hcmus.ecommerce_backend.blog.model.dto.response.BlogResponse;
import com.hcmus.ecommerce_backend.blog.service.BlogService;
import com.hcmus.ecommerce_backend.common.model.dto.CustomResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
@RestController
@RequestMapping("/blogs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Blog", description = "Blog management APIs")
public class BlogController {

    private final BlogService blogService;

    @Operation(summary = "Get all blogs", description = "Retrieves a paginated list of blogs with sorting capabilities")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated blogs")
    @GetMapping
    public ResponseEntity<CustomResponse<Page<BlogResponse>>> getAllBlogs(
            @Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sorting criteria in the format: property(,asc|desc). " +
                    "Default sort order is ascending. " +
                    "Multiple sort criteria are supported.") @RequestParam(required = false) String[] sort) {
        log.info("BlogController | getAllBlogs | page: {}, size: {}, sort: {}",
                page, size, sort != null ? String.join(", ", sort) : "unsorted");
        Pageable pageable = createPageable(page, size, sort);
        Page<BlogResponse> blogs = blogService.getAllBlogs(pageable);
        return ResponseEntity.ok(CustomResponse.successOf(blogs));
    }

    @Operation(summary = "Get blog by ID", description = "Retrieves a specific blog by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Blog found"),
        @ApiResponse(responseCode = "404", description = "Blog not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<BlogResponse>> getBlogById(
            @Parameter(description = "ID of the blog to retrieve", required = true)
            @PathVariable String id) {
        log.info("BlogController | getBlogById | id: {}", id);
        BlogResponse blog = blogService.getBlogById(id);
        return ResponseEntity.ok(CustomResponse.successOf(blog));
    }

    @Operation(summary = "Create a new blog", description = "Creates a new blog with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Blog successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "409", description = "Blog already exists",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CustomResponse<BlogResponse>> createBlog(
            @Parameter(description = "Blog information for creation", required = true)
            @Valid @RequestBody CreateBlogRequest request) {
        log.info("BlogController | createBlog | request: {}", request);
        BlogResponse createdBlog = blogService.createBlog(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.<BlogResponse>builder()
                        .httpStatus(HttpStatus.CREATED)
                        .isSuccess(true)
                        .data(createdBlog)
                        .build());
    }

    @Operation(summary = "Update a blog", description = "Updates an existing blog with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Blog successfully updated"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "404", description = "Blog not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "409", description = "blog title and user id already exists",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<BlogResponse>> updateBlog(
            @Parameter(description = "ID of the blog to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated blog information", required = true)
            @Valid @RequestBody UpdateBlogRequest request) {
        log.info("BlogController | updateBlog | id: {}, request: {}", id, request);
        BlogResponse updatedBlog = blogService.updateBlog(id, request);
        return ResponseEntity.ok(CustomResponse.successOf(updatedBlog));
    }

    @Operation(summary = "Delete a blog", description = "Deletes a blog by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Blog successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Blog not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteBlog(
            @Parameter(description = "ID of the blog to delete", required = true)
            @PathVariable String id) {
        log.info("BlogController | deleteBlog | id: {}", id);
        blogService.deleteBlog(id);
        return ResponseEntity.ok(CustomResponse.SUCCESS);
    }

        private Pageable createPageable(int page, int size, String[] sortParams) {
                if (sortParams == null || sortParams.length == 0) {
                        return PageRequest.of(page, size);
                }

                List<Sort.Order> orders = new ArrayList<>();
                for (String param : sortParams) {
                        if (param.contains(",")) {
                                String[] parts = param.split(",");
                                Sort.Direction direction = parts[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC
                                                : Sort.Direction.ASC;
                                orders.add(new Sort.Order(direction, parts[0]));
                        } else {
                                orders.add(new Sort.Order(Sort.Direction.ASC, param));
                        }
                }

                return PageRequest.of(page, size, Sort.by(orders));
        }
}
