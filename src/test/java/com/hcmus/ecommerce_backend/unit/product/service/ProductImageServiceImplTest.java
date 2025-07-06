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
import com.hcmus.ecommerce_backend.product.exception.ProductImageAlreadyExistsException;
import com.hcmus.ecommerce_backend.product.exception.ProductImageNotFoundException;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.CreateProductImageRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.UpdateListProductImageRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.UpdateProductImageRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductImageResponse;
import com.hcmus.ecommerce_backend.product.model.entity.Product;
import com.hcmus.ecommerce_backend.product.model.entity.ProductImage;
import com.hcmus.ecommerce_backend.product.model.mapper.ProductImageMapper;
import com.hcmus.ecommerce_backend.product.repository.ProductImageRepository;
import com.hcmus.ecommerce_backend.product.service.impl.ProductImageServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceImplTest {

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductImageMapper productImageMapper;

    @InjectMocks
    private ProductImageServiceImpl productImageService;

    private ProductImage productImage;
    private ProductImageResponse productImageResponse;
    private CreateProductImageRequest createProductImageRequest;
    private UpdateProductImageRequest updateProductImageRequest;
    private Product product;

    @BeforeEach
    void setUp() {
        // Setup Product
        product = new Product();
        product.setId("product-1");
        product.setName("Test Product");

        // Setup ProductImage entity
        productImage = new ProductImage();
        productImage.setId("image-1");
        productImage.setUrl("https://example.com/image1.jpg");
        productImage.setProduct(product);

        // Setup ProductImageResponse
        productImageResponse = ProductImageResponse.builder()
                .id("image-1")
                .url("https://example.com/image1.jpg")
                .productId("product-1")
                .build();

        // Setup CreateProductImageRequest
        createProductImageRequest = CreateProductImageRequest.builder()
                .url("https://example.com/image1.jpg")
                .productId("product-1")
                .build();

        // Setup UpdateProductImageRequest
        updateProductImageRequest = UpdateProductImageRequest.builder()
                .url("https://example.com/updated-image.jpg")
                .productId("product-1")
                .build();

        UpdateListProductImageRequest.builder()
                .id("image-1")
                .url("https://example.com/list-image.jpg")
                .productId("product-1")
                .build();
    }

    @Test
    void getAllProductImages_Success() {
        // Given
        List<ProductImage> productImages = Arrays.asList(productImage);
        when(productImageRepository.findAll()).thenReturn(productImages);
        when(productImageMapper.toResponse(productImage)).thenReturn(productImageResponse);

        // When
        List<ProductImageResponse> result = productImageService.getAllProductImages();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(productImageResponse, result.get(0));
        verify(productImageRepository).findAll();
        verify(productImageMapper).toResponse(productImage);
    }

    @Test
    void getAllProductImages_EmptyResult() {
        // Given
        when(productImageRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<ProductImageResponse> result = productImageService.getAllProductImages();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productImageRepository).findAll();
    }

    @Test
    void getAllProductImages_DatabaseError_ReturnsEmptyList() {
        // Given
        when(productImageRepository.findAll()).thenThrow(new DataIntegrityViolationException("Database error"));

        // When
        List<ProductImageResponse> result = productImageService.getAllProductImages();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productImageRepository).findAll();
    }

    @Test
    void getAllProductImages_UnexpectedError_ThrowsException() {
        // Given
        when(productImageRepository.findAll()).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productImageService.getAllProductImages());
        verify(productImageRepository).findAll();
    }

    @Test
    void getProductImageById_Success() {
        // Given
        String id = "image-1";
        when(productImageRepository.findById(id)).thenReturn(Optional.of(productImage));
        when(productImageMapper.toResponse(productImage)).thenReturn(productImageResponse);

        // When
        ProductImageResponse result = productImageService.getProductImageById(id);

        // Then
        assertNotNull(result);
        assertEquals(productImageResponse, result);
        verify(productImageRepository).findById(id);
        verify(productImageMapper).toResponse(productImage);
    }

    @Test
    void getProductImageById_NotFound() {
        // Given
        String id = "non-existent";
        when(productImageRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductImageNotFoundException.class, () -> productImageService.getProductImageById(id));
        verify(productImageRepository).findById(id);
    }

    @Test
    void getProductImageById_DatabaseError() {
        // Given
        String id = "image-1";
        when(productImageRepository.findById(id)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> productImageService.getProductImageById(id));
        verify(productImageRepository).findById(id);
    }

    @Test
    void getProductImageById_UnexpectedError() {
        // Given
        String id = "image-1";
        when(productImageRepository.findById(id)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productImageService.getProductImageById(id));
        verify(productImageRepository).findById(id);
    }

    @Test
    void createProductImage_Success() {
        // Given
        when(productImageRepository.existsByUrlAndProductId("https://example.com/image1.jpg", "product-1"))
                .thenReturn(false);
        when(productImageMapper.toEntity(createProductImageRequest)).thenReturn(productImage);
        when(productImageRepository.save(productImage)).thenReturn(productImage);
        when(productImageMapper.toResponse(productImage)).thenReturn(productImageResponse);

        // When
        ProductImageResponse result = productImageService.createProductImage(createProductImageRequest);

        // Then
        assertNotNull(result);
        assertEquals(productImageResponse, result);
        verify(productImageRepository).existsByUrlAndProductId("https://example.com/image1.jpg", "product-1");
        verify(productImageMapper).toEntity(createProductImageRequest);
        verify(productImageRepository).save(productImage);
        verify(productImageMapper).toResponse(productImage);
    }

    @Test
    void createProductImage_AlreadyExists() {
        // Given
        when(productImageRepository.existsByUrlAndProductId("https://example.com/image1.jpg", "product-1"))
                .thenReturn(true);

        // When & Then
        assertThrows(ProductImageAlreadyExistsException.class, 
                () -> productImageService.createProductImage(createProductImageRequest));
        verify(productImageRepository).existsByUrlAndProductId("https://example.com/image1.jpg", "product-1");
        verify(productImageRepository, never()).save(any());
    }

    @Test
    void createProductImage_DatabaseError() {
        // Given
        when(productImageRepository.existsByUrlAndProductId("https://example.com/image1.jpg", "product-1"))
                .thenReturn(false);
        when(productImageMapper.toEntity(createProductImageRequest)).thenReturn(productImage);
        when(productImageRepository.save(productImage)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> productImageService.createProductImage(createProductImageRequest));
        verify(productImageRepository).save(productImage);
    }

    @Test
    void createProductImage_UnexpectedError() {
        // Given
        when(productImageRepository.existsByUrlAndProductId("https://example.com/image1.jpg", "product-1"))
                .thenReturn(false);
        when(productImageMapper.toEntity(createProductImageRequest)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productImageService.createProductImage(createProductImageRequest));
        verify(productImageMapper).toEntity(createProductImageRequest);
    }

    @Test
    void updateProductImage_Success() {
        // Given
        String id = "image-1";
        when(productImageRepository.findById(id)).thenReturn(Optional.of(productImage));
        when(productImageRepository.existsByUrlAndProductId("https://example.com/updated-image.jpg", "product-1"))
                .thenReturn(false);
        when(productImageRepository.save(productImage)).thenReturn(productImage);
        when(productImageMapper.toResponse(productImage)).thenReturn(productImageResponse);

        // When
        ProductImageResponse result = productImageService.updateProductImage(id, updateProductImageRequest);

        // Then
        assertNotNull(result);
        assertEquals(productImageResponse, result);
        verify(productImageRepository).findById(id);
        verify(productImageRepository).existsByUrlAndProductId("https://example.com/updated-image.jpg", "product-1");
        verify(productImageMapper).updateEntity(updateProductImageRequest, productImage);
        verify(productImageRepository).save(productImage);
        verify(productImageMapper).toResponse(productImage);
    }

    @Test
    void updateProductImage_SameUrl_NoExistenceCheck() {
        // Given
        String id = "image-1";
        updateProductImageRequest.setUrl("https://example.com/image1.jpg"); // Same as existing URL
        when(productImageRepository.findById(id)).thenReturn(Optional.of(productImage));
        when(productImageRepository.save(productImage)).thenReturn(productImage);
        when(productImageMapper.toResponse(productImage)).thenReturn(productImageResponse);

        // When
        ProductImageResponse result = productImageService.updateProductImage(id, updateProductImageRequest);

        // Then
        assertNotNull(result);
        verify(productImageRepository).findById(id);
        verify(productImageRepository, never()).existsByUrlAndProductId(anyString(), anyString());
        verify(productImageRepository).save(productImage);
    }

    @Test
    void updateProductImage_NotFound() {
        // Given
        String id = "non-existent";
        when(productImageRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductImageNotFoundException.class, 
                () -> productImageService.updateProductImage(id, updateProductImageRequest));
        verify(productImageRepository).findById(id);
        verify(productImageRepository, never()).save(any());
    }

    @Test
    void updateProductImage_NewUrlAlreadyExists() {
        // Given
        String id = "image-1";
        when(productImageRepository.findById(id)).thenReturn(Optional.of(productImage));
        when(productImageRepository.existsByUrlAndProductId("https://example.com/updated-image.jpg", "product-1"))
                .thenReturn(true);

        // When & Then
        assertThrows(ProductImageAlreadyExistsException.class, 
                () -> productImageService.updateProductImage(id, updateProductImageRequest));
        verify(productImageRepository).findById(id);
        verify(productImageRepository).existsByUrlAndProductId("https://example.com/updated-image.jpg", "product-1");
        verify(productImageRepository, never()).save(any());
    }

    @Test
    void updateProductImage_DatabaseError() {
        // Given
        String id = "image-1";
        when(productImageRepository.findById(id)).thenReturn(Optional.of(productImage));
        when(productImageRepository.existsByUrlAndProductId("https://example.com/updated-image.jpg", "product-1"))
                .thenReturn(false);
        when(productImageRepository.save(productImage)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, 
                () -> productImageService.updateProductImage(id, updateProductImageRequest));
        verify(productImageRepository).save(productImage);
    }

    @Test
    void deleteProductImage_Success() {
        // Given
        String id = "image-1";
        when(productImageRepository.existsById(id)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> productImageService.deleteProductImage(id));

        // Then
        verify(productImageRepository).existsById(id);
        verify(productImageRepository).deleteById(id);
    }

    @Test
    void deleteProductImage_NotFound() {
        // Given
        String id = "non-existent";
        when(productImageRepository.existsById(id)).thenReturn(false);

        // When & Then
        assertThrows(ProductImageNotFoundException.class, () -> productImageService.deleteProductImage(id));
        verify(productImageRepository).existsById(id);
        verify(productImageRepository, never()).deleteById(id);
    }

    @Test
    void deleteProductImage_DatabaseError() {
        // Given
        String id = "image-1";
        when(productImageRepository.existsById(id)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("Database error")).when(productImageRepository).deleteById(id);

        // When & Then
        assertThrows(DataAccessException.class, () -> productImageService.deleteProductImage(id));
        verify(productImageRepository).existsById(id);
        verify(productImageRepository).deleteById(id);
    }

    @Test
    void deleteProductImage_UnexpectedError() {
        // Given
        String id = "image-1";
        when(productImageRepository.existsById(id)).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(productImageRepository).deleteById(id);

        // When & Then
        assertThrows(RuntimeException.class, () -> productImageService.deleteProductImage(id));
        verify(productImageRepository).existsById(id);
        verify(productImageRepository).deleteById(id);
    }

    @Test
    void getProductImagesByProductId_Success() {
        // Given
        String productId = "product-1";
        List<ProductImage> productImages = Arrays.asList(productImage);
        when(productImageRepository.findByProductId(productId)).thenReturn(productImages);
        when(productImageMapper.toResponse(productImage)).thenReturn(productImageResponse);

        // When
        List<ProductImageResponse> result = productImageService.getProductImagesByProductId(productId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(productImageResponse, result.get(0));
        verify(productImageRepository).findByProductId(productId);
        verify(productImageMapper).toResponse(productImage);
    }

    @Test
    void getProductImagesByProductId_NotFound() {
        // Given
        String productId = "non-existent";
        when(productImageRepository.findByProductId(productId)).thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(ProductImageNotFoundException.class, 
                () -> productImageService.getProductImagesByProductId(productId));
        verify(productImageRepository).findByProductId(productId);
    }

    @Test
    void getProductImagesByProductId_DatabaseError() {
        // Given
        String productId = "product-1";
        when(productImageRepository.findByProductId(productId))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, 
                () -> productImageService.getProductImagesByProductId(productId));
        verify(productImageRepository).findByProductId(productId);
    }

    @Test
    void getProductImagesByProductId_UnexpectedError() {
        // Given
        String productId = "product-1";
        when(productImageRepository.findByProductId(productId))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> productImageService.getProductImagesByProductId(productId));
        verify(productImageRepository).findByProductId(productId);
    }

    @Test
    void updateListProductImage_CreateNewImage() {
        // Given
        UpdateListProductImageRequest createRequest = UpdateListProductImageRequest.builder()
                .id(null) // Empty ID triggers creation
                .url("https://example.com/new-image.jpg")
                .productId("product-1")
                .build();
        
        List<UpdateListProductImageRequest> requests = Arrays.asList(createRequest);
        when(productImageMapper.toEntity(createRequest)).thenReturn(productImage);
        when(productImageRepository.save(productImage)).thenReturn(productImage);

        // When
        assertDoesNotThrow(() -> productImageService.updateListProductImage(requests));

        // Then
        verify(productImageMapper).toEntity(createRequest);
        verify(productImageRepository).save(productImage);
    }

    @Test
    void updateListProductImage_CreateNewImage_EmptyId() {
        // Given
        UpdateListProductImageRequest createRequest = UpdateListProductImageRequest.builder()
                .id("") // Empty string ID triggers creation
                .url("https://example.com/new-image.jpg")
                .productId("product-1")
                .build();
        
        List<UpdateListProductImageRequest> requests = Arrays.asList(createRequest);
        when(productImageMapper.toEntity(createRequest)).thenReturn(productImage);
        when(productImageRepository.save(productImage)).thenReturn(productImage);

        // When
        assertDoesNotThrow(() -> productImageService.updateListProductImage(requests));

        // Then
        verify(productImageMapper).toEntity(createRequest);
        verify(productImageRepository).save(productImage);
    }

    @Test
    void updateListProductImage_DeleteImage() {
        // Given
        UpdateListProductImageRequest deleteRequest = UpdateListProductImageRequest.builder()
                .id("image-1")
                .url(null) // Empty URL triggers deletion
                .productId("product-1")
                .build();
        
        List<UpdateListProductImageRequest> requests = Arrays.asList(deleteRequest);
        when(productImageRepository.existsById("image-1")).thenReturn(true);

        // When
        assertDoesNotThrow(() -> productImageService.updateListProductImage(requests));

        // Then
        verify(productImageRepository).existsById("image-1");
        verify(productImageRepository).deleteById("image-1");
    }

    @Test
    void updateListProductImage_DeleteImage_EmptyUrl() {
        // Given
        UpdateListProductImageRequest deleteRequest = UpdateListProductImageRequest.builder()
                .id("image-1")
                .url("") // Empty string URL triggers deletion
                .productId("product-1")
                .build();
        
        List<UpdateListProductImageRequest> requests = Arrays.asList(deleteRequest);
        when(productImageRepository.existsById("image-1")).thenReturn(true);

        // When
        assertDoesNotThrow(() -> productImageService.updateListProductImage(requests));

        // Then
        verify(productImageRepository).existsById("image-1");
        verify(productImageRepository).deleteById("image-1");
    }

    @Test
    void updateListProductImage_DeleteImage_NotFound() {
        // Given
        UpdateListProductImageRequest deleteRequest = UpdateListProductImageRequest.builder()
                .id("non-existent")
                .url(null)
                .productId("product-1")
                .build();
        
        List<UpdateListProductImageRequest> requests = Arrays.asList(deleteRequest);
        when(productImageRepository.existsById("non-existent")).thenReturn(false);

        // When & Then
        assertThrows(ProductImageNotFoundException.class, 
                () -> productImageService.updateListProductImage(requests));
        verify(productImageRepository).existsById("non-existent");
        verify(productImageRepository, never()).deleteById("non-existent");
    }

    @Test
    void updateListProductImage_MixedOperations() {
        // Given
        UpdateListProductImageRequest createRequest = UpdateListProductImageRequest.builder()
                .id(null)
                .url("https://example.com/new-image.jpg")
                .productId("product-1")
                .build();
        
        UpdateListProductImageRequest deleteRequest = UpdateListProductImageRequest.builder()
                .id("image-1")
                .url(null)
                .productId("product-1")
                .build();
        
        UpdateListProductImageRequest noOpRequest = UpdateListProductImageRequest.builder()
                .id("image-2")
                .url("https://example.com/existing-image.jpg")
                .productId("product-1")
                .build();
        
        List<UpdateListProductImageRequest> requests = Arrays.asList(createRequest, deleteRequest, noOpRequest);
        
        when(productImageMapper.toEntity(createRequest)).thenReturn(productImage);
        when(productImageRepository.save(productImage)).thenReturn(productImage);
        when(productImageRepository.existsById("image-1")).thenReturn(true);

        // When
        assertDoesNotThrow(() -> productImageService.updateListProductImage(requests));

        // Then
        verify(productImageMapper).toEntity(createRequest);
        verify(productImageRepository).save(productImage);
        verify(productImageRepository).existsById("image-1");
        verify(productImageRepository).deleteById("image-1");
        // No operations for noOpRequest since it has both ID and URL
    }

    @Test
    void updateListProductImage_DatabaseError() {
        // Given
        UpdateListProductImageRequest createRequest = UpdateListProductImageRequest.builder()
                .id(null)
                .url("https://example.com/new-image.jpg")
                .productId("product-1")
                .build();
        
        List<UpdateListProductImageRequest> requests = Arrays.asList(createRequest);
        when(productImageMapper.toEntity(createRequest)).thenReturn(productImage);
        when(productImageRepository.save(productImage)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, 
                () -> productImageService.updateListProductImage(requests));
        verify(productImageRepository).save(productImage);
    }

    @Test
    void updateListProductImage_UnexpectedError() {
        // Given
        UpdateListProductImageRequest createRequest = UpdateListProductImageRequest.builder()
                .id(null)
                .url("https://example.com/new-image.jpg")
                .productId("product-1")
                .build();
        
        List<UpdateListProductImageRequest> requests = Arrays.asList(createRequest);
        when(productImageMapper.toEntity(createRequest)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> productImageService.updateListProductImage(requests));
        verify(productImageMapper).toEntity(createRequest);
    }

    @Test
    void updateListProductImage_EmptyList() {
        // Given
        List<UpdateListProductImageRequest> requests = Collections.emptyList();

        // When
        assertDoesNotThrow(() -> productImageService.updateListProductImage(requests));

        // Then
        verifyNoInteractions(productImageRepository);
        verifyNoInteractions(productImageMapper);
    }

    @Test
    void findProductImageById_Success() {
        // Given
        String id = "image-1";
        when(productImageRepository.findById(id)).thenReturn(Optional.of(productImage));

        // When
        ProductImage result = productImageService.findProductImageById(id);

        // Then
        assertNotNull(result);
        assertEquals(productImage, result);
        verify(productImageRepository).findById(id);
    }

    @Test
    void findProductImageById_NotFound() {
        // Given
        String id = "non-existent";
        when(productImageRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductImageNotFoundException.class, 
                () -> productImageService.findProductImageById(id));
        verify(productImageRepository).findById(id);
    }

    @Test
    void findProductImagesByProductId_Success() {
        // Given
        String productId = "product-1";
        List<ProductImage> productImages = Arrays.asList(productImage);
        when(productImageRepository.findByProductId(productId)).thenReturn(productImages);

        // When
        List<ProductImage> result = productImageService.findProductImagesByProductId(productId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(productImage, result.get(0));
        verify(productImageRepository).findByProductId(productId);
    }

    @Test
    void findProductImagesByProductId_NotFound() {
        // Given
        String productId = "non-existent";
        when(productImageRepository.findByProductId(productId)).thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(ProductImageNotFoundException.class, 
                () -> productImageService.findProductImagesByProductId(productId));
        verify(productImageRepository).findByProductId(productId);
    }

    @Test
    void doesProductImageExistById_True() {
        // Given
        String id = "image-1";
        when(productImageRepository.existsById(id)).thenReturn(true);

        // When
        boolean result = productImageService.doesProductImageExistById(id);

        // Then
        assertFalse(result); // Method returns negation of existsById
        verify(productImageRepository).existsById(id);
    }

    @Test
    void doesProductImageExistById_False() {
        // Given
        String id = "non-existent";
        when(productImageRepository.existsById(id)).thenReturn(false);

        // When
        boolean result = productImageService.doesProductImageExistById(id);

        // Then
        assertTrue(result); // Method returns negation of existsById
        verify(productImageRepository).existsById(id);
    }

    @Test
    void checkProductImageExists_NotExists() {
        // Given
        String url = "https://example.com/new-image.jpg";
        String productId = "product-1";
        when(productImageRepository.existsByUrlAndProductId(url, productId)).thenReturn(false);

        // When
        assertDoesNotThrow(() -> productImageService.checkProductImageExists(url, productId));

        // Then
        verify(productImageRepository).existsByUrlAndProductId(url, productId);
    }

    @Test
    void checkProductImageExists_Exists() {
        // Given
        String url = "https://example.com/existing-image.jpg";
        String productId = "product-1";
        when(productImageRepository.existsByUrlAndProductId(url, productId)).thenReturn(true);

        // When & Then
        assertThrows(ProductImageAlreadyExistsException.class, 
                () -> productImageService.checkProductImageExists(url, productId));
        verify(productImageRepository).existsByUrlAndProductId(url, productId);
    }

    @Test
    void getAllProductImages_MapperError() {
        // Given
        List<ProductImage> productImages = Arrays.asList(productImage);
        when(productImageRepository.findAll()).thenReturn(productImages);
        when(productImageMapper.toResponse(productImage)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productImageService.getAllProductImages());
        verify(productImageMapper).toResponse(productImage);
    }

    @Test
    void getProductImageById_MapperError() {
        // Given
        String id = "image-1";
        when(productImageRepository.findById(id)).thenReturn(Optional.of(productImage));
        when(productImageMapper.toResponse(productImage)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productImageService.getProductImageById(id));
        verify(productImageMapper).toResponse(productImage);
    }

    @Test
    void createProductImage_MapperResponseError() {
        // Given
        when(productImageRepository.existsByUrlAndProductId("https://example.com/image1.jpg", "product-1"))
                .thenReturn(false);
        when(productImageMapper.toEntity(createProductImageRequest)).thenReturn(productImage);
        when(productImageRepository.save(productImage)).thenReturn(productImage);
        when(productImageMapper.toResponse(productImage)).thenThrow(new RuntimeException("Response mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> productImageService.createProductImage(createProductImageRequest));
        verify(productImageMapper).toResponse(productImage);
    }

    @Test
    void updateProductImage_MapperResponseError() {
        // Given
        String id = "image-1";
        when(productImageRepository.findById(id)).thenReturn(Optional.of(productImage));
        when(productImageRepository.existsByUrlAndProductId("https://example.com/updated-image.jpg", "product-1"))
                .thenReturn(false);
        when(productImageRepository.save(productImage)).thenReturn(productImage);
        when(productImageMapper.toResponse(productImage)).thenThrow(new RuntimeException("Response mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> productImageService.updateProductImage(id, updateProductImageRequest));
        verify(productImageMapper).toResponse(productImage);
    }

    @Test
    void getProductImagesByProductId_MapperError() {
        // Given
        String productId = "product-1";
        List<ProductImage> productImages = Arrays.asList(productImage);
        when(productImageRepository.findByProductId(productId)).thenReturn(productImages);
        when(productImageMapper.toResponse(productImage)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> productImageService.getProductImagesByProductId(productId));
        verify(productImageMapper).toResponse(productImage);
    }

    @Test
    void getProductImagesByProductId_MultipleImages() {
        // Given
        String productId = "product-1";
        
        ProductImage image2 = new ProductImage();
        image2.setId("image-2");
        image2.setUrl("https://example.com/image2.jpg");
        image2.setProduct(product);

        ProductImageResponse response2 = ProductImageResponse.builder()
                .id("image-2")
                .url("https://example.com/image2.jpg")
                .productId("product-1")
                .build();

        List<ProductImage> productImages = Arrays.asList(productImage, image2);
        when(productImageRepository.findByProductId(productId)).thenReturn(productImages);
        when(productImageMapper.toResponse(productImage)).thenReturn(productImageResponse);
        when(productImageMapper.toResponse(image2)).thenReturn(response2);

        // When
        List<ProductImageResponse> result = productImageService.getProductImagesByProductId(productId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(productImageResponse));
        assertTrue(result.contains(response2));
        verify(productImageMapper, times(2)).toResponse(any(ProductImage.class));
    }

    @Test
    void updateProductImage_MapperUpdateError() {
        // Given
        String id = "image-1";
        when(productImageRepository.findById(id)).thenReturn(Optional.of(productImage));
        when(productImageRepository.existsByUrlAndProductId("https://example.com/updated-image.jpg", "product-1"))
                .thenReturn(false);
        doThrow(new RuntimeException("Update mapping error")).when(productImageMapper)
                .updateEntity(updateProductImageRequest, productImage);

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> productImageService.updateProductImage(id, updateProductImageRequest));
        verify(productImageMapper).updateEntity(updateProductImageRequest, productImage);
    }
}
