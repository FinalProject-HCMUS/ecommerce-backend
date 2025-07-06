package com.hcmus.ecommerce_backend.unit.common.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import com.hcmus.ecommerce_backend.common.exception.ImageUploadException;
import com.hcmus.ecommerce_backend.common.model.entity.SystemSetting;
import com.hcmus.ecommerce_backend.common.model.enums.CloudinaryKeys;
import com.hcmus.ecommerce_backend.common.repository.SystemSettingRepository;
import com.hcmus.ecommerce_backend.common.service.impl.CloudinaryImageStorageService;

@ExtendWith(MockitoExtension.class)
public class CloudinaryImageStorageServiceTest {

    @Mock
    private SystemSettingRepository systemSettingRepository;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryImageStorageService cloudinaryImageStorageService;

    private SystemSetting cloudNameSetting;
    private SystemSetting apiKeySetting;
    private SystemSetting apiSecretSetting;
    private SystemSetting folderSetting;

    @BeforeEach
    void setUp() {
        // Setup SystemSetting mocks
        cloudNameSetting = new SystemSetting();
        cloudNameSetting.setKey(CloudinaryKeys.CLOUNDINARY_CLOUD_NAME.name());
        cloudNameSetting.setValue("test-cloud");

        apiKeySetting = new SystemSetting();
        apiKeySetting.setKey(CloudinaryKeys.CLOUNDINARY_API_KEY.name());
        apiKeySetting.setValue("test-api-key");

        apiSecretSetting = new SystemSetting();
        apiSecretSetting.setKey(CloudinaryKeys.CLOUNDINARY_API_SECRET.name());
        apiSecretSetting.setValue("test-api-secret");

        folderSetting = new SystemSetting();
        folderSetting.setKey(CloudinaryKeys.CLOUNDINARY_FOLDER.name());
        folderSetting.setValue("test-folder");

        // Reset private fields to null to ensure fresh initialization in each test
        ReflectionTestUtils.setField(cloudinaryImageStorageService, "cloudName", null);
        ReflectionTestUtils.setField(cloudinaryImageStorageService, "apiKey", null);
        ReflectionTestUtils.setField(cloudinaryImageStorageService, "apiSecret", null);
        ReflectionTestUtils.setField(cloudinaryImageStorageService, "folder", null);
    }

