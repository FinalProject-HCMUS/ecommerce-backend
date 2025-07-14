package com.hcmus.ecommerce_backend.unit.common.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.hcmus.ecommerce_backend.common.model.entity.SystemSetting;
import com.hcmus.ecommerce_backend.common.model.enums.FirebaseKeys;
import com.hcmus.ecommerce_backend.common.repository.SystemSettingRepository;
import com.hcmus.ecommerce_backend.common.service.impl.FirebaseImageStorageService;

@ExtendWith(MockitoExtension.class)
public class FirebaseImageStorageServiceTest {

    @Mock
    private SystemSettingRepository systemSettingRepository;

    @Mock
    private Storage storage;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private GoogleCredentials googleCredentials;

    @Mock
    private StorageOptions storageOptions;

    @InjectMocks
    private FirebaseImageStorageService firebaseImageStorageService;

    // Declare test data variables
    private SystemSetting bucketNameSetting;
    private SystemSetting serviceAccountSetting;
    private SystemSetting downloadUrlPatternSetting;
    private SystemSetting folderSetting;

    @BeforeEach
    void setUp() {
        // Setup test data
        bucketNameSetting = SystemSetting.builder()
                .key(FirebaseKeys.FIREBASE_BUCKET_NAME.name())
                .value("test-bucket")
                .build();

        serviceAccountSetting = SystemSetting.builder()
                .key(FirebaseKeys.FIREBASE_SERVICE_ACCOUNT.name())
                .value("{\"type\":\"service_account\",\"project_id\":\"test-project\"}")
                .build();

        downloadUrlPatternSetting = SystemSetting.builder()
                .key(FirebaseKeys.FIREBASE_DOWNLOAD_URL.name())
                .value("https://firebasestorage.googleapis.com/v0/b/{bucket}/o/{object}?alt=media")
                .build();

        folderSetting = SystemSetting.builder()
                .key(FirebaseKeys.FIREBASE_FOLDER.name())
                .value("images")
                .build();

        // Reset private fields
        ReflectionTestUtils.setField(firebaseImageStorageService, "bucketName", null);
        ReflectionTestUtils.setField(firebaseImageStorageService, "serviceAccountJson", null);
        ReflectionTestUtils.setField(firebaseImageStorageService, "downloadUrlPattern", null);
        ReflectionTestUtils.setField(firebaseImageStorageService, "folder", null);
        ReflectionTestUtils.setField(firebaseImageStorageService, "storage", null);
    }

