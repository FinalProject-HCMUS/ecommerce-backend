package com.hcmus.ecommerce_backend.unit.common.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hcmus.ecommerce_backend.common.exception.KeyNotFoundException;
import com.hcmus.ecommerce_backend.common.model.dto.SystemSettingResponse;
import com.hcmus.ecommerce_backend.common.model.dto.UpdateSystemSettingRequest;
import com.hcmus.ecommerce_backend.common.model.entity.SystemSetting;
import com.hcmus.ecommerce_backend.common.model.enums.SettingDataType;
import com.hcmus.ecommerce_backend.common.model.mapper.SystemSettingMapper;
import com.hcmus.ecommerce_backend.common.repository.SystemSettingRepository;
import com.hcmus.ecommerce_backend.common.service.impl.SystemSettingServiceImpl;

@ExtendWith(MockitoExtension.class)
public class SystemSettingServiceImplTest {

    @Mock
    private SystemSettingRepository systemSettingRepository;

    @Mock
    private SystemSettingMapper systemSettingMapper;

    @InjectMocks
    private SystemSettingServiceImpl systemSettingService;

    private SystemSetting vnpayTmnCode;
    private SystemSetting vnpayHashSecret;
    private SystemSetting cloudinaryCloudName;
    private SystemSetting mySettingFrontendUrl;
    private SystemSettingResponse vnpayTmnCodeResponse;
    private SystemSettingResponse vnpayHashSecretResponse;
    private SystemSettingResponse cloudinaryCloudNameResponse;
    private SystemSettingResponse mySettingFrontendUrlResponse;

    @BeforeEach
    void setUp() {
        // Create SystemSetting entities
        vnpayTmnCode = SystemSetting.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value("DEMO_TMN_CODE")
                .serviceName("VNPay")
                .type(SettingDataType.String)
                .build();
        vnpayTmnCode.setCreatedAt(LocalDateTime.now());
        vnpayTmnCode.setUpdatedAt(LocalDateTime.now());

        vnpayHashSecret = SystemSetting.builder()
                .id("2")
                .key("vnpay.hashSecret")
                .value("DEMO_HASH_SECRET")
                .serviceName("VNPay")
                .type(SettingDataType.String)
                .build();
        vnpayHashSecret.setCreatedAt(LocalDateTime.now());
        vnpayHashSecret.setUpdatedAt(LocalDateTime.now());

        cloudinaryCloudName = SystemSetting.builder()
                .id("3")
                .key("cloudinary.cloudName")
                .value("demo-cloud")
                .serviceName("Cloudinary")
                .type(SettingDataType.String)
                .build();
        cloudinaryCloudName.setCreatedAt(LocalDateTime.now());
        cloudinaryCloudName.setUpdatedAt(LocalDateTime.now());

        mySettingFrontendUrl = SystemSetting.builder()
                .id("4")
                .key("frontend-url")
                .value("http://localhost:3000")
                .serviceName("MySetting")
                .type(SettingDataType.String)
                .build();
        mySettingFrontendUrl.setCreatedAt(LocalDateTime.now());
        mySettingFrontendUrl.setUpdatedAt(LocalDateTime.now());

        // Create SystemSettingResponse entities
        vnpayTmnCodeResponse = SystemSettingResponse.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value("DEMO_TMN_CODE")
                .serviceName("VNPay")
                .build();

        vnpayHashSecretResponse = SystemSettingResponse.builder()
                .id("2")
                .key("vnpay.hashSecret")
                .value("DEMO_HASH_SECRET")
                .serviceName("VNPay")
                .build();

        cloudinaryCloudNameResponse = SystemSettingResponse.builder()
                .id("3")
                .key("cloudinary.cloudName")
                .value("demo-cloud")
                .serviceName("Cloudinary")
                .build();

        mySettingFrontendUrlResponse = SystemSettingResponse.builder()
                .id("4")
                .key("frontend-url")
                .value("http://localhost:3000")
                .serviceName("MySetting")
                .build();
    }

