package com.hcmus.ecommerce_backend.common.controller;

import com.hcmus.ecommerce_backend.common.model.dto.CustomResponse;
import com.hcmus.ecommerce_backend.common.service.ImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Image Upload", description = "API for uploading images")
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    @Operation(summary = "Upload an image", description = "Uploads an image and returns the URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to upload image",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CustomResponse.class)))
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomResponse<String>> uploadImage(
            @Parameter(description = "Image file to upload", required = true)
            @RequestParam("file") MultipartFile file) {
        
        log.info("ImageUploadController | uploadImage | Uploading image: {}", file.getOriginalFilename());
        
            String imageUrl = imageUploadService.uploadImage(file);
            log.info("ImageUploadController | uploadImage | Image uploaded successfully: {}", imageUrl);
            return ResponseEntity.ok(CustomResponse.successOf(imageUrl));
    }

    @Operation(summary = "Upload an image with options", description = "Uploads an image with options (like resizing) and returns the URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to upload image",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CustomResponse.class)))
    })
    @PostMapping(value = "/upload/with-options", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomResponse<String>> uploadImageWithOptions(
            @Parameter(description = "Image file to upload", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Width to resize the image to")
            @RequestParam(required = false) Integer width,
            @Parameter(description = "Height to resize the image to")
            @RequestParam(required = false) Integer height,
            @Parameter(description = "Cropping method (fill, limit, crop, scale, etc.)")
            @RequestParam(required = false, defaultValue = "fill") String crop) {
        
        log.info("ImageUploadController | uploadImageWithOptions | Uploading image with options: {}",
                file.getOriginalFilename());
        
            Map<String, Object> options = new HashMap<>();
            if (width != null) options.put("width", width);
            if (height != null) options.put("height", height);
            options.put("crop", crop);
            
            String imageUrl = imageUploadService.uploadImage(file, options);
            log.info("ImageUploadController | uploadImageWithOptions | Image uploaded successfully: {}", imageUrl);
            return ResponseEntity.ok(CustomResponse.successOf(imageUrl));
    }

    @Operation(summary = "Upload multiple images", description = "Uploads multiple images and returns their URLs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Images uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to upload images",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CustomResponse.class)))
    })
    @PostMapping(value = "/upload/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomResponse<List<String>>> uploadMultipleImages(
            @Parameter(description = "Image files to upload", required = true)
            @RequestParam("files") List<MultipartFile> files) {

        log.info("ImageUploadController | uploadMultipleImages | Uploading {} images", files.size());

        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String imageUrl = imageUploadService.uploadImage(file);
                imageUrls.add(imageUrl);
                log.info("ImageUploadController | uploadMultipleImages | Image uploaded successfully: {}", imageUrl);
            } catch (Exception e) {
                log.error("ImageUploadController | uploadMultipleImages | Failed to upload image: {}",
                        file.getOriginalFilename(), e);
            }
        }

        log.info("ImageUploadController | uploadMultipleImages | Successfully uploaded {}/{} images",
                imageUrls.size(), files.size());
        return ResponseEntity.ok(CustomResponse.successOf(imageUrls));
    }

    @Operation(summary = "Upload multiple images with options", description = "Uploads multiple images with options (like resizing) and returns their URLs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Images uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to upload images",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CustomResponse.class)))
    })
    @PostMapping(value = "/upload/batch/with-options", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomResponse<List<String>>> uploadMultipleImagesWithOptions(
            @Parameter(description = "Image files to upload", required = true)
            @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "Width to resize the images to")
            @RequestParam(required = false) Integer width,
            @Parameter(description = "Height to resize the images to")
            @RequestParam(required = false) Integer height,
            @Parameter(description = "Cropping method (fill, limit, crop, scale, etc.)")
            @RequestParam(required = false, defaultValue = "fill") String crop) {

        log.info("ImageUploadController | uploadMultipleImagesWithOptions | Uploading {} images with options",
                files.size());

        Map<String, Object> options = new HashMap<>();
        if (width != null) options.put("width", width);
        if (height != null) options.put("height", height);
        options.put("crop", crop);

        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String imageUrl = imageUploadService.uploadImage(file, options);
                imageUrls.add(imageUrl);
                log.info("ImageUploadController | uploadMultipleImagesWithOptions | Image uploaded successfully: {}",
                        imageUrl);
            } catch (Exception e) {
                log.error("ImageUploadController | uploadMultipleImagesWithOptions | Failed to upload image: {}",
                        file.getOriginalFilename(), e);
                // Continue with other images even if one fails
            }
        }

        log.info("ImageUploadController | uploadMultipleImagesWithOptions | Successfully uploaded {}/{} images",
                imageUrls.size(), files.size());
        return ResponseEntity.ok(CustomResponse.successOf(imageUrls));
    }


    @Operation(summary = "Delete an image", description = "Deletes an image by its URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to delete image",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CustomResponse.class)))
    })
    @DeleteMapping("/delete")
    public ResponseEntity<CustomResponse<Boolean>> deleteImage(
            @Parameter(description = "URL of the image to delete", required = true)
            @RequestParam String imageUrl) {
        
        log.info("ImageUploadController | deleteImage | Deleting image: {}", imageUrl);
        
            boolean deleted = imageUploadService.deleteImage(imageUrl);
            log.info("ImageUploadController | deleteImage | Image deletion status: {}", deleted);
            return ResponseEntity.ok(CustomResponse.successOf(deleted));
    }
}