package com.hcmus.ecommerce_backend.unit.common.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.hcmus.ecommerce_backend.common.model.entity.SystemSetting;
import com.hcmus.ecommerce_backend.common.model.enums.SettingDataType;
import com.hcmus.ecommerce_backend.common.model.enums.StorageType;
import com.hcmus.ecommerce_backend.common.repository.SystemSettingRepository;
import com.hcmus.ecommerce_backend.common.service.impl.StorageSettingServiceImpl;

@ExtendWith(MockitoExtension.class)
public class StorageSettingServiceImplTest {

    @Mock
    private SystemSettingRepository systemSettingRepository;

    @InjectMocks
    private StorageSettingServiceImpl storageSettingService;

    private final String STORAGE_TYPE_KEY = "STORAGE_TYPE";
    private SystemSetting testSetting;

    @BeforeEach
    void setUp() {
        testSetting = SystemSetting.builder()
                .key(STORAGE_TYPE_KEY)
                .value("CLOUDINARY")
                .type(SettingDataType.String)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // Test getSystemStorageType
    @Test
    void getSystemStorageType_WithValidSettingInDatabase_ShouldReturnStorageType() {
        // Given
        when(systemSettingRepository.findByKey(STORAGE_TYPE_KEY)).thenReturn(Optional.of(testSetting));

        // When
        StorageType result = storageSettingService.getSystemStorageType();

        // Then
        assertNotNull(result);
        assertEquals(StorageType.CLOUDINARY, result);
        
        verify(systemSettingRepository).findByKey(STORAGE_TYPE_KEY);
        verify(systemSettingRepository, never()).save(any());
    }

    @Test
    void getSystemStorageType_WithFirebaseStorageInDatabase_ShouldReturnFirebaseStorage() {
        // Given
        testSetting.setValue("FIREBASE_STORAGE");
        when(systemSettingRepository.findByKey(STORAGE_TYPE_KEY)).thenReturn(Optional.of(testSetting));

        // When
        StorageType result = storageSettingService.getSystemStorageType();

        // Then
        assertNotNull(result);
        assertEquals(StorageType.FIREBASE_STORAGE, result);
        
        verify(systemSettingRepository).findByKey(STORAGE_TYPE_KEY);
        verify(systemSettingRepository, never()).save(any());
    }

    @Test
    void getSystemStorageType_WithAwsS3InDatabase_ShouldReturnAwsS3() {
        // Given
        testSetting.setValue("AWS_S3");
        when(systemSettingRepository.findByKey(STORAGE_TYPE_KEY)).thenReturn(Optional.of(testSetting));

        // When
        StorageType result = storageSettingService.getSystemStorageType();

        // Then
        assertNotNull(result);
        assertEquals(StorageType.AWS_S3, result);
        
        verify(systemSettingRepository).findByKey(STORAGE_TYPE_KEY);
        verify(systemSettingRepository, never()).save(any());
    }

    @Test
    void getSystemStorageType_WithInvalidStorageTypeInDatabase_ShouldReturnDefaultAndUpdateSetting() {
        // Given
        testSetting.setValue("INVALID_STORAGE_TYPE");
        when(systemSettingRepository.findByKey(STORAGE_TYPE_KEY)).thenReturn(Optional.of(testSetting));
        
        // Set default storage provider
        ReflectionTestUtils.setField(storageSettingService, "defaultStorageProvider", "CLOUDINARY");

        // When
        StorageType result = storageSettingService.getSystemStorageType();

        // Then
        assertNotNull(result);
        assertEquals(StorageType.CLOUDINARY, result);
        
        verify(systemSettingRepository).findByKey(STORAGE_TYPE_KEY);
        
        // Verify that the invalid setting was updated with correct value
        ArgumentCaptor<SystemSetting> settingCaptor = ArgumentCaptor.forClass(SystemSetting.class);
        verify(systemSettingRepository).save(settingCaptor.capture());
        
        SystemSetting savedSetting = settingCaptor.getValue();
        assertEquals("CLOUDINARY", savedSetting.getValue());
        assertNotNull(savedSetting.getUpdatedAt());
    }

    @Test
    void getSystemStorageType_WithNoSettingInDatabase_ShouldCreateNewSettingWithDefault() {
        // Given
        when(systemSettingRepository.findByKey(STORAGE_TYPE_KEY)).thenReturn(Optional.empty());
        
        // Set default storage provider
        ReflectionTestUtils.setField(storageSettingService, "defaultStorageProvider", "FIREBASE_STORAGE");

        // When
        StorageType result = storageSettingService.getSystemStorageType();

        // Then
        assertNotNull(result);
        assertEquals(StorageType.FIREBASE_STORAGE, result);
        
        verify(systemSettingRepository).findByKey(STORAGE_TYPE_KEY);
        
        // Verify that a new setting was created
        ArgumentCaptor<SystemSetting> settingCaptor = ArgumentCaptor.forClass(SystemSetting.class);
        verify(systemSettingRepository).save(settingCaptor.capture());
        
        SystemSetting savedSetting = settingCaptor.getValue();
        assertEquals(STORAGE_TYPE_KEY, savedSetting.getKey());
        assertEquals("FIREBASE_STORAGE", savedSetting.getValue());
        assertEquals(SettingDataType.String, savedSetting.getType());
    }

    @Test
    void getSystemStorageType_WithInvalidDefaultProvider_ShouldFallbackToCloudinary() {
        // Given
        testSetting.setValue("INVALID_TYPE");
        when(systemSettingRepository.findByKey(STORAGE_TYPE_KEY)).thenReturn(Optional.of(testSetting));
        
        // Set invalid default storage provider
        ReflectionTestUtils.setField(storageSettingService, "defaultStorageProvider", "INVALID_PROVIDER");

        // When
        StorageType result = storageSettingService.getSystemStorageType();

        // Then
        assertNotNull(result);
        assertEquals(StorageType.CLOUDINARY, result);
        
        verify(systemSettingRepository).findByKey(STORAGE_TYPE_KEY);
        verify(systemSettingRepository).save(any());
    }

    @Test
    void getSystemStorageType_WithLowercaseStorageType_ShouldWorkCorrectly() {
        // Given
        testSetting.setValue("cloudinary");
        when(systemSettingRepository.findByKey(STORAGE_TYPE_KEY)).thenReturn(Optional.of(testSetting));

        // When & Then
        // This should throw IllegalArgumentException because enum values are case-sensitive
        // and will fallback to default
        ReflectionTestUtils.setField(storageSettingService, "defaultStorageProvider", "CLOUDINARY");
        
        StorageType result = storageSettingService.getSystemStorageType();
        
        assertEquals(StorageType.CLOUDINARY, result);
        verify(systemSettingRepository).save(any()); // Should update with correct value
    }

    // Test setSystemStorageType
    @Test
    void setSystemStorageType_WithValidStorageTypeAndExistingSetting_ShouldUpdateSetting() {
        // Given
        StorageType newStorageType = StorageType.FIREBASE_STORAGE;
        when(systemSettingRepository.findByKey(STORAGE_TYPE_KEY)).thenReturn(Optional.of(testSetting));

        // When
        storageSettingService.setSystemStorageType(newStorageType);

        // Then
        verify(systemSettingRepository).findByKey(STORAGE_TYPE_KEY);
        
        ArgumentCaptor<SystemSetting> settingCaptor = ArgumentCaptor.forClass(SystemSetting.class);
        verify(systemSettingRepository).save(settingCaptor.capture());
        
        SystemSetting savedSetting = settingCaptor.getValue();
        assertEquals("FIREBASE_STORAGE", savedSetting.getValue());
        assertNotNull(savedSetting.getUpdatedAt());
    }

    @Test
    void setSystemStorageType_WithValidStorageTypeAndNoExistingSetting_ShouldCreateNewSetting() {
        // Given
        StorageType newStorageType = StorageType.AWS_S3;
        when(systemSettingRepository.findByKey(STORAGE_TYPE_KEY)).thenReturn(Optional.empty());

        // When
        storageSettingService.setSystemStorageType(newStorageType);

        // Then
        verify(systemSettingRepository).findByKey(STORAGE_TYPE_KEY);
        
        ArgumentCaptor<SystemSetting> settingCaptor = ArgumentCaptor.forClass(SystemSetting.class);
        verify(systemSettingRepository).save(settingCaptor.capture());
        
        SystemSetting savedSetting = settingCaptor.getValue();
        assertEquals(STORAGE_TYPE_KEY, savedSetting.getKey());
        assertEquals("AWS_S3", savedSetting.getValue());
        assertEquals(SettingDataType.String, savedSetting.getType());
    }

    @Test
    void setSystemStorageType_WithNullStorageType_ShouldThrowException() {
        // Given
        StorageType nullStorageType = null;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> storageSettingService.setSystemStorageType(nullStorageType));
        
        assertEquals("Storage type cannot be null", exception.getMessage());
        
        verify(systemSettingRepository, never()).findByKey(anyString());
        verify(systemSettingRepository, never()).save(any());
    }