    // Test getAllSystemSettings without service name filter
    @Test
    void getAllSystemSettings_WithoutServiceNameFilter_ShouldReturnAllSettings() {
        // Given
        List<SystemSetting> allSettings = Arrays.asList(vnpayTmnCode, vnpayHashSecret, cloudinaryCloudName, mySettingFrontendUrl);
        List<SystemSettingResponse> expectedResponses = Arrays.asList(
                vnpayTmnCodeResponse, vnpayHashSecretResponse, cloudinaryCloudNameResponse, mySettingFrontendUrlResponse);

        when(systemSettingRepository.findAll()).thenReturn(allSettings);
        when(systemSettingMapper.toResponse(vnpayTmnCode)).thenReturn(vnpayTmnCodeResponse);
        when(systemSettingMapper.toResponse(vnpayHashSecret)).thenReturn(vnpayHashSecretResponse);
        when(systemSettingMapper.toResponse(cloudinaryCloudName)).thenReturn(cloudinaryCloudNameResponse);
        when(systemSettingMapper.toResponse(mySettingFrontendUrl)).thenReturn(mySettingFrontendUrlResponse);

        // When
        List<SystemSettingResponse> result = systemSettingService.getAllSystemSettings(null);

        // Then
        assertEquals(4, result.size());
        assertEquals(expectedResponses, result);
        verify(systemSettingRepository).findAll();
        verify(systemSettingRepository, never()).findByServiceName(anyString());
        verify(systemSettingMapper, times(4)).toResponse(any(SystemSetting.class));
    }

    @Test
    void getAllSystemSettings_WithEmptyServiceName_ShouldReturnAllSettings() {
        // Given
        List<SystemSetting> allSettings = Arrays.asList(vnpayTmnCode, vnpayHashSecret, cloudinaryCloudName, mySettingFrontendUrl);
        List<SystemSettingResponse> expectedResponses = Arrays.asList(
                vnpayTmnCodeResponse, vnpayHashSecretResponse, cloudinaryCloudNameResponse, mySettingFrontendUrlResponse);

        when(systemSettingRepository.findAll()).thenReturn(allSettings);
        when(systemSettingMapper.toResponse(vnpayTmnCode)).thenReturn(vnpayTmnCodeResponse);
        when(systemSettingMapper.toResponse(vnpayHashSecret)).thenReturn(vnpayHashSecretResponse);
        when(systemSettingMapper.toResponse(cloudinaryCloudName)).thenReturn(cloudinaryCloudNameResponse);
        when(systemSettingMapper.toResponse(mySettingFrontendUrl)).thenReturn(mySettingFrontendUrlResponse);

        // When
        List<SystemSettingResponse> result = systemSettingService.getAllSystemSettings("");

        // Then
        assertEquals(4, result.size());
        assertEquals(expectedResponses, result);
        verify(systemSettingRepository).findAll();
        verify(systemSettingRepository, never()).findByServiceName(anyString());
        verify(systemSettingMapper, times(4)).toResponse(any(SystemSetting.class));
    }

    @Test
    void getAllSystemSettings_WithVNPayServiceName_ShouldReturnVNPaySettings() {
        // Given
        String serviceName = "VNPay";
        List<SystemSetting> vnpaySettings = Arrays.asList(vnpayTmnCode, vnpayHashSecret);
        List<SystemSettingResponse> expectedResponses = Arrays.asList(vnpayTmnCodeResponse, vnpayHashSecretResponse);

        when(systemSettingRepository.findByServiceName(serviceName)).thenReturn(vnpaySettings);
        when(systemSettingMapper.toResponse(vnpayTmnCode)).thenReturn(vnpayTmnCodeResponse);
        when(systemSettingMapper.toResponse(vnpayHashSecret)).thenReturn(vnpayHashSecretResponse);

        // When
        List<SystemSettingResponse> result = systemSettingService.getAllSystemSettings(serviceName);

        // Then
        assertEquals(2, result.size());
        assertEquals(expectedResponses, result);
        verify(systemSettingRepository).findByServiceName(serviceName);
        verify(systemSettingRepository, never()).findAll();
        verify(systemSettingMapper, times(2)).toResponse(any(SystemSetting.class));
    }

