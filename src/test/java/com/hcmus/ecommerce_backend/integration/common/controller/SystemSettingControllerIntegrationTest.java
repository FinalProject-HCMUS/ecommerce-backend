package com.hcmus.ecommerce_backend.integration.common.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmus.ecommerce_backend.common.controller.SystemSettingController;
import com.hcmus.ecommerce_backend.common.exception.KeyNotFoundException;
import com.hcmus.ecommerce_backend.common.model.dto.UpdateSystemSettingRequest;
import com.hcmus.ecommerce_backend.common.model.dto.SystemSettingResponse;
import com.hcmus.ecommerce_backend.common.model.enums.SettingDataType;
import com.hcmus.ecommerce_backend.common.service.SystemSettingService;

@ExtendWith(MockitoExtension.class)
public class SystemSettingControllerIntegrationTest {

    @Mock
    private SystemSettingService systemSettingService;

    @InjectMocks
    private SystemSettingController systemSettingController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private SystemSettingResponse vnpayTmnCode;
    private SystemSettingResponse vnpayHashSecret;
    private SystemSettingResponse vnpayUrl;
    private SystemSettingResponse cloudinaryCloudName;
    private SystemSettingResponse cloudinaryApiKey;
    private SystemSettingResponse cloudinaryApiSecret;
    private SystemSettingResponse mySettingFrontendUrl;
    private SystemSettingResponse mySettingAppName;
    private List<SystemSettingResponse> allSettings;
    private List<SystemSettingResponse> vnpaySettings;
    private List<SystemSettingResponse> cloudinarySettings;
    private List<SystemSettingResponse> mySettings;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(systemSettingController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        setupTestData();
    }

    private void setupTestData() {
        // Create VNPay service settings
        vnpayTmnCode = SystemSettingResponse.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value("DEMO_TMN_CODE")
                .serviceName("VNPay")
                .build();

        vnpayHashSecret = SystemSettingResponse.builder()
                .id("2")
                .key("vnpay.hashSecret")
                .value("DEMO_HASH_SECRET")
                .serviceName("VNPay")
                .build();

        vnpayUrl = SystemSettingResponse.builder()
                .id("3")
                .key("vnpay.url")
                .value("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html")
                .serviceName("VNPay")
                .build();

        // Create Cloudinary service settings
        cloudinaryCloudName = SystemSettingResponse.builder()
                .id("4")
                .key("cloudinary.cloudName")
                .value("demo-cloud")
                .serviceName("Cloudinary")
                .build();

        cloudinaryApiKey = SystemSettingResponse.builder()
                .id("5")
                .key("cloudinary.apiKey")
                .value("demo-api-key")
                .serviceName("Cloudinary")
                .build();

        cloudinaryApiSecret = SystemSettingResponse.builder()
                .id("6")
                .key("cloudinary.apiSecret")
                .value("demo-api-secret")
                .serviceName("Cloudinary")
                .build();

        // Create MySetting service settings
        mySettingFrontendUrl = SystemSettingResponse.builder()
                .id("7")
                .key("frontend-url")
                .value("http://localhost:3000")
                .serviceName("MySetting")
                .build();

        mySettingAppName = SystemSettingResponse.builder()
                .id("8")
                .key("app-name")
                .value("E-Commerce Backend")
                .serviceName("MySetting")
                .build();

        // Group settings by service
        vnpaySettings = Arrays.asList(vnpayTmnCode, vnpayHashSecret, vnpayUrl);
        cloudinarySettings = Arrays.asList(cloudinaryCloudName, cloudinaryApiKey, cloudinaryApiSecret);
        mySettings = Arrays.asList(mySettingFrontendUrl, mySettingAppName);
        allSettings = Arrays.asList(vnpayTmnCode, vnpayHashSecret, vnpayUrl, cloudinaryCloudName, 
                                   cloudinaryApiKey, cloudinaryApiSecret, mySettingFrontendUrl, mySettingAppName);
    }

