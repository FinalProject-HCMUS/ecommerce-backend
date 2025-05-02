package com.hcmus.ecommerce_backend.common.service.impl;

import com.hcmus.ecommerce_backend.common.model.entity.SystemSetting;
import com.hcmus.ecommerce_backend.common.model.enums.SettingDataType;
import com.hcmus.ecommerce_backend.common.model.enums.StorageType;
import com.hcmus.ecommerce_backend.common.repository.SystemSettingRepository;
import com.hcmus.ecommerce_backend.common.service.StorageSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageSettingServiceImpl implements StorageSettingService {

    private final SystemSettingRepository systemSettingRepository;
    
    @Value("${app.storage.default-provider:CLOUDINARY}")
    private String defaultStorageProvider;
    private final String STORAGE_TYPE_KEY = "STORAGE_TYPE";

    @Override
    public StorageType getSystemStorageType() {
        log.info("StorageSettingServiceImpl | getSystemStorageType | Retrieving system storage type from database");

        Optional<SystemSetting> storageSetting = systemSettingRepository.findByKey(STORAGE_TYPE_KEY);

        if (storageSetting.isPresent()) {
            String storageTypeValue = storageSetting.get().getValue();
            try {
                return StorageType.valueOf(storageTypeValue);
            } catch (IllegalArgumentException e) {
                log.warn("StorageSettingServiceImpl | getSystemStorageType | Found invalid storage type in database: {}",
                        storageTypeValue);
                log.warn("StorageSettingServiceImpl | getSystemStorageType | Falling back to default storage type");

                // Invalid storage type in database, returning default
                StorageType defaultType = getDefaultStorageType();

                // Update the invalid value with the default one
                updateInvalidStorageSetting(storageSetting.get(), defaultType);

                return defaultType;
            }
        } else {
            // No storage setting exists, create a new one with default value
            log.info("StorageSettingServiceImpl | getSystemStorageType | No storage type configured, creating with default value");
            StorageType defaultType = getDefaultStorageType();
            createNewStorageSetting(defaultType);
            return defaultType;
        }
    }
    
    @Override
    @Transactional
    public void setSystemStorageType(StorageType storageType) {
        if (storageType == null) {
            log.error("StorageSettingServiceImpl | setSystemStorageType | Attempted to set null storage type");
            throw new IllegalArgumentException("Storage type cannot be null");
        }

        log.info("StorageSettingServiceImpl | setSystemStorageType | Updating system storage type to: {}", storageType);

        Optional<SystemSetting> existingSetting = systemSettingRepository.findByKey(STORAGE_TYPE_KEY);

        SystemSetting setting;
        if (existingSetting.isPresent()) {
            // Update existing setting
            setting = existingSetting.get();
            setting.setValue(storageType.name());
            setting.setUpdatedAt(LocalDateTime.now());
        } else {
            // Create new setting
            setting = SystemSetting.builder()
                    .key(STORAGE_TYPE_KEY)
                    .value(storageType.name())
                    .type(SettingDataType.String)
                    .build();
        }

        systemSettingRepository.save(setting);
        log.info("StorageSettingServiceImpl | setSystemStorageType | System storage type successfully updated to: {}", storageType);
    }
    
    @Override
    public StorageType getDefaultStorageType() {
        log.debug("StorageSettingServiceImpl | getDefaultStorageType | Getting default storage type from properties: {}",
                defaultStorageProvider);

        try {
            return StorageType.valueOf(defaultStorageProvider.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("StorageSettingServiceImpl | getDefaultStorageType | Invalid storage provider configured: {}",
                    defaultStorageProvider);
            log.warn("StorageSettingServiceImpl | getDefaultStorageType | Falling back to CLOUDINARY as default provider");
            return StorageType.CLOUDINARY;
        }
    }

    private void updateInvalidStorageSetting(SystemSetting existingSetting, StorageType correctType) {
        log.info("StorageSettingServiceImpl | updateInvalidStorageSetting | Correcting invalid storage setting to: {}",
                correctType);

        existingSetting.setValue(correctType.name());
        existingSetting.setUpdatedAt(LocalDateTime.now());
        systemSettingRepository.save(existingSetting);
    }

    private void createNewStorageSetting(StorageType storageType) {
        log.info("StorageSettingServiceImpl | createNewStorageSetting | Creating new storage setting with type: {}",
                storageType);

        SystemSetting newSetting = SystemSetting.builder()
                .key(STORAGE_TYPE_KEY)
                .value(storageType.name())
                .type(SettingDataType.String)
                .build();

        systemSettingRepository.save(newSetting);
    }
}