    @Test
    void getAllSystemSettings_WithCloudinaryServiceName_ShouldReturnCloudinarySettings() {
        // Given
        String serviceName = "Cloudinary";
        List<SystemSetting> cloudinarySettings = Arrays.asList(cloudinaryCloudName);
        List<SystemSettingResponse> expectedResponses = Arrays.asList(cloudinaryCloudNameResponse);

        when(systemSettingRepository.findByServiceName(serviceName)).thenReturn(cloudinarySettings);
        when(systemSettingMapper.toResponse(cloudinaryCloudName)).thenReturn(cloudinaryCloudNameResponse);

        // When
        List<SystemSettingResponse> result = systemSettingService.getAllSystemSettings(serviceName);

        // Then
        assertEquals(1, result.size());
        assertEquals(expectedResponses, result);
        verify(systemSettingRepository).findByServiceName(serviceName);
        verify(systemSettingMapper, times(1)).toResponse(any(SystemSetting.class));
    }

    @Test
    void getAllSystemSettings_WithNonExistentServiceName_ShouldReturnEmptyList() {
        // Given
        String serviceName = "NonExistentService";
        List<SystemSetting> emptySettings = Arrays.asList();

        when(systemSettingRepository.findByServiceName(serviceName)).thenReturn(emptySettings);

        // When
        List<SystemSettingResponse> result = systemSettingService.getAllSystemSettings(serviceName);

        // Then
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
        verify(systemSettingRepository).findByServiceName(serviceName);
        verify(systemSettingMapper, never()).toResponse(any(SystemSetting.class));
    }

    // Test updateSystemSettings
    @Test
    void updateSystemSettings_WithValidUpdates_ShouldUpdateSettings() {
        // Given
        UpdateSystemSettingRequest request = UpdateSystemSettingRequest.builder()
                .updates(Arrays.asList(
                        UpdateSystemSettingRequest.KeyValueUpdate.builder()
                                .key("vnpay.tmnCode")
                                .value("NEW_TMN_CODE")
                                .build(),
                        UpdateSystemSettingRequest.KeyValueUpdate.builder()
                                .key("frontend-url")
                                .value("http://localhost:4000")
                                .build()
                ))
                .build();

        List<SystemSetting> allSettings = Arrays.asList(vnpayTmnCode, vnpayHashSecret, cloudinaryCloudName, mySettingFrontendUrl);
        
        // Create updated settings
        SystemSetting updatedVnpayTmnCode = SystemSetting.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value("NEW_TMN_CODE")
                .serviceName("VNPay")
                .type(SettingDataType.String)
                .build();
        
        SystemSetting updatedFrontendUrl = SystemSetting.builder()
                .id("4")
                .key("frontend-url")
                .value("http://localhost:4000")
                .serviceName("MySetting")
                .type(SettingDataType.String)
                .build();

        List<SystemSetting> updatedSettings = Arrays.asList(updatedVnpayTmnCode, vnpayHashSecret, cloudinaryCloudName, updatedFrontendUrl);

        SystemSettingResponse updatedVnpayTmnCodeResponse = SystemSettingResponse.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value("NEW_TMN_CODE")
                .serviceName("VNPay")
                .build();

        SystemSettingResponse updatedFrontendUrlResponse = SystemSettingResponse.builder()
                .id("4")
                .key("frontend-url")
                .value("http://localhost:4000")
                .serviceName("MySetting")
                .build();

        when(systemSettingRepository.findAll()).thenReturn(allSettings);
        when(systemSettingRepository.saveAll(anyList())).thenReturn(updatedSettings);
        when(systemSettingMapper.toResponse(updatedVnpayTmnCode)).thenReturn(updatedVnpayTmnCodeResponse);
        when(systemSettingMapper.toResponse(vnpayHashSecret)).thenReturn(vnpayHashSecretResponse);
        when(systemSettingMapper.toResponse(cloudinaryCloudName)).thenReturn(cloudinaryCloudNameResponse);
        when(systemSettingMapper.toResponse(updatedFrontendUrl)).thenReturn(updatedFrontendUrlResponse);

        // When
        List<SystemSettingResponse> result = systemSettingService.updateSystemSettings(request);

        // Then
        assertEquals(4, result.size());
        assertEquals("NEW_TMN_CODE", updatedVnpayTmnCode.getValue());
        assertEquals("http://localhost:4000", updatedFrontendUrl.getValue());
        verify(systemSettingRepository).findAll();
        verify(systemSettingRepository).saveAll(anyList());
        verify(systemSettingMapper, times(4)).toResponse(any(SystemSetting.class));
    }

