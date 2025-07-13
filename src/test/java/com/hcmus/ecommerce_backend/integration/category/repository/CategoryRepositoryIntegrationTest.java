package com.hcmus.ecommerce_backend.integration.category.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.category.repository.CategoryRepository;

@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory1;
    private Category testCategory2;
    private Category testCategory3;

    @BeforeEach
    void setUp() {
        // Create test categories
        testCategory1 = Category.builder()
                .name("T-Shirts")
                .description("Comfortable cotton t-shirts for everyday wear")
                .stock(100)
                .build();
        testCategory1.setCreatedAt(LocalDateTime.now().minusDays(3));
        testCategory1.setUpdatedAt(LocalDateTime.now().minusDays(3));

        testCategory2 = Category.builder()
                .name("Hoodies")
                .description("Warm and cozy hoodies for cold weather")
                .stock(50)
                .build();
        testCategory2.setCreatedAt(LocalDateTime.now().minusDays(2));
        testCategory2.setUpdatedAt(LocalDateTime.now().minusDays(2));

        testCategory3 = Category.builder()
                .name("Accessories")
                .description("Fashion accessories including bags, belts, and more")
                .stock(200)
                .build();
        testCategory3.setCreatedAt(LocalDateTime.now().minusDays(1));
        testCategory3.setUpdatedAt(LocalDateTime.now().minusDays(1));
    }

    // Test JpaRepository inherited methods
    @Test
    void save_WithValidCategory_ShouldPersistCategory() {
        // Act
        Category savedCategory = categoryRepository.save(testCategory1);

        // Assert
        assertNotNull(savedCategory.getId());
        assertEquals(testCategory1.getName(), savedCategory.getName());
        assertEquals(testCategory1.getDescription(), savedCategory.getDescription());
        assertEquals(testCategory1.getStock(), savedCategory.getStock());
        assertNotNull(savedCategory.getCreatedAt());
        assertNotNull(savedCategory.getUpdatedAt());

        // Verify persistence
        entityManager.flush();
        Category foundCategory = entityManager.find(Category.class, savedCategory.getId());
        assertNotNull(foundCategory);
        assertEquals(savedCategory.getName(), foundCategory.getName());
    }

    @Test
    void findById_WithExistingId_ShouldReturnCategory() {
        // Arrange
        Category savedCategory = entityManager.persistAndFlush(testCategory1);

        // Act
        Optional<Category> foundCategory = categoryRepository.findById(savedCategory.getId());

        // Assert
        assertTrue(foundCategory.isPresent());
        assertEquals(savedCategory.getId(), foundCategory.get().getId());
        assertEquals(savedCategory.getName(), foundCategory.get().getName());
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnEmpty() {
        // Act
        Optional<Category> foundCategory = categoryRepository.findById("non-existent-id");

        // Assert
        assertFalse(foundCategory.isPresent());
    }

    @Test
    void findAll_WithMultipleCategories_ShouldReturnAllCategories() {
        // Arrange
        entityManager.persistAndFlush(testCategory1);
        entityManager.persistAndFlush(testCategory2);
        entityManager.persistAndFlush(testCategory3);

        // Act
        List<Category> allCategories = categoryRepository.findAll();

        // Assert
        assertEquals(3, allCategories.size());
    }

    @Test
    void findAll_WithPagination_ShouldReturnPaginatedResults() {
        // Arrange
        entityManager.persistAndFlush(testCategory1);
        entityManager.persistAndFlush(testCategory2);
        entityManager.persistAndFlush(testCategory3);
        Pageable pageable = PageRequest.of(0, 2, Sort.by("createdAt").descending());

        // Act
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        // Assert
        assertEquals(2, categoryPage.getContent().size());
        assertEquals(3, categoryPage.getTotalElements());
        assertEquals(2, categoryPage.getTotalPages());
        assertTrue(categoryPage.hasNext());
        assertFalse(categoryPage.hasPrevious());
    }

    @Test
    void findAll_WithSorting_ShouldReturnSortedResults() {
        // Arrange
        entityManager.persistAndFlush(testCategory1);
        entityManager.persistAndFlush(testCategory2);
        entityManager.persistAndFlush(testCategory3);
        Sort sort = Sort.by("name").ascending();

        // Act
        List<Category> sortedCategories = categoryRepository.findAll(sort);

        // Assert
        assertEquals(3, sortedCategories.size());
        assertEquals("Accessories", sortedCategories.get(0).getName());
        assertEquals("Hoodies", sortedCategories.get(1).getName());
        assertEquals("T-Shirts", sortedCategories.get(2).getName());
    }

    @Test
    void existsById_WithExistingId_ShouldReturnTrue() {
        // Arrange
        Category savedCategory = entityManager.persistAndFlush(testCategory1);

        // Act
        boolean exists = categoryRepository.existsById(savedCategory.getId());

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsById_WithNonExistentId_ShouldReturnFalse() {
        // Act
        boolean exists = categoryRepository.existsById("non-existent-id");

        // Assert
        assertFalse(exists);
    }

    @Test
    void count_WithMultipleCategories_ShouldReturnCorrectCount() {
        // Arrange
        entityManager.persistAndFlush(testCategory1);
        entityManager.persistAndFlush(testCategory2);

        // Act
        long count = categoryRepository.count();

        // Assert
        assertEquals(2, count);
    }

    @Test
    void deleteById_WithExistingId_ShouldRemoveCategory() {
        // Arrange
        Category savedCategory = entityManager.persistAndFlush(testCategory1);
        String categoryId = savedCategory.getId();

        // Act
        categoryRepository.deleteById(categoryId);

        // Assert
        entityManager.flush();
        assertFalse(categoryRepository.existsById(categoryId));
    }

    @Test
    void delete_WithCategoryEntity_ShouldRemoveCategory() {
        // Arrange
        Category savedCategory = entityManager.persistAndFlush(testCategory1);

        // Act
        categoryRepository.delete(savedCategory);

        // Assert
        entityManager.flush();
        assertFalse(categoryRepository.existsById(savedCategory.getId()));
    }

    @Test
    void deleteAll_WithMultipleCategories_ShouldRemoveAllCategories() {
        // Arrange
        entityManager.persistAndFlush(testCategory1);
        entityManager.persistAndFlush(testCategory2);
        entityManager.persistAndFlush(testCategory3);

        // Act
        categoryRepository.deleteAll();

        // Assert
        entityManager.flush();
        assertEquals(0, categoryRepository.count());
    }

    // Test custom repository methods
    @Test
    void existsByName_WithExistingName_ShouldReturnTrue() {
        // Arrange
        entityManager.persistAndFlush(testCategory1);

        // Act
        boolean exists = categoryRepository.existsByName("T-Shirts");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByName_WithNonExistentName_ShouldReturnFalse() {
        // Arrange
        entityManager.persistAndFlush(testCategory1);

        // Act
        boolean exists = categoryRepository.existsByName("NonExistent");

        // Assert
        assertFalse(exists);
    }

    @Test
    void existsByName_WithEmptyRepository_ShouldReturnFalse() {
        // Act
        boolean exists = categoryRepository.existsByName("Any Name");

        // Assert
        assertFalse(exists);
    }

    @Test
    void existsByName_WithNullName_ShouldReturnFalse() {
        // Act
        boolean exists = categoryRepository.existsByName(null);

        // Assert
        assertFalse(exists);
    }

    @Test
    void searchByKeyword_WithMatchingKeywordInName_ShouldReturnCategories() {
        // Arrange
        entityManager.persistAndFlush(testCategory1);
        entityManager.persistAndFlush(testCategory2);
        entityManager.persistAndFlush(testCategory3);
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Category> matchingCategories = categoryRepository.searchByKeyword("shirt", pageable);

        // Assert
        assertEquals(1, matchingCategories.getContent().size());
        assertEquals("T-Shirts", matchingCategories.getContent().get(0).getName());
    }

    @Test
    void searchByKeyword_WithMatchingKeywordInDescription_ShouldReturnCategories() {
        // Arrange
        entityManager.persistAndFlush(testCategory1);
        entityManager.persistAndFlush(testCategory2);
        entityManager.persistAndFlush(testCategory3);
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Category> matchingCategories = categoryRepository.searchByKeyword("cotton", pageable);

        // Assert
        assertEquals(1, matchingCategories.getContent().size());
        assertEquals("T-Shirts", matchingCategories.getContent().get(0).getName());
    }

    @Test
    void searchByKeyword_WithPartialMatch_ShouldReturnCategories() {
        // Arrange
        entityManager.persistAndFlush(testCategory1);
        entityManager.persistAndFlush(testCategory2);
        entityManager.persistAndFlush(testCategory3);
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Category> matchingCategories = categoryRepository.searchByKeyword("hood", pageable);

        // Assert
        assertEquals(1, matchingCategories.getContent().size());
        assertEquals("Hoodies", matchingCategories.getContent().get(0).getName());
    }

    @Test
    void searchByKeyword_WithCaseInsensitiveMatch_ShouldReturnCategories() {
        // Arrange
        entityManager.persistAndFlush(testCategory1);
        entityManager.persistAndFlush(testCategory2);
        entityManager.persistAndFlush(testCategory3);
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Category> matchingCategories = categoryRepository.searchByKeyword("SHIRT", pageable);

        // Assert
        assertEquals(1, matchingCategories.getContent().size());
        assertEquals("T-Shirts", matchingCategories.getContent().get(0).getName());
    }

    @Test
    void searchByKeyword_WithNonMatchingKeyword_ShouldReturnEmpty() {
        // Arrange
        entityManager.persistAndFlush(testCategory1);
        entityManager.persistAndFlush(testCategory2);
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Category> matchingCategories = categoryRepository.searchByKeyword("nonexistent", pageable);

        // Assert
        assertTrue(matchingCategories.getContent().isEmpty());
    }

    @Test
    void searchByKeyword_WithEmptyRepository_ShouldReturnEmpty() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Category> matchingCategories = categoryRepository.searchByKeyword("any", pageable);

        // Assert
        assertTrue(matchingCategories.getContent().isEmpty());
    }

    @Test
    void searchByKeyword_WithNullKeyword_ShouldReturnEmpty() {
        // Arrange
        entityManager.persistAndFlush(testCategory1);
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Category> matchingCategories = categoryRepository.searchByKeyword(null, pageable);

        // Assert
        assertTrue(matchingCategories.getContent().isEmpty());
    }

    @Test
    void searchByKeyword_WithEmptyKeyword_ShouldReturnAllCategories() {
        // Arrange
        entityManager.persistAndFlush(testCategory1);
        entityManager.persistAndFlush(testCategory2);
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Category> matchingCategories = categoryRepository.searchByKeyword("", pageable);

        // Assert
        assertEquals(2, matchingCategories.getContent().size());
    }

    @Test
    void searchByKeyword_WithPagination_ShouldReturnPaginatedResults() {
        // Arrange
        Category category1 = Category.builder()
                .name("Shirts Category")
                .description("All kinds of shirts")
                .stock(10)
                .build();
        Category category2 = Category.builder()
                .name("Another Shirts")
                .description("More shirts")
                .stock(20)
                .build();
        entityManager.persistAndFlush(category1);
        entityManager.persistAndFlush(category2);
        
        Pageable pageable = PageRequest.of(0, 1, Sort.by("name"));

        // Act
        Page<Category> matchingCategories = categoryRepository.searchByKeyword("shirts", pageable);

        // Assert
        assertEquals(1, matchingCategories.getContent().size());
        assertEquals(2, matchingCategories.getTotalElements());
        assertEquals(2, matchingCategories.getTotalPages());
        assertTrue(matchingCategories.hasNext());
    }

    // Edge cases and boundary tests
    @Test
    void save_WithNullDescription_ShouldSaveSuccessfully() {
        // Arrange
        testCategory1.setDescription(null);

        // Act
        Category savedCategory = categoryRepository.save(testCategory1);

        // Assert
        assertNotNull(savedCategory.getId());
        assertNull(savedCategory.getDescription());
    }

    @Test
    void save_WithZeroStock_ShouldSaveSuccessfully() {
        // Arrange
        testCategory1.setStock(0);

        // Act
        Category savedCategory = categoryRepository.save(testCategory1);

        // Assert
        assertNotNull(savedCategory.getId());
        assertEquals(0, savedCategory.getStock());
    }

    @Test
    void save_WithNegativeStock_ShouldSaveSuccessfully() {
        // Arrange
        testCategory1.setStock(-10);

        // Act
        Category savedCategory = categoryRepository.save(testCategory1);

        // Assert
        assertNotNull(savedCategory.getId());
        assertEquals(-10, savedCategory.getStock());
    }

    @Test
    void save_WithVeryLongDescription_ShouldSaveSuccessfully() {
        // Arrange
        String longDescription = "A".repeat(1000);
        testCategory1.setDescription(longDescription);

        // Act
        Category savedCategory = categoryRepository.save(testCategory1);

        // Assert
        assertNotNull(savedCategory.getId());
        assertEquals(longDescription, savedCategory.getDescription());
    }

    @Test
    void existsByName_WithSpecialCharacters_ShouldWork() {
        // Arrange
        Category specialCategory = Category.builder()
                .name("T-Shirts & Polos")
                .description("Special category")
                .stock(30)
                .build();
        entityManager.persistAndFlush(specialCategory);

        // Act
        boolean exists = categoryRepository.existsByName("T-Shirts & Polos");

        // Assert
        assertTrue(exists);
    }

    @Test
    void searchByKeyword_WithSpecialCharacters_ShouldReturnCategories() {
        // Arrange
        Category specialCategory = Category.builder()
                .name("T-Shirts & Polos")
                .description("Special category")
                .stock(30)
                .build();
        entityManager.persistAndFlush(specialCategory);
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Category> matchingCategories = categoryRepository.searchByKeyword("&", pageable);

        // Assert
        assertEquals(1, matchingCategories.getContent().size());
        assertEquals("T-Shirts & Polos", matchingCategories.getContent().get(0).getName());
    }

    @Test
    void findAll_WithEmptyDatabase_ShouldReturnEmptyList() {
        // Act
        List<Category> categories = categoryRepository.findAll();

        // Assert
        assertTrue(categories.isEmpty());
    }

    @Test
    void save_WithDuplicateNameDifferentCase_ShouldSaveSuccessfully() {
        // Arrange
        entityManager.persistAndFlush(testCategory1);
        
        Category duplicateCategory = Category.builder()
                .name("t-shirts") // Same name but different case
                .description("Different description")
                .stock(25)
                .build();

        // Act
        Category savedCategory = categoryRepository.save(duplicateCategory);

        // Assert
        assertNotNull(savedCategory.getId());
        assertEquals("t-shirts", savedCategory.getName());
        
        // Verify both exist
        assertEquals(2, categoryRepository.count());
    }

    @Test
    void findAll_WithMultipleCategoriesAndStock_ShouldMaintainDataIntegrity() {
        // Arrange
        Category category1 = Category.builder()
                .name("Category 1")
                .description("Description 1")
                .stock(100)
                .build();
        
        Category category2 = Category.builder()
                .name("Category 2")
                .description("Description 2")
                .stock(50)
                .build();
        
        Category category3 = Category.builder()
                .name("Category 3")
                .description("Description 3")
                .stock(200)
                .build();

        entityManager.persistAndFlush(category1);
        entityManager.persistAndFlush(category2);
        entityManager.persistAndFlush(category3);

        // Act
        List<Category> allCategories = categoryRepository.findAll();

        // Assert
        assertEquals(3, allCategories.size());
        
        // Verify stock values are maintained correctly
        assertTrue(allCategories.stream().anyMatch(cat -> cat.getStock() == 100));
        assertTrue(allCategories.stream().anyMatch(cat -> cat.getStock() == 50));
        assertTrue(allCategories.stream().anyMatch(cat -> cat.getStock() == 200));
    }

    @Test
    void searchByKeyword_WithWhitespaceKeyword_ShouldReturnEmpty() {
        // Arrange
        entityManager.persistAndFlush(testCategory1);
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Category> matchingCategories = categoryRepository.searchByKeyword("   ", pageable);

        // Assert
        assertTrue(matchingCategories.getContent().isEmpty());
    }

    @Test
    void existsByName_WithWhitespaceInName_ShouldReturnFalse() {
        // Arrange
        entityManager.persistAndFlush(testCategory1);

        // Act
        boolean exists = categoryRepository.existsByName("  T-Shirts  ");

        // Assert
        assertFalse(exists);
    }

    @Test
    void save_WithMinimumRequiredFields_ShouldSaveSuccessfully() {
        // Arrange
        Category minimalCategory = Category.builder()
                .name("Minimal Category")
                .build();

        // Act
        Category savedCategory = categoryRepository.save(minimalCategory);

        // Assert
        assertNotNull(savedCategory.getId());
        assertEquals("Minimal Category", savedCategory.getName());
        assertNull(savedCategory.getDescription());
        assertEquals(0, savedCategory.getStock()); // Default value
    }

    @Test
    void searchByKeyword_WithNumericKeyword_ShouldWork() {
        // Arrange
        Category numericCategory = Category.builder()
                .name("Category 123")
                .description("Contains numbers")
                .stock(123)
                .build();
        entityManager.persistAndFlush(numericCategory);
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Category> matchingCategories = categoryRepository.searchByKeyword("123", pageable);

        // Assert
        assertEquals(1, matchingCategories.getContent().size());
        assertEquals("Category 123", matchingCategories.getContent().get(0).getName());
    }
}