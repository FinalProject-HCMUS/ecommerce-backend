package com.hcmus.ecommerce_backend.common.service.impl;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.hcmus.ecommerce_backend.common.exception.ImageUploadException;
import com.hcmus.ecommerce_backend.common.model.entity.SystemSetting;
import com.hcmus.ecommerce_backend.common.model.enums.FirebaseKeys;
import com.hcmus.ecommerce_backend.common.repository.SystemSettingRepository;
import com.hcmus.ecommerce_backend.common.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseImageStorageService implements ImageStorageService {

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    private String bucketName;
    private String serviceAccountJson;
    private String downloadUrlPattern;
    private String folder;
    private Storage storage;

    private Storage getFirebaseStorageInstance() {
        if (storage == null) {
            try {
                if (bucketName == null || serviceAccountJson == null || downloadUrlPattern == null || folder == null) {
                    SystemSetting bucketNameSetting = systemSettingRepository.findByKey(FirebaseKeys.FIREBASE_BUCKET_NAME.name()).orElse(null);
                    SystemSetting serviceAccountSetting = systemSettingRepository.findByKey(FirebaseKeys.FIREBASE_SERVICE_ACCOUNT.name()).orElse(null);
                    SystemSetting downloadUrlPatternSetting = systemSettingRepository.findByKey(FirebaseKeys.FIREBASE_DOWNLOAD_URL.name()).orElse(null);
                    SystemSetting folderSetting = systemSettingRepository.findByKey(FirebaseKeys.FIREBASE_FOLDER.name()).orElse(null);

                    if (bucketNameSetting != null) {
                        bucketName = bucketNameSetting.getValue();
                    }
                    if (serviceAccountSetting != null) {
                        serviceAccountJson = serviceAccountSetting.getValue();
                    }
                    if (downloadUrlPatternSetting != null) {
                        downloadUrlPattern = downloadUrlPatternSetting.getValue();
                    }
                    if (folderSetting != null) {
                        folder = folderSetting.getValue();
                    }
                }

                // Initialize Firebase Storage with service account JSON from DB
                GoogleCredentials credentials = GoogleCredentials.fromStream(
                        new ByteArrayInputStream(serviceAccountJson.getBytes())
                );

                storage = StorageOptions.newBuilder()
                        .setCredentials(credentials)
                        .build()
                        .getService();

                log.info("FirebaseImageStorageService | getFirebaseStorageInstance | Storage initialized successfully");
            } catch (Exception e) {
                log.error("FirebaseImageStorageService | getFirebaseStorageInstance | Failed to initialize storage: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to initialize Firebase Storage", e);
            }
        }
        return storage;
    }

    @Override
    public String uploadImage(MultipartFile file) {
        return uploadImage(file, null);
    }

    @Override
    public String uploadImage(MultipartFile file, Map<String, Object> options) {
        log.info("FirebaseImageStorageService | uploadImage | Uploading to folder: {}", folder);
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String filename = UUID.randomUUID().toString() + extension;
            String path = folder + "/" + filename;

            BlobId blobId = BlobId.of(bucketName, path);
            BlobInfo.Builder blobInfoBuilder = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType());

            // Apply transformation options if available
            if (options != null && !options.isEmpty()) {
                Map<String, String> metadata = new HashMap<>();
                if (options.containsKey("width")) {
                    metadata.put("width", options.get("width").toString());
                }
                if (options.containsKey("height")) {
                    metadata.put("height", options.get("height").toString());
                }
                if (options.containsKey("crop")) {
                    metadata.put("crop", options.get("crop").toString());
                }
                blobInfoBuilder.setMetadata(metadata);
            }

            BlobInfo blobInfo = blobInfoBuilder.build();
            getFirebaseStorageInstance().create(blobInfo, file.getBytes());

            String imageUrl = downloadUrlPattern
                    .replace("{bucket}", bucketName)
                    .replace("{object}", path);

            log.info("FirebaseImageStorageService | uploadImage | Successfully uploaded image: {}", imageUrl);
            return imageUrl;
        } catch (IOException e) {
            log.error("FirebaseImageStorageService | uploadImage | Error uploading image: {}", e.getMessage(), e);
            throw new ImageUploadException();
        }
    }

    @Override
    public boolean deleteImage(String imageUrl) {
        log.info("FirebaseImageStorageService | deleteImage | Deleting image: {}", imageUrl);
        try {
            String objectPath = extractObjectPathFromUrl(imageUrl);
            log.info("FirebaseImageStorageService | deleteImage | Extracted object path: {}", objectPath);

            BlobId blobId = BlobId.of(bucketName, objectPath);
            boolean deleted = getFirebaseStorageInstance().delete(blobId);

            log.info("FirebaseImageStorageService | deleteImage | Delete status: {}", deleted);
            return deleted;
        } catch (Exception e) {
            log.error("FirebaseImageStorageService | deleteImage | Error deleting image: {}", e.getMessage(), e);
            return false;
        }
    }

    private String extractObjectPathFromUrl(String imageUrl) {
        if (imageUrl == null) return null;

        // Extract the path from Firebase Storage URL format
        // Common format: https://firebasestorage.googleapis.com/v0/b/{bucket}/o/{object}?alt=media
        if (imageUrl.contains("/o/")) {
            int startIndex = imageUrl.indexOf("/o/") + 3;
            String path = imageUrl.substring(startIndex);

            // Remove query parameters
            if (path.contains("?")) {
                path = path.substring(0, path.indexOf("?"));
            }

            // URL-decode the path
            path = path.replace("%2F", "/");

            log.debug("FirebaseImageStorageService | extractObjectPathFromUrl | Extracted path: {}", path);
            return path;
        }

        log.warn("FirebaseImageStorageService | extractObjectPathFromUrl | Could not extract path from URL: {}", imageUrl);
        throw new IllegalArgumentException("Invalid Firebase Storage URL format");
    }
}