    @Test
    void updateSystemSettings_WithSingleUpdate_ShouldUpdateSingleSetting() {
        // Given
        UpdateSystemSettingRequest request = UpdateSystemSettingRequest.builder()
                .updates(Arrays.asList(
                        UpdateSystemSettingRequest.KeyValueUpdate.builder()
                                .key("vnpay.tmnCode")
                                .value("UPDATED_TMN_CODE")
                                .build()
                ))
                .build();

        List<SystemSetting> allSettings = Arrays.asList(vnpayTmnCode, vnpayHashSecret, cloudinaryCloudName, mySettingFrontendUrl);
        
        SystemSetting updatedVnpayTmnCode = SystemSetting.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value("UPDATED_TMN_CODE")
                .serviceName("VNPay")
                .type(SettingDataType.String)
                .build();

        List<SystemSetting> updatedSettings = Arrays.asList(updatedVnpayTmnCode, vnpayHashSecret, cloudinaryCloudName, mySettingFrontendUrl);

        SystemSettingResponse updatedVnpayTmnCodeResponse = SystemSettingResponse.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value("UPDATED_TMN_CODE")
                .serviceName("VNPay")
                .build();

        when(systemSettingRepository.findAll()).thenReturn(allSettings);
        when(systemSettingRepository.saveAll(anyList())).thenReturn(updatedSettings);
        when(systemSettingMapper.toResponse(updatedVnpayTmnCode)).thenReturn(updatedVnpayTmnCodeResponse);
        when(systemSettingMapper.toResponse(vnpayHashSecret)).thenReturn(vnpayHashSecretResponse);
        when(systemSettingMapper.toResponse(cloudinaryCloudName)).thenReturn(cloudinaryCloudNameResponse);
        when(systemSettingMapper.toResponse(mySettingFrontendUrl)).thenReturn(mySettingFrontendUrlResponse);

        // When
        List<SystemSettingResponse> result = systemSettingService.updateSystemSettings(request);

        // Then
        assertEquals(4, result.size());
        assertEquals("UPDATED_TMN_CODE", updatedVnpayTmnCode.getValue());
        verify(systemSettingRepository).findAll();
        verify(systemSettingRepository).saveAll(anyList());
        verify(systemSettingMapper, times(4)).toResponse(any(SystemSetting.class));
    }

    // @Test
    // void updateSystemSettings_WithNonExistentKey_ShouldThrowKeyNotFoundException() {
    //     // Given
    //     UpdateSystemSettingRequest request = UpdateSystemSettingRequest.builder()
    //             .updates(Arrays.asList(
    //                     UpdateSystemSettingRequest.KeyValueUpdate.builder()
    //                             .key("non-existent-key")
    //                             .value("some-value")
    //                             .build()
    //             ))
    //             .build();

    //     List<SystemSetting> allSettings = Arrays.asList(vnpayTmnCode, vnpayHashSecret, cloudinaryCloudName, mySettingFrontendUrl);

    //     when(systemSettingRepository.findAll()).thenReturn(allSettings);

    //     // When & Then
    //     KeyNotFoundException exception = assertThrows(KeyNotFoundException.class, 
    //             () -> systemSettingService.updateSystemSettings(request));
        
    //     assertEquals("non-existent-key", exception.getMessage());
    //     verify(systemSettingRepository).findAll();
    //     verify(systemSettingRepository, never()).saveAll(anyList());
    //     verify(systemSettingMapper, never()).toResponse(any(SystemSetting.class));
    // }

