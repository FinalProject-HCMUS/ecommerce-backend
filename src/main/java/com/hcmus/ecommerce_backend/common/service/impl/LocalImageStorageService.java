package com.hcmus.ecommerce_backend.common.service.impl;

import com.hcmus.ecommerce_backend.common.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalImageStorageService implements ImageStorageService {

    @Value("${app.storage.local.base-path:upload-dir}")
    private String basePath;
    
    @Value("${app.storage.local.base-url:http://localhost:8080/images}")
    private String baseUrl;

    @Override
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        return uploadImage(file, folder, null);
    }

    @Override
    public String uploadImage(MultipartFile file, String folder, Map<String, Object> options) throws IOException {
        log.info("LocalImageStorageService | uploadImage | Uploading image to folder: {}", folder);
        try {
            String filename = UUID.randomUUID().toString() + getExtension(file.getOriginalFilename());
            
            Path folderPath = Paths.get(basePath, folder);
            Files.createDirectories(folderPath);
            
            Path filePath = folderPath.resolve(filename);
            Files.write(filePath, file.getBytes());
            
            String imageUrl = baseUrl + "/" + folder + "/" + filename;
            log.info("LocalImageStorageService | uploadImage | Image uploaded successfully: {}", imageUrl);
            return imageUrl;
        } catch (IOException e) {
            log.error("LocalImageStorageService | uploadImage | Error uploading image: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean deleteImage(String imageUrl) throws IOException {
        log.info("LocalImageStorageService | deleteImage | Deleting image: {}", imageUrl);
        try {
            String relativePath = imageUrl.replace(baseUrl, "");
            Path path = Paths.get(basePath, relativePath);
            boolean deleted = Files.deleteIfExists(path);
            log.info("LocalImageStorageService | deleteImage | Image deletion status: {}", deleted);
            return deleted;
        } catch (IOException e) {
            log.error("LocalImageStorageService | deleteImage | Error deleting image: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    private String getExtension(String fileName) {
        return fileName != null && fileName.contains(".") 
            ? fileName.substring(fileName.lastIndexOf(".")) 
            : "";
    }
}