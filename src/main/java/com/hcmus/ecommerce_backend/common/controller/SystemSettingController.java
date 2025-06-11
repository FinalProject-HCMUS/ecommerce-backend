package com.hcmus.ecommerce_backend.common.controller;

import com.hcmus.ecommerce_backend.common.model.dto.UpdateSystemSettingRequest;
import com.hcmus.ecommerce_backend.common.model.dto.CustomResponse;
import com.hcmus.ecommerce_backend.common.model.dto.SystemSettingResponse;
import com.hcmus.ecommerce_backend.common.service.SystemSettingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system-settings")
@RequiredArgsConstructor
@Slf4j
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    @Operation(summary = "Get all system settings", description = "Retrieves all system settings, optionally filtered by service name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved system settings"),
    })
    @GetMapping
    public ResponseEntity<List<SystemSettingResponse>> getAllSystemSettings(
            @Parameter(description = "Name of the service to filter settings") @RequestParam (required = false) String serviceName) {
        log.info("SystemSettingController | getAllSystemSettings | Service name: {}", serviceName);
        List<SystemSettingResponse> settings = systemSettingService.getAllSystemSettings(serviceName);
        return ResponseEntity.ok(settings);
    }

    @Operation(summary = "Update multiple system settings", description = "Updates multiple system settings based on key-value pairs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated system settings"),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class))),
        @ApiResponse(responseCode = "404", description = "Key not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CustomResponse.class))),
    })
    @PutMapping
    public ResponseEntity<List<SystemSettingResponse>> updateSystemSettings(
            @RequestBody UpdateSystemSettingRequest request) {
        log.info("SystemSettingController | updateSystemSettings | Request: {}", request);
        List<SystemSettingResponse> updatedSettings = systemSettingService.updateSystemSettings(request);
        return ResponseEntity.ok(updatedSettings);
    }

    @Operation(summary = "Get all distinct service names", description = "Retrieves a list of all distinct service names from system settings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved service names"),
    })
    @GetMapping("/service-names")
    public ResponseEntity<List<String>> getAllServiceNames() {
        log.info("SystemSettingController | getAllServiceNames | Retrieving all distinct service names");
        List<String> serviceNames = systemSettingService.getAllServiceNames();
        return ResponseEntity.ok(serviceNames);
    }
}