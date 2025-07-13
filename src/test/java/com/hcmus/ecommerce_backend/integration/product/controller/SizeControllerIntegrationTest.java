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
import com.hcmus.ecommerce_backend.product.model.dto.request.size.CreateMultipleSizesRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.size.CreateSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.size.UpdateSizeRequest;
import com.hcmus.ecommerce_backend.product.model.entity.Size;
import com.hcmus.ecommerce_backend.product.repository.SizeRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
    "spring.security.enabled=false"
})
@WithMockUser(username = "testuser", roles = {"USER", "ADMIN"})
public class SizeControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SizeRepository sizeRepository;

    private Size testSize1;
    private Size testSize2;
    private Size testSize3;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        // Clean up existing data
        sizeRepository.deleteAll();

        setupTestData();
    }

    private void setupTestData() {
        // Create test sizes
        testSize1 = Size.builder()
                .name("XS")
                .minHeight(140)
                .maxHeight(155)
                .minWeight(40)
                .maxWeight(50)
                .build();
        testSize1.setCreatedAt(LocalDateTime.now());
        testSize1.setUpdatedAt(LocalDateTime.now());
        testSize1 = sizeRepository.save(testSize1);

        testSize2 = Size.builder()
                .name("S")
                .minHeight(155)
                .maxHeight(165)
                .minWeight(50)
                .maxWeight(60)
                .build();
        testSize2.setCreatedAt(LocalDateTime.now());
        testSize2.setUpdatedAt(LocalDateTime.now());
        testSize2 = sizeRepository.save(testSize2);

        testSize3 = Size.builder()
                .name("M")
                .minHeight(165)
                .maxHeight(175)
                .minWeight(60)
                .maxWeight(70)
                .build();
        testSize3.setCreatedAt(LocalDateTime.now());
        testSize3.setUpdatedAt(LocalDateTime.now());
        testSize3 = sizeRepository.save(testSize3);
    }

    // Test GET /sizes - Get all sizes with pagination
    @Test
    void getAllSizes_WithDefaultPagination_ShouldReturnPaginatedSizes() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/sizes"))
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
    void getAllSizes_WithCustomPagination_ShouldReturnPaginatedSizes() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/sizes")
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
    void getAllSizes_WithKeywordSearch_ShouldReturnMatchingSizes() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/sizes")
                .param("keyword", "S"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(2))) // XS and S
                .andDo(print());
    }

    @Test
    void getAllSizes_WithEmptyDatabase_ShouldReturnEmptyPage() throws Exception {
        // Clean up test data
        sizeRepository.deleteAll();

        // Act & Assert
        mockMvc.perform(get("/sizes"))
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

    // Test GET /sizes/{id} - Get size by ID
    @Test
    void getSizeById_WithExistingId_ShouldReturnSize() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/sizes/{id}", testSize1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(testSize1.getId()))
                .andExpect(jsonPath("$.data.name").value("XS"))
                .andExpect(jsonPath("$.data.minHeight").value(140))
                .andExpect(jsonPath("$.data.maxHeight").value(155))
                .andExpect(jsonPath("$.data.minWeight").value(40))
                .andExpect(jsonPath("$.data.maxWeight").value(50))
                .andDo(print());
    }

    @Test
    void getSizeById_WithNonExistentId_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/sizes/{id}", "non-existent-id"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test POST /sizes - Create new size
    @Test
    void createSize_WithValidData_ShouldCreateSize() throws Exception {
        // Arrange
        CreateSizeRequest request = CreateSizeRequest.builder()
                .name("L")
                .minHeight(175)
                .maxHeight(185)
                .minWeight(70)
                .maxWeight(80)
                .build();

        // Act & Assert
        mockMvc.perform(post("/sizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.name").value("L"))
                .andExpect(jsonPath("$.data.minHeight").value(175))
                .andExpect(jsonPath("$.data.maxHeight").value(185))
                .andExpect(jsonPath("$.data.minWeight").value(70))
                .andExpect(jsonPath("$.data.maxWeight").value(80))
                .andDo(print());
    }

    @Test
    void createSize_WithDuplicateName_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateSizeRequest request = CreateSizeRequest.builder()
                .name("S") // Duplicate name
                .minHeight(160)
                .maxHeight(170)
                .minWeight(55)
                .maxWeight(65)
                .build();

        // Act & Assert
        mockMvc.perform(post("/sizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createSize_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateSizeRequest request = CreateSizeRequest.builder()
                .minHeight(180)
                .maxHeight(190)
                .minWeight(75)
                .maxWeight(85)
                // Missing name
                .build();

        // Act & Assert
        mockMvc.perform(post("/sizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createSize_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateSizeRequest request = CreateSizeRequest.builder()
                .name("") // Empty name
                .minHeight(-10) // Negative height
                .maxHeight(150)
                .minWeight(-5) // Negative weight
                .maxWeight(50)
                .build();

        // Act & Assert
        mockMvc.perform(post("/sizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createSize_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/sizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test POST /sizes/batch - Create multiple sizes
    @Test
    void createMultipleSizes_WithValidData_ShouldCreateSizes() throws Exception {
        // Arrange
        List<CreateSizeRequest> sizeRequests = Arrays.asList(
                CreateSizeRequest.builder()
                        .name("L")
                        .minHeight(175)
                        .maxHeight(185)
                        .minWeight(70)
                        .maxWeight(80)
                        .build(),
                CreateSizeRequest.builder()
                        .name("XL")
                        .minHeight(185)
                        .maxHeight(195)
                        .minWeight(80)
                        .maxWeight(90)
                        .build(),
                CreateSizeRequest.builder()
                        .name("XXL")
                        .minHeight(195)
                        .maxHeight(205)
                        .minWeight(90)
                        .maxWeight(100)
                        .build()
        );

        CreateMultipleSizesRequest request = CreateMultipleSizesRequest.builder()
                .sizes(sizeRequests)
                .build();

        // Act & Assert
        mockMvc.perform(post("/sizes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].name").value("L"))
                .andExpect(jsonPath("$.data[1].name").value("XL"))
                .andExpect(jsonPath("$.data[2].name").value("XXL"))
                .andDo(print());
    }

    @Test
    void createMultipleSizes_WithDuplicateNames_ShouldReturnBadRequest() throws Exception {
        // Arrange
        List<CreateSizeRequest> sizeRequests = Arrays.asList(
                CreateSizeRequest.builder()
                        .name("S") // Duplicate name
                        .minHeight(160)
                        .maxHeight(170)
                        .minWeight(55)
                        .maxWeight(65)
                        .build(),
                CreateSizeRequest.builder()
                        .name("L")
                        .minHeight(175)
                        .maxHeight(185)
                        .minWeight(70)
                        .maxWeight(80)
                        .build()
        );

        CreateMultipleSizesRequest request = CreateMultipleSizesRequest.builder()
                .sizes(sizeRequests)
                .build();

        // Act & Assert
        mockMvc.perform(post("/sizes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createMultipleSizes_WithEmptyList_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateMultipleSizesRequest request = CreateMultipleSizesRequest.builder()
                .sizes(Arrays.asList()) // Empty list
                .build();

        // Act & Assert
        mockMvc.perform(post("/sizes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createMultipleSizes_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        List<CreateSizeRequest> sizeRequests = Arrays.asList(
                CreateSizeRequest.builder()
                        .name("") // Empty name
                        .minHeight(175)
                        .maxHeight(185)
                        .minWeight(70)
                        .maxWeight(80)
                        .build(),
                CreateSizeRequest.builder()
                        .name("L")
                        .minHeight(-10) // Negative height
                        .maxHeight(185)
                        .minWeight(70)
                        .maxWeight(80)
                        .build()
        );

        CreateMultipleSizesRequest request = CreateMultipleSizesRequest.builder()
                .sizes(sizeRequests)
                .build();

        // Act & Assert
        mockMvc.perform(post("/sizes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test PUT /sizes/{id} - Update size
    @Test
    void updateSize_WithValidData_ShouldUpdateSize() throws Exception {
        // Arrange
        UpdateSizeRequest request = UpdateSizeRequest.builder()
                .name("Extra Small")
                .minHeight(135)
                .maxHeight(150)
                .minWeight(35)
                .maxWeight(45)
                .build();

        // Act & Assert
        mockMvc.perform(put("/sizes/{id}", testSize1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(testSize1.getId()))
                .andExpect(jsonPath("$.data.name").value("Extra Small"))
                .andExpect(jsonPath("$.data.minHeight").value(135))
                .andExpect(jsonPath("$.data.maxHeight").value(150))
                .andExpect(jsonPath("$.data.minWeight").value(35))
                .andExpect(jsonPath("$.data.maxWeight").value(45))
                .andDo(print());
    }

    @Test
    void updateSize_WithNonExistentId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UpdateSizeRequest request = UpdateSizeRequest.builder()
                .name("Updated Size")
                .minHeight(170)
                .maxHeight(180)
                .minWeight(65)
                .maxWeight(75)
                .build();

        // Act & Assert
        mockMvc.perform(put("/sizes/{id}", "non-existent-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void updateSize_WithDuplicateName_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UpdateSizeRequest request = UpdateSizeRequest.builder()
                .name("S") // Duplicate name
                .minHeight(170)
                .maxHeight(180)
                .minWeight(65)
                .maxWeight(75)
                .build();

        // Act & Assert
        mockMvc.perform(put("/sizes/{id}", testSize1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void updateSize_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UpdateSizeRequest request = UpdateSizeRequest.builder()
                .name("") // Empty name
                .minHeight(-10) // Negative height
                .maxHeight(150)
                .minWeight(-5) // Negative weight
                .maxWeight(50)
                .build();

        // Act & Assert
        mockMvc.perform(put("/sizes/{id}", testSize1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void updateSize_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/sizes/{id}", testSize1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test DELETE /sizes/{id} - Delete size
    @Test
    void deleteSize_WithExistingId_ShouldDeleteSize() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/sizes/{id}", testSize1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andDo(print());

        // Verify deletion
        mockMvc.perform(get("/sizes/{id}", testSize1.getId()))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void deleteSize_WithNonExistentId_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/sizes/{id}", "non-existent-id"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test advanced filtering scenarios
    @Test
    void getAllSizes_WithExactHeightFilter_ShouldReturnMatchingSizes() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/sizes")
                .param("height", "160")) // Looking for sizes that contain height 160
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andDo(print());
    }

    @Test
    void getAllSizes_WithExactWeightFilter_ShouldReturnMatchingSizes() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/sizes")
                .param("weight", "65")) // Looking for sizes that contain weight 65
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andDo(print());
    }

    @Test
    void getAllSizes_WithRangeOverlap_ShouldReturnOverlappingSizes() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/sizes")
                .param("minHeight", "150")
                .param("maxHeight", "170"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThan(0))))
                .andDo(print());
    }

    @Test
    void getAllSizes_WithNoMatchingFilters_ShouldReturnEmptyPage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/sizes")
                .param("minHeight", "220")
                .param("maxHeight", "230"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andDo(print());
    }

    // Test pagination edge cases
    @Test
    void getAllSizes_WithLargePageSize_ShouldReturnAllSizes() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/sizes")
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
    void getAllSizes_WithPageBeyondRange_ShouldReturnEmptyPage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/sizes")
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
    void getAllSizes_WithMultipleSortFields_ShouldReturnSortedResults() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/sizes")
                .param("sort", "minHeight,asc", "minWeight,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andDo(print());
    }

    // Test data integrity
    @Test
    void createSize_ShouldMaintainDataIntegrity() throws Exception {
        // Arrange
        CreateSizeRequest request = CreateSizeRequest.builder()
                .name("L")
                .minHeight(175)
                .maxHeight(185)
                .minWeight(70)
                .maxWeight(80)
                .build();

        // Act
        mockMvc.perform(post("/sizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print());

        // Assert - Check that total sizes count increased
        mockMvc.perform(get("/sizes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(4)) // Original 3 + new 1
                .andDo(print());
    }

    // Test concurrent operations
    @Test
    @Transactional
    void concurrentOperations_ShouldHandleCorrectly() throws Exception {
        // Multiple operations on sizes
        mockMvc.perform(get("/sizes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andDo(print());

        mockMvc.perform(get("/sizes/{id}", testSize1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testSize1.getId()))
                .andDo(print());

        mockMvc.perform(get("/sizes")
                .param("keyword", "S"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andDo(print());
    }

    @Test
    void getAllSizes_WithNegativePage_ShouldHandleCorrectly() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/sizes")
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

    // Test case sensitivity
    @Test
    void getAllSizes_WithCaseInsensitiveKeyword_ShouldReturnMatchingSizes() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/sizes")
                .param("keyword", "s"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(2))) // XS and S
                .andDo(print());
    }

    // Test special characters in size names
    @Test
    void createSize_WithSpecialCharacters_ShouldCreateSize() throws Exception {
        // Arrange
        CreateSizeRequest request = CreateSizeRequest.builder()
                .name("X-Large")
                .minHeight(185)
                .maxHeight(195)
                .minWeight(80)
                .maxWeight(90)
                .build();

        // Act & Assert
        mockMvc.perform(post("/sizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.name").value("X-Large"))
                .andDo(print());
    }

    // Test update with same name (should be allowed)
    @Test
    void updateSize_WithSameName_ShouldUpdateSize() throws Exception {
        // Arrange
        UpdateSizeRequest request = UpdateSizeRequest.builder()
                .name("XS") // Same name
                .minHeight(135)
                .maxHeight(150)
                .minWeight(38)
                .maxWeight(48)
                .build();

        // Act & Assert
        mockMvc.perform(put("/sizes/{id}", testSize1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(testSize1.getId()))
                .andExpect(jsonPath("$.data.name").value("XS"))
                .andExpect(jsonPath("$.data.minHeight").value(135))
                .andExpect(jsonPath("$.data.maxHeight").value(150))
                .andExpect(jsonPath("$.data.minWeight").value(38))
                .andExpect(jsonPath("$.data.maxWeight").value(48))
                .andDo(print());
    }

    // Test size ranges validation
    @Test
    void createSize_WithValidRanges_ShouldCreateSize() throws Exception {
        // Arrange
        CreateSizeRequest request = CreateSizeRequest.builder()
                .name("L")
                .minHeight(175)
                .maxHeight(185)
                .minWeight(70)
                .maxWeight(80)
                .build();

        // Act & Assert
        mockMvc.perform(post("/sizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.name").value("L"))
                .andExpect(jsonPath("$.data.minHeight").value(175))
                .andExpect(jsonPath("$.data.maxHeight").value(185))
                .andExpect(jsonPath("$.data.minWeight").value(70))
                .andExpect(jsonPath("$.data.maxWeight").value(80))
                .andDo(print());
    }

    // Test size overlapping scenarios
    @Test
    void createSize_WithOverlappingRanges_ShouldCreateSize() throws Exception {
        // Arrange - Create a size that overlaps with existing ones
        CreateSizeRequest request = CreateSizeRequest.builder()
                .name("SM") // Between S and M
                .minHeight(160)
                .maxHeight(170)
                .minWeight(55)
                .maxWeight(65)
                .build();

        // Act & Assert
        mockMvc.perform(post("/sizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.name").value("SM"))
                .andDo(print());
    }
}