package com.hcmus.ecommerce_backend.unit.product.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
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
import org.springframework.data.jpa.domain.Specification;
import com.hcmus.ecommerce_backend.category.exception.CategoryNotFoundException;
import com.hcmus.ecommerce_backend.category.repository.CategoryRepository;
import com.hcmus.ecommerce_backend.product.exception.ProductAlreadyExistsException;
import com.hcmus.ecommerce_backend.product.exception.ProductNotFoundException;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.CreateProductRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.UpdateProductRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductResponse;
import com.hcmus.ecommerce_backend.product.model.entity.Product;
import com.hcmus.ecommerce_backend.product.model.mapper.ProductMapper;
import com.hcmus.ecommerce_backend.product.repository.ProductRepository;
import com.hcmus.ecommerce_backend.product.service.impl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductResponse productResponse;
    private CreateProductRequest createProductRequest;
    private UpdateProductRequest updateProductRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Setup Product entity
        product = new Product();
        product.setId("product-1");
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(99.99);
        product.setEnable(true);
        product.setInStock(true);

        // Setup ProductResponse
        productResponse = ProductResponse.builder()
                .id("product-1")
                .name("Test Product")
                .description("Test Description")
                .price(99.99)
                .enable(true)
                .inStock(true)
                .build();

        // Setup CreateProductRequest
        createProductRequest = CreateProductRequest.builder()
                .name("Test Product")
                .description("Test Description")
                .price(99.99)
                .categoryId("category-1")
                .build();

        // Setup UpdateProductRequest
        updateProductRequest = UpdateProductRequest.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(149.99)
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAllProducts_WithNoFilters_Success() {
        // Given
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // When
        Page<ProductResponse> result = productService.getAllProducts(pageable, null, null, null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(productResponse, result.getContent().get(0));
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
        verify(productMapper).toResponse(product);
    }

    @Test
    void getAllProducts_WithKeywordFilter_Success() {
        // Given
        String keyword = "test";
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // When
        Page<ProductResponse> result = productService.getAllProducts(pageable, keyword, null, null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllProducts_WithCategoryFilter_Success() {
        // Given
        String category = "category-1";
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // When
        Page<ProductResponse> result = productService.getAllProducts(pageable, null, category, null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllProducts_WithPriceRangeFilter_Success() {
        // Given
        Double fromPrice = 50.0;
        Double toPrice = 150.0;
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // When
        Page<ProductResponse> result = productService.getAllProducts(pageable, null, null, fromPrice, toPrice, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllProducts_WithColorAndSizeFilter_Success() {
        // Given
        String color = "Red";
        String size = "M";
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // When
        Page<ProductResponse> result = productService.getAllProducts(pageable, null, null, null, null, color, size, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllProducts_WithEnabledAndStockFilters_Success() {
        // Given
        Boolean enabled = true;
        Boolean inStock = true;
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // When
        Page<ProductResponse> result = productService.getAllProducts(pageable, null, null, null, null, null, null, enabled, inStock);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllProducts_DatabaseError_ReturnsEmptyPage() {
        // Given
        when(productRepository.findAll(any(Specification.class), eq(pageable)))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When
        Page<ProductResponse> result = productService.getAllProducts(pageable, null, null, null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllProducts_UnexpectedError_ThrowsException() {
        // Given
        when(productRepository.findAll(any(Specification.class), eq(pageable)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> productService.getAllProducts(pageable, null, null, null, null, null, null, null, null));
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getProductById_Success() {
        // Given
        String id = "product-1";
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // When
        ProductResponse result = productService.getProductById(id);

        // Then
        assertNotNull(result);
        assertEquals(productResponse, result);
        verify(productRepository).findById(id);
        verify(productMapper).toResponse(product);
    }

    @Test
    void getProductById_NotFound() {
        // Given
        String id = "non-existent";
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(id));
        verify(productRepository).findById(id);
    }

    @Test
    void getProductById_DatabaseError() {
        // Given
        String id = "product-1";
        when(productRepository.findById(id)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> productService.getProductById(id));
        verify(productRepository).findById(id);
    }

    @Test
    void getProductById_UnexpectedError() {
        // Given
        String id = "product-1";
        when(productRepository.findById(id)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productService.getProductById(id));
        verify(productRepository).findById(id);
    }

    @Test
    void createProduct_Success() {
        // Given
        when(productRepository.existsByName("Test Product")).thenReturn(false);
        when(categoryRepository.existsById("category-1")).thenReturn(true);
        when(productMapper.toEntity(createProductRequest)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // When
        ProductResponse result = productService.createProduct(createProductRequest);

        // Then
        assertNotNull(result);
        assertEquals(productResponse, result);
        assertTrue(product.isEnable());
        assertTrue(product.isInStock());
        verify(productRepository).existsByName("Test Product");
        verify(categoryRepository).existsById("category-1");
        verify(productMapper).toEntity(createProductRequest);
        verify(productRepository).save(product);
        verify(productMapper).toResponse(product);
    }

    @Test
    void createProduct_ProductAlreadyExists() {
        // Given
        when(productRepository.existsByName("Test Product")).thenReturn(true);

        // When & Then
        assertThrows(ProductAlreadyExistsException.class, () -> productService.createProduct(createProductRequest));
        verify(productRepository).existsByName("Test Product");
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_CategoryNotFound() {
        // Given
        when(productRepository.existsByName("Test Product")).thenReturn(false);
        when(categoryRepository.existsById("category-1")).thenReturn(false);

        // When & Then
        assertThrows(CategoryNotFoundException.class, () -> productService.createProduct(createProductRequest));
        verify(productRepository).existsByName("Test Product");
        verify(categoryRepository).existsById("category-1");
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_DatabaseError() {
        // Given
        when(productRepository.existsByName("Test Product")).thenReturn(false);
        when(categoryRepository.existsById("category-1")).thenReturn(true);
        when(productMapper.toEntity(createProductRequest)).thenReturn(product);
        when(productRepository.save(product)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> productService.createProduct(createProductRequest));
        verify(productRepository).save(product);
    }

    @Test
    void createProduct_UnexpectedError() {
        // Given
        when(productRepository.existsByName("Test Product")).thenReturn(false);
        when(categoryRepository.existsById("category-1")).thenReturn(true);
        when(productMapper.toEntity(createProductRequest)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productService.createProduct(createProductRequest));
        verify(productMapper).toEntity(createProductRequest);
    }

    @Test
    void updateProduct_Success() {
        // Given
        String id = "product-1";
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.existsByName("Updated Product")).thenReturn(false);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // When
        ProductResponse result = productService.updateProduct(id, updateProductRequest);

        // Then
        assertNotNull(result);
        assertEquals(productResponse, result);
        verify(productRepository).findById(id);
        verify(productRepository).existsByName("Updated Product");
        verify(productMapper).updateEntity(updateProductRequest, product);
        verify(productRepository).save(product);
        verify(productMapper).toResponse(product);
    }

    @Test
    void updateProduct_SameName_NoNameCheck() {
        // Given
        String id = "product-1";
        updateProductRequest.setName("Test Product"); // Same as existing name
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // When
        ProductResponse result = productService.updateProduct(id, updateProductRequest);

        // Then
        assertNotNull(result);
        verify(productRepository).findById(id);
        verify(productRepository, never()).existsByName(anyString());
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_NotFound() {
        // Given
        String id = "non-existent";
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> productService.updateProduct(id, updateProductRequest));
        verify(productRepository).findById(id);
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_NewNameAlreadyExists() {
        // Given
        String id = "product-1";
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.existsByName("Updated Product")).thenReturn(true);

        // When & Then
        assertThrows(ProductAlreadyExistsException.class, () -> productService.updateProduct(id, updateProductRequest));
        verify(productRepository).findById(id);
        verify(productRepository).existsByName("Updated Product");
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_DatabaseError() {
        // Given
        String id = "product-1";
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.existsByName("Updated Product")).thenReturn(false);
        when(productRepository.save(product)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> productService.updateProduct(id, updateProductRequest));
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_UnexpectedError() {
        // Given
        String id = "product-1";
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.existsByName("Updated Product")).thenReturn(false);
        doThrow(new RuntimeException("Mapping error")).when(productMapper).updateEntity(updateProductRequest, product);

        // When & Then
        assertThrows(RuntimeException.class, () -> productService.updateProduct(id, updateProductRequest));
        verify(productMapper).updateEntity(updateProductRequest, product);
    }

    @Test
    void deleteProduct_Success() {
        // Given
        String id = "product-1";
        when(productRepository.existsById(id)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> productService.deleteProduct(id));

        // Then
        verify(productRepository).existsById(id);
        verify(productRepository).deleteById(id);
    }

    @Test
    void deleteProduct_NotFound() {
        // Given
        String id = "non-existent";
        when(productRepository.existsById(id)).thenReturn(false);

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(id));
        verify(productRepository).existsById(id);
        verify(productRepository, never()).deleteById(id);
    }

    @Test
    void deleteProduct_DatabaseError() {
        // Given
        String id = "product-1";
        when(productRepository.existsById(id)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("Database error")).when(productRepository).deleteById(id);

        // When & Then
        assertThrows(DataAccessException.class, () -> productService.deleteProduct(id));
        verify(productRepository).existsById(id);
        verify(productRepository).deleteById(id);
    }

    @Test
    void deleteProduct_UnexpectedError() {
        // Given
        String id = "product-1";
        when(productRepository.existsById(id)).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(productRepository).deleteById(id);

        // When & Then
        assertThrows(RuntimeException.class, () -> productService.deleteProduct(id));
        verify(productRepository).existsById(id);
        verify(productRepository).deleteById(id);
    }

    @Test
    void getTopTrendingProducts_Success() {
        // Given
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);
        List<Product> products = Arrays.asList(product);
        when(productRepository.findTopTrendingProducts(pageable)).thenReturn(products);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // When
        List<ProductResponse> result = productService.getTopTrendingProducts(page, size);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(productResponse, result.get(0));
        verify(productRepository).findTopTrendingProducts(pageable);
        verify(productMapper).toResponse(product);
    }

    @Test
    void getTopTrendingProducts_EmptyResult() {
        // Given
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);
        when(productRepository.findTopTrendingProducts(pageable)).thenReturn(Collections.emptyList());

        // When
        List<ProductResponse> result = productService.getTopTrendingProducts(page, size);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).findTopTrendingProducts(pageable);
    }

    @Test
    void getTopSellingProducts_Success() {
        // Given
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);
        List<Product> products = Arrays.asList(product);
        when(productRepository.findTopSellingProducts(pageable)).thenReturn(products);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // When
        List<ProductResponse> result = productService.getTopSellingProducts(page, size);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(productResponse, result.get(0));
        verify(productRepository).findTopSellingProducts(pageable);
        verify(productMapper).toResponse(product);
    }

    @Test
    void getTopSellingProducts_EmptyResult() {
        // Given
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);
        when(productRepository.findTopSellingProducts(pageable)).thenReturn(Collections.emptyList());

        // When
        List<ProductResponse> result = productService.getTopSellingProducts(page, size);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).findTopSellingProducts(pageable);
    }

    @Test
    void findProductById_Success() {
        // Given
        String id = "product-1";
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        // When
        Product result = productService.findProductById(id);

        // Then
        assertNotNull(result);
        assertEquals(product, result);
        verify(productRepository).findById(id);
    }

    @Test
    void findProductById_NotFound() {
        // Given
        String id = "non-existent";
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class, () -> productService.findProductById(id));
        verify(productRepository).findById(id);
    }

    @Test
    void doesProductExistById_True() {
        // Given
        String id = "product-1";
        when(productRepository.existsById(id)).thenReturn(true);

        // When
        boolean result = productService.doesProductExistById(id);

        // Then
        assertTrue(result);
        verify(productRepository).existsById(id);
    }

    @Test
    void doesProductExistById_False() {
        // Given
        String id = "non-existent";
        when(productRepository.existsById(id)).thenReturn(false);

        // When
        boolean result = productService.doesProductExistById(id);

        // Then
        assertFalse(result);
        verify(productRepository).existsById(id);
    }

    @Test
    void checkProductNameExists_Exists() {
        // Given
        String name = "Test Product";
        when(productRepository.existsByName(name)).thenReturn(true);

        // When & Then
        assertThrows(ProductAlreadyExistsException.class, () -> productService.checkProductNameExists(name));
        verify(productRepository).existsByName(name);
    }

    @Test
    void checkProductNameExists_NotExists() {
        // Given
        String name = "Test Product";
        when(productRepository.existsByName(name)).thenReturn(false);

        // When
        assertDoesNotThrow(() -> productService.checkProductNameExists(name));

        // Then
        verify(productRepository).existsByName(name);
    }

    @Test
    void validateCategory_Exists() {
        // Given
        String categoryId = "category-1";
        when(categoryRepository.existsById(categoryId)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> productService.validateCategory(categoryId));

        // Then
        verify(categoryRepository).existsById(categoryId);
    }

    @Test
    void validateCategory_NotExists() {
        // Given
        String categoryId = "non-existent";
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        // When & Then
        assertThrows(CategoryNotFoundException.class, () -> productService.validateCategory(categoryId));
        verify(categoryRepository).existsById(categoryId);
    }

    @Test
    void getAllProducts_EmptyKeyword_IgnoresFilter() {
        // Given
        String keyword = "";
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // When
        Page<ProductResponse> result = productService.getAllProducts(pageable, keyword, null, null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllProducts_EmptyCategory_IgnoresFilter() {
        // Given
        String category = "";
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // When
        Page<ProductResponse> result = productService.getAllProducts(pageable, null, category, null, null, null, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllProducts_EmptyColorAndSize_IgnoresFilter() {
        // Given
        String color = "";
        String size = "";
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // When
        Page<ProductResponse> result = productService.getAllProducts(pageable, null, null, null, null, color, size, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllProducts_OnlyColorFilter_Success() {
        // Given
        String color = "Red";
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // When
        Page<ProductResponse> result = productService.getAllProducts(pageable, null, null, null, null, color, null, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllProducts_OnlySizeFilter_Success() {
        // Given
        String size = "M";
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // When
        Page<ProductResponse> result = productService.getAllProducts(pageable, null, null, null, null, null, size, null, null);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllProducts_AllFilters_Success() {
        // Given
        String keyword = "test";
        String category = "category-1";
        Double fromPrice = 50.0;
        Double toPrice = 150.0;
        String color = "Red";
        String size = "M";
        Boolean enabled = true;
        Boolean inStock = true;

        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // When
        Page<ProductResponse> result = productService.getAllProducts(pageable, keyword, category, fromPrice, toPrice, color, size, enabled, inStock);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllProducts_MapperError() {
        // Given
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);
        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);
        when(productMapper.toResponse(product)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> productService.getAllProducts(pageable, null, null, null, null, null, null, null, null));
        verify(productMapper).toResponse(product);
    }

    @Test
    void getProductById_MapperError() {
        // Given
        String id = "product-1";
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productService.getProductById(id));
        verify(productMapper).toResponse(product);
    }

    @Test
    void createProduct_MapperToEntityError() {
        // Given
        when(productRepository.existsByName("Test Product")).thenReturn(false);
        when(categoryRepository.existsById("category-1")).thenReturn(true);
        when(productMapper.toEntity(createProductRequest)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productService.createProduct(createProductRequest));
        verify(productMapper).toEntity(createProductRequest);
    }

    @Test
    void createProduct_MapperToResponseError() {
        // Given
        when(productRepository.existsByName("Test Product")).thenReturn(false);
        when(categoryRepository.existsById("category-1")).thenReturn(true);
        when(productMapper.toEntity(createProductRequest)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenThrow(new RuntimeException("Response mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productService.createProduct(createProductRequest));
        verify(productMapper).toResponse(product);
    }

    @Test
    void updateProduct_MapperResponseError() {
        // Given
        String id = "product-1";
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.existsByName("Updated Product")).thenReturn(false);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenThrow(new RuntimeException("Response mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productService.updateProduct(id, updateProductRequest));
        verify(productMapper).toResponse(product);
    }

    @Test
    void getTopTrendingProducts_MapperError() {
        // Given
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);
        List<Product> products = Arrays.asList(product);
        when(productRepository.findTopTrendingProducts(pageable)).thenReturn(products);
        when(productMapper.toResponse(product)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productService.getTopTrendingProducts(page, size));
        verify(productMapper).toResponse(product);
    }

    @Test
    void getTopSellingProducts_MapperError() {
        // Given
        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);
        List<Product> products = Arrays.asList(product);
        when(productRepository.findTopSellingProducts(pageable)).thenReturn(products);
        when(productMapper.toResponse(product)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productService.getTopSellingProducts(page, size));
        verify(productMapper).toResponse(product);
    }
}