    @Test
    void uploadImage_WithoutOptions_Success() throws IOException {
        // Given
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_CLOUD_NAME.name()))
        //         .thenReturn(Optional.of(cloudNameSetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_API_KEY.name()))
        //         .thenReturn(Optional.of(apiKeySetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_API_SECRET.name()))
        //         .thenReturn(Optional.of(apiSecretSetting));


        byte[] fileBytes = "test image content".getBytes();
        when(multipartFile.getBytes()).thenReturn(fileBytes);

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://res.cloudinary.com/test-cloud/image/upload/test-folder/test-image.jpg");

        try (MockedStatic<ObjectUtils> objectUtilsMock = mockStatic(ObjectUtils.class)) {
            objectUtilsMock.when(() -> ObjectUtils.asMap(anyString(), any(), anyString(), any(), anyString(), any()))
                    .thenReturn(Map.of("cloud_name", "test-cloud", "api_key", "test-api-key", "api_secret",
                            "test-api-secret"));

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(eq(fileBytes), any(Map.class))).thenReturn(uploadResult);

            objectUtilsMock.when(ObjectUtils::emptyMap).thenReturn(Map.of());

            // Use reflection to inject mocked Cloudinary instance
            CloudinaryImageStorageService spyService = spy(cloudinaryImageStorageService);
            doReturn(cloudinary).when(spyService).getCloudinaryInstance();

            // When
            String result = spyService.uploadImage(multipartFile);

            // Then
            assertEquals("https://res.cloudinary.com/test-cloud/image/upload/test-folder/test-image.jpg", result);
            // verify(uploader).upload(eq(fileBytes), argThat(params -> {
            //     Map<String, Object> paramMap = (Map<String, Object>) params;
            //     return "test-folder".equals(paramMap.get("folder")) &&
            //             "auto".equals(paramMap.get("resource_type"));
            // }));
        }
    }

    @Test
    void uploadImage_WithOptions_Success() throws IOException {
        // Given
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_CLOUD_NAME.name()))
        //         .thenReturn(Optional.of(cloudNameSetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_API_KEY.name()))
        //         .thenReturn(Optional.of(apiKeySetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_API_SECRET.name()))
        //         .thenReturn(Optional.of(apiSecretSetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_FOLDER.name()))
        //         .thenReturn(Optional.of(folderSetting));

        byte[] fileBytes = "test image content".getBytes();
        when(multipartFile.getBytes()).thenReturn(fileBytes);

        Map<String, Object> options = new HashMap<>();
        options.put("width", 800);
        options.put("height", 600);
        options.put("crop", "fill");

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url",
                "https://res.cloudinary.com/test-cloud/image/upload/c_fill,h_600,w_800/test-folder/test-image.jpg");

        try (MockedStatic<ObjectUtils> objectUtilsMock = mockStatic(ObjectUtils.class)) {
            objectUtilsMock.when(() -> ObjectUtils.asMap(anyString(), any(), anyString(), any(), anyString(), any()))
                    .thenReturn(Map.of("cloud_name", "test-cloud", "api_key", "test-api-key", "api_secret",
                            "test-api-secret"));

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(eq(fileBytes), any(Map.class))).thenReturn(uploadResult);

            CloudinaryImageStorageService spyService = spy(cloudinaryImageStorageService);
            doReturn(cloudinary).when(spyService).getCloudinaryInstance();

            // When
            String result = spyService.uploadImage(multipartFile, options);

            // Then
            assertEquals(
                    "https://res.cloudinary.com/test-cloud/image/upload/c_fill,h_600,w_800/test-folder/test-image.jpg",
                    result);
            // verify(uploader).upload(eq(fileBytes), argThat(params -> {
            //     Map<String, Object> paramMap = (Map<String, Object>) params;
            //     return "test-folder".equals(paramMap.get("folder")) &&
            //             "auto".equals(paramMap.get("resource_type")) &&
            //             Integer.valueOf(800).equals(paramMap.get("width")) &&
            //             Integer.valueOf(600).equals(paramMap.get("height")) &&
            //             "fill".equals(paramMap.get("crop"));
            // }));
        }
    }

    @Test
    void uploadImage_WithNullOptions_Success() throws IOException {
        // Given
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_CLOUD_NAME.name()))
        //         .thenReturn(Optional.of(cloudNameSetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_API_KEY.name()))
        //         .thenReturn(Optional.of(apiKeySetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_API_SECRET.name()))
        //         .thenReturn(Optional.of(apiSecretSetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_FOLDER.name()))
        //         .thenReturn(Optional.of(folderSetting));

        byte[] fileBytes = "test image content".getBytes();
        when(multipartFile.getBytes()).thenReturn(fileBytes);

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://res.cloudinary.com/test-cloud/image/upload/test-folder/test-image.jpg");

        try (MockedStatic<ObjectUtils> objectUtilsMock = mockStatic(ObjectUtils.class)) {
            objectUtilsMock.when(() -> ObjectUtils.asMap(anyString(), any(), anyString(), any(), anyString(), any()))
                    .thenReturn(Map.of("cloud_name", "test-cloud", "api_key", "test-api-key", "api_secret",
                            "test-api-secret"));

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(eq(fileBytes), any(Map.class))).thenReturn(uploadResult);

            CloudinaryImageStorageService spyService = spy(cloudinaryImageStorageService);
            doReturn(cloudinary).when(spyService).getCloudinaryInstance();

            // When
            String result = spyService.uploadImage(multipartFile, null);

            // Then
            assertEquals("https://res.cloudinary.com/test-cloud/image/upload/test-folder/test-image.jpg", result);
            // verify(uploader).upload(eq(fileBytes), argThat(params -> {
            //     Map<String, Object> paramMap = (Map<String, Object>) params;
            //     return "test-folder".equals(paramMap.get("folder")) &&
            //             "auto".equals(paramMap.get("resource_type")) &&
            //             paramMap.size() == 2; // Only folder and resource_type
            // }));
        }
    }

    @Test
    void uploadImage_IOExceptionThrown_ThrowsImageUploadException() throws IOException {
        // Given
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_CLOUD_NAME.name()))
        //         .thenReturn(Optional.of(cloudNameSetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_API_KEY.name()))
        //         .thenReturn(Optional.of(apiKeySetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_API_SECRET.name()))
        //         .thenReturn(Optional.of(apiSecretSetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_FOLDER.name()))
        //         .thenReturn(Optional.of(folderSetting));

        when(multipartFile.getBytes()).thenThrow(new IOException("File read error"));

        CloudinaryImageStorageService spyService = spy(cloudinaryImageStorageService);
        doReturn(cloudinary).when(spyService).getCloudinaryInstance();

        // When & Then
        assertThrows(ImageUploadException.class, () -> spyService.uploadImage(multipartFile));
    }

    @Test
    void uploadImage_CloudinaryUploadFails_ThrowsImageUploadException() throws IOException {
        // Given
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_CLOUD_NAME.name()))
        //         .thenReturn(Optional.of(cloudNameSetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_API_KEY.name()))
        //         .thenReturn(Optional.of(apiKeySetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_API_SECRET.name()))
        //         .thenReturn(Optional.of(apiSecretSetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_FOLDER.name()))
        //         .thenReturn(Optional.of(folderSetting));

        byte[] fileBytes = "test image content".getBytes();
        when(multipartFile.getBytes()).thenReturn(fileBytes);

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class))).thenThrow(new IOException("Cloudinary upload failed"));

        CloudinaryImageStorageService spyService = spy(cloudinaryImageStorageService);
        doReturn(cloudinary).when(spyService).getCloudinaryInstance();

        // When & Then
        assertThrows(ImageUploadException.class, () -> spyService.uploadImage(multipartFile));
    }

    @Test
    void deleteImage_Success() throws IOException {
        // Given
        String imageUrl = "https://res.cloudinary.com/test-cloud/image/upload/v1746266513/test-folder/test-image.jpg";

        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_CLOUD_NAME.name()))
        //         .thenReturn(Optional.of(cloudNameSetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_API_KEY.name()))
        //         .thenReturn(Optional.of(apiKeySetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_API_SECRET.name()))
        //         .thenReturn(Optional.of(apiSecretSetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_FOLDER.name()))
        //         .thenReturn(Optional.of(folderSetting));

        Map<String, Object> deleteResult = new HashMap<>();
        deleteResult.put("result", "ok");

        try (MockedStatic<ObjectUtils> objectUtilsMock = mockStatic(ObjectUtils.class)) {
            objectUtilsMock.when(() -> ObjectUtils.asMap(anyString(), any(), anyString(), any(), anyString(), any()))
                    .thenReturn(Map.of("cloud_name", "test-cloud", "api_key", "test-api-key", "api_secret",
                            "test-api-secret"));
            objectUtilsMock.when(ObjectUtils::emptyMap).thenReturn(Map.of());

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.destroy(eq("test-folder/test-image"), any(Map.class))).thenReturn(deleteResult);

            CloudinaryImageStorageService spyService = spy(cloudinaryImageStorageService);
            doReturn(cloudinary).when(spyService).getCloudinaryInstance();

            // When
            boolean result = spyService.deleteImage(imageUrl);

            // Then
            assertTrue(result);
            verify(uploader).destroy(eq("test-folder/test-image"), any(Map.class));
        }
    }

    @Test
    void deleteImage_DeleteFails_ReturnsFalse() throws IOException {
        // Given
        String imageUrl = "https://res.cloudinary.com/test-cloud/image/upload/test-folder/test-image.jpg";

        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_CLOUD_NAME.name()))
        //         .thenReturn(Optional.of(cloudNameSetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_API_KEY.name()))
        //         .thenReturn(Optional.of(apiKeySetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_API_SECRET.name()))
        //         .thenReturn(Optional.of(apiSecretSetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_FOLDER.name()))
        //         .thenReturn(Optional.of(folderSetting));

        Map<String, Object> deleteResult = new HashMap<>();
        deleteResult.put("result", "not found");

        try (MockedStatic<ObjectUtils> objectUtilsMock = mockStatic(ObjectUtils.class)) {
            objectUtilsMock.when(() -> ObjectUtils.asMap(anyString(), any(), anyString(), any(), anyString(), any()))
                    .thenReturn(Map.of("cloud_name", "test-cloud", "api_key", "test-api-key", "api_secret",
                            "test-api-secret"));
            objectUtilsMock.when(ObjectUtils::emptyMap).thenReturn(Map.of());

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.destroy(eq("test-folder/test-image"), any(Map.class))).thenReturn(deleteResult);

            CloudinaryImageStorageService spyService = spy(cloudinaryImageStorageService);
            doReturn(cloudinary).when(spyService).getCloudinaryInstance();

            // When
            boolean result = spyService.deleteImage(imageUrl);

            // Then
            assertFalse(result);
        }
    }

    @Test
    void deleteImage_IOExceptionThrown_ReturnsFalse() throws IOException {
        // Given
        String imageUrl = "https://res.cloudinary.com/test-cloud/image/upload/test-folder/test-image.jpg";

        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_CLOUD_NAME.name()))
        //         .thenReturn(Optional.of(cloudNameSetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_API_KEY.name()))
        //         .thenReturn(Optional.of(apiKeySetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_API_SECRET.name()))
        //         .thenReturn(Optional.of(apiSecretSetting));
        // when(systemSettingRepository.findByKey(CloudinaryKeys.CLOUNDINARY_FOLDER.name()))
        //         .thenReturn(Optional.of(folderSetting));

        try (MockedStatic<ObjectUtils> objectUtilsMock = mockStatic(ObjectUtils.class)) {
            objectUtilsMock.when(() -> ObjectUtils.asMap(anyString(), any(), anyString(), any(), anyString(), any()))
                    .thenReturn(Map.of("cloud_name", "test-cloud", "api_key", "test-api-key", "api_secret",
                            "test-api-secret"));
            objectUtilsMock.when(ObjectUtils::emptyMap).thenReturn(Map.of());

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.destroy(anyString(), any(Map.class))).thenThrow(new IOException("Delete failed"));

            CloudinaryImageStorageService spyService = spy(cloudinaryImageStorageService);
            doReturn(cloudinary).when(spyService).getCloudinaryInstance();

            // When
            boolean result = spyService.deleteImage(imageUrl);

            // Then
            assertFalse(result);
        }
    }

    @Test
    void extractPublicIdFromUrl_WithVersionNumber_Success() {
        // Given
        String imageUrl = "https://res.cloudinary.com/test-cloud/image/upload/v1746266513/test-folder/test-image.jpg";

        // When
        String result = cloudinaryImageStorageService.extractPublicIdFromUrl(imageUrl);

        // Then
        assertEquals("test-folder/test-image", result);
    }

    @Test
    void extractPublicIdFromUrl_WithoutVersionNumber_Success() {
        // Given
        String imageUrl = "https://res.cloudinary.com/test-cloud/image/upload/test-folder/test-image.jpg";

        // When
        String result = cloudinaryImageStorageService.extractPublicIdFromUrl(imageUrl);

        // Then
        assertEquals("test-folder/test-image", result);
    }

    @Test
    void extractPublicIdFromUrl_WithoutFileExtension_Success() {
        // Given
        String imageUrl = "https://res.cloudinary.com/test-cloud/image/upload/test-folder/test-image";

        // When
        String result = cloudinaryImageStorageService.extractPublicIdFromUrl(imageUrl);

        // Then
        assertEquals("test-folder/test-image", result);
    }

    @Test
    void extractPublicIdFromUrl_ComplexPath_Success() {
        // Given
        String imageUrl = "https://res.cloudinary.com/test-cloud/image/upload/v1746266513/ecommerce/products/categories/test-image.png";

        // When
        String result = cloudinaryImageStorageService.extractPublicIdFromUrl(imageUrl);

        // Then
        assertEquals("ecommerce/products/categories/test-image", result);
    }

    @Test
    void extractPublicIdFromUrl_NullUrl_ReturnsNull() {
        // When
        String result = cloudinaryImageStorageService.extractPublicIdFromUrl(null);

        // Then
        assertNull(result);
    }

    @Test
    void extractPublicIdFromUrl_InvalidUrl_ReturnsNull() {
        // Given
        String imageUrl = "https://example.com/invalid-url.jpg";

        // When
        String result = cloudinaryImageStorageService.extractPublicIdFromUrl(imageUrl);

        // Then
        assertNull(result);
    }

    @Test
    void extractPublicIdFromUrl_VersionWithoutSlash_Success() {
        // Given
        String imageUrl = "https://res.cloudinary.com/test-cloud/image/upload/v1746266513";

        // When
        String result = cloudinaryImageStorageService.extractPublicIdFromUrl(imageUrl);

        // Then
        assertEquals("v1746266513", result);
    }

    
}
