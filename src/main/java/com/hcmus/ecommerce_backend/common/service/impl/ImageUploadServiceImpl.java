package com.hcmus.ecommerce_backend.common.service.impl;

import com.hcmus.ecommerce_backend.common.factory.StorageServiceFactory;
import com.hcmus.ecommerce_backend.common.model.enums.StorageType;
import com.hcmus.ecommerce_backend.common.service.ImageStorageService;
import com.hcmus.ecommerce_backend.common.service.ImageUploadService;
import com.hcmus.ecommerce_backend.common.service.StorageSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageUploadServiceImpl implements ImageUploadService {

    private final StorageServiceFactory storageServiceFactory;
    private final StorageSettingService storageSettingService;

    @Override
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        return uploadImage(file, folder, null);
    }

    @Override
    public String uploadImage(MultipartFile file, String folder, Map<String, Object> options) throws IOException {
        log.info("ImageUploadServiceImpl | uploadImage | Uploading image to folder: {}", folder);
        
        // Get the system's configured storage type
        StorageType storageType = storageSettingService.getSystemStorageType();
        log.info("ImageUploadServiceImpl | uploadImage | Using storage provider: {}", storageType);
        
        // Get the appropriate storage service
        ImageStorageService storageService = storageServiceFactory.getStorageService(storageType);
        
        // Upload the image
        String imageUrl = storageService.uploadImage(file, folder, options);
        log.info("ImageUploadServiceImpl | uploadImage | Image uploaded successfully: {}", imageUrl);
        
        return imageUrl;
    }

    @Override
    public boolean deleteImage(String imageUrl) throws IOException {
        log.info("ImageUploadServiceImpl | deleteImage | Deleting image: {}", imageUrl);
        
        // Determine which storage service to use based on the image URL
        ImageStorageService storageService = determineStorageServiceFromUrl(imageUrl);
        
        // Delete the image
        boolean deleted = storageService.deleteImage(imageUrl);
        log.info("ImageUploadServiceImpl | deleteImage | Image deletion status: {}", deleted);
        
        return deleted;
    }
    
    /**
     * Determines which storage service to use based on the image URL pattern
     */
    private ImageStorageService determineStorageServiceFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            // Default to system setting if URL is empty
            return storageServiceFactory.getStorageService(storageSettingService.getSystemStorageType());
        }
        
        // Determine storage provider based on URL pattern
        if (imageUrl.contains("cloudinary.com")) {
            return storageServiceFactory.getStorageService(StorageType.CLOUDINARY);
        } else if (imageUrl.contains("firebasestorage.googleapis.com")) {
            return storageServiceFactory.getStorageService(StorageType.FIREBASE_STORAGE);
        } else if (imageUrl.contains("amazonaws.com")) {
            return storageServiceFactory.getStorageService(StorageType.AWS_S3);
        } else {
            // If can't determine from URL, use local storage
            return storageServiceFactory.getStorageService(StorageType.LOCAL_STORAGE);
        }
    }
}