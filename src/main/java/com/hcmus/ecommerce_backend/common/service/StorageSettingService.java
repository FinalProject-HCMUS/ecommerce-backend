package com.hcmus.ecommerce_backend.common.service;

import com.hcmus.ecommerce_backend.common.model.enums.StorageType;

public interface StorageSettingService {
    /**
     * Gets the current system storage type
     * @return the storage type
     */
    StorageType getSystemStorageType();
    
    /**
     * Sets the system storage type
     * @param storageType the storage type
     */
    void setSystemStorageType(StorageType storageType);
    
    /**
     * Gets the default storage type
     * @return the default storage type
     */
    StorageType getDefaultStorageType();
}