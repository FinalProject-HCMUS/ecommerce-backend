package com.hcmus.ecommerce_backend.common.factory;

import com.hcmus.ecommerce_backend.common.model.enums.StorageType;
import com.hcmus.ecommerce_backend.common.service.ImageStorageService;
import com.hcmus.ecommerce_backend.common.service.impl.CloudinaryImageStorageService;
import com.hcmus.ecommerce_backend.common.service.impl.FirebaseImageStorageService;
import com.hcmus.ecommerce_backend.common.service.impl.LocalImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StorageServiceFactory {

    private final CloudinaryImageStorageService cloudinaryImageStorageService;
    private final FirebaseImageStorageService firebaseImageStorageService;
    private final LocalImageStorageService localImageStorageService;
    
    /**
     * Returns the appropriate storage service based on the storage type
     * 
     * @param storageType the type of storage to use
     * @return the corresponding ImageStorageService
     */
    public ImageStorageService getStorageService(StorageType storageType) {
        log.debug("StorageServiceFactory | getStorageService | Getting service for type: {}", storageType);
        
        return switch (storageType) {
            case CLOUDINARY -> cloudinaryImageStorageService;
            case FIREBASE_STORAGE -> firebaseImageStorageService;
            case LOCAL_STORAGE -> localImageStorageService;
            case AWS_S3 -> null; // Not implemented yet
            default -> {
                log.warn("StorageServiceFactory | getStorageService | Unknown storage type: {}, defaulting to CLOUDINARY", storageType);
                yield cloudinaryImageStorageService;
            }
        };
    }
}