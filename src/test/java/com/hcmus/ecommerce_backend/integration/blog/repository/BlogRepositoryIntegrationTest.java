package com.hcmus.ecommerce_backend.integration.blog.repository;

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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import com.hcmus.ecommerce_backend.blog.model.entity.Blog;
import com.hcmus.ecommerce_backend.blog.repository.BlogRepository;

@DataJpaTest
@ActiveProfiles("test")
class BlogRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BlogRepository blogRepository;

    private Blog testBlog1;
    private Blog testBlog2;
    private Blog testBlog3;

    @BeforeEach
    void setUp() {
        // Create test blogs
        testBlog1 = Blog.builder()
                .title("Spring Boot Tutorial")
                .content("This is a comprehensive tutorial about Spring Boot framework")
                .image("https://example.com/spring-boot.jpg")
                .userId("user-1")
                .build();
        testBlog1.setCreatedAt(LocalDateTime.now().minusDays(3));
        testBlog1.setUpdatedAt(LocalDateTime.now().minusDays(3));

        testBlog2 = Blog.builder()
                .title("Java Best Practices")
                .content("Learn about Java coding best practices and design patterns")
                .image("https://example.com/java.jpg")
                .userId("user-1")
                .build();
        testBlog2.setCreatedAt(LocalDateTime.now().minusDays(2));
        testBlog2.setUpdatedAt(LocalDateTime.now().minusDays(2));

        testBlog3 = Blog.builder()
                .title("Database Design")
                .content("Understanding database normalization and design principles")
                .image("https://example.com/database.jpg")
                .userId("user-2")
                .build();
        testBlog3.setCreatedAt(LocalDateTime.now().minusDays(1));
        testBlog3.setUpdatedAt(LocalDateTime.now().minusDays(1));
    }

    // Test JpaRepository inherited methods
    @Test
    void save_WithValidBlog_ShouldPersistBlog() {
        // Act
        Blog savedBlog = blogRepository.save(testBlog1);

        // Assert
        assertNotNull(savedBlog.getId());
        assertEquals(testBlog1.getTitle(), savedBlog.getTitle());
        assertEquals(testBlog1.getContent(), savedBlog.getContent());
        assertEquals(testBlog1.getUserId(), savedBlog.getUserId());
        assertEquals(testBlog1.getImage(), savedBlog.getImage());
        assertNotNull(savedBlog.getCreatedAt());
        assertNotNull(savedBlog.getUpdatedAt());

        // Verify persistence
        entityManager.flush();
        Blog foundBlog = entityManager.find(Blog.class, savedBlog.getId());
        assertNotNull(foundBlog);
        assertEquals(savedBlog.getTitle(), foundBlog.getTitle());
    }

    @Test
    void findById_WithExistingId_ShouldReturnBlog() {
        // Arrange
        Blog savedBlog = entityManager.persistAndFlush(testBlog1);

        // Act
        Optional<Blog> foundBlog = blogRepository.findById(savedBlog.getId());

        // Assert
        assertTrue(foundBlog.isPresent());
        assertEquals(savedBlog.getId(), foundBlog.get().getId());
        assertEquals(savedBlog.getTitle(), foundBlog.get().getTitle());
    }

    @Test
    void findById_WithNonExistentId_ShouldReturnEmpty() {
        // Act
        Optional<Blog> foundBlog = blogRepository.findById("non-existent-id");

        // Assert
        assertFalse(foundBlog.isPresent());
    }

    @Test
    void findAll_WithMultipleBlogs_ShouldReturnAllBlogs() {
        // Arrange
        entityManager.persistAndFlush(testBlog1);
        entityManager.persistAndFlush(testBlog2);
        entityManager.persistAndFlush(testBlog3);

        // Act
        List<Blog> allBlogs = blogRepository.findAll();

        // Assert
        assertEquals(3, allBlogs.size());
    }

    @Test
    void findAll_WithPagination_ShouldReturnPaginatedResults() {
        // Arrange
        entityManager.persistAndFlush(testBlog1);
        entityManager.persistAndFlush(testBlog2);
        entityManager.persistAndFlush(testBlog3);
        Pageable pageable = PageRequest.of(0, 2, Sort.by("createdAt").descending());

        // Act
        Page<Blog> blogPage = blogRepository.findAll(pageable);

        // Assert
        assertEquals(2, blogPage.getContent().size());
        assertEquals(3, blogPage.getTotalElements());
        assertEquals(2, blogPage.getTotalPages());
        assertTrue(blogPage.hasNext());
        assertFalse(blogPage.hasPrevious());
    }

    @Test
    void findAll_WithSorting_ShouldReturnSortedResults() {
        // Arrange
        entityManager.persistAndFlush(testBlog1);
        entityManager.persistAndFlush(testBlog2);
        entityManager.persistAndFlush(testBlog3);
        Sort sort = Sort.by("title").ascending();

        // Act
        List<Blog> sortedBlogs = blogRepository.findAll(sort);

        // Assert
        assertEquals(3, sortedBlogs.size());
        assertEquals("Database Design", sortedBlogs.get(0).getTitle());
        assertEquals("Java Best Practices", sortedBlogs.get(1).getTitle());
        assertEquals("Spring Boot Tutorial", sortedBlogs.get(2).getTitle());
    }

    @Test
    void existsById_WithExistingId_ShouldReturnTrue() {
        // Arrange
        Blog savedBlog = entityManager.persistAndFlush(testBlog1);

        // Act
        boolean exists = blogRepository.existsById(savedBlog.getId());

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsById_WithNonExistentId_ShouldReturnFalse() {
        // Act
        boolean exists = blogRepository.existsById("non-existent-id");

        // Assert
        assertFalse(exists);
    }

    @Test
    void count_WithMultipleBlogs_ShouldReturnCorrectCount() {
        // Arrange
        entityManager.persistAndFlush(testBlog1);
        entityManager.persistAndFlush(testBlog2);

        // Act
        long count = blogRepository.count();

        // Assert
        assertEquals(2, count);
    }

    @Test
    void deleteById_WithExistingId_ShouldRemoveBlog() {
        // Arrange
        Blog savedBlog = entityManager.persistAndFlush(testBlog1);
        String blogId = savedBlog.getId();

        // Act
        blogRepository.deleteById(blogId);

        // Assert
        entityManager.flush();
        assertFalse(blogRepository.existsById(blogId));
    }

    @Test
    void delete_WithBlogEntity_ShouldRemoveBlog() {
        // Arrange
        Blog savedBlog = entityManager.persistAndFlush(testBlog1);

        // Act
        blogRepository.delete(savedBlog);

        // Assert
        entityManager.flush();
        assertFalse(blogRepository.existsById(savedBlog.getId()));
    }

    @Test
    void deleteAll_WithMultipleBlogs_ShouldRemoveAllBlogs() {
        // Arrange
        entityManager.persistAndFlush(testBlog1);
        entityManager.persistAndFlush(testBlog2);
        entityManager.persistAndFlush(testBlog3);

        // Act
        blogRepository.deleteAll();

        // Assert
        entityManager.flush();
        assertEquals(0, blogRepository.count());
    }

    // Test custom repository methods
    @Test
    void existsByTitleAndUserId_WithExistingTitleAndUserId_ShouldReturnTrue() {
        // Arrange
        entityManager.persistAndFlush(testBlog1);

        // Act
        boolean exists = blogRepository.existsByTitleAndUserId(testBlog1.getTitle(), testBlog1.getUserId());

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByTitleAndUserId_WithNonExistentTitle_ShouldReturnFalse() {
        // Arrange
        entityManager.persistAndFlush(testBlog1);

        // Act
        boolean exists = blogRepository.existsByTitleAndUserId("Non-existent Title", testBlog1.getUserId());

        // Assert
        assertFalse(exists);
    }

    @Test
    void existsByTitleAndUserId_WithNonExistentUserId_ShouldReturnFalse() {
        // Arrange
        entityManager.persistAndFlush(testBlog1);

        // Act
        boolean exists = blogRepository.existsByTitleAndUserId(testBlog1.getTitle(), "non-existent-user");

        // Assert
        assertFalse(exists);
    }

    @Test
    void existsByTitleAndUserId_WithBothNonExistent_ShouldReturnFalse() {
        // Arrange
        entityManager.persistAndFlush(testBlog1);

        // Act
        boolean exists = blogRepository.existsByTitleAndUserId("Non-existent Title", "non-existent-user");

        // Assert
        assertFalse(exists);
    }

    @Test
    void existsByTitleAndUserId_WithEmptyRepository_ShouldReturnFalse() {
        // Act
        boolean exists = blogRepository.existsByTitleAndUserId("Any Title", "any-user");

        // Assert
        assertFalse(exists);
    }

    @Test
    void findByUserId_WithExistingUserId_ShouldReturnUserBlogs() {
        // Arrange
        entityManager.persistAndFlush(testBlog1);
        entityManager.persistAndFlush(testBlog2);
        entityManager.persistAndFlush(testBlog3);

        // Act
        List<Blog> userBlogs = blogRepository.findByUserId("user-1");

        // Assert
        assertEquals(2, userBlogs.size());
        assertTrue(userBlogs.stream().allMatch(blog -> "user-1".equals(blog.getUserId())));
        assertTrue(userBlogs.stream().anyMatch(blog -> testBlog1.getTitle().equals(blog.getTitle())));
        assertTrue(userBlogs.stream().anyMatch(blog -> testBlog2.getTitle().equals(blog.getTitle())));
    }

    @Test
    void findByUserId_WithNonExistentUserId_ShouldReturnEmptyList() {
        // Arrange
        entityManager.persistAndFlush(testBlog1);
        entityManager.persistAndFlush(testBlog2);

        // Act
        List<Blog> userBlogs = blogRepository.findByUserId("non-existent-user");

        // Assert
        assertTrue(userBlogs.isEmpty());
    }

    @Test
    void findByUserId_WithEmptyRepository_ShouldReturnEmptyList() {
        // Act
        List<Blog> userBlogs = blogRepository.findByUserId("any-user");

        // Assert
        assertTrue(userBlogs.isEmpty());
    }

    @Test
    void findByUserId_WithSingleUserHavingOneBlog_ShouldReturnSingleBlog() {
        // Arrange
        entityManager.persistAndFlush(testBlog3); // user-2 has only one blog

        // Act
        List<Blog> userBlogs = blogRepository.findByUserId("user-2");

        // Assert
        assertEquals(1, userBlogs.size());
        assertEquals(testBlog3.getTitle(), userBlogs.get(0).getTitle());
        assertEquals("user-2", userBlogs.get(0).getUserId());
    }

    // Test JpaSpecificationExecutor methods
    @Test
    void findAll_WithSpecification_ShouldReturnFilteredResults() {
        // Arrange
        entityManager.persistAndFlush(testBlog1);
        entityManager.persistAndFlush(testBlog2);
        entityManager.persistAndFlush(testBlog3);

        Specification<Blog> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%java%");

        // Act
        List<Blog> filteredBlogs = blogRepository.findAll(spec);

        // Assert
        assertEquals(1, filteredBlogs.size());
        assertEquals(testBlog2.getTitle(), filteredBlogs.get(0).getTitle());
    }

    @Test
    void findAll_WithSpecificationAndPagination_ShouldReturnPaginatedFilteredResults() {
        // Arrange
        entityManager.persistAndFlush(testBlog1);
        entityManager.persistAndFlush(testBlog2);
        entityManager.persistAndFlush(testBlog3);

        Specification<Blog> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("userId"), "user-1");
        Pageable pageable = PageRequest.of(0, 1, Sort.by("title"));

        // Act
        Page<Blog> filteredPage = blogRepository.findAll(spec, pageable);

        // Assert
        assertEquals(1, filteredPage.getContent().size());
        assertEquals(2, filteredPage.getTotalElements());
        assertEquals(2, filteredPage.getTotalPages());
        assertEquals("Java Best Practices", filteredPage.getContent().get(0).getTitle());
    }

    @Test
    void count_WithSpecification_ShouldReturnCorrectCount() {
        // Arrange
        entityManager.persistAndFlush(testBlog1);
        entityManager.persistAndFlush(testBlog2);
        entityManager.persistAndFlush(testBlog3);

        Specification<Blog> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("userId"), "user-1");

        // Act
        long count = blogRepository.count(spec);

        // Assert
        assertEquals(2, count);
    }

    @Test
    void findOne_WithSpecification_ShouldReturnSingleResult() {
        // Arrange
        entityManager.persistAndFlush(testBlog1);

        Specification<Blog> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("title"), testBlog1.getTitle());

        // Act
        Optional<Blog> foundBlog = blogRepository.findOne(spec);

        // Assert
        assertTrue(foundBlog.isPresent());
        assertEquals(testBlog1.getTitle(), foundBlog.get().getTitle());
    }

    @Test
    void findOne_WithSpecificationNoMatch_ShouldReturnEmpty() {
        // Arrange
        entityManager.persistAndFlush(testBlog1);

        Specification<Blog> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("title"), "Non-existent Title");

        // Act
        Optional<Blog> foundBlog = blogRepository.findOne(spec);

        // Assert
        assertFalse(foundBlog.isPresent());
    }

    // Edge cases and boundary tests
    @Test
    void save_WithNullImage_ShouldSaveSuccessfully() {
        // Arrange
        testBlog1.setImage(null);

        // Act
        Blog savedBlog = blogRepository.save(testBlog1);

        // Assert
        assertNotNull(savedBlog.getId());
        assertNull(savedBlog.getImage());
    }

    @Test
    void existsByTitleAndUserId_WithNullTitle_ShouldReturnFalse() {
        // Act
        boolean exists = blogRepository.existsByTitleAndUserId(null, "user-1");

        // Assert
        assertFalse(exists);
    }

    @Test
    void existsByTitleAndUserId_WithNullUserId_ShouldReturnFalse() {
        // Act
        boolean exists = blogRepository.existsByTitleAndUserId("Test Title", null);

        // Assert
        assertFalse(exists);
    }

    @Test
    void findByUserId_WithNullUserId_ShouldReturnEmptyList() {
        // Act
        List<Blog> userBlogs = blogRepository.findByUserId(null);

        // Assert
        assertTrue(userBlogs.isEmpty());
    }

    @Test
    void save_WithVeryLongContent_ShouldSaveSuccessfully() {
        // Arrange
        String longContent = "A".repeat(5000);
        testBlog1.setContent(longContent);

        // Act
        Blog savedBlog = blogRepository.save(testBlog1);

        // Assert
        assertNotNull(savedBlog.getId());
        assertEquals(longContent, savedBlog.getContent());
    }

    @Test
    void findAll_WithMultipleUsersAndBlogs_ShouldMaintainDataIntegrity() {
        // Arrange
        Blog blog1User1 = Blog.builder()
                .title("Blog 1 User 1")
                .content("Content 1")
                .userId("user-1")
                .build();
        
        Blog blog2User1 = Blog.builder()
                .title("Blog 2 User 1")
                .content("Content 2")
                .userId("user-1")
                .build();
        
        Blog blog1User2 = Blog.builder()
                .title("Blog 1 User 2")
                .content("Content 3")
                .userId("user-2")
                .build();

        entityManager.persistAndFlush(blog1User1);
        entityManager.persistAndFlush(blog2User1);
        entityManager.persistAndFlush(blog1User2);

        // Act
        List<Blog> user1Blogs = blogRepository.findByUserId("user-1");
        List<Blog> user2Blogs = blogRepository.findByUserId("user-2");
        List<Blog> allBlogs = blogRepository.findAll();

        // Assert
        assertEquals(2, user1Blogs.size());
        assertEquals(1, user2Blogs.size());
        assertEquals(3, allBlogs.size());
        
        // Verify no cross-contamination
        assertTrue(user1Blogs.stream().allMatch(blog -> "user-1".equals(blog.getUserId())));
        assertTrue(user2Blogs.stream().allMatch(blog -> "user-2".equals(blog.getUserId())));
    }

    @Test
    void complexSpecification_WithMultipleConditions_ShouldReturnCorrectResults() {
        // Arrange
        entityManager.persistAndFlush(testBlog1);
        entityManager.persistAndFlush(testBlog2);
        entityManager.persistAndFlush(testBlog3);

        Specification<Blog> spec = (root, query, criteriaBuilder) -> {
            return criteriaBuilder.and(
                criteriaBuilder.equal(root.get("userId"), "user-1"),
                criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%spring%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), "%tutorial%")
                )
            );
        };

        // Act
        List<Blog> filteredBlogs = blogRepository.findAll(spec);

        // Assert
        assertEquals(1, filteredBlogs.size());
        assertEquals(testBlog1.getTitle(), filteredBlogs.get(0).getTitle());
    }
}