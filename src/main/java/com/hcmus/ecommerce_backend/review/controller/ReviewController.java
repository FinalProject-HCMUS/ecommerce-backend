package com.hcmus.ecommerce_backend.review.controller;

import com.hcmus.ecommerce_backend.common.model.dto.CustomResponse;
import com.hcmus.ecommerce_backend.common.utils.CreatePageable;
import com.hcmus.ecommerce_backend.review.model.dto.request.CreateReviewRequest;
import com.hcmus.ecommerce_backend.review.model.dto.response.ReviewResponse;
import com.hcmus.ecommerce_backend.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Review", description = "Review management APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Get all reviews with pagination", description = "Retrieves a paginated list of reviews with sorting capabilities")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated reviews")
    @GetMapping("")
    public ResponseEntity<CustomResponse<Page<ReviewResponse>>> getAllReviewsPaginated(
            @Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sorting criteria in the format: property(,asc|desc). " +
                    "Default sort order is ascending. " +
                    "Multiple sort criteria are supported.") @RequestParam(required = false) String[] sort) {

        log.info("ReviewController | getAllReviewsPaginated | page: {}, size: {}, sort: {}",
                page, size, sort != null ? String.join(", ", sort) : "unsorted");

        Pageable pageable = CreatePageable.build(page, size, sort);
        Page<ReviewResponse> reviews = reviewService.getAllReviewsPaginated(pageable);

        return ResponseEntity.ok(CustomResponse.successOf(reviews));
    }

    @Operation(summary = "Search reviews", description = "Search reviews by keyword and/or rating with pagination and sorting")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved search results")
    @GetMapping("/search")
    public ResponseEntity<CustomResponse<Page<ReviewResponse>>> searchReviews(
            @Parameter(description = "Keyword to search in headline or comment") @RequestParam(required = false) String keyword,

            @Parameter(description = "Minimum rating (1-5)") @RequestParam(required = false) @Min(1) @Max(5) Integer minRating,

            @Parameter(description = "Maximum rating (1-5)") @RequestParam(required = false) @Min(1) @Max(5) Integer maxRating,

            @Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sorting criteria in the format: property(,asc|desc). " +
                    "Default sort order is ascending. " +
                    "Multiple sort criteria are supported.") @RequestParam(required = false) String[] sort) {

        log.info(
                "ReviewController | searchReviews | keyword: {}, minRating: {}, maxRating: {}, page: {}, size: {}, sort: {}",
                keyword, minRating, maxRating, page, size, sort != null ? String.join(", ", sort) : "unsorted");

        Pageable pageable = CreatePageable.build(page, size, sort);
        Page<ReviewResponse> reviews = reviewService.searchReviews(keyword, minRating, maxRating, pageable);

        return ResponseEntity.ok(CustomResponse.successOf(reviews));
    }

    @Operation(summary = "Get review by ID", description = "Retrieves a specific review by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review found"),
            @ApiResponse(responseCode = "404", description = "Review not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<ReviewResponse>> getReviewById(
            @Parameter(description = "ID of the review to retrieve", required = true) @PathVariable String id) {
        log.info("ReviewController | getReviewById | id: {}", id);
        ReviewResponse review = reviewService.getReviewById(id);
        return ResponseEntity.ok(CustomResponse.successOf(review));
    }

    @Operation(summary = "Get reviews by order ID with pagination", description = "Retrieves a paginated list of reviews for a specific order")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated order reviews")
    @GetMapping("/order/{orderId}/paginated")
    public ResponseEntity<CustomResponse<Page<ReviewResponse>>> getReviewsByOrderIdPaginated(
            @Parameter(description = "ID of the order", required = true) @PathVariable String orderId,

            @Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sorting criteria in the format: property(,asc|desc). " +
                    "Default sort order is ascending. " +
                    "Multiple sort criteria are supported.") @RequestParam(required = false) String[] sort) {

        log.info("ReviewController | getReviewsByOrderIdPaginated | orderId: {}, page: {}, size: {}, sort: {}",
                orderId, page, size, sort != null ? String.join(", ", sort) : "unsorted");

        Pageable pageable = CreatePageable.build(page, size, sort);
        Page<ReviewResponse> reviews = reviewService.getReviewsByOrderIdPaginated(orderId, pageable);

        return ResponseEntity.ok(CustomResponse.successOf(reviews));
    }

    @Operation(summary = "Search reviews by order ID", description = "Search reviews for a specific order with filters and pagination")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved search results")
    @GetMapping("/order/{orderId}/search")
    public ResponseEntity<CustomResponse<Page<ReviewResponse>>> searchReviewsByOrderId(
            @Parameter(description = "ID of the order", required = true) @PathVariable String orderId,

            @Parameter(description = "Keyword to search in headline or comment") @RequestParam(required = false) String keyword,

            @Parameter(description = "Minimum rating (1-5)") @RequestParam(required = false) @Min(1) @Max(5) Integer minRating,

            @Parameter(description = "Maximum rating (1-5)") @RequestParam(required = false) @Min(1) @Max(5) Integer maxRating,

            @Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sorting criteria in the format: property(,asc|desc). " +
                    "Default sort order is ascending. " +
                    "Multiple sort criteria are supported.") @RequestParam(required = false) String[] sort) {

        log.info(
                "ReviewController | searchReviewsByOrderId | orderId: {}, keyword: {}, minRating: {}, maxRating: {}, page: {}, size: {}, sort: {}",
                orderId, keyword, minRating, maxRating, page, size,
                sort != null ? String.join(", ", sort) : "unsorted");

        Pageable pageable = CreatePageable.build(page, size, sort);
        Page<ReviewResponse> reviews = reviewService.searchReviewsByOrderId(orderId, keyword, minRating, maxRating,
                pageable);

        return ResponseEntity.ok(CustomResponse.successOf(reviews));
    }

    @Operation(summary = "Get reviews by rating range", description = "Retrieves reviews within a specific rating range")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved reviews by rating range")
    @GetMapping("/rating")
    public ResponseEntity<CustomResponse<List<ReviewResponse>>> getReviewsByRatingRange(
            @Parameter(description = "Minimum rating (1-5)", required = true) @RequestParam @Min(1) @Max(5) Integer minRating,
            @Parameter(description = "Maximum rating (1-5)", required = true) @RequestParam @Min(1) @Max(5) Integer maxRating) {
        log.info("ReviewController | getReviewsByRatingRange | minRating: {}, maxRating: {}", minRating, maxRating);
        List<ReviewResponse> reviews = reviewService.getReviewsByRatingRange(minRating, maxRating);
        return ResponseEntity.ok(CustomResponse.successOf(reviews));
    }

    @Operation(summary = "Create a new review", description = "Creates a new review with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Review successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class))),
            @ApiResponse(responseCode = "409", description = "Review already exists for the order", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CustomResponse<ReviewResponse>> createReview(
            @Parameter(description = "Review information for creation", required = true) @Valid @RequestBody CreateReviewRequest request) {
        log.info("ReviewController | createReview | request: {}", request);
        ReviewResponse createdReview = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.<ReviewResponse>builder()
                        .httpStatus(HttpStatus.CREATED)
                        .isSuccess(true)
                        .data(createdReview)
                        .build());
    }

    @Operation(summary = "Delete a review", description = "Deletes a review by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Review not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteReview(
            @Parameter(description = "ID of the review to delete", required = true) @PathVariable String id) {
        log.info("ReviewController | deleteReview | id: {}", id);
        reviewService.deleteReview(id);
        return ResponseEntity.ok(CustomResponse.SUCCESS);
    }
}