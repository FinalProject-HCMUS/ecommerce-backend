package com.hcmus.ecommerce_backend.integration.product.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmus.ecommerce_backend.integration.BaseIntegrationTest;
import com.hcmus.ecommerce_backend.product.model.dto.request.color.CreateColorRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.color.CreateMultipleColorsRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.color.UpdateColorRequest;
import com.hcmus.ecommerce_backend.product.model.entity.Color;
import com.hcmus.ecommerce_backend.product.repository.ColorRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
    "spring.security.enabled=false"
})
@WithMockUser(username = "testuser", roles = {"USER", "ADMIN"})
public class ColorControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ColorRepository colorRepository;

    private Color testColor1;
    private Color testColor2;
    private Color testColor3;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        // Clean up existing data
        colorRepository.deleteAll();

        setupTestData();
    }

    private void setupTestData() {
        // Create test colors
        testColor1 = Color.builder()
                .name("Red")
                .code("#FF0000")
                .build();
        testColor1.setCreatedAt(LocalDateTime.now());
        testColor1.setUpdatedAt(LocalDateTime.now());
        testColor1 = colorRepository.save(testColor1);

        testColor2 = Color.builder()
                .name("Blue")
                .code("#0000FF")
                .build();
        testColor2.setCreatedAt(LocalDateTime.now());
        testColor2.setUpdatedAt(LocalDateTime.now());
        testColor2 = colorRepository.save(testColor2);

        testColor3 = Color.builder()
                .name("Green")
                .code("#00FF00")
                .build();
        testColor3.setCreatedAt(LocalDateTime.now());
        testColor3.setUpdatedAt(LocalDateTime.now());
        testColor3 = colorRepository.save(testColor3);
    }

    // Test GET /colors - Get all colors with pagination
    @Test
    void getAllColors_WithDefaultPagination_ShouldReturnPaginatedColors() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/colors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.number").value(0))
                .andDo(print());
    }

    @Test
    void getAllColors_WithCustomPagination_ShouldReturnPaginatedColors() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/colors")
                .param("page", "0")
                .param("size", "2")
                .param("sort", "name,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.number").value(0))
                .andDo(print());
    }

    @Test
    void getAllColors_WithKeywordSearch_ShouldReturnMatchingColors() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/colors")
                .param("keyword", "Red"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name").value("Red"))
                .andDo(print());
    }

    @Test
    void getAllColors_WithPartialKeywordSearch_ShouldReturnMatchingColors() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/colors")
                .param("keyword", "e"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3))) // Red, Blue, Green all contain 'e'
                .andDo(print());
    }

    @Test
    void getAllColors_WithNonExistentKeyword_ShouldReturnEmptyPage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/colors")
                .param("keyword", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andDo(print());
    }

    @Test
    void getAllColors_WithEmptyKeyword_ShouldReturnAllColors() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/colors")
                .param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andDo(print());
    }

    @Test
    void getAllColors_WithNullKeyword_ShouldReturnAllColors() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/colors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andDo(print());
    }

    @Test
    void getAllColors_WithEmptyDatabase_ShouldReturnEmptyPage() throws Exception {
        // Clean up test data
        colorRepository.deleteAll();

        // Act & Assert
        mockMvc.perform(get("/colors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.totalPages").value(0))
                .andDo(print());
    }

    // Test GET /colors/{id} - Get color by ID
    @Test
    void getColorById_WithExistingId_ShouldReturnColor() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/colors/{id}", testColor1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(testColor1.getId()))
                .andExpect(jsonPath("$.data.name").value("Red"))
                .andExpect(jsonPath("$.data.code").value("#FF0000"))
                .andDo(print());
    }

    @Test
    void getColorById_WithNonExistentId_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/colors/{id}", "non-existent-id"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test POST /colors - Create new color
    @Test
    void createColor_WithValidData_ShouldCreateColor() throws Exception {
        // Arrange
        CreateColorRequest request = CreateColorRequest.builder()
                .name("Yellow")
                .code("#FFFF00")
                .build();

        // Act & Assert
        mockMvc.perform(post("/colors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.name").value("Yellow"))
                .andExpect(jsonPath("$.data.code").value("#FFFF00"))
                .andDo(print());
    }

    @Test
    void createColor_WithDuplicateName_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateColorRequest request = CreateColorRequest.builder()
                .name("Red") // Duplicate name
                .code("#FF0001")
                .build();

        // Act & Assert
        mockMvc.perform(post("/colors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // @Test
    // void createColor_WithDuplicateCode_ShouldReturnConflict() throws Exception {
    //     // Arrange
    //     CreateColorRequest request = CreateColorRequest.builder()
    //             .name("Dark Red")
    //             .code("#FF0000") // Duplicate code
    //             .build();

    //     // Act & Assert
    //     mockMvc.perform(post("/colors")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isConflict())
    //             .andDo(print());
    // }

    @Test
    void createColor_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateColorRequest request = CreateColorRequest.builder()
                .name("Yellow")
                // Missing code
                .build();

        // Act & Assert
        mockMvc.perform(post("/colors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createColor_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateColorRequest request = CreateColorRequest.builder()
                .name("") // Empty name
                .code("") // Empty code
                .build();

        // Act & Assert
        mockMvc.perform(post("/colors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createColor_WithInvalidHexCode_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateColorRequest request = CreateColorRequest.builder()
                .name("Orange")
                .code("INVALID_HEX") // Invalid hex code
                .build();

        // Act & Assert
        mockMvc.perform(post("/colors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createColor_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/colors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test POST /colors/batch - Create multiple colors
    @Test
    void createMultipleColors_WithValidData_ShouldCreateColors() throws Exception {
        // Arrange
        List<CreateColorRequest> colorRequests = Arrays.asList(
                CreateColorRequest.builder()
                        .name("Yellow")
                        .code("#FFFF00")
                        .build(),
                CreateColorRequest.builder()
                        .name("Purple")
                        .code("#800080")
                        .build(),
                CreateColorRequest.builder()
                        .name("Orange")
                        .code("#FFA500")
                        .build()
        );

        CreateMultipleColorsRequest request = CreateMultipleColorsRequest.builder()
                .colors(colorRequests)
                .build();

        // Act & Assert
        mockMvc.perform(post("/colors/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].name").value("Yellow"))
                .andExpect(jsonPath("$.data[1].name").value("Purple"))
                .andExpect(jsonPath("$.data[2].name").value("Orange"))
                .andDo(print());
    }

    @Test
    void createMultipleColors_WithDuplicateNames_ShouldReturnBadRequest() throws Exception {
        // Arrange
        List<CreateColorRequest> colorRequests = Arrays.asList(
                CreateColorRequest.builder()
                        .name("Red") // Duplicate name
                        .code("#FF0001")
                        .build(),
                CreateColorRequest.builder()
                        .name("Yellow")
                        .code("#FFFF00")
                        .build()
        );

        CreateMultipleColorsRequest request = CreateMultipleColorsRequest.builder()
                .colors(colorRequests)
                .build();

        // Act & Assert
        mockMvc.perform(post("/colors/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createMultipleColors_WithEmptyList_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateMultipleColorsRequest request = CreateMultipleColorsRequest.builder()
                .colors(Arrays.asList()) // Empty list
                .build();

        // Act & Assert
        mockMvc.perform(post("/colors/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createMultipleColors_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        List<CreateColorRequest> colorRequests = Arrays.asList(
                CreateColorRequest.builder()
                        .name("") // Empty name
                        .code("#FFFF00")
                        .build(),
                CreateColorRequest.builder()
                        .name("Purple")
                        .code("INVALID_HEX") // Invalid hex code
                        .build()
        );

        CreateMultipleColorsRequest request = CreateMultipleColorsRequest.builder()
                .colors(colorRequests)
                .build();

        // Act & Assert
        mockMvc.perform(post("/colors/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test PUT /colors/{id} - Update color
    @Test
    void updateColor_WithValidData_ShouldUpdateColor() throws Exception {
        // Arrange
        UpdateColorRequest request = UpdateColorRequest.builder()
                .name("Dark Red")
                .code("#8B0000")
                .build();

        // Act & Assert
        mockMvc.perform(put("/colors/{id}", testColor1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(testColor1.getId()))
                .andExpect(jsonPath("$.data.name").value("Dark Red"))
                .andExpect(jsonPath("$.data.code").value("#8B0000"))
                .andDo(print());
    }

    @Test
    void updateColor_WithNonExistentId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UpdateColorRequest request = UpdateColorRequest.builder()
                .name("Updated Color")
                .code("#123456")
                .build();

        // Act & Assert
        mockMvc.perform(put("/colors/{id}", "non-existent-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void updateColor_WithDuplicateName_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UpdateColorRequest request = UpdateColorRequest.builder()
                .name("Blue") // Duplicate name
                .code("#123456")
                .build();

        // Act & Assert
        mockMvc.perform(put("/colors/{id}", testColor1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // @Test
    // void updateColor_WithDuplicateCode_ShouldReturnConflict() throws Exception {
    //     // Arrange
    //     UpdateColorRequest request = UpdateColorRequest.builder()
    //             .name("Dark Red")
    //             .code("#0000FF") // Duplicate code
    //             .build();

    //     // Act & Assert
    //     mockMvc.perform(put("/colors/{id}", testColor1.getId())
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isConflict())
    //             .andDo(print());
    // }

    @Test
    void updateColor_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UpdateColorRequest request = UpdateColorRequest.builder()
                .name("") // Empty name
                .code("INVALID_HEX") // Invalid hex code
                .build();

        // Act & Assert
        mockMvc.perform(put("/colors/{id}", testColor1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void updateColor_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/colors/{id}", testColor1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test DELETE /colors/{id} - Delete color
    @Test
    void deleteColor_WithExistingId_ShouldDeleteColor() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/colors/{id}", testColor1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andDo(print());

        // Verify deletion
        mockMvc.perform(get("/colors/{id}", testColor1.getId()))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void deleteColor_WithNonExistentId_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/colors/{id}", "non-existent-id"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test advanced search scenarios
    @Test
    void getAllColors_WithCaseInsensitiveKeyword_ShouldReturnMatchingColors() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/colors")
                .param("keyword", "red"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name").value("Red"))
                .andDo(print());
    }

    @Test
    void getAllColors_WithSpecialCharacterKeyword_ShouldReturnMatchingColors() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/colors")
                .param("keyword", "#FF"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andDo(print());
    }

    // Test pagination edge cases
    @Test
    void getAllColors_WithLargePageSize_ShouldReturnAllColors() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/colors")
                .param("page", "0")
                .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.size").value(100))
                .andDo(print());
    }

    @Test
    void getAllColors_WithPageBeyondRange_ShouldReturnEmptyPage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/colors")
                .param("page", "10")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.number").value(10))
                .andDo(print());
    }

    @Test
    void getAllColors_WithMultipleSortFields_ShouldReturnSortedResults() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/colors")
                .param("sort", "name,asc", "code,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andDo(print());
    }

    // @Test
    // void getAllColors_WithZeroPageSize_ShouldHandleCorrectly() throws Exception {
    //     // Act & Assert
    //     mockMvc.perform(get("/colors")
    //             .param("page", "0")
    //             .param("size", "0"))
    //             .andExpect(status().isOk())
    //             .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data").exists())
    //             .andExpect(jsonPath("$.data.content").isArray())
    //             .andExpect(jsonPath("$.data.content").isEmpty())
    //             .andExpect(jsonPath("$.data.size").value(0))
    //             .andDo(print());
    // }

    @Test
    void getAllColors_WithNegativePage_ShouldHandleCorrectly() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/colors")
                .param("page", "-1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.number").value(0)) // Should default to 0
                .andDo(print());
    }

    // Test data integrity
    @Test
    void createColor_ShouldMaintainDataIntegrity() throws Exception {
        // Arrange
        CreateColorRequest request = CreateColorRequest.builder()
                .name("Yellow")
                .code("#FFFF00")
                .build();

        // Act
        mockMvc.perform(post("/colors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print());

        // Assert - Check that total colors count increased
        mockMvc.perform(get("/colors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(4)) // Original 3 + new 1
                .andDo(print());
    }

    // Test concurrent operations
    @Test
    @Transactional
    void concurrentOperations_ShouldHandleCorrectly() throws Exception {
        // Multiple operations on colors
        mockMvc.perform(get("/colors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andDo(print());

        mockMvc.perform(get("/colors/{id}", testColor1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testColor1.getId()))
                .andDo(print());

        mockMvc.perform(get("/colors")
                .param("keyword", "Red"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andDo(print());
    }

    // // Test color code validation
    // @Test
    // void createColor_WithValidHexCodes_ShouldCreateColors() throws Exception {
    //     // Test different valid hex code formats
    //     String[] validCodes = {"#FF0000", "#f00", "#123456", "#ABC", "#abc123"};
        
    //     for (int i = 0; i < validCodes.length; i++) {
    //         CreateColorRequest request = CreateColorRequest.builder()
    //                 .name("Color" + i)
    //                 .code(validCodes[i])
    //                 .build();

    //         mockMvc.perform(post("/colors")
    //                 .contentType(MediaType.APPLICATION_JSON)
    //                 .content(objectMapper.writeValueAsString(request)))
    //                 .andExpect(status().isCreated())
    //                 .andExpect(jsonPath("$.data.code").value(validCodes[i]))
    //                 .andDo(print());
    //     }
    // }

    // @Test
    // void createColor_WithInvalidHexCodes_ShouldReturnBadRequest() throws Exception {
    //     // Test different invalid hex code formats
    //     String[] invalidCodes = {"FF0000", "#GG0000", "#12345", "#1234567", "invalid", ""};
        
    //     for (String invalidCode : invalidCodes) {
    //         CreateColorRequest request = CreateColorRequest.builder()
    //                 .name("TestColor")
    //                 .code(invalidCode)
    //                 .build();

    //         mockMvc.perform(post("/colors")
    //                 .contentType(MediaType.APPLICATION_JSON)
    //                 .content(objectMapper.writeValueAsString(request)))
    //                 .andExpect(status().isBadRequest())
    //                 .andDo(print());
    //     }
    // }

    // Test special characters in color names
    @Test
    void createColor_WithSpecialCharactersInName_ShouldCreateColor() throws Exception {
        // Arrange
        CreateColorRequest request = CreateColorRequest.builder()
                .name("Light-Blue")
                .code("#ADD8E6")
                .build();

        // Act & Assert
        mockMvc.perform(post("/colors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.name").value("Light-Blue"))
                .andDo(print());
    }

    // Test update with same name/code (should be allowed)
    @Test
    void updateColor_WithSameName_ShouldUpdateColor() throws Exception {
        // Arrange
        UpdateColorRequest request = UpdateColorRequest.builder()
                .name("Red") // Same name
                .code("#8B0000") // Different code
                .build();

        // Act & Assert
        mockMvc.perform(put("/colors/{id}", testColor1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(testColor1.getId()))
                .andExpect(jsonPath("$.data.name").value("Red"))
                .andExpect(jsonPath("$.data.code").value("#8B0000"))
                .andDo(print());
    }

    @Test
    void updateColor_WithSameCode_ShouldUpdateColor() throws Exception {
        // Arrange
        UpdateColorRequest request = UpdateColorRequest.builder()
                .name("Dark Red") // Different name
                .code("#FF0000") // Same code
                .build();

        // Act & Assert
        mockMvc.perform(put("/colors/{id}", testColor1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(testColor1.getId()))
                .andExpect(jsonPath("$.data.name").value("Dark Red"))
                .andExpect(jsonPath("$.data.code").value("#FF0000"))
                .andDo(print());
    }

    // // Test batch operations with mixed results
    // @Test
    // void createMultipleColors_WithDuplicateInBatch_ShouldReturnBadRequest() throws Exception {
    //     // Arrange
    //     List<CreateColorRequest> colorRequests = Arrays.asList(
    //             CreateColorRequest.builder()
    //                     .name("Yellow")
    //                     .code("#FFFF00")
    //                     .build(),
    //             CreateColorRequest.builder()
    //                     .name("Yellow") // Duplicate name within batch
    //                     .code("#FFFF01")
    //                     .build()
    //     );

    //     CreateMultipleColorsRequest request = CreateMultipleColorsRequest.builder()
    //             .colors(colorRequests)
    //             .build();

    //     // Act & Assert
    //     mockMvc.perform(post("/colors/batch")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isBadRequest())
    //             .andDo(print());
    // }

    // // Test long color names
    // @Test
    // void createColor_WithLongName_ShouldCreateColor() throws Exception {
    //     // Arrange
    //     String longName = "Very Very Very Long Color Name That Exceeds Normal Length";
    //     CreateColorRequest request = CreateColorRequest.builder()
    //             .name(longName)
    //             .code("#123456")
    //             .build();

    //     // Act & Assert
    //     mockMvc.perform(post("/colors")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isCreated())
    //             .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data").exists())
    //             .andExpect(jsonPath("$.data.name").value(longName))
    //             .andDo(print());
    // }

    // Test Unicode characters in color names
    @Test
    void createColor_WithUnicodeCharacters_ShouldCreateColor() throws Exception {
        // Arrange
        CreateColorRequest request = CreateColorRequest.builder()
                .name("红色") // Chinese characters
                .code("#FF0000")
                .build();

        // Act & Assert
        mockMvc.perform(post("/colors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.name").value("红色"))
                .andDo(print());
    }
}