    @Test
    void getAllSystemSettings_WithVNPayServiceName_ShouldReturnVNPaySettings() throws Exception {
        // Given
        when(systemSettingService.getAllSystemSettings("VNPay")).thenReturn(vnpaySettings);

        // When & Then
        mockMvc.perform(get("/system-settings")
                .param("serviceName", "VNPay"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].serviceName").value("VNPay"))
                .andExpect(jsonPath("$[1].serviceName").value("VNPay"))
                .andExpect(jsonPath("$[2].serviceName").value("VNPay"));

        verify(systemSettingService).getAllSystemSettings("VNPay");
    }

    @Test
    void getAllSystemSettings_WithCloudinaryServiceName_ShouldReturnCloudinarySettings() throws Exception {
        // Given
        when(systemSettingService.getAllSystemSettings("Cloudinary")).thenReturn(cloudinarySettings);

        // When & Then
        mockMvc.perform(get("/system-settings")
                .param("serviceName", "Cloudinary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].serviceName").value("Cloudinary"))
                .andExpect(jsonPath("$[1].serviceName").value("Cloudinary"))
                .andExpect(jsonPath("$[2].serviceName").value("Cloudinary"));

        verify(systemSettingService).getAllSystemSettings("Cloudinary");
    }

    @Test
    void getAllSystemSettings_WithMySettingServiceName_ShouldReturnMySettings() throws Exception {
        // Given
        when(systemSettingService.getAllSystemSettings("MySetting")).thenReturn(mySettings);

        // When & Then
        mockMvc.perform(get("/system-settings")
                .param("serviceName", "MySetting"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].serviceName").value("MySetting"))
                .andExpect(jsonPath("$[1].serviceName").value("MySetting"));

        verify(systemSettingService).getAllSystemSettings("MySetting");
    }

    @Test
    void getAllSystemSettings_WithNonExistentServiceName_ShouldReturnEmptyList() throws Exception {
        // Given
        when(systemSettingService.getAllSystemSettings("NonExistentService")).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/system-settings")
                .param("serviceName", "NonExistentService"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(systemSettingService).getAllSystemSettings("NonExistentService");
    }

    @Test
    void getAllSystemSettings_WithEmptyServiceName_ShouldReturnAllSettings() throws Exception {
        // Given
        when(systemSettingService.getAllSystemSettings("")).thenReturn(allSettings);

        // When & Then
        mockMvc.perform(get("/system-settings")
                .param("serviceName", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(8));

        verify(systemSettingService).getAllSystemSettings("");
    }

    // Test PUT /system-settings - Update multiple system settings
    @Test
    void updateSystemSettings_WithValidUpdates_ShouldUpdateSettings() throws Exception {
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

        // Create updated settings
        SystemSettingResponse updatedVnpay = SystemSettingResponse.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value("NEW_TMN_CODE")
                .serviceName("VNPay")
                .build();

        SystemSettingResponse updatedFrontend = SystemSettingResponse.builder()
                .id("7")
                .key("frontend-url")
                .value("http://localhost:4000")
                .serviceName("MySetting")
                .build();

        List<SystemSettingResponse> updatedAllSettings = Arrays.asList(
                updatedVnpay, vnpayHashSecret, vnpayUrl, cloudinaryCloudName, 
                cloudinaryApiKey, cloudinaryApiSecret, updatedFrontend, mySettingAppName
        );

        when(systemSettingService.updateSystemSettings(any(UpdateSystemSettingRequest.class)))
                .thenReturn(updatedAllSettings);

        // When & Then
        mockMvc.perform(put("/system-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(8))
                .andExpect(jsonPath("$[0].key").value("vnpay.tmnCode"))
                .andExpect(jsonPath("$[0].value").value("NEW_TMN_CODE"))
                .andExpect(jsonPath("$[6].key").value("frontend-url"))
                .andExpect(jsonPath("$[6].value").value("http://localhost:4000"));

        verify(systemSettingService).updateSystemSettings(any(UpdateSystemSettingRequest.class));
    }

    @Test
    void updateSystemSettings_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(put("/system-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest());

        verify(systemSettingService, never()).updateSystemSettings(any());
    }

    @Test
    void updateSystemSettings_WithMultipleUpdates_ShouldUpdateAll() throws Exception {
        // Given
        UpdateSystemSettingRequest request = UpdateSystemSettingRequest.builder()
                .updates(Arrays.asList(
                        UpdateSystemSettingRequest.KeyValueUpdate.builder()
                                .key("vnpay.tmnCode")
                                .value("NEW_TMN_CODE")
                                .build(),
                        UpdateSystemSettingRequest.KeyValueUpdate.builder()
                                .key("vnpay.hashSecret")
                                .value("NEW_HASH_SECRET")
                                .build(),
                        UpdateSystemSettingRequest.KeyValueUpdate.builder()
                                .key("cloudinary.cloudName")
                                .value("new-cloud-name")
                                .build()
                ))
                .build();

        SystemSettingResponse updatedVnpayTmn = SystemSettingResponse.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value("NEW_TMN_CODE")
                .serviceName("VNPay")
                .build();

        SystemSettingResponse updatedVnpayHash = SystemSettingResponse.builder()
                .id("2")
                .key("vnpay.hashSecret")
                .value("NEW_HASH_SECRET")
                .serviceName("VNPay")
                .build();

        SystemSettingResponse updatedCloudinary = SystemSettingResponse.builder()
                .id("4")
                .key("cloudinary.cloudName")
                .value("new-cloud-name")
                .serviceName("Cloudinary")
                .build();

        List<SystemSettingResponse> updatedSettings = Arrays.asList(
                updatedVnpayTmn, updatedVnpayHash, vnpayUrl, updatedCloudinary, 
                cloudinaryApiKey, cloudinaryApiSecret, mySettingFrontendUrl, mySettingAppName
        );

        when(systemSettingService.updateSystemSettings(any(UpdateSystemSettingRequest.class)))
                .thenReturn(updatedSettings);

        // When & Then
        mockMvc.perform(put("/system-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(8))
                .andExpect(jsonPath("$[0].key").value("vnpay.tmnCode"))
                .andExpect(jsonPath("$[0].value").value("NEW_TMN_CODE"))
                .andExpect(jsonPath("$[1].key").value("vnpay.hashSecret"))
                .andExpect(jsonPath("$[1].value").value("NEW_HASH_SECRET"))
                .andExpect(jsonPath("$[3].key").value("cloudinary.cloudName"))
                .andExpect(jsonPath("$[3].value").value("new-cloud-name"));

        verify(systemSettingService).updateSystemSettings(any(UpdateSystemSettingRequest.class));
    }

    // Test GET /system-settings/service-names - Get all distinct service names
    @Test
    void getAllServiceNames_ShouldReturnDistinctServiceNames() throws Exception {
        // Given
        List<String> serviceNames = Arrays.asList("VNPay", "Cloudinary", "MySetting");
        when(systemSettingService.getAllServiceNames()).thenReturn(serviceNames);

        // When & Then
        mockMvc.perform(get("/system-settings/service-names"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("VNPay"))
                .andExpect(jsonPath("$[1]").value("Cloudinary"))
                .andExpect(jsonPath("$[2]").value("MySetting"));

        verify(systemSettingService).getAllServiceNames();
    }

    @Test
    void getAllServiceNames_WithEmptyDatabase_ShouldReturnEmptyList() throws Exception {
        // Given
        when(systemSettingService.getAllServiceNames()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/system-settings/service-names"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(systemSettingService).getAllServiceNames();
    }

    // Test unicode and special characters
    @Test
    void updateSystemSettings_WithUnicodeCharacters_ShouldUpdateSuccessfully() throws Exception {
        // Given
        String unicodeValue = "こんにちは 世界 🌍 éñáçà";
        UpdateSystemSettingRequest request = UpdateSystemSettingRequest.builder()
                .updates(Arrays.asList(
                        UpdateSystemSettingRequest.KeyValueUpdate.builder()
                                .key("app-name")
                                .value(unicodeValue)
                                .build()
                ))
                .build();

        SystemSettingResponse updatedAppName = SystemSettingResponse.builder()
                .id("8")
                .key("app-name")
                .value(unicodeValue)
                .serviceName("MySetting")
                .build();

        List<SystemSettingResponse> updatedSettings = Arrays.asList(
                vnpayTmnCode, vnpayHashSecret, vnpayUrl, cloudinaryCloudName, 
                cloudinaryApiKey, cloudinaryApiSecret, mySettingFrontendUrl, updatedAppName
        );

        when(systemSettingService.updateSystemSettings(any(UpdateSystemSettingRequest.class)))
                .thenReturn(updatedSettings);

        // When & Then
        mockMvc.perform(put("/system-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[7].key").value("app-name"))
                .andExpect(jsonPath("$[7].value").value(unicodeValue));

        verify(systemSettingService).updateSystemSettings(any(UpdateSystemSettingRequest.class));
    }

    // Test edge cases
    @Test
    void updateSystemSettings_WithSameValueUpdate_ShouldUpdateSuccessfully() throws Exception {
        // Given
        UpdateSystemSettingRequest request = UpdateSystemSettingRequest.builder()
                .updates(Arrays.asList(
                        UpdateSystemSettingRequest.KeyValueUpdate.builder()
                                .key("vnpay.tmnCode")
                                .value("DEMO_TMN_CODE") // Same as current value
                                .build()
                ))
                .build();

        when(systemSettingService.updateSystemSettings(any(UpdateSystemSettingRequest.class)))
                .thenReturn(allSettings);

        // When & Then
        mockMvc.perform(put("/system-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].key").value("vnpay.tmnCode"))
                .andExpect(jsonPath("$[0].value").value("DEMO_TMN_CODE"));

        verify(systemSettingService).updateSystemSettings(any(UpdateSystemSettingRequest.class));
    }

    @Test
    void updateSystemSettings_WithVeryLongValue_ShouldUpdateSuccessfully() throws Exception {
        // Given
        String longValue = "A".repeat(1000); // Very long value
        UpdateSystemSettingRequest request = UpdateSystemSettingRequest.builder()
                .updates(Arrays.asList(
                        UpdateSystemSettingRequest.KeyValueUpdate.builder()
                                .key("vnpay.tmnCode")
                                .value(longValue)
                                .build()
                ))
                .build();

        SystemSettingResponse updatedVnpay = SystemSettingResponse.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value(longValue)
                .serviceName("VNPay")
                .build();

        List<SystemSettingResponse> updatedSettings = Arrays.asList(
                updatedVnpay, vnpayHashSecret, vnpayUrl, cloudinaryCloudName, 
                cloudinaryApiKey, cloudinaryApiSecret, mySettingFrontendUrl, mySettingAppName
        );

        when(systemSettingService.updateSystemSettings(any(UpdateSystemSettingRequest.class)))
                .thenReturn(updatedSettings);

        // When & Then
        mockMvc.perform(put("/system-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].key").value("vnpay.tmnCode"))
                .andExpect(jsonPath("$[0].value").value(longValue));

        verify(systemSettingService).updateSystemSettings(any(UpdateSystemSettingRequest.class));
    }

    @Test
    void getAllSystemSettings_WithLargeDataset_ShouldReturnAllData() throws Exception {
        // Given
        List<SystemSettingResponse> largeSettingsList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeSettingsList.add(SystemSettingResponse.builder()
                    .id(String.valueOf(i))
                    .key("key-" + i)
                    .value("value-" + i)
                    .serviceName("Service-" + (i % 10))
                    .build());
        }

        when(systemSettingService.getAllSystemSettings(null)).thenReturn(largeSettingsList);

        // When & Then
        mockMvc.perform(get("/system-settings"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(100));

        verify(systemSettingService).getAllSystemSettings(null);
    }

    @Test
    void getAllSystemSettings_WithCaseSensitiveServiceName_ShouldReturnCorrectResults() throws Exception {
        // Given
        when(systemSettingService.getAllSystemSettings("vnpay")).thenReturn(Arrays.asList());
        when(systemSettingService.getAllSystemSettings("VNPay")).thenReturn(vnpaySettings);

        // When & Then - Test lowercase
        mockMvc.perform(get("/system-settings")
                .param("serviceName", "vnpay"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        // When & Then - Test correct case
        mockMvc.perform(get("/system-settings")
                .param("serviceName", "VNPay"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));

        verify(systemSettingService).getAllSystemSettings("vnpay");
        verify(systemSettingService).getAllSystemSettings("VNPay");
    }

    @Test
    void updateSystemSettings_WithNullValue_ShouldUpdateWithNull() throws Exception {
        // Given
        UpdateSystemSettingRequest request = UpdateSystemSettingRequest.builder()
                .updates(Arrays.asList(
                        UpdateSystemSettingRequest.KeyValueUpdate.builder()
                                .key("vnpay.tmnCode")
                                .value(null)
                                .build()
                ))
                .build();

        SystemSettingResponse updatedVnpay = SystemSettingResponse.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value(null)
                .serviceName("VNPay")
                .build();

        List<SystemSettingResponse> updatedSettings = Arrays.asList(
                updatedVnpay, vnpayHashSecret, vnpayUrl, cloudinaryCloudName, 
                cloudinaryApiKey, cloudinaryApiSecret, mySettingFrontendUrl, mySettingAppName
        );

        when(systemSettingService.updateSystemSettings(any(UpdateSystemSettingRequest.class)))
                .thenReturn(updatedSettings);

        // When & Then
        mockMvc.perform(put("/system-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].key").value("vnpay.tmnCode"));

        verify(systemSettingService).updateSystemSettings(any(UpdateSystemSettingRequest.class));
    }

    @Test
    void updateSystemSettings_WithEmptyValue_ShouldUpdateWithEmpty() throws Exception {
        // Given
        UpdateSystemSettingRequest request = UpdateSystemSettingRequest.builder()
                .updates(Arrays.asList(
                        UpdateSystemSettingRequest.KeyValueUpdate.builder()
                                .key("vnpay.tmnCode")
                                .value("")
                                .build()
                ))
                .build();

        SystemSettingResponse updatedVnpay = SystemSettingResponse.builder()
                .id("1")
                .key("vnpay.tmnCode")
                .value("")
                .serviceName("VNPay")
                .build();

        List<SystemSettingResponse> updatedSettings = Arrays.asList(
                updatedVnpay, vnpayHashSecret, vnpayUrl, cloudinaryCloudName, 
                cloudinaryApiKey, cloudinaryApiSecret, mySettingFrontendUrl, mySettingAppName
        );

        when(systemSettingService.updateSystemSettings(any(UpdateSystemSettingRequest.class)))
                .thenReturn(updatedSettings);

        // When & Then
        mockMvc.perform(put("/system-settings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].key").value("vnpay.tmnCode"))
                .andExpect(jsonPath("$[0].value").value(""));

        verify(systemSettingService).updateSystemSettings(any(UpdateSystemSettingRequest.class));
    }
}