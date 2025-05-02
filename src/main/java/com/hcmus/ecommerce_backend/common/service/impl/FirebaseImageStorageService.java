package com.hcmus.ecommerce_backend.common.service.impl;

import com.hcmus.ecommerce_backend.common.service.ImageStorageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseImageStorageService implements ImageStorageService {

    @Value("${app.storage.firebase.bucket-name:name}")
    private String bucketName;
    
    @Value("${app.storage.firebase.credentials-path:path}")
    private String credentialsPath;
    
    @Value("${app.storage.firebase.download-url:url}")
    private String downloadUrlPattern;
    
//    private Storage storage;
    
//    @PostConstruct
//    public void initialize() {
//        try {
//            InputStream serviceAccount = new ClassPathResource(credentialsPath).getInputStream();
//            storage = StorageOptions.newBuilder()
//                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                    .build()
//                    .getService();
//            log.info("FirebaseImageStorageService initialized successfully");
//        } catch (IOException e) {
//            log.error("Failed to initialize Firebase storage: {}", e.getMessage(), e);
//        }
//    }
    
    @Override
    public String uploadImage(MultipartFile file) throws IOException {
        return uploadImage(file, null);
    }
    
    @Override
    public String uploadImage(MultipartFile file, Map<String, Object> options) throws IOException {
        log.info("FirebaseImageStorageService | uploadImage | Uploading to folder: {}", folder);
        return "url";
//        try {
//            String originalFilename = file.getOriginalFilename();
//            String extension = "";
//            if (originalFilename != null && originalFilename.contains(".")) {
//                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
//            }
//
//            String filename = UUID.randomUUID().toString() + extension;
//            String path = folder + "/" + filename;
//
//            BlobId blobId = BlobId.of(bucketName, path);
//            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
//                    .setContentType(file.getContentType())
//                    .build();
//
//            storage.create(blobInfo, file.getBytes());
//
//            String imageUrl = downloadUrlPattern.replace("{bucket}", bucketName).replace("{object}", path);
//            log.info("FirebaseImageStorageService | uploadImage | Successfully uploaded image: {}", imageUrl);
//
//            return imageUrl;
//        } catch (IOException e) {
//            log.error("FirebaseImageStorageService | uploadImage | Error uploading image: {}", e.getMessage(), e);
//            throw e;
//        }
    }
    
    @Override
    public boolean deleteImage(String imageUrl) throws IOException {
        log.info("FirebaseImageStorageService | deleteImage | Deleting image: {}", imageUrl);
        return true;
//        try {
//            String objectPath = extractObjectPathFromUrl(imageUrl);
//            BlobId blobId = BlobId.of(bucketName, objectPath);
//            boolean deleted = storage.delete(blobId);
//
//            log.info("FirebaseImageStorageService | deleteImage | Delete status: {}", deleted);
//            return deleted;
//        } catch (Exception e) {
//            log.error("FirebaseImageStorageService | deleteImage | Error deleting image: {}", e.getMessage(), e);
//            throw new IOException("Failed to delete image", e);
//        }
    }
    
    private String extractObjectPathFromUrl(String imageUrl) {
        // Extract the path from Firebase URL
        // This is a simplified implementation that assumes the URL format from downloadUrlPattern
        String baseUrl = downloadUrlPattern.split("\\{")[0];
        if (imageUrl.startsWith(baseUrl)) {
            String path = imageUrl.substring(baseUrl.length());
            // Remove any query parameters
            if (path.contains("?")) {
                path = path.substring(0, path.indexOf("?"));
            }
            return path;
        }
        throw new IllegalArgumentException("Invalid Firebase Storage URL format");
    }
}