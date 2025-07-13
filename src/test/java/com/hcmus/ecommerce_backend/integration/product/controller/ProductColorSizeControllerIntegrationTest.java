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
import com.hcmus.ecommerce_backend.product.model.dto.request.product.CreateMultipleProductColorSizesRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.CreateProductColorSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.UpdateProductColorSizeRequest;
import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.product.model.entity.Color;
import com.hcmus.ecommerce_backend.product.model.entity.Product;
import com.hcmus.ecommerce_backend.product.model.entity.ProductColorSize;
import com.hcmus.ecommerce_backend.product.model.entity.Size;
import com.hcmus.ecommerce_backend.category.repository.CategoryRepository;
import com.hcmus.ecommerce_backend.product.repository.ColorRepository;
import com.hcmus.ecommerce_backend.product.repository.ProductColorSizeRepository;
import com.hcmus.ecommerce_backend.product.repository.ProductRepository;
import com.hcmus.ecommerce_backend.product.repository.SizeRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
    "spring.security.enabled=false"
})
@WithMockUser(username = "testuser", roles = {"USER", "ADMIN"})
public class ProductColorSizeControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductColorSizeRepository productColorSizeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private SizeRepository sizeRepository;

    private ProductColorSize testPcs1;
    private ProductColorSize testPcs2;
    private ProductColorSize testPcs3;
    private Product testProduct1;
    private Product testProduct2;
    private Category testCategory;
    private Color testColor1;
    private Color testColor2;
    private Size testSize1;
    private Size testSize2;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        // Clean up existing data in correct order
        productColorSizeRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        colorRepository.deleteAll();
        sizeRepository.deleteAll();

        setupTestData();
    }

    private void setupTestData() {
        // Create test category
        testCategory = Category.builder()
                .name("Test Category")
                .description("Test category description")
                .build();
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());
        testCategory = categoryRepository.save(testCategory);

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

        // Create test sizes
        testSize1 = Size.builder()
                .name("M")
                .minHeight(165)
                .maxHeight(175)
                .minWeight(60)
                .maxWeight(70)
                .build();
        testSize1.setCreatedAt(LocalDateTime.now());
        testSize1.setUpdatedAt(LocalDateTime.now());
        testSize1 = sizeRepository.save(testSize1);

        testSize2 = Size.builder()
                .name("L")
                .minHeight(175)
                .maxHeight(185)
                .minWeight(70)
                .maxWeight(80)
                .build();
        testSize2.setCreatedAt(LocalDateTime.now());
        testSize2.setUpdatedAt(LocalDateTime.now());
        testSize2 = sizeRepository.save(testSize2);

        // Create test products
        testProduct1 = Product.builder()
                .name("Test Product 1")
                .description("Test product 1 description")
                .price(100.0)
                .category(testCategory)
                .enable(true)
                .build();
        testProduct1.setCreatedAt(LocalDateTime.now());
        testProduct1.setUpdatedAt(LocalDateTime.now());
        testProduct1 = productRepository.save(testProduct1);

        testProduct2 = Product.builder()
                .name("Test Product 2")
                .description("Test product 2 description")
                .price(200.0)
                .category(testCategory)
                .enable(true)
                .build();
        testProduct2.setCreatedAt(LocalDateTime.now());
        testProduct2.setUpdatedAt(LocalDateTime.now());
        testProduct2 = productRepository.save(testProduct2);

        // Create test product color sizes
        testPcs1 = ProductColorSize.builder()
                .product(testProduct1)
                .color(testColor1)
                .size(testSize1)
                .quantity(10)
                .build();
        testPcs1.setCreatedAt(LocalDateTime.now());
        testPcs1.setUpdatedAt(LocalDateTime.now());
        testPcs1 = productColorSizeRepository.save(testPcs1);

        testPcs2 = ProductColorSize.builder()
                .product(testProduct1)
                .color(testColor2)
                .size(testSize2)
                .quantity(5)
                .build();
        testPcs2.setCreatedAt(LocalDateTime.now());
        testPcs2.setUpdatedAt(LocalDateTime.now());
        testPcs2 = productColorSizeRepository.save(testPcs2);

        testPcs3 = ProductColorSize.builder()
                .product(testProduct2)
                .color(testColor1)
                .size(testSize1)
                .quantity(0) // Out of stock
                .build();
        testPcs3.setCreatedAt(LocalDateTime.now());
        testPcs3.setUpdatedAt(LocalDateTime.now());
        testPcs3 = productColorSizeRepository.save(testPcs3);
    }

    // // Test GET /product-color-sizes - Get all product color sizes with pagination
    // @Test
    // void getAllProductColorSizes_WithDefaultPagination_ShouldReturnPaginatedResults() throws Exception {
    //     // Act & Assert
    //     mockMvc.perform(get("/product-color-sizes"))
    //             .andExpect(status().isOk())
    //             .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data").exists())
    //             .andExpect(jsonPath("$.data.content").isArray())
    //             .andExpect(jsonPath("$.data.content", hasSize(3)))
    //             .andExpect(jsonPath("$.data.totalElements").value(3))
    //             .andExpect(jsonPath("$.data.totalPages").value(1))
    //             .andExpect(jsonPath("$.data.size").value(10))
    //             .andExpect(jsonPath("$.data.number").value(0))
    //             .andExpect(jsonPath("$.data.content[0].productId").exists())
    //             .andExpect(jsonPath("$.data.content[0].colorId").exists())
    //             .andExpect(jsonPath("$.data.content[0].sizeId").exists())
    //             .andExpect(jsonPath("$.data.content[0].quantity").exists())
    //             .andDo(print());
    // }

    @Test
    void getAllProductColorSizes_WithCustomPagination_ShouldReturnPaginatedResults() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/product-color-sizes")
                .param("page", "0")
                .param("size", "2")
                .param("sort", "quantity,desc"))
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
    void getAllProductColorSizes_WithEmptyDatabase_ShouldReturnEmptyPage() throws Exception {
        // Clean up test data
        productColorSizeRepository.deleteAll();

        // Act & Assert
        mockMvc.perform(get("/product-color-sizes"))
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

    // Test GET /product-color-sizes/{id} - Get product color size by ID
    // @Test
    // void getProductColorSizeById_WithExistingId_ShouldReturnProductColorSize() throws Exception {
    //     // Act & Assert
    //     mockMvc.perform(get("/product-color-sizes/{id}", testPcs1.getId()))
    //             .andExpect(status().isOk())
    //             .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data").exists())
    //             .andExpect(jsonPath("$.data.id").value(testPcs1.getId()))
    //             .andExpect(jsonPath("$.data.productId").value(testProduct1.getId()))
    //             .andExpect(jsonPath("$.data.colorId").value(testColor1.getId()))
    //             .andExpect(jsonPath("$.data.sizeId").value(testSize1.getId()))
    //             .andExpect(jsonPath("$.data.quantity").value(10))
    //             .andDo(print());
    // }

    // @Test
    // void getProductColorSizeById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
    //     // Act & Assert
    //     mockMvc.perform(get("/product-color-sizes/{id}", "non-existent-id"))
    //             .andExpect(status().isNotFound())
    //             .andDo(print());
    // }

    // Test POST /product-color-sizes - Create new product color size
    // @Test
    // void createProductColorSize_WithValidData_ShouldCreateProductColorSize() throws Exception {
    //     // Arrange
    //     CreateProductColorSizeRequest request = CreateProductColorSizeRequest.builder()
    //             .productId(testProduct2.getId())
    //             .colorId(testColor2.getId())
    //             .sizeId(testSize2.getId())
    //             .quantity(15)
    //             .build();

    //     // Act & Assert
    //     mockMvc.perform(post("/product-color-sizes")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isCreated())
    //             .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data").exists())
    //             .andExpect(jsonPath("$.data.productId").value(testProduct2.getId()))
    //             .andExpect(jsonPath("$.data.colorId").value(testColor2.getId()))
    //             .andExpect(jsonPath("$.data.sizeId").value(testSize2.getId()))
    //             .andExpect(jsonPath("$.data.quantity").value(15))
    //             .andDo(print());
    // }

    @Test
    void createProductColorSize_WithDuplicateEntry_ShouldReturnBadRequest() throws Exception {
        // Arrange - Create duplicate of existing entry
        CreateProductColorSizeRequest request = CreateProductColorSizeRequest.builder()
                .productId(testProduct1.getId())
                .colorId(testColor1.getId())
                .sizeId(testSize1.getId())
                .quantity(20)
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-color-sizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // @Test
    // void createProductColorSize_WithNonExistentProduct_ShouldReturnNotFound() throws Exception {
    //     // Arrange
    //     CreateProductColorSizeRequest request = CreateProductColorSizeRequest.builder()
    //             .productId("non-existent-product")
    //             .colorId(testColor1.getId())
    //             .sizeId(testSize1.getId())
    //             .quantity(10)
    //             .build();

    //     // Act & Assert
    //     mockMvc.perform(post("/product-color-sizes")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isNotFound())
    //             .andDo(print());
    // }

    // @Test
    // void createProductColorSize_WithNonExistentColor_ShouldReturnNotFound() throws Exception {
    //     // Arrange
    //     CreateProductColorSizeRequest request = CreateProductColorSizeRequest.builder()
    //             .productId(testProduct1.getId())
    //             .colorId("non-existent-color")
    //             .sizeId(testSize1.getId())
    //             .quantity(10)
    //             .build();

    //     // Act & Assert
    //     mockMvc.perform(post("/product-color-sizes")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isNotFound())
    //             .andDo(print());
    // }

    // @Test
    // void createProductColorSize_WithNonExistentSize_ShouldReturnNotFound() throws Exception {
    //     // Arrange
    //     CreateProductColorSizeRequest request = CreateProductColorSizeRequest.builder()
    //             .productId(testProduct1.getId())
    //             .colorId(testColor1.getId())
    //             .sizeId("non-existent-size")
    //             .quantity(10)
    //             .build();

    //     // Act & Assert
    //     mockMvc.perform(post("/product-color-sizes")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isNotFound())
    //             .andDo(print());
    // }

    @Test
    void createProductColorSize_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateProductColorSizeRequest request = CreateProductColorSizeRequest.builder()
                .quantity(10)
                // Missing productId, colorId, sizeId
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-color-sizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createProductColorSize_WithNegativeQuantity_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateProductColorSizeRequest request = CreateProductColorSizeRequest.builder()
                .productId(testProduct1.getId())
                .colorId(testColor1.getId())
                .sizeId(testSize1.getId())
                .quantity(-5) // Negative quantity
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-color-sizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createProductColorSize_WithZeroQuantity_ShouldCreateProductColorSize() throws Exception {
        // Arrange
        CreateProductColorSizeRequest request = CreateProductColorSizeRequest.builder()
                .productId(testProduct2.getId())
                .colorId(testColor2.getId())
                .sizeId(testSize2.getId())
                .quantity(0) // Zero quantity is allowed
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-color-sizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.quantity").value(0))
                .andDo(print());
    }

    @Test
    void createProductColorSize_WithEmptyIds_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateProductColorSizeRequest request = CreateProductColorSizeRequest.builder()
                .productId("")
                .colorId("")
                .sizeId("")
                .quantity(10)
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-color-sizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createProductColorSize_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/product-color-sizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test POST /product-color-sizes/batch - Create multiple product color sizes
    @Test
    void createMultipleProductColorSizes_WithValidData_ShouldCreateAllProductColorSizes() throws Exception {
        // Arrange
        List<CreateProductColorSizeRequest> requests = Arrays.asList(
                CreateProductColorSizeRequest.builder()
                        .productId(testProduct2.getId())
                        .colorId(testColor2.getId())
                        .sizeId(testSize2.getId())
                        .quantity(10)
                        .build(),
                CreateProductColorSizeRequest.builder()
                        .productId(testProduct1.getId())
                        .colorId(testColor1.getId())
                        .sizeId(testSize2.getId())
                        .quantity(5)
                        .build()
        );

        CreateMultipleProductColorSizesRequest batchRequest = CreateMultipleProductColorSizesRequest.builder()
                .productColorSizes(requests)
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-color-sizes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].quantity").value(10))
                .andExpect(jsonPath("$.data[1].quantity").value(5))
                .andDo(print());
    }

    @Test
    void createMultipleProductColorSizes_WithEmptyList_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateMultipleProductColorSizesRequest batchRequest = CreateMultipleProductColorSizesRequest.builder()
                .productColorSizes(Arrays.asList())
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-color-sizes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createMultipleProductColorSizes_WithDuplicateEntries_ShouldReturnBadRequest() throws Exception {
        // Arrange - Include duplicate of existing entry
        List<CreateProductColorSizeRequest> requests = Arrays.asList(
                CreateProductColorSizeRequest.builder()
                        .productId(testProduct1.getId())
                        .colorId(testColor1.getId())
                        .sizeId(testSize1.getId()) // This already exists
                        .quantity(10)
                        .build()
        );

        CreateMultipleProductColorSizesRequest batchRequest = CreateMultipleProductColorSizesRequest.builder()
                .productColorSizes(requests)
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-color-sizes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createMultipleProductColorSizes_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        List<CreateProductColorSizeRequest> requests = Arrays.asList(
                CreateProductColorSizeRequest.builder()
                        .productId("") // Empty productId
                        .colorId(testColor1.getId())
                        .sizeId(testSize1.getId())
                        .quantity(10)
                        .build()
        );

        CreateMultipleProductColorSizesRequest batchRequest = CreateMultipleProductColorSizesRequest.builder()
                .productColorSizes(requests)
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-color-sizes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test PUT /product-color-sizes/{id} - Update product color size
    // @Test
    // void updateProductColorSize_WithValidData_ShouldUpdateProductColorSize() throws Exception {
    //     // Arrange
    //     UpdateProductColorSizeRequest request = UpdateProductColorSizeRequest.builder()
    //             .productId(testProduct1.getId())
    //             .colorId(testColor1.getId())
    //             .sizeId(testSize1.getId())
    //             .quantity(25)
    //             .build();

    //     // Act & Assert
    //     mockMvc.perform(put("/product-color-sizes/{id}", testPcs1.getId())
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isOk())
    //             .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data").exists())
    //             .andExpect(jsonPath("$.data.id").value(testPcs1.getId()))
    //             .andExpect(jsonPath("$.data.productId").value(testProduct1.getId()))
    //             .andExpect(jsonPath("$.data.colorId").value(testColor1.getId()))
    //             .andExpect(jsonPath("$.data.sizeId").value(testSize1.getId()))
    //             .andExpect(jsonPath("$.data.quantity").value(25))
    //             .andDo(print());
    // }

    @Test
    void updateProductColorSize_WithNonExistentId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UpdateProductColorSizeRequest request = UpdateProductColorSizeRequest.builder()
                .productId(testProduct1.getId())
                .colorId(testColor1.getId())
                .sizeId(testSize1.getId())
                .quantity(25)
                .build();

        // Act & Assert
        mockMvc.perform(put("/product-color-sizes/{id}", "non-existent-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void updateProductColorSize_WithDuplicateEntry_ShouldReturnBadRequest() throws Exception {
        // Arrange - Try to update to match existing combination
        UpdateProductColorSizeRequest request = UpdateProductColorSizeRequest.builder()
                .productId(testProduct1.getId())
                .colorId(testColor2.getId())
                .sizeId(testSize2.getId()) // This combination already exists in testPcs2
                .quantity(15)
                .build();

        // Act & Assert
        mockMvc.perform(put("/product-color-sizes/{id}", testPcs1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // @Test
    // void updateProductColorSize_WithNonExistentProduct_ShouldReturnNotFound() throws Exception {
    //     // Arrange
    //     UpdateProductColorSizeRequest request = UpdateProductColorSizeRequest.builder()
    //             .productId("non-existent-product")
    //             .colorId(testColor1.getId())
    //             .sizeId(testSize1.getId())
    //             .quantity(25)
    //             .build();

    //     // Act & Assert
    //     mockMvc.perform(put("/product-color-sizes/{id}", testPcs1.getId())
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isNotFound())
    //             .andDo(print());
    // }

    @Test
    void updateProductColorSize_WithInvalidQuantity_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UpdateProductColorSizeRequest request = UpdateProductColorSizeRequest.builder()
                .productId(testProduct1.getId())
                .colorId(testColor1.getId())
                .sizeId(testSize1.getId())
                .quantity(0) // Invalid quantity for update (must be at least 1)
                .build();

        // Act & Assert
        mockMvc.perform(put("/product-color-sizes/{id}", testPcs1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void updateProductColorSize_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UpdateProductColorSizeRequest request = UpdateProductColorSizeRequest.builder()
                .quantity(25)
                // Missing productId, colorId, sizeId
                .build();

        // Act & Assert
        mockMvc.perform(put("/product-color-sizes/{id}", testPcs1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void updateProductColorSize_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/product-color-sizes/{id}", testPcs1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test DELETE /product-color-sizes/{id} - Delete product color size
    @Test
    void deleteProductColorSize_WithExistingId_ShouldDeleteProductColorSize() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/product-color-sizes/{id}", testPcs1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andDo(print());

        // Verify deletion
        mockMvc.perform(get("/product-color-sizes/{id}", testPcs1.getId()))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void deleteProductColorSize_WithNonExistentId_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/product-color-sizes/{id}", "non-existent-id"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test GET /product-color-sizes/product/{productId} - Get product color sizes by product ID
    // @Test
    // void getProductColorSizesByProductId_WithExistingProductId_ShouldReturnProductColorSizes() throws Exception {
    //     // Act & Assert
    //     mockMvc.perform(get("/product-color-sizes/product/{productId}", testProduct1.getId()))
    //             .andExpect(status().isOk())
    //             .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data").exists())
    //             .andExpect(jsonPath("$.data").isArray())
    //             .andExpect(jsonPath("$.data", hasSize(2))) // testPcs1 and testPcs2
    //             .andExpect(jsonPath("$.data[0].productId").value(testProduct1.getId()))
    //             .andExpect(jsonPath("$.data[1].productId").value(testProduct1.getId()))
    //             .andDo(print());
    // }

    // @Test
    // void getProductColorSizesByProductId_WithNonExistentProductId_ShouldReturnNotFound() throws Exception {
    //     // Act & Assert
    //     mockMvc.perform(get("/product-color-sizes/product/{productId}", "non-existent-product"))
    //             .andExpect(status().isNotFound())
    //             .andDo(print());
    // }

    @Test
    void getProductColorSizesByProductId_WithProductWithoutColorSizes_ShouldReturnEmptyList() throws Exception {
        // Create a product without color sizes
        Product productWithoutColorSizes = Product.builder()
                .name("Product Without Color Sizes")
                .description("Test product without color sizes")
                .price(300.0)
                .category(testCategory)
                .enable(true)
                .build();
        productWithoutColorSizes.setCreatedAt(LocalDateTime.now());
        productWithoutColorSizes.setUpdatedAt(LocalDateTime.now());
        productWithoutColorSizes = productRepository.save(productWithoutColorSizes);

        // Act & Assert
        mockMvc.perform(get("/product-color-sizes/product/{productId}", productWithoutColorSizes.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());
    }

    // Test data integrity
    @Test
    void createProductColorSize_ShouldMaintainDataIntegrity() throws Exception {
        // Arrange
        CreateProductColorSizeRequest request = CreateProductColorSizeRequest.builder()
                .productId(testProduct2.getId())
                .colorId(testColor2.getId())
                .sizeId(testSize2.getId())
                .quantity(20)
                .build();

        // Act
        mockMvc.perform(post("/product-color-sizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print());

        // Assert - Check that total product color sizes count increased
        mockMvc.perform(get("/product-color-sizes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(4)) // Original 3 + new 1
                .andDo(print());
    }

    // Test concurrent operations
    @Test
    @Transactional
    void concurrentOperations_ShouldHandleCorrectly() throws Exception {
        // Multiple operations on product color sizes
        mockMvc.perform(get("/product-color-sizes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andDo(print());

        mockMvc.perform(get("/product-color-sizes/{id}", testPcs1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testPcs1.getId()))
                .andDo(print());

        mockMvc.perform(get("/product-color-sizes/product/{productId}", testProduct1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andDo(print());
    }

    // Test boundary values
    @Test
    void createProductColorSize_WithMaxQuantity_ShouldCreateProductColorSize() throws Exception {
        // Arrange
        CreateProductColorSizeRequest request = CreateProductColorSizeRequest.builder()
                .productId(testProduct2.getId())
                .colorId(testColor2.getId())
                .sizeId(testSize2.getId())
                .quantity(Integer.MAX_VALUE)
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-color-sizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.quantity").value(Integer.MAX_VALUE))
                .andDo(print());
    }

    // Test edge cases
    // @Test
    // void createProductColorSize_WithDifferentProductSameColorSize_ShouldCreateProductColorSize() throws Exception {
    //     // Arrange - Same color and size but different product
    //     CreateProductColorSizeRequest request = CreateProductColorSizeRequest.builder()
    //             .productId(testProduct2.getId()) // Different product
    //             .colorId(testColor1.getId()) // Same color as testPcs1
    //             .sizeId(testSize1.getId()) // Same size as testPcs1
    //             .quantity(10)
    //             .build();

    //     // Act & Assert
    //     mockMvc.perform(post("/product-color-sizes")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isCreated())
    //             .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data").exists())
    //             .andExpect(jsonPath("$.data.productId").value(testProduct2.getId()))
    //             .andExpect(jsonPath("$.data.colorId").value(testColor1.getId()))
    //             .andExpect(jsonPath("$.data.sizeId").value(testSize1.getId()))
    //             .andDo(print());
    // }

    // Test sorting
    @Test
    void getAllProductColorSizes_WithMultipleSortFields_ShouldReturnSortedResults() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/product-color-sizes")
                .param("sort", "product.name,asc", "quantity,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andDo(print());
    }

    // Test pagination edge cases
    @Test
    void getAllProductColorSizes_WithPageBeyondRange_ShouldReturnEmptyPage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/product-color-sizes")
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

    // Test null values handling
    @Test
    void createProductColorSize_WithNullValues_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/product-color-sizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":null,\"colorId\":null,\"sizeId\":null,\"quantity\":10}"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void updateProductColorSize_WithNullValues_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/product-color-sizes/{id}", testPcs1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":null,\"colorId\":null,\"sizeId\":null,\"quantity\":10}"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test database constraints
    @Test
    void deleteProductColorSize_ShouldNotAffectOtherEntries() throws Exception {
        // Act - Delete one entry
        mockMvc.perform(delete("/product-color-sizes/{id}", testPcs1.getId()))
                .andExpect(status().isOk())
                .andDo(print());

        // Assert - Other entries should still exist
        mockMvc.perform(get("/product-color-sizes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2)) // testPcs2 and testPcs3
                .andDo(print());

        // Assert - Product should still exist
        mockMvc.perform(get("/product-color-sizes/product/{productId}", testProduct1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1))) // Only testPcs2 remains
                .andDo(print());
    }

    // Test business logic
    @Test
    void createProductColorSize_WithLargeQuantity_ShouldCreateProductColorSize() throws Exception {
        // Arrange
        CreateProductColorSizeRequest request = CreateProductColorSizeRequest.builder()
                .productId(testProduct2.getId())
                .colorId(testColor2.getId())
                .sizeId(testSize2.getId())
                .quantity(99999)
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-color-sizes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.quantity").value(99999))
                .andDo(print());
    }

    @Test
    void updateProductColorSize_WithSameData_ShouldUpdateProductColorSize() throws Exception {
        // Arrange - Update with same values
        UpdateProductColorSizeRequest request = UpdateProductColorSizeRequest.builder()
                .productId(testProduct1.getId())
                .colorId(testColor1.getId())
                .sizeId(testSize1.getId())
                .quantity(10) // Same quantity
                .build();

        // Act & Assert
        mockMvc.perform(put("/product-color-sizes/{id}", testPcs1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(testPcs1.getId()))
                .andExpect(jsonPath("$.data.quantity").value(10))
                .andDo(print());
    }
}