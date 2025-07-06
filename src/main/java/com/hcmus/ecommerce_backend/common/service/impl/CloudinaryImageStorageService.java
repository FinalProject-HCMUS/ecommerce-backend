package com.hcmus.ecommerce_backend.common.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.hcmus.ecommerce_backend.common.exception.ImageUploadException;
import com.hcmus.ecommerce_backend.common.model.entity.SystemSetting;
import com.hcmus.ecommerce_backend.common.model.enums.CloudinaryKeys;
import com.hcmus.ecommerce_backend.common.repository.SystemSettingRepository;
import com.hcmus.ecommerce_backend.common.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
//import java.util.HashMap;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryImageStorageService implements ImageStorageService {

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    private String cloudName;
    
    private String apiKey;
    
    private String apiSecret;

    private String folder;

    public Cloudinary getCloudinaryInstance() {

        if (cloudName == null || apiKey == null || apiSecret == null || folder == null) {
            SystemSetting cloudNameSetting = systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_CLOUD_NAME.name()).orElse(null);
            SystemSetting apiKeySetting = systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_API_KEY.name()).orElse(null);
            SystemSetting apiSecretSetting = systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_API_SECRET.name()).orElse(null);
            SystemSetting folderSetting = systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_FOLDER.name()).orElse(null);
            if (folderSetting != null) {
                folder = folderSetting.getValue();
            }
            if (cloudNameSetting != null) {
                cloudName = cloudNameSetting.getValue();
            }
            if (apiKeySetting != null) {
                apiKey = apiKeySetting.getValue();
            }
            if (apiSecretSetting != null) {
                apiSecret = apiSecretSetting.getValue();
            }
        }

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }
    
    @Override
    public String uploadImage(MultipartFile file) {
        return uploadImage(file, null);
    }
    
    @Override
    public String uploadImage(MultipartFile file, Map<String, Object> options) {
        log.info("CloudinaryImageStorageService | uploadImage | Uploading to folder: {}", folder);
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("folder", folder);
            params.put("resource_type", "auto");

            if (options != null) {
                params.putAll(options);
            }

            Map uploadResult = getCloudinaryInstance().uploader().upload(file.getBytes(), params);
            String imageUrl = (String) uploadResult.get("secure_url");

            log.info("CloudinaryImageStorageService | uploadImage | Successfully uploaded image: {}", imageUrl);
            return imageUrl;
        } catch (IOException e) {
            log.error("CloudinaryImageStorageService | uploadImage | Error uploading image: {}", e.getMessage(), e);
            throw new ImageUploadException();
        }
    }
    
    @Override
    public boolean deleteImage(String imageUrl) {
        log.info("CloudinaryImageStorageService | deleteImage | Deleting image: {}", imageUrl);
        try {
            // Extract public_id from URL
            String publicId = extractPublicIdFromUrl(imageUrl);
            log.info("CloudinaryImageStorageService | deleteImage | Extracted public_id: {}", publicId);
            Map result = getCloudinaryInstance().uploader().destroy(publicId, ObjectUtils.emptyMap());
            String status = (String) result.get("result");

            boolean success = "ok".equals(status);
            log.info("CloudinaryImageStorageService | deleteImage | Delete status: {}", status);

            return success;
        } catch (IOException e) {
            log.error("CloudinaryImageStorageService | deleteImage | Error deleting image: {}", e.getMessage(), e);
            return false;
        }
    }
    
    public String extractPublicIdFromUrl(String imageUrl) {
        if (imageUrl == null) return null;

        // First check if the URL contains /upload/
        int uploadIndex = imageUrl.indexOf("/upload/");
        if (uploadIndex == -1) return null;

        // Get everything after /upload/
        String afterUpload = imageUrl.substring(uploadIndex + 8);

        // Extract public_id by removing version number (if present) and file extension
        String publicId;

        // Handle version number: v1746266513/ecommerce/iv6dya5h5llzqvcewctu.jpg
        if (afterUpload.startsWith("v")) {
            int firstSlash = afterUpload.indexOf("/");
            if (firstSlash > 0) {
                // Get everything after the version part
                publicId = afterUpload.substring(firstSlash + 1);
            } else {
                publicId = afterUpload;
            }
        } else {
            publicId = afterUpload;
        }

        // Remove file extension
        int lastDotIndex = publicId.lastIndexOf(".");
        if (lastDotIndex > 0) {
            publicId = publicId.substring(0, lastDotIndex);
        }

        return publicId;
    }
}