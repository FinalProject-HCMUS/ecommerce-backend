package com.hcmus.ecommerce_backend.product.controller;

    import com.hcmus.ecommerce_backend.product.model.dto.request.color.CreateColorRequest;
    import com.hcmus.ecommerce_backend.product.model.dto.request.color.CreateMultipleColorsRequest;
    import com.hcmus.ecommerce_backend.product.model.dto.request.color.UpdateColorRequest;
    import com.hcmus.ecommerce_backend.product.model.dto.response.ColorResponse;
    import com.hcmus.ecommerce_backend.product.service.ColorService;
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
    @RequestMapping("/colors")
    @RequiredArgsConstructor
    @Slf4j
    @Tag(name = "Color", description = "Color management APIs")
    public class ColorController {

        private final ColorService colorService;

        @Operation(summary = "Get all colors", description = "Retrieves a paginated list of colors with filtering and sorting capabilities")
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated colors")
        @GetMapping
        public ResponseEntity<CustomResponse<Page<ColorResponse>>> getAllColors(
                @Parameter(description = "Zero-based page index (0..N)") @RequestParam(defaultValue = "0") int page,
                @Parameter(description = "The size of the page to be returned") @RequestParam(defaultValue = "10") int size,
                @Parameter(description = "Sorting criteria in the format: property(,asc|desc). " +
                        "Default sort order is ascending. " +
                        "Multiple sort criteria are supported.") @RequestParam(required = false) String[] sort,
                @Parameter(description = "Keyword to search in color name") @RequestParam(required = false) String keyword) {

            log.info("ColorController | getAllColors | page: {}, size: {}, sort: {}, keyword: {}",
                    page, size, sort != null ? String.join(", ", sort) : "unsorted", keyword);

            Pageable pageable = CreatePageable.build(page, size, sort);
            Page<ColorResponse> colors = colorService.searchColors(pageable, keyword);
            return ResponseEntity.ok(CustomResponse.successOf(colors));
        }

        @Operation(summary = "Get color by ID", description = "Retrieves a specific color by its ID")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Color found"),
            @ApiResponse(responseCode = "404", description = "Color not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CustomResponse.class)))
        })
        @GetMapping("/{id}")
        public ResponseEntity<CustomResponse<ColorResponse>> getColorById(
                @Parameter(description = "ID of the color to retrieve", required = true)
                @PathVariable String id) {
            log.info("ColorController | getColorById | id: {}", id);
            ColorResponse color = colorService.getColorById(id);
            return ResponseEntity.ok(CustomResponse.successOf(color));
        }

        @Operation(summary = "Create a new color", description = "Creates a new color with the provided information")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Color successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CustomResponse.class))),
            @ApiResponse(responseCode = "409", description = "Color already exists",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CustomResponse.class)))
        })
        @PostMapping
        public ResponseEntity<CustomResponse<ColorResponse>> createColor(
                @Parameter(description = "Color information for creation", required = true)
                @Valid @RequestBody CreateColorRequest request) {
            log.info("ColorController | createColor | request: {}", request);
            ColorResponse createdColor = colorService.createColor(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(CustomResponse.createdOf(createdColor));
        }

        @Operation(summary = "Create multiple colors", description = "Creates multiple colors at once with the provided information")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Colors successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CustomResponse.class))),
            @ApiResponse(responseCode = "409", description = "One or more colors already exist",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CustomResponse.class)))
        })
        @PostMapping("/batch")
        public ResponseEntity<CustomResponse<List<ColorResponse>>> createMultipleColors(
                @Parameter(description = "List of colors for creation", required = true)
                @Valid @RequestBody CreateMultipleColorsRequest request) {
            log.info("ColorController | createMultipleColors | request with {} colors", request.getColors().size());
            List<ColorResponse> createdColors = colorService.createMultipleColors(request.getColors());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(CustomResponse.createdOf(createdColors));
        }

        @Operation(summary = "Update a color", description = "Updates an existing color with the provided information")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Color successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CustomResponse.class))),
            @ApiResponse(responseCode = "404", description = "Color not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CustomResponse.class))),
            @ApiResponse(responseCode = "409", description = "Color name already exists",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CustomResponse.class)))
        })
        @PutMapping("/{id}")
        public ResponseEntity<CustomResponse<ColorResponse>> updateColor(
                @Parameter(description = "ID of the color to update", required = true)
                @PathVariable String id,
                @Parameter(description = "Updated color information", required = true)
                @Valid @RequestBody UpdateColorRequest request) {
            log.info("ColorController | updateColor | id: {}, request: {}", id, request);
            ColorResponse updatedColor = colorService.updateColor(id, request);
            return ResponseEntity.ok(CustomResponse.successOf(updatedColor));
        }

        @Operation(summary = "Delete a color", description = "Deletes a color by its ID")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Color successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Color not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CustomResponse.class)))
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<CustomResponse<Void>> deleteColor(
                @Parameter(description = "ID of the color to delete", required = true)
                @PathVariable String id) {
            log.info("ColorController | deleteColor | id: {}", id);
            colorService.deleteColor(id);
            return ResponseEntity.ok(CustomResponse.SUCCESS);
        }
    }