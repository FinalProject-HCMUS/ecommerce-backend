package com.hcmus.ecommerce_backend.unit.category.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.hcmus.ecommerce_backend.category.exception.CategoryAlreadyExistsException;
import com.hcmus.ecommerce_backend.category.exception.CategoryNotFoundException;
import com.hcmus.ecommerce_backend.category.model.dto.request.CreateCategoryRequest;
import com.hcmus.ecommerce_backend.category.model.dto.request.UpdateCategoryRequest;
import com.hcmus.ecommerce_backend.category.model.dto.response.CategoryResponse;
import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.category.model.mapper.CategoryMapper;
import com.hcmus.ecommerce_backend.category.repository.CategoryRepository;
import com.hcmus.ecommerce_backend.category.service.impl.CategoryServiceImpl;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category testCategory;
    private CategoryResponse testCategoryResponse;
    private CreateCategoryRequest createCategoryRequest;
    private UpdateCategoryRequest updateCategoryRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id("category-id-1")
                .name("Electronics")
                .description("Electronic devices and gadgets")
                .stock(100)
                .build();
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());

        testCategoryResponse = CategoryResponse.builder()
                .id("category-id-1")
                .name("Electronics")
                .description("Electronic devices and gadgets")
                .stock(100)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createCategoryRequest = CreateCategoryRequest.builder()
                .name("Home Appliances")
                .description("Appliances for household use")
                .stock(50)
                .build();

        updateCategoryRequest = UpdateCategoryRequest.builder()
                .name("Updated Electronics")
                .description("Updated electronic devices")
                .stock(150)
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void searchCategories_WithoutKeyword_ShouldReturnAllCategories() {
        // Arrange
        Page<Category> categoryPage = new PageImpl<>(Arrays.asList(testCategory));
        
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(categoryMapper.toResponse(testCategory)).thenReturn(testCategoryResponse);

        // Act
        Page<CategoryResponse> result = categoryService.searchCategories(pageable, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testCategoryResponse.getId(), result.getContent().get(0).getId());
        verify(categoryRepository).findAll(pageable);
        verify(categoryMapper).toResponse(testCategory);
    }

    @Test
    void searchCategories_WithEmptyKeyword_ShouldReturnAllCategories() {
        // Arrange
        Page<Category> categoryPage = new PageImpl<>(Arrays.asList(testCategory));
        
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(categoryMapper.toResponse(testCategory)).thenReturn(testCategoryResponse);

        // Act
        Page<CategoryResponse> result = categoryService.searchCategories(pageable, "");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(categoryRepository).findAll(pageable);
        verify(categoryMapper).toResponse(testCategory);
    }

    @Test
    void searchCategories_WithKeyword_ShouldReturnFilteredCategories() {
        // Arrange
        String keyword = "electronics";
        Page<Category> categoryPage = new PageImpl<>(Arrays.asList(testCategory));
        
        when(categoryRepository.searchByKeyword(keyword, pageable)).thenReturn(categoryPage);
        when(categoryMapper.toResponse(testCategory)).thenReturn(testCategoryResponse);

        // Act
        Page<CategoryResponse> result = categoryService.searchCategories(pageable, keyword);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testCategoryResponse.getName(), result.getContent().get(0).getName());
        verify(categoryRepository).searchByKeyword(keyword, pageable);
        verify(categoryMapper).toResponse(testCategory);
    }

    @Test
    void searchCategories_WhenDatabaseError_ShouldReturnEmptyPage() {
        // Arrange
        when(categoryRepository.findAll(pageable)).thenThrow(new DataAccessException("Database error") {});

        // Act
        Page<CategoryResponse> result = categoryService.searchCategories(pageable, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(categoryRepository).findAll(pageable);
    }

    @Test
    void searchCategories_WhenUnexpectedError_ShouldThrowException() {
        // Arrange
        when(categoryRepository.findAll(pageable)).thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> categoryService.searchCategories(pageable, null));
        verify(categoryRepository).findAll(pageable);
    }

    @Test
    void getAllCategoriesWithoutPagination_ShouldReturnAllCategories() {
        // Arrange
        List<Category> categories = Arrays.asList(testCategory);
        
        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryMapper.toResponse(testCategory)).thenReturn(testCategoryResponse);

        // Act
        List<CategoryResponse> result = categoryService.getAllCategoriesWithoutPagination();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCategoryResponse.getId(), result.get(0).getId());
        verify(categoryRepository).findAll();
        verify(categoryMapper).toResponse(testCategory);
    }

    @Test
    void getAllCategoriesWithoutPagination_WhenEmpty_ShouldReturnEmptyList() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<CategoryResponse> result = categoryService.getAllCategoriesWithoutPagination();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(categoryRepository).findAll();
        verify(categoryMapper, never()).toResponse(any());
    }

    @Test
    void getCategoryById_WithValidId_ShouldReturnCategoryResponse() {
        // Arrange
        String categoryId = "category-id-1";
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryMapper.toResponse(testCategory)).thenReturn(testCategoryResponse);

        // Act
        CategoryResponse result = categoryService.getCategoryById(categoryId);

        // Assert
        assertNotNull(result);
        assertEquals(testCategoryResponse.getId(), result.getId());
        assertEquals(testCategoryResponse.getName(), result.getName());
        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper).toResponse(testCategory);
    }

    @Test
    void getCategoryById_WithInvalidId_ShouldThrowCategoryNotFoundException() {
        // Arrange
        String categoryId = "invalid-id";
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CategoryNotFoundException.class, () -> categoryService.getCategoryById(categoryId));
        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper, never()).toResponse(any());
    }

    @Test
    void getCategoryById_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        String categoryId = "category-id-1";
        when(categoryRepository.findById(categoryId)).thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, () -> categoryService.getCategoryById(categoryId));
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void createCategory_WithValidRequest_ShouldReturnCreatedCategory() {
        // Arrange
        when(categoryRepository.existsByName(createCategoryRequest.getName())).thenReturn(false);
        when(categoryMapper.toEntity(createCategoryRequest)).thenReturn(testCategory);
        when(categoryRepository.save(testCategory)).thenReturn(testCategory);
        when(categoryMapper.toResponse(testCategory)).thenReturn(testCategoryResponse);

        // Act
        CategoryResponse result = categoryService.createCategory(createCategoryRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testCategoryResponse.getId(), result.getId());
        verify(categoryRepository).existsByName(createCategoryRequest.getName());
        verify(categoryRepository).save(testCategory);
        verify(categoryMapper).toEntity(createCategoryRequest);
        verify(categoryMapper).toResponse(testCategory);
    }

    @Test
    void createCategory_WithDuplicateName_ShouldThrowCategoryAlreadyExistsException() {
        // Arrange
        when(categoryRepository.existsByName(createCategoryRequest.getName())).thenReturn(true);

        // Act & Assert
        assertThrows(CategoryAlreadyExistsException.class, 
                () -> categoryService.createCategory(createCategoryRequest));
        verify(categoryRepository).existsByName(createCategoryRequest.getName());
        verify(categoryRepository, never()).save(any());
        verify(categoryMapper, never()).toEntity(any());
    }

    @Test
    void createCategory_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        when(categoryRepository.existsByName(createCategoryRequest.getName())).thenReturn(false);
        when(categoryMapper.toEntity(createCategoryRequest)).thenReturn(testCategory);
        when(categoryRepository.save(testCategory)).thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, () -> categoryService.createCategory(createCategoryRequest));
        verify(categoryRepository).save(testCategory);
    }

    @Test
    void updateCategory_WithValidRequest_ShouldReturnUpdatedCategory() {
        // Arrange
        String categoryId = "category-id-1";
        Category updatedCategory = Category.builder()
                .id(categoryId)
                .name(updateCategoryRequest.getName())
                .description(updateCategoryRequest.getDescription())
                .stock(updateCategoryRequest.getStock())
                .build();
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(testCategory)).thenReturn(updatedCategory);
        when(categoryMapper.toResponse(updatedCategory)).thenReturn(testCategoryResponse);

        // Act
        CategoryResponse result = categoryService.updateCategory(categoryId, updateCategoryRequest);

        // Assert
        assertNotNull(result);
        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper).updateEntity(updateCategoryRequest, testCategory);
        verify(categoryRepository).save(testCategory);
        verify(categoryMapper).toResponse(updatedCategory);
    }

    @Test
    void updateCategory_WithDifferentName_ShouldCheckForDuplicates() {
        // Arrange
        String categoryId = "category-id-1";
        testCategory.setName("Different Name");
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName(updateCategoryRequest.getName())).thenReturn(false);
        when(categoryRepository.save(testCategory)).thenReturn(testCategory);
        when(categoryMapper.toResponse(testCategory)).thenReturn(testCategoryResponse);

        // Act
        CategoryResponse result = categoryService.updateCategory(categoryId, updateCategoryRequest);

        // Assert
        assertNotNull(result);
        verify(categoryRepository).existsByName(updateCategoryRequest.getName());
    }

    @Test
    void updateCategory_WithSameName_ShouldNotCheckForDuplicates() {
        // Arrange
        String categoryId = "category-id-1";
        updateCategoryRequest.setName(testCategory.getName());
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(testCategory)).thenReturn(testCategory);
        when(categoryMapper.toResponse(testCategory)).thenReturn(testCategoryResponse);

        // Act
        CategoryResponse result = categoryService.updateCategory(categoryId, updateCategoryRequest);

        // Assert
        assertNotNull(result);
        verify(categoryRepository, never()).existsByName(any());
    }

    @Test
    void updateCategory_WithDuplicateName_ShouldThrowCategoryAlreadyExistsException() {
        // Arrange
        String categoryId = "category-id-1";
        testCategory.setName("Different Name");
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.existsByName(updateCategoryRequest.getName())).thenReturn(true);

        // Act & Assert
        assertThrows(CategoryAlreadyExistsException.class, 
                () -> categoryService.updateCategory(categoryId, updateCategoryRequest));
        verify(categoryRepository).existsByName(updateCategoryRequest.getName());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_WithInvalidId_ShouldThrowCategoryNotFoundException() {
        // Arrange
        String categoryId = "invalid-id";
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CategoryNotFoundException.class, 
                () -> categoryService.updateCategory(categoryId, updateCategoryRequest));
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        String categoryId = "category-id-1";
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(testCategory)).thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, 
                () -> categoryService.updateCategory(categoryId, updateCategoryRequest));
        verify(categoryRepository).save(testCategory);
    }

    @Test
    void deleteCategory_WithValidId_ShouldDeleteCategory() {
        // Arrange
        String categoryId = "category-id-1";
        when(categoryRepository.existsById(categoryId)).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> categoryService.deleteCategory(categoryId));

        // Assert
        verify(categoryRepository).existsById(categoryId);
        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    void deleteCategory_WithInvalidId_ShouldThrowCategoryNotFoundException() {
        // Arrange
        String categoryId = "invalid-id";
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        // Act & Assert
        assertThrows(CategoryNotFoundException.class, () -> categoryService.deleteCategory(categoryId));
        verify(categoryRepository).existsById(categoryId);
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void deleteCategory_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        String categoryId = "category-id-1";
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        doThrow(new DataAccessException("Database error") {}).when(categoryRepository).deleteById(categoryId);

        // Act & Assert
        assertThrows(DataAccessException.class, () -> categoryService.deleteCategory(categoryId));
        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    void deleteCategory_WhenUnexpectedError_ShouldThrowException() {
        // Arrange
        String categoryId = "category-id-1";
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(categoryRepository).deleteById(categoryId);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> categoryService.deleteCategory(categoryId));
        verify(categoryRepository).deleteById(categoryId);
    }
}
