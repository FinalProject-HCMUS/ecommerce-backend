package com.hcmus.ecommerce_backend.unit.product.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.category.repository.CategoryRepository;
import com.hcmus.ecommerce_backend.product.exception.ProductColorSizeAlreadyExistsException;
import com.hcmus.ecommerce_backend.product.exception.ProductColorSizeNotFoundException;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.CreateProductColorSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.UpdateProductColorSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ColorResponse;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductColorSizeResponse;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductResponse;
import com.hcmus.ecommerce_backend.product.model.dto.response.SizeResponse;
import com.hcmus.ecommerce_backend.product.model.entity.Color;
import com.hcmus.ecommerce_backend.product.model.entity.Product;
import com.hcmus.ecommerce_backend.product.model.entity.ProductColorSize;
import com.hcmus.ecommerce_backend.product.model.entity.Size;
import com.hcmus.ecommerce_backend.product.model.mapper.ProductColorSizeMapper;
import com.hcmus.ecommerce_backend.product.repository.ProductColorSizeRepository;
import com.hcmus.ecommerce_backend.product.repository.ProductRepository;
import com.hcmus.ecommerce_backend.product.service.impl.ProductColorSizeServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ProductColorSizeServiceImplTest {

    @Mock
    private ProductColorSizeRepository productColorSizeRepository;

    @Mock
    private ProductColorSizeMapper productColorSizeMapper;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductColorSizeServiceImpl productColorSizeService;

    private ProductColorSize productColorSize;
    private ProductColorSizeResponse productColorSizeResponse;
    private CreateProductColorSizeRequest createProductColorSizeRequest;
    private UpdateProductColorSizeRequest updateProductColorSizeRequest;
    private Product product;
    private Color color;
    private Size size;
    private Category category;
    private Pageable pageable;
    private ProductResponse productResponse;
    private ColorResponse colorResponse;
    private SizeResponse sizeResponse;

    @BeforeEach
    void setUp() {
        // Setup Category
        category = new Category();
        category.setId("category-1");
        category.setName("Test Category");
        category.setStock(100);

        // Setup Product
        product = new Product();
        product.setId("product-1");
        product.setName("Test Product");
        product.setTotal(50);
        product.setCategory(category);

        // Setup Color
        color = new Color();
        color.setId("color-1");
        color.setName("Red");

        // Setup Size
        size = new Size();
        size.setId("size-1");
        size.setName("Medium");

        // Setup ProductColorSize entity
        productColorSize = new ProductColorSize();
        productColorSize.setId("pcs-1");
        productColorSize.setProduct(product);
        productColorSize.setColor(color);
        productColorSize.setSize(size);
        productColorSize.setQuantity(10);

        // Setup nested response objects
        productResponse = ProductResponse.builder()
                .id("product-1")
                .name("Test Product")
                .description("Test Description")
                .build();
            
        colorResponse = ColorResponse.builder()
                .id("color-1")
                .name("Red")
                .code("#FF0000")
                .build();

        sizeResponse = SizeResponse.builder()
                .id("size-1")
                .name("Medium")
                .minHeight(150)
                .build();

        // Setup ProductColorSizeResponse with nested objects
        productColorSizeResponse = ProductColorSizeResponse.builder()
                .id("pcs-1")
                .product(productResponse)
                .color(colorResponse)
                .size(sizeResponse)
                .quantity(10)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Setup CreateProductColorSizeRequest
        createProductColorSizeRequest = CreateProductColorSizeRequest.builder()
                .productId("product-1")
                .colorId("color-1")
                .sizeId("size-1")
                .quantity(10)
                .build();

        // Setup UpdateProductColorSizeRequest
        updateProductColorSizeRequest = UpdateProductColorSizeRequest.builder()
                .colorId("color-1")
                .sizeId("size-1")
                .quantity(15)
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAllProductColorSizes_Success() {
        // Given
        List<ProductColorSize> productColorSizes = Arrays.asList(productColorSize);
        Page<ProductColorSize> productColorSizePage = new PageImpl<>(productColorSizes, pageable, 1);
        when(productColorSizeRepository.findAll(pageable)).thenReturn(productColorSizePage);
        when(productColorSizeMapper.toResponse(productColorSize)).thenReturn(productColorSizeResponse);

        // When
        Page<ProductColorSizeResponse> result = productColorSizeService.getAllProductColorSizes(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(productColorSizeResponse, result.getContent().get(0));
        verify(productColorSizeRepository).findAll(pageable);
        verify(productColorSizeMapper).toResponse(productColorSize);
    }

    @Test
    void getAllProductColorSizes_EmptyResult() {
        // Given
        Page<ProductColorSize> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(productColorSizeRepository.findAll(pageable)).thenReturn(emptyPage);

        // When
        Page<ProductColorSizeResponse> result = productColorSizeService.getAllProductColorSizes(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(productColorSizeRepository).findAll(pageable);
    }

    @Test
    void getAllProductColorSizes_DatabaseError_ReturnsEmptyPage() {
        // Given
        when(productColorSizeRepository.findAll(pageable)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When
        Page<ProductColorSizeResponse> result = productColorSizeService.getAllProductColorSizes(pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productColorSizeRepository).findAll(pageable);
    }

    @Test
    void getAllProductColorSizes_UnexpectedError_ThrowsException() {
        // Given
        when(productColorSizeRepository.findAll(pageable)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productColorSizeService.getAllProductColorSizes(pageable));
        verify(productColorSizeRepository).findAll(pageable);
    }

    @Test
    void getProductColorSizeById_Success() {
        // Given
        String id = "pcs-1";
        when(productColorSizeRepository.findById(id)).thenReturn(Optional.of(productColorSize));
        when(productColorSizeMapper.toResponse(productColorSize)).thenReturn(productColorSizeResponse);

        // When
        ProductColorSizeResponse result = productColorSizeService.getProductColorSizeById(id);

        // Then
        assertNotNull(result);
        assertEquals(productColorSizeResponse, result);
        assertEquals("pcs-1", result.getId());
        assertEquals(10, result.getQuantity());
        assertNotNull(result.getProduct());
        assertNotNull(result.getColor());
        assertNotNull(result.getSize());
        verify(productColorSizeRepository).findById(id);
        verify(productColorSizeMapper).toResponse(productColorSize);
    }

    @Test
    void getProductColorSizeById_NotFound() {
        // Given
        String id = "non-existent";
        when(productColorSizeRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductColorSizeNotFoundException.class, () -> productColorSizeService.getProductColorSizeById(id));
        verify(productColorSizeRepository).findById(id);
    }

    @Test
    void getProductColorSizeById_DatabaseError() {
        // Given
        String id = "pcs-1";
        when(productColorSizeRepository.findById(id)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> productColorSizeService.getProductColorSizeById(id));
        verify(productColorSizeRepository).findById(id);
    }

    @Test
    void getProductColorSizeById_UnexpectedError() {
        // Given
        String id = "pcs-1";
        when(productColorSizeRepository.findById(id)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productColorSizeService.getProductColorSizeById(id));
        verify(productColorSizeRepository).findById(id);
    }

    @Test
    void createProductColorSize_Success() {
        // Given
        when(productColorSizeRepository.existsByProductIdAndColorIdAndSizeId("product-1", "color-1", "size-1"))
                .thenReturn(false);
        when(productColorSizeMapper.toEntity(createProductColorSizeRequest)).thenReturn(productColorSize);
        when(productColorSizeRepository.save(productColorSize)).thenReturn(productColorSize);
        when(productColorSizeMapper.toResponse(productColorSize)).thenReturn(productColorSizeResponse);

        // When
        ProductColorSizeResponse result = productColorSizeService.createProductColorSize(createProductColorSizeRequest);

        // Then
        assertNotNull(result);
        assertEquals(productColorSizeResponse, result);
        verify(productColorSizeRepository).existsByProductIdAndColorIdAndSizeId("product-1", "color-1", "size-1");
        verify(productColorSizeMapper).toEntity(createProductColorSizeRequest);
        verify(productColorSizeRepository).save(productColorSize);
        verify(productColorSizeMapper).toResponse(productColorSize);
    }

    @Test
    void createProductColorSize_AlreadyExists() {
        // Given
        when(productColorSizeRepository.existsByProductIdAndColorIdAndSizeId("product-1", "color-1", "size-1"))
                .thenReturn(true);

        // When & Then
        assertThrows(ProductColorSizeAlreadyExistsException.class, 
                () -> productColorSizeService.createProductColorSize(createProductColorSizeRequest));
        verify(productColorSizeRepository).existsByProductIdAndColorIdAndSizeId("product-1", "color-1", "size-1");
        verify(productColorSizeRepository, never()).save(any());
    }

    @Test
    void createProductColorSize_DatabaseError() {
        // Given
        when(productColorSizeRepository.existsByProductIdAndColorIdAndSizeId("product-1", "color-1", "size-1"))
                .thenReturn(false);
        when(productColorSizeMapper.toEntity(createProductColorSizeRequest)).thenReturn(productColorSize);
        when(productColorSizeRepository.save(productColorSize)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> productColorSizeService.createProductColorSize(createProductColorSizeRequest));
        verify(productColorSizeRepository).save(productColorSize);
    }

    @Test
    void createMultipleProductColorSizes_Success() {
        // Given
        CreateProductColorSizeRequest request2 = CreateProductColorSizeRequest.builder()
                .productId("product-1")
                .colorId("color-2")
                .sizeId("size-1")
                .quantity(5)
                .build();

        ProductColorSize productColorSize2 = new ProductColorSize();
        productColorSize2.setId("pcs-2");
        productColorSize2.setQuantity(5);

        ProductColorSizeResponse response2 = ProductColorSizeResponse.builder()
                .id("pcs-2")
                .quantity(5)
                .product(productResponse)
                .color(colorResponse)
                .size(sizeResponse)
                .build();

        List<CreateProductColorSizeRequest> requests = Arrays.asList(createProductColorSizeRequest, request2);
        List<ProductColorSize> productColorSizes = Arrays.asList(productColorSize, productColorSize2);
        List<ProductColorSize> savedProductColorSizes = Arrays.asList(productColorSize, productColorSize2);

        when(productColorSizeRepository.existsByProductIdAndColorIdAndSizeId("product-1", "color-1", "size-1"))
                .thenReturn(false);
        when(productColorSizeRepository.existsByProductIdAndColorIdAndSizeId("product-1", "color-2", "size-1"))
                .thenReturn(false);
        when(productColorSizeMapper.toEntity(createProductColorSizeRequest)).thenReturn(productColorSize);
        when(productColorSizeMapper.toEntity(request2)).thenReturn(productColorSize2);
        when(productColorSizeRepository.saveAll(productColorSizes)).thenReturn(savedProductColorSizes);
        when(productColorSizeMapper.toResponse(productColorSize)).thenReturn(productColorSizeResponse);
        when(productColorSizeMapper.toResponse(productColorSize2)).thenReturn(response2);

        // When
        List<ProductColorSizeResponse> result = productColorSizeService.createMultipleProductColorSizes(requests);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(productColorSizeResponse));
        assertTrue(result.contains(response2));
        verify(productColorSizeRepository).saveAll(productColorSizes);
    }

    @Test
    void createMultipleProductColorSizes_OneAlreadyExists() {
        // Given
        CreateProductColorSizeRequest request2 = CreateProductColorSizeRequest.builder()
                .productId("product-1")
                .colorId("color-2")
                .sizeId("size-1")
                .quantity(5)
                .build();

        List<CreateProductColorSizeRequest> requests = Arrays.asList(createProductColorSizeRequest, request2);

        when(productColorSizeRepository.existsByProductIdAndColorIdAndSizeId("product-1", "color-1", "size-1"))
                .thenReturn(false);
        when(productColorSizeRepository.existsByProductIdAndColorIdAndSizeId("product-1", "color-2", "size-1"))
                .thenReturn(true);

        // When & Then
        assertThrows(ProductColorSizeAlreadyExistsException.class, 
                () -> productColorSizeService.createMultipleProductColorSizes(requests));
        verify(productColorSizeRepository, never()).saveAll(any());
    }

    @Test
    void createMultipleProductColorSizes_EmptyList() {
        // Given
        List<CreateProductColorSizeRequest> requests = Collections.emptyList();
        when(productColorSizeRepository.saveAll(Collections.emptyList())).thenReturn(Collections.emptyList());

        // When
        List<ProductColorSizeResponse> result = productColorSizeService.createMultipleProductColorSizes(requests);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productColorSizeRepository).saveAll(Collections.emptyList());
    }

    @Test
    void createMultipleProductColorSizes_DatabaseError() {
        // Given
        List<CreateProductColorSizeRequest> requests = Arrays.asList(createProductColorSizeRequest);
        List<ProductColorSize> productColorSizes = Arrays.asList(productColorSize);

        when(productColorSizeRepository.existsByProductIdAndColorIdAndSizeId("product-1", "color-1", "size-1"))
                .thenReturn(false);
        when(productColorSizeMapper.toEntity(createProductColorSizeRequest)).thenReturn(productColorSize);
        when(productColorSizeRepository.saveAll(productColorSizes)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> productColorSizeService.createMultipleProductColorSizes(requests));
        verify(productColorSizeRepository).saveAll(productColorSizes);
    }

    @Test
    void updateProductColorSize_Success_SameColorAndSize() {
        // Given
        String id = "pcs-1";
        
        // Create updated response with new quantity
        ProductColorSizeResponse updatedResponse = ProductColorSizeResponse.builder()
                .id("pcs-1")
                .product(productResponse)
                .color(colorResponse)
                .size(sizeResponse)
                .quantity(15) // Updated quantity
                .createdAt(productColorSizeResponse.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(productColorSizeRepository.findById(id)).thenReturn(Optional.of(productColorSize));
        when(productColorSizeRepository.save(productColorSize)).thenReturn(productColorSize);
        when(productColorSizeMapper.toResponse(productColorSize)).thenReturn(updatedResponse);
        when(productRepository.save(product)).thenReturn(product);
        when(categoryRepository.save(category)).thenReturn(category);

        // When
        ProductColorSizeResponse result = productColorSizeService.updateProductColorSize(id, updateProductColorSizeRequest);

        // Then
        assertNotNull(result);
        assertEquals(updatedResponse, result);
        assertEquals("pcs-1", result.getId());
        assertEquals(15, result.getQuantity());
        assertEquals(55, product.getTotal()); // 50 + (15 - 10)
        assertEquals(105, category.getStock()); // 100 + (15 - 10)
        verify(productColorSizeRepository).findById(id);
        verify(productColorSizeMapper).updateEntity(updateProductColorSizeRequest, productColorSize);
        verify(productColorSizeRepository).save(productColorSize);
        verify(productRepository).save(product);
        verify(categoryRepository).save(category);
    }

    @Test
    void updateProductColorSize_Success_DifferentColorAndSize() {
        // Given
        String id = "pcs-1";
        updateProductColorSizeRequest.setColorId("color-2");
        updateProductColorSizeRequest.setSizeId("size-2");

        when(productColorSizeRepository.findById(id)).thenReturn(Optional.of(productColorSize));
        when(productColorSizeRepository.existsByProductIdAndColorIdAndSizeId("product-1", "color-2", "size-2"))
                .thenReturn(false);
        when(productColorSizeRepository.save(productColorSize)).thenReturn(productColorSize);
        when(productColorSizeMapper.toResponse(productColorSize)).thenReturn(productColorSizeResponse);
        when(productRepository.save(product)).thenReturn(product);
        when(categoryRepository.save(category)).thenReturn(category);

        // When
        ProductColorSizeResponse result = productColorSizeService.updateProductColorSize(id, updateProductColorSizeRequest);

        // Then
        assertNotNull(result);
        verify(productColorSizeRepository).existsByProductIdAndColorIdAndSizeId("product-1", "color-2", "size-2");
    }

    @Test
    void updateProductColorSize_NotFound() {
        // Given
        String id = "non-existent";
        when(productColorSizeRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductColorSizeNotFoundException.class, 
                () -> productColorSizeService.updateProductColorSize(id, updateProductColorSizeRequest));
        verify(productColorSizeRepository).findById(id);
        verify(productColorSizeRepository, never()).save(any());
    }

    @Test
    void updateProductColorSize_ColorSizeCombinationAlreadyExists() {
        // Given
        String id = "pcs-1";
        updateProductColorSizeRequest.setColorId("color-2");
        updateProductColorSizeRequest.setSizeId("size-2");

        when(productColorSizeRepository.findById(id)).thenReturn(Optional.of(productColorSize));
        when(productColorSizeRepository.existsByProductIdAndColorIdAndSizeId("product-1", "color-2", "size-2"))
                .thenReturn(true);

        // When & Then
        assertThrows(ProductColorSizeAlreadyExistsException.class, 
                () -> productColorSizeService.updateProductColorSize(id, updateProductColorSizeRequest));
        verify(productColorSizeRepository).findById(id);
        verify(productColorSizeRepository, never()).save(any());
    }

    @Test
    void updateProductColorSize_DatabaseError() {
        // Given
        String id = "pcs-1";
        when(productColorSizeRepository.findById(id)).thenReturn(Optional.of(productColorSize));
        when(productColorSizeRepository.save(productColorSize)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, 
                () -> productColorSizeService.updateProductColorSize(id, updateProductColorSizeRequest));
        verify(productColorSizeRepository).save(productColorSize);
    }

    @Test
    void deleteProductColorSize_Success() {
        // Given
        String id = "pcs-1";
        when(productColorSizeRepository.existsById(id)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> productColorSizeService.deleteProductColorSize(id));

        // Then
        verify(productColorSizeRepository).existsById(id);
        verify(productColorSizeRepository).deleteById(id);
    }

    @Test
    void deleteProductColorSize_NotFound() {
        // Given
        String id = "non-existent";
        when(productColorSizeRepository.existsById(id)).thenReturn(false);

        // When & Then
        assertThrows(ProductColorSizeNotFoundException.class, () -> productColorSizeService.deleteProductColorSize(id));
        verify(productColorSizeRepository).existsById(id);
        verify(productColorSizeRepository, never()).deleteById(id);
    }

    @Test
    void deleteProductColorSize_DatabaseError() {
        // Given
        String id = "pcs-1";
        when(productColorSizeRepository.existsById(id)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("Database error")).when(productColorSizeRepository).deleteById(id);

        // When & Then
        assertThrows(DataAccessException.class, () -> productColorSizeService.deleteProductColorSize(id));
        verify(productColorSizeRepository).existsById(id);
        verify(productColorSizeRepository).deleteById(id);
    }

    @Test
    void getProductColorSizesByProductId_Success() {
        // Given
        String productId = "product-1";
        List<ProductColorSize> productColorSizes = Arrays.asList(productColorSize);
        when(productColorSizeRepository.findByProductId(productId)).thenReturn(productColorSizes);
        when(productColorSizeMapper.toResponse(productColorSize)).thenReturn(productColorSizeResponse);

        // When
        List<ProductColorSizeResponse> result = productColorSizeService.getProductColorSizesByProductId(productId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(productColorSizeResponse, result.get(0));
        verify(productColorSizeRepository).findByProductId(productId);
        verify(productColorSizeMapper).toResponse(productColorSize);
    }

    @Test
    void getProductColorSizesByProductId_EmptyResult() {
        // Given
        String productId = "product-1";
        when(productColorSizeRepository.findByProductId(productId)).thenReturn(Collections.emptyList());

        // When
        List<ProductColorSizeResponse> result = productColorSizeService.getProductColorSizesByProductId(productId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productColorSizeRepository).findByProductId(productId);
    }

    @Test
    void getProductColorSizesByProductId_DatabaseError_ReturnsEmptyList() {
        // Given
        String productId = "product-1";
        when(productColorSizeRepository.findByProductId(productId))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When
        List<ProductColorSizeResponse> result = productColorSizeService.getProductColorSizesByProductId(productId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productColorSizeRepository).findByProductId(productId);
    }

    @Test
    void getProductColorSizesByProductId_UnexpectedError_ThrowsException() {
        // Given
        String productId = "product-1";
        when(productColorSizeRepository.findByProductId(productId))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> productColorSizeService.getProductColorSizesByProductId(productId));
        verify(productColorSizeRepository).findByProductId(productId);
    }

    @Test
    void getAllProductColorSizes_MapperError() {
        // Given
        List<ProductColorSize> productColorSizes = Arrays.asList(productColorSize);
        Page<ProductColorSize> productColorSizePage = new PageImpl<>(productColorSizes, pageable, 1);
        when(productColorSizeRepository.findAll(pageable)).thenReturn(productColorSizePage);
        when(productColorSizeMapper.toResponse(productColorSize)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productColorSizeService.getAllProductColorSizes(pageable));
        verify(productColorSizeMapper).toResponse(productColorSize);
    }

    @Test
    void getProductColorSizeById_MapperError() {
        // Given
        String id = "pcs-1";
        when(productColorSizeRepository.findById(id)).thenReturn(Optional.of(productColorSize));
        when(productColorSizeMapper.toResponse(productColorSize)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productColorSizeService.getProductColorSizeById(id));
        verify(productColorSizeMapper).toResponse(productColorSize);
    }

    @Test
    void createProductColorSize_MapperToEntityError() {
        // Given
        when(productColorSizeRepository.existsByProductIdAndColorIdAndSizeId("product-1", "color-1", "size-1"))
                .thenReturn(false);
        when(productColorSizeMapper.toEntity(createProductColorSizeRequest)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productColorSizeService.createProductColorSize(createProductColorSizeRequest));
        verify(productColorSizeMapper).toEntity(createProductColorSizeRequest);
    }

    @Test
    void createProductColorSize_MapperToResponseError() {
        // Given
        when(productColorSizeRepository.existsByProductIdAndColorIdAndSizeId("product-1", "color-1", "size-1"))
                .thenReturn(false);
        when(productColorSizeMapper.toEntity(createProductColorSizeRequest)).thenReturn(productColorSize);
        when(productColorSizeRepository.save(productColorSize)).thenReturn(productColorSize);
        when(productColorSizeMapper.toResponse(productColorSize)).thenThrow(new RuntimeException("Response mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productColorSizeService.createProductColorSize(createProductColorSizeRequest));
        verify(productColorSizeMapper).toResponse(productColorSize);
    }

    @Test
    void createMultipleProductColorSizes_MapperError() {
        // Given
        List<CreateProductColorSizeRequest> requests = Arrays.asList(createProductColorSizeRequest);
        when(productColorSizeRepository.existsByProductIdAndColorIdAndSizeId("product-1", "color-1", "size-1"))
                .thenReturn(false);
        when(productColorSizeMapper.toEntity(createProductColorSizeRequest)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productColorSizeService.createMultipleProductColorSizes(requests));
        verify(productColorSizeMapper).toEntity(createProductColorSizeRequest);
    }

    @Test
    void createMultipleProductColorSizes_UnexpectedError() {
        // Given
        List<CreateProductColorSizeRequest> requests = Arrays.asList(createProductColorSizeRequest);
        when(productColorSizeRepository.existsByProductIdAndColorIdAndSizeId("product-1", "color-1", "size-1"))
                .thenReturn(false);
        when(productColorSizeMapper.toEntity(createProductColorSizeRequest)).thenReturn(productColorSize);
        when(productColorSizeRepository.saveAll(any())).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productColorSizeService.createMultipleProductColorSizes(requests));
        verify(productColorSizeRepository).saveAll(any());
    }

    @Test
    void updateProductColorSize_MapperUpdateError() {
        // Given
        String id = "pcs-1";
        when(productColorSizeRepository.findById(id)).thenReturn(Optional.of(productColorSize));
        doThrow(new RuntimeException("Update mapping error")).when(productColorSizeMapper)
                .updateEntity(updateProductColorSizeRequest, productColorSize);

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> productColorSizeService.updateProductColorSize(id, updateProductColorSizeRequest));
        verify(productColorSizeMapper).updateEntity(updateProductColorSizeRequest, productColorSize);
    }

    @Test
    void updateProductColorSize_MapperResponseError() {
        // Given
        String id = "pcs-1";
        when(productColorSizeRepository.findById(id)).thenReturn(Optional.of(productColorSize));
        when(productColorSizeRepository.save(productColorSize)).thenReturn(productColorSize);
        when(productRepository.save(product)).thenReturn(product);
        when(categoryRepository.save(category)).thenReturn(category);
        when(productColorSizeMapper.toResponse(productColorSize)).thenThrow(new RuntimeException("Response mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> productColorSizeService.updateProductColorSize(id, updateProductColorSizeRequest));
        verify(productColorSizeMapper).toResponse(productColorSize);
    }

    @Test
    void getProductColorSizesByProductId_MapperError() {
        // Given
        String productId = "product-1";
        List<ProductColorSize> productColorSizes = Arrays.asList(productColorSize);
        when(productColorSizeRepository.findByProductId(productId)).thenReturn(productColorSizes);
        when(productColorSizeMapper.toResponse(productColorSize)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> productColorSizeService.getProductColorSizesByProductId(productId));
        verify(productColorSizeMapper).toResponse(productColorSize);
    }

    @Test
    void checkProductColorSizeExists_NotExists() {
        // Given
        when(productColorSizeRepository.existsByProductIdAndColorIdAndSizeId("product-1", "color-1", "size-1"))
                .thenReturn(false);

        // When
        assertDoesNotThrow(() -> productColorSizeService.createProductColorSize(createProductColorSizeRequest));

        // Then
        verify(productColorSizeRepository).existsByProductIdAndColorIdAndSizeId("product-1", "color-1", "size-1");
    }

    @Test
    void findProductColorSizeById_Success() {
        // Given
        String id = "pcs-1";
        when(productColorSizeRepository.findById(id)).thenReturn(Optional.of(productColorSize));
        when(productColorSizeMapper.toResponse(productColorSize)).thenReturn(productColorSizeResponse);

        // When
        ProductColorSizeResponse result = productColorSizeService.getProductColorSizeById(id);

        // Then
        assertNotNull(result);
        assertEquals(productColorSizeResponse, result);
        assertEquals("pcs-1", result.getId());
        assertEquals(10, result.getQuantity());
        verify(productColorSizeRepository).findById(id);
        verify(productColorSizeMapper).toResponse(productColorSize);
    }

    @Test
    void getAllProductColorSizes_MultipleItems() {
        // Given
        ProductColorSize productColorSize2 = new ProductColorSize();
        productColorSize2.setId("pcs-2");
        productColorSize2.setQuantity(5);

        ProductColorSizeResponse response2 = ProductColorSizeResponse.builder()
                .id("pcs-2")
                .quantity(5)
                .product(productResponse)
                .color(colorResponse)
                .size(sizeResponse)
                .build();

        List<ProductColorSize> productColorSizes = Arrays.asList(productColorSize, productColorSize2);
        Page<ProductColorSize> productColorSizePage = new PageImpl<>(productColorSizes, pageable, 2);
        
        when(productColorSizeRepository.findAll(pageable)).thenReturn(productColorSizePage);
        when(productColorSizeMapper.toResponse(productColorSize)).thenReturn(productColorSizeResponse);
        when(productColorSizeMapper.toResponse(productColorSize2)).thenReturn(response2);

        // When
        Page<ProductColorSizeResponse> result = productColorSizeService.getAllProductColorSizes(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        verify(productColorSizeMapper, times(2)).toResponse(any(ProductColorSize.class));
    }

    @Test
    void getProductColorSizesByProductId_MultipleItems() {
        // Given
        String productId = "product-1";
        
        ProductColorSize productColorSize2 = new ProductColorSize();
        productColorSize2.setId("pcs-2");
        productColorSize2.setQuantity(5);

        ProductColorSizeResponse response2 = ProductColorSizeResponse.builder()
                .id("pcs-2")
                .quantity(5)
                .product(productResponse)
                .color(colorResponse)
                .size(sizeResponse)
                .build();

        List<ProductColorSize> productColorSizes = Arrays.asList(productColorSize, productColorSize2);
        when(productColorSizeRepository.findByProductId(productId)).thenReturn(productColorSizes);
        when(productColorSizeMapper.toResponse(productColorSize)).thenReturn(productColorSizeResponse);
        when(productColorSizeMapper.toResponse(productColorSize2)).thenReturn(response2);

        // When
        List<ProductColorSizeResponse> result = productColorSizeService.getProductColorSizesByProductId(productId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productColorSizeMapper, times(2)).toResponse(any(ProductColorSize.class));
    }

    @Test
    void deleteProductColorSize_UnexpectedError() {
        // Given
        String id = "pcs-1";
        when(productColorSizeRepository.existsById(id)).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(productColorSizeRepository).deleteById(id);

        // When & Then
        assertThrows(RuntimeException.class, () -> productColorSizeService.deleteProductColorSize(id));
        verify(productColorSizeRepository).existsById(id);
        verify(productColorSizeRepository).deleteById(id);
    }

    @Test
    void updateProductColorSize_QuantityDecrease() {
        // Given
        String id = "pcs-1";
        updateProductColorSizeRequest.setQuantity(5); // Decrease from 10 to 5
        
        ProductColorSizeResponse decreasedResponse = ProductColorSizeResponse.builder()
                .id("pcs-1")
                .product(productResponse)
                .color(colorResponse)
                .size(sizeResponse)
                .quantity(5) // Decreased quantity
                .createdAt(productColorSizeResponse.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(productColorSizeRepository.findById(id)).thenReturn(Optional.of(productColorSize));
        when(productColorSizeRepository.save(productColorSize)).thenReturn(productColorSize);
        when(productColorSizeMapper.toResponse(productColorSize)).thenReturn(decreasedResponse);
        when(productRepository.save(product)).thenReturn(product);
        when(categoryRepository.save(category)).thenReturn(category);

        // When
        ProductColorSizeResponse result = productColorSizeService.updateProductColorSize(id, updateProductColorSizeRequest);

        // Then
        assertNotNull(result);
        assertEquals(5, result.getQuantity());
        assertEquals(45, product.getTotal()); // 50 + (5 - 10) = 45
        assertEquals(95, category.getStock()); // 100 + (5 - 10) = 95
        verify(productRepository).save(product);
        verify(categoryRepository).save(category);
    }

    @Test
    void createProductColorSize_UnexpectedError() {
        // Given
        when(productColorSizeRepository.existsByProductIdAndColorIdAndSizeId("product-1", "color-1", "size-1"))
                .thenReturn(false);
        when(productColorSizeMapper.toEntity(createProductColorSizeRequest)).thenReturn(productColorSize);
        when(productColorSizeRepository.save(productColorSize)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productColorSizeService.createProductColorSize(createProductColorSizeRequest));
        verify(productColorSizeRepository).save(productColorSize);
    }

    @Test
    void updateProductColorSize_UnexpectedError() {
        // Given
        String id = "pcs-1";
        when(productColorSizeRepository.findById(id)).thenReturn(Optional.of(productColorSize));
        when(productColorSizeRepository.save(productColorSize)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> productColorSizeService.updateProductColorSize(id, updateProductColorSizeRequest));
        verify(productColorSizeRepository).save(productColorSize);
    }
}
