package com.hcmus.ecommerce_backend.statistics.controller;

import com.hcmus.ecommerce_backend.common.model.dto.CustomResponse;
import com.hcmus.ecommerce_backend.statistics.service.StatisticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Statistics", description = "Statistics management APIs")
public class StatisticController {

    private final StatisticService statisticService;

    @Operation(summary = "Get sales analysis data", description = "Retrieve sales analysis data by month or year")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved sales data",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/analysis")
    public ResponseEntity<CustomResponse<Map<String, Object>>> getSalesAnalysis(
            @Parameter(description = "Choose whether to retrieve data by month or year", required = true)
            @RequestParam String type,
            @Parameter(description = "Month-year (e.g., '10-2024') or year (e.g., '2024') depending on `type`", required = true)
            @RequestParam String date) {
        log.info("StatisticController | getSalesAnalysis | type: {}, date: {}", type, date);
        Map<String, Object> analysisData = statisticService.getSalesAnalysis(type, date);
        return ResponseEntity.ok(CustomResponse.successOf(analysisData));
    }

    @Operation(summary = "Get top 10 best-selling products", description = "Retrieve the top 10 best-selling products for a specific month or year")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved best-selling products",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/best-sellers")
    public ResponseEntity<CustomResponse<Map<String, Object>>> getBestSellers(
            @Parameter(description = "Choose whether to retrieve data by month or year", required = true)
            @RequestParam String type,
            @Parameter(description = "Month-year (e.g., '10-2024') or year (e.g., '2024') depending on `type`", required = true)
            @RequestParam String date) {
        log.info("StatisticController | getBestSellers | type: {}, date: {}", type, date);
        Map<String, Object> bestSellers = statisticService.getBestSellers(type, date);
        return ResponseEntity.ok(CustomResponse.successOf(bestSellers));
    }

    @Operation(summary = "Get product category statistics", description = "Retrieve statistics for product categories")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved product category statistics",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    @GetMapping("/product-categories")
    public ResponseEntity<CustomResponse<Map<String, Object>>> getProductCategoryStatistics() {
        log.info("StatisticController | getProductCategoryStatistics");
        Map<String, Object> categoryStatistics = statisticService.getProductCategoryStatistics();
        return ResponseEntity.ok(CustomResponse.successOf(categoryStatistics));
    }

    @Operation(summary = "Retrieve incomplete orders and estimated revenue", description = "Returns a list of all orders that are not yet completed, along with the estimated total revenue if they are fulfilled")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved incomplete orders and estimated revenue",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    @GetMapping("/order-incomplete")
    public ResponseEntity<CustomResponse<Map<String, Object>>> getIncompleteOrders(
            @Parameter(description = "Optional filter by month and year (format MM-YYYY)", required = false)
            @RequestParam(required = false) String date) {
        log.info("StatisticController | getIncompleteOrders | date: {}", date);
        Map<String, Object> incompleteOrders = statisticService.getIncompleteOrders(date);
        return ResponseEntity.ok(CustomResponse.successOf(incompleteOrders));
    }

}