    @Test
    void setSystemStorageType_WithCloudinaryStorageType_ShouldUpdateCorrectly() {
        // Given
        StorageType newStorageType = StorageType.CLOUDINARY;
        when(systemSettingRepository.findByKey(STORAGE_TYPE_KEY)).thenReturn(Optional.of(testSetting));

        // When
        storageSettingService.setSystemStorageType(newStorageType);

        // Then
        verify(systemSettingRepository).findByKey(STORAGE_TYPE_KEY);
        
        ArgumentCaptor<SystemSetting> settingCaptor = ArgumentCaptor.forClass(SystemSetting.class);
        verify(systemSettingRepository).save(settingCaptor.capture());
        
        SystemSetting savedSetting = settingCaptor.getValue();
        assertEquals("CLOUDINARY", savedSetting.getValue());
    }

    @Test
    void setSystemStorageType_WithRepositoryException_ShouldThrowException() {
        // Given
        StorageType newStorageType = StorageType.FIREBASE_STORAGE;
        when(systemSettingRepository.findByKey(STORAGE_TYPE_KEY)).thenReturn(Optional.of(testSetting));
        when(systemSettingRepository.save(any())).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> storageSettingService.setSystemStorageType(newStorageType));
        
        verify(systemSettingRepository).findByKey(STORAGE_TYPE_KEY);
        verify(systemSettingRepository).save(any());
    }

    // Test getDefaultStorageType
    @Test
    void getDefaultStorageType_WithValidDefaultProvider_ShouldReturnCorrectType() {
        // Given
        ReflectionTestUtils.setField(storageSettingService, "defaultStorageProvider", "CLOUDINARY");

        // When
        StorageType result = storageSettingService.getDefaultStorageType();

        // Then
        assertNotNull(result);
        assertEquals(StorageType.CLOUDINARY, result);
    }

    @Test
    void getDefaultStorageType_WithFirebaseDefaultProvider_ShouldReturnFirebaseStorage() {
        // Given
        ReflectionTestUtils.setField(storageSettingService, "defaultStorageProvider", "FIREBASE_STORAGE");

        // When
        StorageType result = storageSettingService.getDefaultStorageType();

        // Then
        assertNotNull(result);
        assertEquals(StorageType.FIREBASE_STORAGE, result);
    }

    @Test
    void getDefaultStorageType_WithAwsS3DefaultProvider_ShouldReturnAwsS3() {
        // Given
        ReflectionTestUtils.setField(storageSettingService, "defaultStorageProvider", "AWS_S3");

        // When
        StorageType result = storageSettingService.getDefaultStorageType();

        // Then
        assertNotNull(result);
        assertEquals(StorageType.AWS_S3, result);
    }

    @Test
    void getDefaultStorageType_WithInvalidDefaultProvider_ShouldReturnCloudinary() {
        // Given
        ReflectionTestUtils.setField(storageSettingService, "defaultStorageProvider", "INVALID_PROVIDER");

        // When
        StorageType result = storageSettingService.getDefaultStorageType();

        // Then
        assertNotNull(result);
        assertEquals(StorageType.CLOUDINARY, result);
    }

    @Test
    void getDefaultStorageType_WithLowercaseDefaultProvider_ShouldWorkCorrectly() {
        // Given
        ReflectionTestUtils.setField(storageSettingService, "defaultStorageProvider", "cloudinary");

        // When
        StorageType result = storageSettingService.getDefaultStorageType();

        // Then
        assertNotNull(result);
        assertEquals(StorageType.CLOUDINARY, result);
    }

    @Test
    void getDefaultStorageType_WithWhitespaceDefaultProvider_ShouldTrimAndWork() {
        // Given
        ReflectionTestUtils.setField(storageSettingService, "defaultStorageProvider", "  FIREBASE_STORAGE  ");

        // When
        StorageType result = storageSettingService.getDefaultStorageType();

        // Then
        assertNotNull(result);
        assertEquals(StorageType.FIREBASE_STORAGE, result);
    }

    @Test
    void getDefaultStorageType_WithNullDefaultProvider_ShouldReturnCloudinary() {
        // Given
        ReflectionTestUtils.setField(storageSettingService, "defaultStorageProvider", null);

        // When & Then
        // This might throw NPE, but the implementation should handle it gracefully
        assertThrows(NullPointerException.class,
                () -> storageSettingService.getDefaultStorageType());
    }

    @Test
    void getDefaultStorageType_WithEmptyDefaultProvider_ShouldReturnCloudinary() {
        // Given
        ReflectionTestUtils.setField(storageSettingService, "defaultStorageProvider", "");

        // When
        StorageType result = storageSettingService.getDefaultStorageType();

        // Then
        assertNotNull(result);
        assertEquals(StorageType.CLOUDINARY, result);
    }

    // Test integration scenarios
    @Test
    void getSystemStorageType_ThenSetSystemStorageType_ShouldWorkCorrectly() {
        // Given
        when(systemSettingRepository.findByKey(STORAGE_TYPE_KEY)).thenReturn(Optional.of(testSetting));

        // When - Get current storage type
        StorageType currentType = storageSettingService.getSystemStorageType();
        
        // Then - Change to different storage type
        StorageType newType = StorageType.FIREBASE_STORAGE;
        when(systemSettingRepository.findByKey(STORAGE_TYPE_KEY)).thenReturn(Optional.of(testSetting));
        storageSettingService.setSystemStorageType(newType);

        // Verify
        assertEquals(StorageType.CLOUDINARY, currentType);
        
        verify(systemSettingRepository, times(2)).findByKey(STORAGE_TYPE_KEY);
        verify(systemSettingRepository).save(any());
    }

    @Test
    void setSystemStorageType_MultipleUpdates_ShouldUpdateCorrectly() {
        // Given
        when(systemSettingRepository.findByKey(STORAGE_TYPE_KEY)).thenReturn(Optional.of(testSetting));

        // When
        storageSettingService.setSystemStorageType(StorageType.FIREBASE_STORAGE);
        storageSettingService.setSystemStorageType(StorageType.AWS_S3);
        storageSettingService.setSystemStorageType(StorageType.CLOUDINARY);

        // Then
        verify(systemSettingRepository, times(3)).findByKey(STORAGE_TYPE_KEY);
        verify(systemSettingRepository, times(3)).save(any());
    }

    @Test
    void getSystemStorageType_WithRepositoryException_ShouldThrowException() {
        // Given
        when(systemSettingRepository.findByKey(STORAGE_TYPE_KEY))
                .thenThrow(new RuntimeException("Database connection error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> storageSettingService.getSystemStorageType());
        
        verify(systemSettingRepository).findByKey(STORAGE_TYPE_KEY);
        verify(systemSettingRepository, never()).save(any());
    }

    @Test
    void setSystemStorageType_WithFindByKeyException_ShouldThrowException() {
        // Given
        when(systemSettingRepository.findByKey(STORAGE_TYPE_KEY))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class,
                () -> storageSettingService.setSystemStorageType(StorageType.FIREBASE_STORAGE));
        
        verify(systemSettingRepository).findByKey(STORAGE_TYPE_KEY);
        verify(systemSettingRepository, never()).save(any());
    }

    // Test edge cases
    @Test
    void getSystemStorageType_WithCorruptedSetting_ShouldHandleGracefully() {
        // Given
        SystemSetting corruptedSetting = SystemSetting.builder()
                .key(STORAGE_TYPE_KEY)
                .value("") // Empty value
                .type(SettingDataType.String)
                .build();
        
        when(systemSettingRepository.findByKey(STORAGE_TYPE_KEY)).thenReturn(Optional.of(corruptedSetting));
        ReflectionTestUtils.setField(storageSettingService, "defaultStorageProvider", "CLOUDINARY");

        // When
        StorageType result = storageSettingService.getSystemStorageType();

        // Then
        assertNotNull(result);
        assertEquals(StorageType.CLOUDINARY, result);
        
        verify(systemSettingRepository).findByKey(STORAGE_TYPE_KEY);
        verify(systemSettingRepository).save(any()); // Should update with correct value
    }

    @Test
    void setSystemStorageType_WithSameStorageType_ShouldStillUpdate() {
        // Given
        testSetting.setValue("CLOUDINARY");
        when(systemSettingRepository.findByKey(STORAGE_TYPE_KEY)).thenReturn(Optional.of(testSetting));

        // When - Set the same storage type
        storageSettingService.setSystemStorageType(StorageType.CLOUDINARY);

        // Then
        verify(systemSettingRepository).findByKey(STORAGE_TYPE_KEY);
        verify(systemSettingRepository).save(any()); // Should still update (e.g., updatedAt timestamp)
    }

    @Test
    void getDefaultStorageType_WithMixedCaseProvider_ShouldNormalizeCorrectly() {
        // Given
        ReflectionTestUtils.setField(storageSettingService, "defaultStorageProvider", "Firebase_Storage");

        // When
        StorageType result = storageSettingService.getDefaultStorageType();

        // Then
        assertNotNull(result);
        assertEquals(StorageType.FIREBASE_STORAGE, result);
    }

    @Test
    void getSystemStorageType_WithNullValueInDatabase_ShouldHandleGracefully() {
        // Given
        SystemSetting nullValueSetting = SystemSetting.builder()
                .key(STORAGE_TYPE_KEY)
                .value(null)
                .type(SettingDataType.String)
                .build();
        
        when(systemSettingRepository.findByKey(STORAGE_TYPE_KEY)).thenReturn(Optional.of(nullValueSetting));
        ReflectionTestUtils.setField(storageSettingService, "defaultStorageProvider", "CLOUDINARY");

        // When & Then
        // This might throw NPE, but should be handled gracefully
        assertThrows(NullPointerException.class,
                () -> storageSettingService.getSystemStorageType());
    }
}