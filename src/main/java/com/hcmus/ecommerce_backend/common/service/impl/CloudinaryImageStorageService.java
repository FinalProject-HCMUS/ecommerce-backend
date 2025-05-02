package com.hcmus.ecommerce_backend.common.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.hcmus.ecommerce_backend.common.model.entity.SystemSetting;
import com.hcmus.ecommerce_backend.common.model.enums.CloudinaryKeys;
import com.hcmus.ecommerce_backend.common.repository.SystemSettingRepository;
import com.hcmus.ecommerce_backend.common.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private SystemSettingRepository systemSettingRepository;

    private String cloudName;
    
    private String apiKey;
    
    private String apiSecret;

    private String folder;

    private Cloudinary getCloudinaryInstance() {

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
    public String uploadImage(MultipartFile file) throws IOException {
        return uploadImage(file, null);
    }
    
    @Override
    public String uploadImage(MultipartFile file, Map<String, Object> options) throws IOException {
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
            throw e;
        }
    }
    
    @Override
    public boolean deleteImage(String imageUrl) throws IOException {
        log.info("CloudinaryImageStorageService | deleteImage | Deleting image: {}", imageUrl);
        try {
            // Extract public_id from URL
            String publicId = extractPublicIdFromUrl(imageUrl);

            Map result = getCloudinaryInstance().uploader().destroy(publicId, ObjectUtils.emptyMap());
            String status = (String) result.get("result");

            boolean success = "ok".equals(status);
            log.info("CloudinaryImageStorageService | deleteImage | Delete status: {}", status);

            return success;
        } catch (IOException e) {
            log.error("CloudinaryImageStorageService | deleteImage | Error deleting image: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private String extractPublicIdFromUrl(String imageUrl) {
        // Extract public_id from Cloudinary URL
        // Format: https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/{public_id}.{extension}
        
        if (imageUrl == null) return null;
        
        // Remove file extension
        String withoutExtension = imageUrl.substring(0, imageUrl.lastIndexOf('.'));
        
        // Get the part after /upload/
        int uploadIndex = withoutExtension.indexOf("/upload/");
        if (uploadIndex == -1) return null;
        
        String afterUpload = withoutExtension.substring(uploadIndex + 8);
        
        // Remove version if present (/v1234567890/)
        if (afterUpload.startsWith("/v")) {
            int versionEndIndex = afterUpload.indexOf("/", 2);
            if (versionEndIndex != -1) {
                afterUpload = afterUpload.substring(versionEndIndex + 1);
            }
        }
        
        return afterUpload;
    }
}