    // @Test
    // void updateSystemSettings_WithMixedValidAndInvalidKeys_ShouldThrowKeyNotFoundException() {
    //     // Given
    //     UpdateSystemSettingRequest request = UpdateSystemSettingRequest.builder()
    //             .updates(Arrays.asList(
    //                     UpdateSystemSettingRequest.KeyValueUpdate.builder()
    //                             .key("vnpay.tmnCode")
    //                             .value("VALID_UPDATE")
    //                             .build(),
    //                     UpdateSystemSettingRequest.KeyValueUpdate.builder()
    //                             .key("invalid-key")
    //                             .value("invalid-value")
    //                             .build()
    //             ))
    //             .build();

    //     List<SystemSetting> allSettings = Arrays.asList(vnpayTmnCode, vnpayHashSecret, cloudinaryCloudName, mySettingFrontendUrl);

    //     when(systemSettingRepository.findAll()).thenReturn(allSettings);

    //     // When & Then
    //     KeyNotFoundException exception = assertThrows(KeyNotFoundException.class, 
    //             () -> systemSettingService.updateSystemSettings(request));
        
    //     assertEquals("invalid-key", exception.getMessage());
    //     verify(systemSettingRepository).findAll();
    //     verify(systemSettingRepository, never()).saveAll(anyList());
    //     verify(systemSettingMapper, never()).toResponse(any(SystemSetting.class));
    // }

    @Test
    void updateSystemSettings_WithNullValue_ShouldUpdateWithNull() {
        // Given
        UpdateSystemSettingRequest request = UpdateSystemSettingRequest.builder()
                .updates(Arrays.asList(
                        UpdateSystemSettingRequest.KeyValueUpdate.builder()
                                .key("vnpay.tmnCode")
                                .value(null)
                                .build()
                ))
                .build();

        List<SystemSetting> allSettings = Arrays.asList(vnpayTmnCode, vnpayHashSecret, cloudinaryCloudName, mySettingFrontendUrl);
        
        SystemSetting updatedVnpayTmnCode = SystemSetting.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value(null)
                .serviceName("VNPay")
                .type(SettingDataType.String)
                .build();

        List<SystemSetting> updatedSettings = Arrays.asList(updatedVnpayTmnCode, vnpayHashSecret, cloudinaryCloudName, mySettingFrontendUrl);

        SystemSettingResponse updatedVnpayTmnCodeResponse = SystemSettingResponse.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value(null)
                .serviceName("VNPay")
                .build();

        when(systemSettingRepository.findAll()).thenReturn(allSettings);
        when(systemSettingRepository.saveAll(anyList())).thenReturn(updatedSettings);
        when(systemSettingMapper.toResponse(updatedVnpayTmnCode)).thenReturn(updatedVnpayTmnCodeResponse);
        when(systemSettingMapper.toResponse(vnpayHashSecret)).thenReturn(vnpayHashSecretResponse);
        when(systemSettingMapper.toResponse(cloudinaryCloudName)).thenReturn(cloudinaryCloudNameResponse);
        when(systemSettingMapper.toResponse(mySettingFrontendUrl)).thenReturn(mySettingFrontendUrlResponse);

        // When
        List<SystemSettingResponse> result = systemSettingService.updateSystemSettings(request);

        // Then
        assertEquals(4, result.size());
        assertNull(updatedVnpayTmnCode.getValue());
        verify(systemSettingRepository).findAll();
        verify(systemSettingRepository).saveAll(anyList());
        verify(systemSettingMapper, times(4)).toResponse(any(SystemSetting.class));
    }

