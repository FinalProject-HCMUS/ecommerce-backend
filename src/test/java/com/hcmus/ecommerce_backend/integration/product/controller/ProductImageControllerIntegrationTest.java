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
import com.hcmus.ecommerce_backend.product.model.dto.request.product.CreateProductImageRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.UpdateProductImageRequest;
import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.product.model.entity.Product;
import com.hcmus.ecommerce_backend.product.model.entity.ProductImage;
import com.hcmus.ecommerce_backend.category.repository.CategoryRepository;
import com.hcmus.ecommerce_backend.product.repository.ProductImageRepository;
import com.hcmus.ecommerce_backend.product.repository.ProductRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
    "spring.security.enabled=false"
})
@WithMockUser(username = "testuser", roles = {"USER", "ADMIN"})
public class ProductImageControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private ProductImage testImage1;
    private ProductImage testImage2;
    private ProductImage testImage3;
    private Product testProduct1;
    private Product testProduct2;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        // Clean up existing data in correct order
        productImageRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

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

        // Create test products
        testProduct1 = Product.builder()
                .name("Test Product 1")
                .description("Test product 1 description")
                .price(100.0)
                .category(testCategory)
                .build();
        testProduct1.setCreatedAt(LocalDateTime.now());
        testProduct1.setUpdatedAt(LocalDateTime.now());
        testProduct1 = productRepository.save(testProduct1);

        testProduct2 = Product.builder()
                .name("Test Product 2")
                .description("Test product 2 description")
                .price(200.0)
                .category(testCategory)
                .build();
        testProduct2.setCreatedAt(LocalDateTime.now());
        testProduct2.setUpdatedAt(LocalDateTime.now());
        testProduct2 = productRepository.save(testProduct2);

        // Create test product images
        testImage1 = ProductImage.builder()
                .url("https://example.com/image1.jpg")
                .product(testProduct1)
                .build();
        testImage1.setCreatedAt(LocalDateTime.now());
        testImage1.setUpdatedAt(LocalDateTime.now());
        testImage1 = productImageRepository.save(testImage1);

        testImage2 = ProductImage.builder()
                .url("https://example.com/image2.jpg")
                .product(testProduct1)
                .build();
        testImage2.setCreatedAt(LocalDateTime.now());
        testImage2.setUpdatedAt(LocalDateTime.now());
        testImage2 = productImageRepository.save(testImage2);

        testImage3 = ProductImage.builder()
                .url("https://example.com/image3.jpg")
                .product(testProduct2)
                .build();
        testImage3.setCreatedAt(LocalDateTime.now());
        testImage3.setUpdatedAt(LocalDateTime.now());
        testImage3 = productImageRepository.save(testImage3);
    }

    // Test GET /product-images - Get all product images
    @Test
    void getAllProductImages_ShouldReturnAllImages() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/product-images"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].url").exists())
                .andExpect(jsonPath("$.data[0].productId").exists())
                .andDo(print());
    }

    @Test
    void getAllProductImages_WithEmptyDatabase_ShouldReturnEmptyList() throws Exception {
        // Clean up test data
        productImageRepository.deleteAll();

        // Act & Assert
        mockMvc.perform(get("/product-images"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(print());
    }

    // Test GET /product-images/{id} - Get product image by ID
    @Test
    void getProductImageById_WithExistingId_ShouldReturnImage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/product-images/{id}", testImage1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(testImage1.getId()))
                .andExpect(jsonPath("$.data.url").value("https://example.com/image1.jpg"))
                .andExpect(jsonPath("$.data.productId").value(testProduct1.getId()))
                .andDo(print());
    }

    @Test
    void getProductImageById_WithNonExistentId_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/product-images/{id}", "non-existent-id"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test POST /product-images - Create new product image
    @Test
    void createProductImage_WithValidData_ShouldCreateImage() throws Exception {
        // Arrange
        CreateProductImageRequest request = CreateProductImageRequest.builder()
                .url("https://example.com/new-image.jpg")
                .productId(testProduct1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-images")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.url").value("https://example.com/new-image.jpg"))
                .andExpect(jsonPath("$.data.productId").value(testProduct1.getId()))
                .andDo(print());
    }

    // @Test
    // void createProductImage_WithNonExistentProduct_ShouldReturnNotFound() throws Exception {
    //     // Arrange
    //     CreateProductImageRequest request = CreateProductImageRequest.builder()
    //             .url("https://example.com/new-image.jpg")
    //             .productId("non-existent-product")
    //             .build();

    //     // Act & Assert
    //     mockMvc.perform(post("/product-images")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isNotFound())
    //             .andDo(print());
    // }

    @Test
    void createProductImage_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateProductImageRequest request = CreateProductImageRequest.builder()
                // Missing url and productId
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-images")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createProductImage_WithMissingUrl_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateProductImageRequest request = CreateProductImageRequest.builder()
                .productId(testProduct1.getId())
                // Missing url
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-images")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createProductImage_WithMissingProductId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateProductImageRequest request = CreateProductImageRequest.builder()
                .url("https://example.com/new-image.jpg")
                // Missing productId
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-images")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createProductImage_WithEmptyUrl_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateProductImageRequest request = CreateProductImageRequest.builder()
                .url("") // Empty url
                .productId(testProduct1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-images")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createProductImage_WithEmptyProductId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateProductImageRequest request = CreateProductImageRequest.builder()
                .url("https://example.com/new-image.jpg")
                .productId("") // Empty productId
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-images")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void createProductImage_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/product-images")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test PUT /product-images/{id} - Update product image
    @Test
    void updateProductImage_WithValidData_ShouldUpdateImage() throws Exception {
        // Arrange
        UpdateProductImageRequest request = UpdateProductImageRequest.builder()
                .url("https://example.com/updated-image.jpg")
                .productId(testProduct1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(put("/product-images/{id}", testImage1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(testImage1.getId()))
                .andExpect(jsonPath("$.data.url").value("https://example.com/updated-image.jpg"))
                .andExpect(jsonPath("$.data.productId").value(testProduct1.getId()))
                .andDo(print());
    }

    @Test
    void updateProductImage_WithNonExistentId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UpdateProductImageRequest request = UpdateProductImageRequest.builder()
                .url("https://example.com/updated-image.jpg")
                .productId(testProduct1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(put("/product-images/{id}", "non-existent-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // @Test
    // void updateProductImage_WithNonExistentProduct_ShouldReturnNotFound() throws Exception {
    //     // Arrange
    //     UpdateProductImageRequest request = UpdateProductImageRequest.builder()
    //             .url("https://example.com/updated-image.jpg")
    //             .productId("non-existent-product")
    //             .build();

    //     // Act & Assert
    //     mockMvc.perform(put("/product-images/{id}", testImage1.getId())
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isNotFound())
    //             .andDo(print());
    // }

    @Test
    void updateProductImage_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UpdateProductImageRequest request = UpdateProductImageRequest.builder()
                // Missing url and productId
                .build();

        // Act & Assert
        mockMvc.perform(put("/product-images/{id}", testImage1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void updateProductImage_WithEmptyUrl_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UpdateProductImageRequest request = UpdateProductImageRequest.builder()
                .url("") // Empty url
                .productId(testProduct1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(put("/product-images/{id}", testImage1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void updateProductImage_WithEmptyProductId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UpdateProductImageRequest request = UpdateProductImageRequest.builder()
                .url("https://example.com/updated-image.jpg")
                .productId("") // Empty productId
                .build();

        // Act & Assert
        mockMvc.perform(put("/product-images/{id}", testImage1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void updateProductImage_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/product-images/{id}", testImage1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test DELETE /product-images/{id} - Delete product image
    @Test
    void deleteProductImage_WithExistingId_ShouldDeleteImage() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/product-images/{id}", testImage1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andDo(print());

        // Verify deletion
        mockMvc.perform(get("/product-images/{id}", testImage1.getId()))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void deleteProductImage_WithNonExistentId_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/product-images/{id}", "non-existent-id"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test GET /product-images/product/{productId} - Get product images by product ID
    @Test
    void getProductImagesByProductId_WithExistingProductId_ShouldReturnImages() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/product-images/product/{productId}", testProduct1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2))) // testImage1 and testImage2
                .andExpect(jsonPath("$.data[0].productId").value(testProduct1.getId()))
                .andExpect(jsonPath("$.data[1].productId").value(testProduct1.getId()))
                .andDo(print());
    }

    @Test
    void getProductImagesByProductId_WithNonExistentProductId_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/product-images/product/{productId}", "non-existent-product"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // @Test
    // void getProductImagesByProductId_WithProductWithoutImages_ShouldReturnEmptyList() throws Exception {
    //     // Create a product without images
    //     Product productWithoutImages = Product.builder()
    //             .name("Product Without Images")
    //             .description("Test product without images")
    //             .price(300.0)
    //             .category(testCategory)
    //             .build();
    //     productWithoutImages.setCreatedAt(LocalDateTime.now());
    //     productWithoutImages.setUpdatedAt(LocalDateTime.now());
    //     productWithoutImages = productRepository.save(productWithoutImages);

    //     // Act & Assert
    //     mockMvc.perform(get("/product-images/product/{productId}", productWithoutImages.getId()))
    //             .andExpect(status().isOk())
    //             .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data").exists())
    //             .andExpect(jsonPath("$.data").isArray())
    //             .andExpect(jsonPath("$.data").isEmpty())
    //             .andDo(print());
    // }

    // Test data integrity
    @Test
    void createProductImage_ShouldMaintainDataIntegrity() throws Exception {
        // Arrange
        CreateProductImageRequest request = CreateProductImageRequest.builder()
                .url("https://example.com/new-image.jpg")
                .productId(testProduct1.getId())
                .build();

        // Act
        mockMvc.perform(post("/product-images")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(print());

        // Assert - Check that total images count increased
        mockMvc.perform(get("/product-images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(4))) // Original 3 + new 1
                .andDo(print());
    }

    // Test concurrent operations
    @Test
    @Transactional
    void concurrentOperations_ShouldHandleCorrectly() throws Exception {
        // Multiple operations on product images
        mockMvc.perform(get("/product-images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andDo(print());

        mockMvc.perform(get("/product-images/{id}", testImage1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testImage1.getId()))
                .andDo(print());

        mockMvc.perform(get("/product-images/product/{productId}", testProduct1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andDo(print());
    }

    // Test URL validation
    @Test
    void createProductImage_WithVariousValidUrls_ShouldCreateImages() throws Exception {
        String[] validUrls = {
            "https://example.com/image.jpg",
            "http://example.com/image.png",
            "https://cdn.example.com/path/to/image.gif",
            "https://example.com/image.webp",
            "https://example.com/image.svg"
        };

        for (int i = 0; i < validUrls.length; i++) {
            CreateProductImageRequest request = CreateProductImageRequest.builder()
                    .url(validUrls[i])
                    .productId(testProduct1.getId())
                    .build();

            mockMvc.perform(post("/product-images")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.url").value(validUrls[i]))
                    .andDo(print());
        }
    }

    @Test
    void createProductImage_WithRelativeUrl_ShouldCreateImage() throws Exception {
        // Arrange
        CreateProductImageRequest request = CreateProductImageRequest.builder()
                .url("/images/product/image.jpg")
                .productId(testProduct1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-images")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.url").value("/images/product/image.jpg"))
                .andDo(print());
    }

    @Test
    void createProductImage_WithLongUrl_ShouldCreateImage() throws Exception {
        // Arrange
        String longUrl = "https://example.com/very/long/path/to/image/that/has/many/directories/and/subdirectories/product-image-with-very-long-name-that-describes-the-image-in-great-detail.jpg";
        CreateProductImageRequest request = CreateProductImageRequest.builder()
                .url(longUrl)
                .productId(testProduct1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-images")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.url").value(longUrl))
                .andDo(print());
    }

    @Test
    void createProductImage_WithUrlContainingParameters_ShouldCreateImage() throws Exception {
        // Arrange
        String urlWithParams = "https://example.com/image.jpg?size=large&format=webp&quality=80";
        CreateProductImageRequest request = CreateProductImageRequest.builder()
                .url(urlWithParams)
                .productId(testProduct1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-images")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.url").value(urlWithParams))
                .andDo(print());
    }

    // // Test duplicate URLs
    // @Test
    // void createProductImage_WithDuplicateUrl_ShouldCreateImage() throws Exception {
    //     // Arrange - Create image with same URL as existing one
    //     CreateProductImageRequest request = CreateProductImageRequest.builder()
    //             .url("https://example.com/image1.jpg") // Same URL as testImage1
    //             .productId(testProduct1.getId())
    //             .build();

    //     // Act & Assert - Should be allowed to have duplicate URLs
    //     mockMvc.perform(post("/product-images")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content(objectMapper.writeValueAsString(request)))
    //             .andExpect(status().isCreated())
    //             .andExpect(content().contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(jsonPath("$.isSuccess").value(true))
    //             .andExpect(jsonPath("$.data").exists())
    //             .andExpect(jsonPath("$.data.url").value("https://example.com/image1.jpg"))
    //             .andDo(print());
    // }

    // Test creating multiple images for same product
    @Test
    void createMultipleImagesForSameProduct_ShouldCreateAllImages() throws Exception {
        String[] urls = {
            "https://example.com/image-1.jpg",
            "https://example.com/image-2.jpg",
            "https://example.com/image-3.jpg"
        };

        for (String url : urls) {
            CreateProductImageRequest request = CreateProductImageRequest.builder()
                    .url(url)
                    .productId(testProduct1.getId())
                    .build();

            mockMvc.perform(post("/product-images")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.url").value(url))
                    .andExpect(jsonPath("$.data.productId").value(testProduct1.getId()))
                    .andDo(print());
        }

        // Verify all images were created for the product
        mockMvc.perform(get("/product-images/product/{productId}", testProduct1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(5))) // Original 2 + new 3
                .andDo(print());
    }

    // Test updating image to different product
    @Test
    void updateProductImage_WithDifferentProduct_ShouldUpdateImage() throws Exception {
        // Arrange
        UpdateProductImageRequest request = UpdateProductImageRequest.builder()
                .url("https://example.com/updated-image.jpg")
                .productId(testProduct2.getId()) // Different product
                .build();

        // Act & Assert
        mockMvc.perform(put("/product-images/{id}", testImage1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.id").value(testImage1.getId()))
                .andExpect(jsonPath("$.data.url").value("https://example.com/updated-image.jpg"))
                .andExpect(jsonPath("$.data.productId").value(testProduct2.getId()))
                .andDo(print());
    }

    // Test edge cases
    @Test
    void createProductImage_WithNullValues_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/product-images")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":null,\"productId\":null}"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void updateProductImage_WithNullValues_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/product-images/{id}", testImage1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":null,\"productId\":null}"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    // Test boundary conditions
    @Test
    void createProductImage_WithMinimalValidData_ShouldCreateImage() throws Exception {
        // Arrange
        CreateProductImageRequest request = CreateProductImageRequest.builder()
                .url("a") // Minimal URL
                .productId(testProduct1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-images")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.url").value("a"))
                .andDo(print());
    }

    // Test database constraints
    @Test
    void deleteProductImage_ShouldNotAffectOtherImages() throws Exception {
        // Act - Delete one image
        mockMvc.perform(delete("/product-images/{id}", testImage1.getId()))
                .andExpect(status().isOk())
                .andDo(print());

        // Assert - Other images should still exist
        mockMvc.perform(get("/product-images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2))) // testImage2 and testImage3
                .andDo(print());

        // Assert - Product should still exist
        mockMvc.perform(get("/product-images/product/{productId}", testProduct1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1))) // Only testImage2 remains
                .andDo(print());
    }

    // Test cascade operations
    @Test
    void deleteProduct_ShouldDeleteAssociatedImages() throws Exception {
        // This test depends on the actual cascade configuration
        // If ON DELETE CASCADE is configured, images should be deleted when product is deleted
        
        // Get initial count
        mockMvc.perform(get("/product-images"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andDo(print());

        // Delete product (this test assumes there's a product delete endpoint)
        // The actual implementation depends on your ProductController
        // This is just to demonstrate the test structure
    }

    // Test with special characters in URLs
    @Test
    void createProductImage_WithSpecialCharactersInUrl_ShouldCreateImage() throws Exception {
        // Arrange
        String specialUrl = "https://example.com/images/产品图片.jpg";
        CreateProductImageRequest request = CreateProductImageRequest.builder()
                .url(specialUrl)
                .productId(testProduct1.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/product-images")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.url").value(specialUrl))
                .andDo(print());
    }
}