    // Test deleteImage
    @Test
    void deleteImage_WithValidUrl_ShouldReturnTrue() {
        // Given
        String imageUrl = "https://firebasestorage.googleapis.com/v0/b/test-bucket/o/images%2Ftest.jpg?alt=media";
        
        // Set cached configuration
        ReflectionTestUtils.setField(firebaseImageStorageService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(firebaseImageStorageService, "storage", storage);

        when(storage.delete(any(BlobId.class))).thenReturn(true);

        // When
        boolean result = firebaseImageStorageService.deleteImage(imageUrl);

        // Then
        assertTrue(result);
        verify(storage).delete(any(BlobId.class));
    }

    @Test
    void deleteImage_WithValidUrlButDeleteFails_ShouldReturnFalse() {
        // Given
        String imageUrl = "https://firebasestorage.googleapis.com/v0/b/test-bucket/o/images%2Ftest.jpg?alt=media";
        
        // Set cached configuration
        ReflectionTestUtils.setField(firebaseImageStorageService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(firebaseImageStorageService, "storage", storage);

        when(storage.delete(any(BlobId.class))).thenReturn(false);

        // When
        boolean result = firebaseImageStorageService.deleteImage(imageUrl);

        // Then
        assertFalse(result);
        verify(storage).delete(any(BlobId.class));
    }

    @Test
    void deleteImage_WithStorageException_ShouldReturnFalse() {
        // Given
        String imageUrl = "https://firebasestorage.googleapis.com/v0/b/test-bucket/o/images%2Ftest.jpg?alt=media";
        
        // Set cached configuration
        ReflectionTestUtils.setField(firebaseImageStorageService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(firebaseImageStorageService, "storage", storage);

        when(storage.delete(any(BlobId.class))).thenThrow(new RuntimeException("Storage error"));

        // When
        boolean result = firebaseImageStorageService.deleteImage(imageUrl);

        // Then
        assertFalse(result);
        verify(storage).delete(any(BlobId.class));
    }

    @Test
    void deleteImage_WithUrlWithoutQueryParams_ShouldWork() {
        // Given
        String imageUrl = "https://firebasestorage.googleapis.com/v0/b/test-bucket/o/images%2Ftest.jpg";
        
        // Set cached configuration
        ReflectionTestUtils.setField(firebaseImageStorageService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(firebaseImageStorageService, "storage", storage);

        when(storage.delete(any(BlobId.class))).thenReturn(true);

        // When
        boolean result = firebaseImageStorageService.deleteImage(imageUrl);

        // Then
        assertTrue(result);
        verify(storage).delete(any(BlobId.class));
    }

    @Test
    void deleteImage_WithComplexPath_ShouldWork() {
        // Given
        String imageUrl = "https://firebasestorage.googleapis.com/v0/b/test-bucket/o/images%2Fsubfolder%2Ftest.jpg?alt=media";
        
        // Set cached configuration
        ReflectionTestUtils.setField(firebaseImageStorageService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(firebaseImageStorageService, "storage", storage);

        when(storage.delete(any(BlobId.class))).thenReturn(true);

        // When
        boolean result = firebaseImageStorageService.deleteImage(imageUrl);

        // Then
        assertTrue(result);
        verify(storage).delete(any(BlobId.class));
    }

    // Test configuration caching
    @Test
    void uploadImage_WithCachedConfiguration_ShouldNotQueryDatabase() throws IOException {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getBytes()).thenReturn("test image content".getBytes());

        // Set cached configuration
        ReflectionTestUtils.setField(firebaseImageStorageService, "bucketName", "cached-bucket");
        ReflectionTestUtils.setField(firebaseImageStorageService, "serviceAccountJson", "{\"type\":\"service_account\"}");
        ReflectionTestUtils.setField(firebaseImageStorageService, "downloadUrlPattern", "https://cached.com/{bucket}/{object}");
        ReflectionTestUtils.setField(firebaseImageStorageService, "folder", "cached-folder");
        ReflectionTestUtils.setField(firebaseImageStorageService, "storage", storage);

        when(storage.create(any(BlobInfo.class), any(byte[].class))).thenReturn(null);

        // When
        String result = firebaseImageStorageService.uploadImage(multipartFile);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("https://cached.com/cached-bucket/cached-folder/"));
        assertTrue(result.contains(".jpg"));

        // Should not query database when configuration is cached
        verify(systemSettingRepository, never()).findByKey(anyString());
        verify(storage).create(any(BlobInfo.class), eq("test image content".getBytes()));
    }

    // Test edge cases for extractObjectPathFromUrl
    @Test
    void extractObjectPathFromUrl_WithVariousUrlFormats_ShouldExtractCorrectly() {
        // Given
        String[] testUrls = {
            "https://firebasestorage.googleapis.com/v0/b/test-bucket/o/images%2Ftest.jpg?alt=media",
            "https://firebasestorage.googleapis.com/v0/b/test-bucket/o/images%2Fsubfolder%2Ftest.jpg?alt=media&token=abc123",
            "https://firebasestorage.googleapis.com/v0/b/test-bucket/o/images%2Ftest.jpg",
            "https://firebasestorage.googleapis.com/v0/b/test-bucket/o/folder%2Fsubfolder%2Ftest-file.png?alt=media"
        };

        // Set cached configuration
        ReflectionTestUtils.setField(firebaseImageStorageService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(firebaseImageStorageService, "storage", storage);

        when(storage.delete(any(BlobId.class))).thenReturn(true);

        // When & Then
        for (String url : testUrls) {
            boolean result = firebaseImageStorageService.deleteImage(url);
            assertTrue(result, "Should successfully delete image with URL: " + url);
        }

        verify(storage, times(testUrls.length)).delete(any(BlobId.class));
    }
}