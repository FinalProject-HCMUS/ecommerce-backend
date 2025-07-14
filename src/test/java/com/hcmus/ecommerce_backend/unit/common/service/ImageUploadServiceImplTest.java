package com.hcmus.ecommerce_backend.unit.common.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.hcmus.ecommerce_backend.common.exception.ImageUploadException;
import com.hcmus.ecommerce_backend.common.factory.StorageServiceFactory;
import com.hcmus.ecommerce_backend.common.model.enums.StorageType;
import com.hcmus.ecommerce_backend.common.service.ImageStorageService;
import com.hcmus.ecommerce_backend.common.service.StorageSettingService;
import com.hcmus.ecommerce_backend.common.service.impl.ImageUploadServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ImageUploadServiceImplTest {

    @Mock
    private StorageServiceFactory storageServiceFactory;

    @Mock
    private StorageSettingService storageSettingService;

    @Mock
    private ImageStorageService cloudinaryStorageService;

    @Mock
    private ImageStorageService firebaseStorageService;

    @Mock
    private ImageStorageService awsS3StorageService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ImageUploadServiceImpl imageUploadService;

    @BeforeEach
    void setUp() {
        // Empty setup - no unnecessary stubbing
    }

    // Test uploadImage(MultipartFile file)
    @Test
    void uploadImage_WithValidFile_ShouldUploadSuccessfully() {
        // Given
        String expectedUrl = "https://cloudinary.com/test/image.jpg";
        
        when(storageSettingService.getSystemStorageType()).thenReturn(StorageType.CLOUDINARY);
        when(storageServiceFactory.getStorageService(StorageType.CLOUDINARY)).thenReturn(cloudinaryStorageService);
        when(cloudinaryStorageService.uploadImage(multipartFile, null)).thenReturn(expectedUrl);

        // When
        String result = imageUploadService.uploadImage(multipartFile);

        // Then
        assertNotNull(result);
        assertEquals(expectedUrl, result);
        
        verify(storageSettingService).getSystemStorageType();
        verify(storageServiceFactory).getStorageService(StorageType.CLOUDINARY);
        verify(cloudinaryStorageService).uploadImage(multipartFile, null);
    }

    @Test
    void uploadImage_WithFirebaseStorageType_ShouldUploadSuccessfully() {
        // Given
        String expectedUrl = "https://firebasestorage.googleapis.com/v0/b/bucket/o/image.jpg?alt=media";
        
        when(storageSettingService.getSystemStorageType()).thenReturn(StorageType.FIREBASE_STORAGE);
        when(storageServiceFactory.getStorageService(StorageType.FIREBASE_STORAGE)).thenReturn(firebaseStorageService);
        when(firebaseStorageService.uploadImage(multipartFile, null)).thenReturn(expectedUrl);

        // When
        String result = imageUploadService.uploadImage(multipartFile);

        // Then
        assertNotNull(result);
        assertEquals(expectedUrl, result);
        
        verify(storageSettingService).getSystemStorageType();
        verify(storageServiceFactory).getStorageService(StorageType.FIREBASE_STORAGE);
        verify(firebaseStorageService).uploadImage(multipartFile, null);
    }

    @Test
    void uploadImage_WithAwsS3StorageType_ShouldUploadSuccessfully() {
        // Given
        String expectedUrl = "https://bucket.s3.amazonaws.com/image.jpg";
        
        when(storageSettingService.getSystemStorageType()).thenReturn(StorageType.AWS_S3);
        when(storageServiceFactory.getStorageService(StorageType.AWS_S3)).thenReturn(awsS3StorageService);
        when(awsS3StorageService.uploadImage(multipartFile, null)).thenReturn(expectedUrl);

        // When
        String result = imageUploadService.uploadImage(multipartFile);

        // Then
        assertNotNull(result);
        assertEquals(expectedUrl, result);
        
        verify(storageSettingService).getSystemStorageType();
        verify(storageServiceFactory).getStorageService(StorageType.AWS_S3);
        verify(awsS3StorageService).uploadImage(multipartFile, null);
    }

    @Test
    void uploadImage_WithStorageServiceException_ShouldThrowException() {
        // Given
        when(storageSettingService.getSystemStorageType()).thenReturn(StorageType.CLOUDINARY);
        when(storageServiceFactory.getStorageService(StorageType.CLOUDINARY)).thenReturn(cloudinaryStorageService);
        when(cloudinaryStorageService.uploadImage(multipartFile, null))
                .thenThrow(new ImageUploadException("Upload failed"));

        // When & Then
        assertThrows(ImageUploadException.class,
                () -> imageUploadService.uploadImage(multipartFile));

        verify(storageSettingService).getSystemStorageType();
        verify(storageServiceFactory).getStorageService(StorageType.CLOUDINARY);
        verify(cloudinaryStorageService).uploadImage(multipartFile, null);
    }

    @Test
    void uploadImage_WithFactoryException_ShouldThrowException() {
        // Given
        when(storageSettingService.getSystemStorageType()).thenReturn(StorageType.CLOUDINARY);
        when(storageServiceFactory.getStorageService(StorageType.CLOUDINARY))
                .thenThrow(new RuntimeException("Factory error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> imageUploadService.uploadImage(multipartFile));

        verify(storageSettingService).getSystemStorageType();
        verify(storageServiceFactory).getStorageService(StorageType.CLOUDINARY);
    }

    @Test
    void uploadImage_WithSettingServiceException_ShouldThrowException() {
        // Given
        when(storageSettingService.getSystemStorageType())
                .thenThrow(new RuntimeException("Setting service error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> imageUploadService.uploadImage(multipartFile));

        verify(storageSettingService).getSystemStorageType();
        verify(storageServiceFactory, never()).getStorageService(any());
    }

    // Test uploadImage(MultipartFile file, Map<String, Object> options)
    @Test
    void uploadImage_WithValidFileAndOptions_ShouldUploadSuccessfully() {
        // Given
        Map<String, Object> options = new HashMap<>();
        options.put("width", 800);
        options.put("height", 600);
        options.put("crop", "fill");
        
        String expectedUrl = "https://cloudinary.com/test/image.jpg";
        
        when(storageSettingService.getSystemStorageType()).thenReturn(StorageType.CLOUDINARY);
        when(storageServiceFactory.getStorageService(StorageType.CLOUDINARY)).thenReturn(cloudinaryStorageService);
        when(cloudinaryStorageService.uploadImage(multipartFile, options)).thenReturn(expectedUrl);

        // When
        String result = imageUploadService.uploadImage(multipartFile, options);

        // Then
        assertNotNull(result);
        assertEquals(expectedUrl, result);
        
        verify(storageSettingService).getSystemStorageType();
        verify(storageServiceFactory).getStorageService(StorageType.CLOUDINARY);
        verify(cloudinaryStorageService).uploadImage(multipartFile, options);
    }

    @Test
    void uploadImage_WithEmptyOptions_ShouldUploadSuccessfully() {
        // Given
        Map<String, Object> options = new HashMap<>();
        String expectedUrl = "https://cloudinary.com/test/image.jpg";
        
        when(storageSettingService.getSystemStorageType()).thenReturn(StorageType.CLOUDINARY);
        when(storageServiceFactory.getStorageService(StorageType.CLOUDINARY)).thenReturn(cloudinaryStorageService);
        when(cloudinaryStorageService.uploadImage(multipartFile, options)).thenReturn(expectedUrl);

        // When
        String result = imageUploadService.uploadImage(multipartFile, options);

        // Then
        assertNotNull(result);
        assertEquals(expectedUrl, result);
        
        verify(storageSettingService).getSystemStorageType();
        verify(storageServiceFactory).getStorageService(StorageType.CLOUDINARY);
        verify(cloudinaryStorageService).uploadImage(multipartFile, options);
    }

    @Test
    void uploadImage_WithNullOptions_ShouldUploadSuccessfully() {
        // Given
        Map<String, Object> options = null;
        String expectedUrl = "https://cloudinary.com/test/image.jpg";
        
        when(storageSettingService.getSystemStorageType()).thenReturn(StorageType.CLOUDINARY);
        when(storageServiceFactory.getStorageService(StorageType.CLOUDINARY)).thenReturn(cloudinaryStorageService);
        when(cloudinaryStorageService.uploadImage(multipartFile, options)).thenReturn(expectedUrl);

        // When
        String result = imageUploadService.uploadImage(multipartFile, options);

        // Then
        assertNotNull(result);
        assertEquals(expectedUrl, result);
        
        verify(storageSettingService).getSystemStorageType();
        verify(storageServiceFactory).getStorageService(StorageType.CLOUDINARY);
        verify(cloudinaryStorageService).uploadImage(multipartFile, options);
    }

    @Test
    void uploadImage_WithFirebaseAndOptions_ShouldUploadSuccessfully() {
        // Given
        Map<String, Object> options = new HashMap<>();
        options.put("quality", 90);
        options.put("format", "webp");
        
        String expectedUrl = "https://firebasestorage.googleapis.com/v0/b/bucket/o/image.jpg?alt=media";
        
        when(storageSettingService.getSystemStorageType()).thenReturn(StorageType.FIREBASE_STORAGE);
        when(storageServiceFactory.getStorageService(StorageType.FIREBASE_STORAGE)).thenReturn(firebaseStorageService);
        when(firebaseStorageService.uploadImage(multipartFile, options)).thenReturn(expectedUrl);

        // When
        String result = imageUploadService.uploadImage(multipartFile, options);

        // Then
        assertNotNull(result);
        assertEquals(expectedUrl, result);
        
        verify(storageSettingService).getSystemStorageType();
        verify(storageServiceFactory).getStorageService(StorageType.FIREBASE_STORAGE);
        verify(firebaseStorageService).uploadImage(multipartFile, options);
    }

    @Test
    void uploadImage_WithAwsS3AndOptions_ShouldUploadSuccessfully() {
        // Given
        Map<String, Object> options = new HashMap<>();
        options.put("acl", "public-read");
        options.put("metadata", "test");
        
        String expectedUrl = "https://bucket.s3.amazonaws.com/image.jpg";
        
        when(storageSettingService.getSystemStorageType()).thenReturn(StorageType.AWS_S3);
        when(storageServiceFactory.getStorageService(StorageType.AWS_S3)).thenReturn(awsS3StorageService);
        when(awsS3StorageService.uploadImage(multipartFile, options)).thenReturn(expectedUrl);

        // When
        String result = imageUploadService.uploadImage(multipartFile, options);

        // Then
        assertNotNull(result);
        assertEquals(expectedUrl, result);
        
        verify(storageSettingService).getSystemStorageType();
        verify(storageServiceFactory).getStorageService(StorageType.AWS_S3);
        verify(awsS3StorageService).uploadImage(multipartFile, options);
    }

    @Test
    void uploadImage_WithOptionsAndStorageException_ShouldThrowException() {
        // Given
        Map<String, Object> options = new HashMap<>();
        options.put("width", 800);
        
        when(storageSettingService.getSystemStorageType()).thenReturn(StorageType.CLOUDINARY);
        when(storageServiceFactory.getStorageService(StorageType.CLOUDINARY)).thenReturn(cloudinaryStorageService);
        when(cloudinaryStorageService.uploadImage(multipartFile, options))
                .thenThrow(new ImageUploadException("Upload failed"));

        // When & Then
        assertThrows(ImageUploadException.class,
                () -> imageUploadService.uploadImage(multipartFile, options));

        verify(storageSettingService).getSystemStorageType();
        verify(storageServiceFactory).getStorageService(StorageType.CLOUDINARY);
        verify(cloudinaryStorageService).uploadImage(multipartFile, options);
    }

    // Test deleteImage(String imageUrl)
    @Test
    void deleteImage_WithCloudinaryUrl_ShouldDeleteSuccessfully() {
        // Given
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/v1234567890/sample.jpg";
        
        when(storageServiceFactory.getStorageService(StorageType.CLOUDINARY)).thenReturn(cloudinaryStorageService);
        when(cloudinaryStorageService.deleteImage(imageUrl)).thenReturn(true);

        // When
        boolean result = imageUploadService.deleteImage(imageUrl);

        // Then
        assertTrue(result);
        
        verify(storageServiceFactory).getStorageService(StorageType.CLOUDINARY);
        verify(cloudinaryStorageService).deleteImage(imageUrl);
    }

    @Test
    void deleteImage_WithFirebaseUrl_ShouldDeleteSuccessfully() {
        // Given
        String imageUrl = "https://firebasestorage.googleapis.com/v0/b/bucket/o/image.jpg?alt=media";
        
        when(storageServiceFactory.getStorageService(StorageType.FIREBASE_STORAGE)).thenReturn(firebaseStorageService);
        when(firebaseStorageService.deleteImage(imageUrl)).thenReturn(true);

        // When
        boolean result = imageUploadService.deleteImage(imageUrl);

        // Then
        assertTrue(result);
        
        verify(storageServiceFactory).getStorageService(StorageType.FIREBASE_STORAGE);
        verify(firebaseStorageService).deleteImage(imageUrl);
    }

    @Test
    void deleteImage_WithAwsS3Url_ShouldDeleteSuccessfully() {
        // Given
        String imageUrl = "https://bucket.s3.amazonaws.com/image.jpg";
        
        when(storageServiceFactory.getStorageService(StorageType.AWS_S3)).thenReturn(awsS3StorageService);
        when(awsS3StorageService.deleteImage(imageUrl)).thenReturn(true);

        // When
        boolean result = imageUploadService.deleteImage(imageUrl);

        // Then
        assertTrue(result);
        
        verify(storageServiceFactory).getStorageService(StorageType.AWS_S3);
        verify(awsS3StorageService).deleteImage(imageUrl);
    }

    @Test
    void deleteImage_WithUnknownUrl_ShouldUseCloudinaryAsDefault() {
        // Given
        String imageUrl = "https://unknown-service.com/image.jpg";
        
        when(storageServiceFactory.getStorageService(StorageType.CLOUDINARY)).thenReturn(cloudinaryStorageService);
        when(cloudinaryStorageService.deleteImage(imageUrl)).thenReturn(true);

        // When
        boolean result = imageUploadService.deleteImage(imageUrl);

        // Then
        assertTrue(result);
        
        verify(storageServiceFactory).getStorageService(StorageType.CLOUDINARY);
        verify(cloudinaryStorageService).deleteImage(imageUrl);
    }

    @Test
    void deleteImage_WithNullUrl_ShouldUseSystemDefault() {
        // Given
        String imageUrl = null;
        
        when(storageSettingService.getSystemStorageType()).thenReturn(StorageType.FIREBASE_STORAGE);
        when(storageServiceFactory.getStorageService(StorageType.FIREBASE_STORAGE)).thenReturn(firebaseStorageService);
        when(firebaseStorageService.deleteImage(imageUrl)).thenReturn(false);

        // When
        boolean result = imageUploadService.deleteImage(imageUrl);

        // Then
        assertFalse(result);
        
        verify(storageSettingService).getSystemStorageType();
        verify(storageServiceFactory).getStorageService(StorageType.FIREBASE_STORAGE);
        verify(firebaseStorageService).deleteImage(imageUrl);
    }

    @Test
    void deleteImage_WithEmptyUrl_ShouldUseSystemDefault() {
        // Given
        String imageUrl = "";
        
        when(storageSettingService.getSystemStorageType()).thenReturn(StorageType.AWS_S3);
        when(storageServiceFactory.getStorageService(StorageType.AWS_S3)).thenReturn(awsS3StorageService);
        when(awsS3StorageService.deleteImage(imageUrl)).thenReturn(false);

        // When
        boolean result = imageUploadService.deleteImage(imageUrl);

        // Then
        assertFalse(result);
        
        verify(storageSettingService).getSystemStorageType();
        verify(storageServiceFactory).getStorageService(StorageType.AWS_S3);
        verify(awsS3StorageService).deleteImage(imageUrl);
    }

    @Test
    void deleteImage_WithDeleteFailure_ShouldReturnFalse() {
        // Given
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/v1234567890/sample.jpg";
        
        when(storageServiceFactory.getStorageService(StorageType.CLOUDINARY)).thenReturn(cloudinaryStorageService);
        when(cloudinaryStorageService.deleteImage(imageUrl)).thenReturn(false);

        // When
        boolean result = imageUploadService.deleteImage(imageUrl);

        // Then
        assertFalse(result);
        
        verify(storageServiceFactory).getStorageService(StorageType.CLOUDINARY);
        verify(cloudinaryStorageService).deleteImage(imageUrl);
    }

    @Test
    void deleteImage_WithStorageException_ShouldThrowException() {
        // Given
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/v1234567890/sample.jpg";
        
        when(storageServiceFactory.getStorageService(StorageType.CLOUDINARY)).thenReturn(cloudinaryStorageService);
        when(cloudinaryStorageService.deleteImage(imageUrl))
                .thenThrow(new RuntimeException("Delete failed"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> imageUploadService.deleteImage(imageUrl));

        verify(storageServiceFactory).getStorageService(StorageType.CLOUDINARY);
        verify(cloudinaryStorageService).deleteImage(imageUrl);
    }

    @Test
    void deleteImage_WithFactoryException_ShouldThrowException() {
        // Given
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/sample.jpg";
        
        when(storageServiceFactory.getStorageService(StorageType.CLOUDINARY))
                .thenThrow(new RuntimeException("Factory error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> imageUploadService.deleteImage(imageUrl));

        verify(storageServiceFactory).getStorageService(StorageType.CLOUDINARY);
    }

    // Test URL pattern detection edge cases
    @Test
    void deleteImage_WithCloudinaryUrlVariations_ShouldDetectCorrectly() {
        // Given
        String[] cloudinaryUrls = {
            "https://res.cloudinary.com/demo/image/upload/sample.jpg",
            "https://cloudinary.com/demo/image/upload/sample.jpg",
            "http://res.cloudinary.com/demo/image/upload/sample.jpg",
            "https://my-account.cloudinary.com/demo/image/upload/sample.jpg"
        };
        
        when(storageServiceFactory.getStorageService(StorageType.CLOUDINARY)).thenReturn(cloudinaryStorageService);
        when(cloudinaryStorageService.deleteImage(anyString())).thenReturn(true);

        // When & Then
        for (String url : cloudinaryUrls) {
            boolean result = imageUploadService.deleteImage(url);
            assertTrue(result, "Should detect Cloudinary URL: " + url);
        }
        
        verify(storageServiceFactory, times(cloudinaryUrls.length)).getStorageService(StorageType.CLOUDINARY);
        verify(cloudinaryStorageService, times(cloudinaryUrls.length)).deleteImage(anyString());
    }

    @Test
    void deleteImage_WithFirebaseUrlVariations_ShouldDetectCorrectly() {
        // Given
        String[] firebaseUrls = {
            "https://firebasestorage.googleapis.com/v0/b/bucket/o/image.jpg?alt=media",
            "https://firebasestorage.googleapis.com/v0/b/bucket/o/folder%2Fimage.jpg?alt=media&token=abc123",
            "https://firebasestorage.googleapis.com/v0/b/bucket/o/image.jpg",
            "http://firebasestorage.googleapis.com/v0/b/bucket/o/image.jpg?alt=media"
        };
        
        when(storageServiceFactory.getStorageService(StorageType.FIREBASE_STORAGE)).thenReturn(firebaseStorageService);
        when(firebaseStorageService.deleteImage(anyString())).thenReturn(true);

        // When & Then
        for (String url : firebaseUrls) {
            boolean result = imageUploadService.deleteImage(url);
            assertTrue(result, "Should detect Firebase URL: " + url);
        }
        
        verify(storageServiceFactory, times(firebaseUrls.length)).getStorageService(StorageType.FIREBASE_STORAGE);
        verify(firebaseStorageService, times(firebaseUrls.length)).deleteImage(anyString());
    }

    @Test
    void deleteImage_WithAwsS3UrlVariations_ShouldDetectCorrectly() {
        // Given
        String[] awsUrls = {
            "https://bucket.s3.amazonaws.com/image.jpg",
            "https://bucket.s3.us-east-1.amazonaws.com/image.jpg",
            "https://my-bucket.s3.amazonaws.com/folder/image.jpg",
            "http://bucket.s3.amazonaws.com/image.jpg"
        };
        
        when(storageServiceFactory.getStorageService(StorageType.AWS_S3)).thenReturn(awsS3StorageService);
        when(awsS3StorageService.deleteImage(anyString())).thenReturn(true);

        // When & Then
        for (String url : awsUrls) {
            boolean result = imageUploadService.deleteImage(url);
            assertTrue(result, "Should detect AWS S3 URL: " + url);
        }
        
        verify(storageServiceFactory, times(awsUrls.length)).getStorageService(StorageType.AWS_S3);
        verify(awsS3StorageService, times(awsUrls.length)).deleteImage(anyString());
    }

    @Test
    void deleteImage_WithUrlContainingMultipleProviders_ShouldUseFirstMatch() {
        // Given - URL contains both cloudinary and firebase keywords
        String imageUrl = "https://cloudinary.com/test/firebasestorage.googleapis.com/image.jpg";
        
        when(storageServiceFactory.getStorageService(StorageType.CLOUDINARY)).thenReturn(cloudinaryStorageService);
        when(cloudinaryStorageService.deleteImage(imageUrl)).thenReturn(true);

        // When
        boolean result = imageUploadService.deleteImage(imageUrl);

        // Then
        assertTrue(result);
        
        // Should use Cloudinary since it's checked first
        verify(storageServiceFactory).getStorageService(StorageType.CLOUDINARY);
        verify(cloudinaryStorageService).deleteImage(imageUrl);
    }

    // Test integration scenarios
    @Test
    void uploadAndDeleteImage_WithSameStorageType_ShouldWorkCorrectly() {
        // Given
        Map<String, Object> options = new HashMap<>();
        options.put("width", 800);
        
        String uploadedUrl = "https://res.cloudinary.com/demo/image/upload/v1234567890/sample.jpg";
        
        when(storageSettingService.getSystemStorageType()).thenReturn(StorageType.CLOUDINARY);
        when(storageServiceFactory.getStorageService(StorageType.CLOUDINARY)).thenReturn(cloudinaryStorageService);
        when(cloudinaryStorageService.uploadImage(multipartFile, options)).thenReturn(uploadedUrl);
        when(cloudinaryStorageService.deleteImage(uploadedUrl)).thenReturn(true);

        // When
        String uploadResult = imageUploadService.uploadImage(multipartFile, options);
        boolean deleteResult = imageUploadService.deleteImage(uploadResult);

        // Then
        assertEquals(uploadedUrl, uploadResult);
        assertTrue(deleteResult);
        
        verify(storageSettingService).getSystemStorageType();
        verify(storageServiceFactory, times(2)).getStorageService(StorageType.CLOUDINARY);
        verify(cloudinaryStorageService).uploadImage(multipartFile, options);
        verify(cloudinaryStorageService).deleteImage(uploadedUrl);
    }

    @Test
    void uploadAndDeleteImage_WithDifferentStorageTypes_ShouldWorkCorrectly() {
        // Given
        String uploadedUrl = "https://firebasestorage.googleapis.com/v0/b/bucket/o/image.jpg?alt=media";
        
        // Upload with Firebase (system default)
        when(storageSettingService.getSystemStorageType()).thenReturn(StorageType.FIREBASE_STORAGE);
        when(storageServiceFactory.getStorageService(StorageType.FIREBASE_STORAGE)).thenReturn(firebaseStorageService);
        when(firebaseStorageService.uploadImage(multipartFile, null)).thenReturn(uploadedUrl);
        
        // Delete will also use Firebase (detected from URL)
        when(firebaseStorageService.deleteImage(uploadedUrl)).thenReturn(true);

        // When
        String uploadResult = imageUploadService.uploadImage(multipartFile);
        boolean deleteResult = imageUploadService.deleteImage(uploadResult);

        // Then
        assertEquals(uploadedUrl, uploadResult);
        assertTrue(deleteResult);
        
        verify(storageSettingService).getSystemStorageType();
        verify(storageServiceFactory, times(2)).getStorageService(StorageType.FIREBASE_STORAGE);
        verify(firebaseStorageService).uploadImage(multipartFile, null);
        verify(firebaseStorageService).deleteImage(uploadedUrl);
    }
}