    @Test
    void updateSystemSettings_WithEmptyValue_ShouldUpdateWithEmptyString() {
        // Given
        UpdateSystemSettingRequest request = UpdateSystemSettingRequest.builder()
                .updates(Arrays.asList(
                        UpdateSystemSettingRequest.KeyValueUpdate.builder()
                                .key("vnpay.tmnCode")
                                .value("")
                                .build()
                ))
                .build();

        List<SystemSetting> allSettings = Arrays.asList(vnpayTmnCode, vnpayHashSecret, cloudinaryCloudName, mySettingFrontendUrl);
        
        SystemSetting updatedVnpayTmnCode = SystemSetting.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value("")
                .serviceName("VNPay")
                .type(SettingDataType.String)
                .build();

        List<SystemSetting> updatedSettings = Arrays.asList(updatedVnpayTmnCode, vnpayHashSecret, cloudinaryCloudName, mySettingFrontendUrl);

        SystemSettingResponse updatedVnpayTmnCodeResponse = SystemSettingResponse.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value("")
                .serviceName("VNPay")
                .build();

        when(systemSettingRepository.findAll()).thenReturn(allSettings);
        when(systemSettingRepository.saveAll(anyList())).thenReturn(updatedSettings);
        when(systemSettingMapper.toResponse(updatedVnpayTmnCode)).thenReturn(updatedVnpayTmnCodeResponse);
        when(systemSettingMapper.toResponse(vnpayHashSecret)).thenReturn(vnpayHashSecretResponse);
        when(systemSettingMapper.toResponse(cloudinaryCloudName)).thenReturn(cloudinaryCloudNameResponse);
        when(systemSettingMapper.toResponse(mySettingFrontendUrl)).thenReturn(mySettingFrontendUrlResponse);

        // When
        List<SystemSettingResponse> result = systemSettingService.updateSystemSettings(request);

        // Then
        assertEquals(4, result.size());
        assertEquals("", updatedVnpayTmnCode.getValue());
        verify(systemSettingRepository).findAll();
        verify(systemSettingRepository).saveAll(anyList());
        verify(systemSettingMapper, times(4)).toResponse(any(SystemSetting.class));
    }

    @Test
    void updateSystemSettings_WithDuplicateKeysInRequest_ShouldUpdateWithLastValue() {
        // Given
        UpdateSystemSettingRequest request = UpdateSystemSettingRequest.builder()
                .updates(Arrays.asList(
                        UpdateSystemSettingRequest.KeyValueUpdate.builder()
                                .key("vnpay.tmnCode")
                                .value("FIRST_VALUE")
                                .build(),
                        UpdateSystemSettingRequest.KeyValueUpdate.builder()
                                .key("vnpay.tmnCode")
                                .value("SECOND_VALUE")
                                .build()
                ))
                .build();

        List<SystemSetting> allSettings = Arrays.asList(vnpayTmnCode, vnpayHashSecret, cloudinaryCloudName, mySettingFrontendUrl);
        
        SystemSetting updatedVnpayTmnCode = SystemSetting.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value("SECOND_VALUE")
                .serviceName("VNPay")
                .type(SettingDataType.String)
                .build();

        List<SystemSetting> updatedSettings = Arrays.asList(updatedVnpayTmnCode, vnpayHashSecret, cloudinaryCloudName, mySettingFrontendUrl);

        SystemSettingResponse updatedVnpayTmnCodeResponse = SystemSettingResponse.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value("SECOND_VALUE")
                .serviceName("VNPay")
                .build();

        when(systemSettingRepository.findAll()).thenReturn(allSettings);
        when(systemSettingRepository.saveAll(anyList())).thenReturn(updatedSettings);
        when(systemSettingMapper.toResponse(updatedVnpayTmnCode)).thenReturn(updatedVnpayTmnCodeResponse);
        when(systemSettingMapper.toResponse(vnpayHashSecret)).thenReturn(vnpayHashSecretResponse);
        when(systemSettingMapper.toResponse(cloudinaryCloudName)).thenReturn(cloudinaryCloudNameResponse);
        when(systemSettingMapper.toResponse(mySettingFrontendUrl)).thenReturn(mySettingFrontendUrlResponse);

        // When
        List<SystemSettingResponse> result = systemSettingService.updateSystemSettings(request);

        // Then
        assertEquals(4, result.size());
        assertEquals("SECOND_VALUE", updatedVnpayTmnCode.getValue());
        verify(systemSettingRepository).findAll();
        verify(systemSettingRepository).saveAll(anyList());
        verify(systemSettingMapper, times(4)).toResponse(any(SystemSetting.class));
    }

    // Test getAllServiceNames
    @Test
    void getAllServiceNames_ShouldReturnDistinctServiceNames() {
        // Given
        List<String> serviceNames = Arrays.asList("VNPay", "Cloudinary", "MySetting");

        when(systemSettingRepository.findDistinctServiceNames()).thenReturn(serviceNames);

        // When
        List<String> result = systemSettingService.getAllServiceNames();

        // Then
        assertEquals(3, result.size());
        assertTrue(result.contains("VNPay"));
        assertTrue(result.contains("Cloudinary"));
        assertTrue(result.contains("MySetting"));
        verify(systemSettingRepository).findDistinctServiceNames();
    }

