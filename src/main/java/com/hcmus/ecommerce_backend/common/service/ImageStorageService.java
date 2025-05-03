package com.hcmus.ecommerce_backend.common.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

/**
 * Interface for image storage services following the Strategy pattern.
 * Each implementation represents a different storage provider.
 */
public interface ImageStorageService {
    
    /**
     * Uploads an image file to a specific folder
     * 
     * @param file The image file to upload
     * @return URL of the uploaded image
     * @throws IOException if upload fails
     */
    String uploadImage(MultipartFile file);
    
    /**
     * Uploads an image file with additional options
     * 
     * @param file The image file to upload
     * @param options Additional provider-specific options
     * @return URL of the uploaded image
     * @throws IOException if upload fails
     */
    String uploadImage(MultipartFile file, Map<String, Object> options);
    
    /**
     * Deletes an image by URL
     * 
     * @param imageUrl URL of the image to delete
     * @return true if deleted successfully, false otherwise
     * @throws IOException if deletion fails
     */
    boolean deleteImage(String imageUrl);
}