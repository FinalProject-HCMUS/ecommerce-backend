package com.hcmus.ecommerce_backend.common.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface ImageUploadService {
    /**
     * Uploads an image using the system's configured storage
     * @param file the image file to upload
     * @param folder the destination folder for the image
     * @return URL of the uploaded image
     * @throws IOException if upload fails
     */
    String uploadImage(MultipartFile file);
    
    /**
     * Uploads an image with additional options using the system's configured storage
     * @param file the image file to upload
     * @param folder the destination folder for the image
     * @param options additional provider-specific options
     * @return URL of the uploaded image
     * @throws IOException if upload fails
     */
    String uploadImage(MultipartFile file, Map<String, Object> options);
    
    /**
     * Deletes an image by its URL
     * @param imageUrl URL of the image to delete
     * @return true if deleted successfully, false otherwise
     * @throws IOException if deletion fails
     */
    boolean deleteImage(String imageUrl);
}