    @Test
    void getAllServiceNames_WithEmptyDatabase_ShouldReturnEmptyList() {
        // Given
        List<String> emptyServiceNames = Arrays.asList();

        when(systemSettingRepository.findDistinctServiceNames()).thenReturn(emptyServiceNames);

        // When
        List<String> result = systemSettingService.getAllServiceNames();

        // Then
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
        verify(systemSettingRepository).findDistinctServiceNames();
    }

    @Test
    void getAllServiceNames_WithDuplicateServiceNames_ShouldReturnDistinctNames() {
        // Given
        List<String> serviceNames = Arrays.asList("VNPay", "Cloudinary", "MySetting");

        when(systemSettingRepository.findDistinctServiceNames()).thenReturn(serviceNames);

        // When
        List<String> result = systemSettingService.getAllServiceNames();

        // Then
        assertEquals(3, result.size());
        assertEquals(serviceNames, result);
        verify(systemSettingRepository).findDistinctServiceNames();
    }

    // Test edge cases
    @Test
    void updateSystemSettings_WithEmptyUpdatesList_ShouldReturnAllSettings() {
        // Given
        UpdateSystemSettingRequest request = UpdateSystemSettingRequest.builder()
                .updates(Arrays.asList())
                .build();

        List<SystemSetting> allSettings = Arrays.asList(vnpayTmnCode, vnpayHashSecret, cloudinaryCloudName, mySettingFrontendUrl);
        List<SystemSettingResponse> expectedResponses = Arrays.asList(
                vnpayTmnCodeResponse, vnpayHashSecretResponse, cloudinaryCloudNameResponse, mySettingFrontendUrlResponse);

        when(systemSettingRepository.findAll()).thenReturn(allSettings);
        when(systemSettingRepository.saveAll(anyList())).thenReturn(allSettings);
        when(systemSettingMapper.toResponse(vnpayTmnCode)).thenReturn(vnpayTmnCodeResponse);
        when(systemSettingMapper.toResponse(vnpayHashSecret)).thenReturn(vnpayHashSecretResponse);
        when(systemSettingMapper.toResponse(cloudinaryCloudName)).thenReturn(cloudinaryCloudNameResponse);
        when(systemSettingMapper.toResponse(mySettingFrontendUrl)).thenReturn(mySettingFrontendUrlResponse);

        // When
        List<SystemSettingResponse> result = systemSettingService.updateSystemSettings(request);

        // Then
        assertEquals(4, result.size());
        assertEquals(expectedResponses, result);
        verify(systemSettingRepository).findAll();
        verify(systemSettingRepository).saveAll(anyList());
        verify(systemSettingMapper, times(4)).toResponse(any(SystemSetting.class));
    }

    @Test
    void updateSystemSettings_WithSpecialCharacters_ShouldUpdateSuccessfully() {
        // Given
        String specialValue = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~";
        UpdateSystemSettingRequest request = UpdateSystemSettingRequest.builder()
                .updates(Arrays.asList(
                        UpdateSystemSettingRequest.KeyValueUpdate.builder()
                                .key("vnpay.tmnCode")
                                .value(specialValue)
                                .build()
                ))
                .build();

        List<SystemSetting> allSettings = Arrays.asList(vnpayTmnCode, vnpayHashSecret, cloudinaryCloudName, mySettingFrontendUrl);
        
        SystemSetting updatedVnpayTmnCode = SystemSetting.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value(specialValue)
                .serviceName("VNPay")
                .type(SettingDataType.String)
                .build();

        List<SystemSetting> updatedSettings = Arrays.asList(updatedVnpayTmnCode, vnpayHashSecret, cloudinaryCloudName, mySettingFrontendUrl);

        SystemSettingResponse updatedVnpayTmnCodeResponse = SystemSettingResponse.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value(specialValue)
                .serviceName("VNPay")
                .build();

        when(systemSettingRepository.findAll()).thenReturn(allSettings);
        when(systemSettingRepository.saveAll(anyList())).thenReturn(updatedSettings);
        when(systemSettingMapper.toResponse(updatedVnpayTmnCode)).thenReturn(updatedVnpayTmnCodeResponse);
        when(systemSettingMapper.toResponse(vnpayHashSecret)).thenReturn(vnpayHashSecretResponse);
        when(systemSettingMapper.toResponse(cloudinaryCloudName)).thenReturn(cloudinaryCloudNameResponse);
        when(systemSettingMapper.toResponse(mySettingFrontendUrl)).thenReturn(mySettingFrontendUrlResponse);

        // When
        List<SystemSettingResponse> result = systemSettingService.updateSystemSettings(request);

        // Then
        assertEquals(4, result.size());
        assertEquals(specialValue, updatedVnpayTmnCode.getValue());
        verify(systemSettingRepository).findAll();
        verify(systemSettingRepository).saveAll(anyList());
        verify(systemSettingMapper, times(4)).toResponse(any(SystemSetting.class));
    }

    @Test
    void updateSystemSettings_WithUnicodeCharacters_ShouldUpdateSuccessfully() {
        // Given
        String unicodeValue = "こんにちは 世界 🌍 éñáçà";
        UpdateSystemSettingRequest request = UpdateSystemSettingRequest.builder()
                .updates(Arrays.asList(
                        UpdateSystemSettingRequest.KeyValueUpdate.builder()
                                .key("vnpay.tmnCode")
                                .value(unicodeValue)
                                .build()
                ))
                .build();

        List<SystemSetting> allSettings = Arrays.asList(vnpayTmnCode, vnpayHashSecret, cloudinaryCloudName, mySettingFrontendUrl);
        
        SystemSetting updatedVnpayTmnCode = SystemSetting.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value(unicodeValue)
                .serviceName("VNPay")
                .type(SettingDataType.String)
                .build();

        List<SystemSetting> updatedSettings = Arrays.asList(updatedVnpayTmnCode, vnpayHashSecret, cloudinaryCloudName, mySettingFrontendUrl);

        SystemSettingResponse updatedVnpayTmnCodeResponse = SystemSettingResponse.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value(unicodeValue)
                .serviceName("VNPay")
                .build();

        when(systemSettingRepository.findAll()).thenReturn(allSettings);
        when(systemSettingRepository.saveAll(anyList())).thenReturn(updatedSettings);
        when(systemSettingMapper.toResponse(updatedVnpayTmnCode)).thenReturn(updatedVnpayTmnCodeResponse);
        when(systemSettingMapper.toResponse(vnpayHashSecret)).thenReturn(vnpayHashSecretResponse);
        when(systemSettingMapper.toResponse(cloudinaryCloudName)).thenReturn(cloudinaryCloudNameResponse);
        when(systemSettingMapper.toResponse(mySettingFrontendUrl)).thenReturn(mySettingFrontendUrlResponse);

        // When
        List<SystemSettingResponse> result = systemSettingService.updateSystemSettings(request);

        // Then
        assertEquals(4, result.size());
        assertEquals(unicodeValue, updatedVnpayTmnCode.getValue());
        verify(systemSettingRepository).findAll();
        verify(systemSettingRepository).saveAll(anyList());
        verify(systemSettingMapper, times(4)).toResponse(any(SystemSetting.class));
    }

    @Test
    void updateSystemSettings_RepositoryThrowsException_ShouldPropagateException() {
        // Given
        UpdateSystemSettingRequest request = UpdateSystemSettingRequest.builder()
                .updates(Arrays.asList(
                        UpdateSystemSettingRequest.KeyValueUpdate.builder()
                                .key("vnpay.tmnCode")
                                .value("NEW_VALUE")
                                .build()
                ))
                .build();

        when(systemSettingRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> systemSettingService.updateSystemSettings(request));
        verify(systemSettingRepository).findAll();
        verify(systemSettingRepository, never()).saveAll(anyList());
        verify(systemSettingMapper, never()).toResponse(any(SystemSetting.class));
    }
}