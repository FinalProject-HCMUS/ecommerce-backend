package com.hcmus.ecommerce_backend.unit.blog.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.Optional;

import com.hcmus.ecommerce_backend.blog.service.impl.BlogServiceImpl;
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
import org.springframework.data.jpa.domain.Specification;
import com.hcmus.ecommerce_backend.blog.exception.BlogAlreadyExistsException;
import com.hcmus.ecommerce_backend.blog.exception.BlogNotFoundException;
import com.hcmus.ecommerce_backend.blog.model.dto.request.CreateBlogRequest;
import com.hcmus.ecommerce_backend.blog.model.dto.request.UpdateBlogRequest;
import com.hcmus.ecommerce_backend.blog.model.dto.response.BlogResponse;
import com.hcmus.ecommerce_backend.blog.model.entity.Blog;
import com.hcmus.ecommerce_backend.blog.model.mapper.BlogMapper;
import com.hcmus.ecommerce_backend.blog.repository.BlogRepository;
import com.hcmus.ecommerce_backend.user.exception.UserNotFoundException;
import com.hcmus.ecommerce_backend.user.repository.UserRepository;
import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
class BlogServiceImplTest {

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private BlogMapper blogMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BlogServiceImpl blogService;

    private Blog testBlog;
    private BlogResponse testBlogResponse;
    private CreateBlogRequest createBlogRequest;
    private UpdateBlogRequest updateBlogRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testBlog = new Blog();
        testBlog.setId("blog-id-1");
        testBlog.setTitle("Test Blog");
        testBlog.setContent("Test Content");
        testBlog.setUserId("user-id-1");
        testBlog.setCreatedAt(LocalDateTime.now());
        testBlog.setUpdatedAt(LocalDateTime.now());

        testBlogResponse = new BlogResponse();
        testBlogResponse.setId("blog-id-1");
        testBlogResponse.setTitle("Test Blog");
        testBlogResponse.setContent("Test Content");

        createBlogRequest = new CreateBlogRequest();
        createBlogRequest.setTitle("New Blog");
        createBlogRequest.setContent("New Content");
        createBlogRequest.setUserId("user-id-1");

        updateBlogRequest = new UpdateBlogRequest();
        updateBlogRequest.setTitle("Updated Blog");
        updateBlogRequest.setContent("Updated Content");

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAllBlogs_WithoutKeySearch_ShouldReturnPaginatedBlogs() {
        // Arrange
        Page<Blog> blogPage = new PageImpl<>(Arrays.asList(testBlog));
        Page<BlogResponse> expectedResponse = new PageImpl<>(Arrays.asList(testBlogResponse));
        
        when(blogRepository.findAll(pageable)).thenReturn(blogPage);
        when(blogMapper.toResponse(testBlog)).thenReturn(testBlogResponse);

        // Act
        Page<BlogResponse> result = blogService.getAllBlogs(pageable, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testBlogResponse.getId(), result.getContent().get(0).getId());
        verify(blogRepository).findAll(pageable);
        verify(blogMapper).toResponse(testBlog);
    }

    @Test
    void getAllBlogs_WithKeySearch_ShouldReturnFilteredBlogs() {
        // Arrange
        Page<Blog> blogPage = new PageImpl<>(Arrays.asList(testBlog));
        String keySearch = "test";
        
        when(blogRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(blogPage);
        when(blogMapper.toResponse(testBlog)).thenReturn(testBlogResponse);

        // Act
        Page<BlogResponse> result = blogService.getAllBlogs(pageable, keySearch);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(blogRepository).findAll(any(Specification.class), eq(pageable));
        verify(blogMapper).toResponse(testBlog);
    }

    @Test
    void getAllBlogs_WithEmptyKeySearch_ShouldReturnAllBlogs() {
        // Arrange
        Page<Blog> blogPage = new PageImpl<>(Arrays.asList(testBlog));
        
        when(blogRepository.findAll(pageable)).thenReturn(blogPage);
        when(blogMapper.toResponse(testBlog)).thenReturn(testBlogResponse);

        // Act
        Page<BlogResponse> result = blogService.getAllBlogs(pageable, "   ");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(blogRepository).findAll(pageable);
        verify(blogMapper).toResponse(testBlog);
    }

    @Test
    void getAllBlogs_WhenDatabaseError_ShouldReturnEmptyPage() {
        // Arrange
        when(blogRepository.findAll(pageable)).thenThrow(new DataAccessException("Database error") {});

        // Act
        Page<BlogResponse> result = blogService.getAllBlogs(pageable, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(blogRepository).findAll(pageable);
    }

    @Test
    void getAllBlogs_WhenUnexpectedError_ShouldThrowException() {
        // Arrange
        when(blogRepository.findAll(pageable)).thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> blogService.getAllBlogs(pageable, null));
        verify(blogRepository).findAll(pageable);
    }

    @Test
    void getBlogById_WithValidId_ShouldReturnBlogResponse() {
        // Arrange
        String blogId = "blog-id-1";
        when(blogRepository.findById(blogId)).thenReturn(Optional.of(testBlog));
        when(blogMapper.toResponse(testBlog)).thenReturn(testBlogResponse);

        // Act
        BlogResponse result = blogService.getBlogById(blogId);

        // Assert
        assertNotNull(result);
        assertEquals(testBlogResponse.getId(), result.getId());
        verify(blogRepository).findById(blogId);
        verify(blogMapper).toResponse(testBlog);
    }

    @Test
    void getBlogById_WithInvalidId_ShouldThrowBlogNotFoundException() {
        // Arrange
        String blogId = "invalid-id";
        when(blogRepository.findById(blogId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BlogNotFoundException.class, () -> blogService.getBlogById(blogId));
        verify(blogRepository).findById(blogId);
        verify(blogMapper, never()).toResponse(any());
    }

    @Test
    void getBlogById_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        String blogId = "blog-id-1";
        when(blogRepository.findById(blogId)).thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, () -> blogService.getBlogById(blogId));
        verify(blogRepository).findById(blogId);
    }

    @Test
    void createBlog_WithValidRequest_ShouldReturnCreatedBlog() {
        // Arrange
        when(blogRepository.existsByTitleAndUserId(createBlogRequest.getTitle(), createBlogRequest.getUserId()))
                .thenReturn(false);
        when(userRepository.existsById(createBlogRequest.getUserId())).thenReturn(true);
        when(blogMapper.toEntity(createBlogRequest)).thenReturn(testBlog);
        when(blogRepository.save(testBlog)).thenReturn(testBlog);
        when(blogMapper.toResponse(testBlog)).thenReturn(testBlogResponse);

        // Act
        BlogResponse result = blogService.createBlog(createBlogRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testBlogResponse.getId(), result.getId());
        verify(blogRepository).existsByTitleAndUserId(createBlogRequest.getTitle(), createBlogRequest.getUserId());
        verify(userRepository).existsById(createBlogRequest.getUserId());
        verify(blogRepository).save(testBlog);
        verify(blogMapper).toEntity(createBlogRequest);
        verify(blogMapper).toResponse(testBlog);
    }

    @Test
    void createBlog_WithDuplicateTitle_ShouldThrowBlogAlreadyExistsException() {
        // Arrange
        when(blogRepository.existsByTitleAndUserId(createBlogRequest.getTitle(), createBlogRequest.getUserId()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(BlogAlreadyExistsException.class, () -> blogService.createBlog(createBlogRequest));
        verify(blogRepository).existsByTitleAndUserId(createBlogRequest.getTitle(), createBlogRequest.getUserId());
        verify(userRepository, never()).existsById(any());
        verify(blogRepository, never()).save(any());
    }

    @Test
    void createBlog_WithInvalidUser_ShouldThrowUserNotFoundException() {
        // Arrange
        when(blogRepository.existsByTitleAndUserId(createBlogRequest.getTitle(), createBlogRequest.getUserId()))
                .thenReturn(false);
        when(userRepository.existsById(createBlogRequest.getUserId())).thenReturn(false);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> blogService.createBlog(createBlogRequest));
        verify(blogRepository).existsByTitleAndUserId(createBlogRequest.getTitle(), createBlogRequest.getUserId());
        verify(userRepository).existsById(createBlogRequest.getUserId());
        verify(blogRepository, never()).save(any());
    }

    @Test
    void createBlog_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        when(blogRepository.existsByTitleAndUserId(createBlogRequest.getTitle(), createBlogRequest.getUserId()))
                .thenReturn(false);
        when(userRepository.existsById(createBlogRequest.getUserId())).thenReturn(true);
        when(blogMapper.toEntity(createBlogRequest)).thenReturn(testBlog);
        when(blogRepository.save(testBlog)).thenThrow(new DataAccessException("Database error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, () -> blogService.createBlog(createBlogRequest));
        verify(blogRepository).save(testBlog);
    }

    @Test
    void updateBlog_WithValidRequest_ShouldReturnUpdatedBlog() {
        // Arrange
        String blogId = "blog-id-1";
        Blog updatedBlog = new Blog();
        updatedBlog.setId(blogId);
        updatedBlog.setTitle(updateBlogRequest.getTitle());
        
        when(blogRepository.findById(blogId)).thenReturn(Optional.of(testBlog));
        when(blogRepository.save(testBlog)).thenReturn(updatedBlog);
        when(blogMapper.toResponse(updatedBlog)).thenReturn(testBlogResponse);

        // Act
        BlogResponse result = blogService.updateBlog(blogId, updateBlogRequest);

        // Assert
        assertNotNull(result);
        verify(blogRepository).findById(blogId);
        verify(blogMapper).updateEntity(updateBlogRequest, testBlog);
        verify(blogRepository).save(testBlog);
        verify(blogMapper).toResponse(updatedBlog);
    }

    @Test
    void updateBlog_WithDifferentTitle_ShouldCheckForDuplicates() {
        // Arrange
        String blogId = "blog-id-1";
        testBlog.setTitle("Different Title");
        
        when(blogRepository.findById(blogId)).thenReturn(Optional.of(testBlog));
        when(blogRepository.existsByTitleAndUserId(updateBlogRequest.getTitle(), testBlog.getUserId()))
                .thenReturn(false);
        when(blogRepository.save(testBlog)).thenReturn(testBlog);
        when(blogMapper.toResponse(testBlog)).thenReturn(testBlogResponse);

        // Act
        BlogResponse result = blogService.updateBlog(blogId, updateBlogRequest);

        // Assert
        assertNotNull(result);
        verify(blogRepository).existsByTitleAndUserId(updateBlogRequest.getTitle(), testBlog.getUserId());
    }

    @Test
    void updateBlog_WithDuplicateTitle_ShouldThrowBlogAlreadyExistsException() {
        // Arrange
        String blogId = "blog-id-1";
        testBlog.setTitle("Different Title");
        
        when(blogRepository.findById(blogId)).thenReturn(Optional.of(testBlog));
        when(blogRepository.existsByTitleAndUserId(updateBlogRequest.getTitle(), testBlog.getUserId()))
                .thenReturn(true);

        // Act & Assert
        assertThrows(BlogAlreadyExistsException.class, () -> blogService.updateBlog(blogId, updateBlogRequest));
        verify(blogRepository).existsByTitleAndUserId(updateBlogRequest.getTitle(), testBlog.getUserId());
        verify(blogRepository, never()).save(any());
    }

    @Test
    void updateBlog_WithInvalidId_ShouldThrowBlogNotFoundException() {
        // Arrange
        String blogId = "invalid-id";
        when(blogRepository.findById(blogId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BlogNotFoundException.class, () -> blogService.updateBlog(blogId, updateBlogRequest));
        verify(blogRepository).findById(blogId);
        verify(blogRepository, never()).save(any());
    }

    @Test
    void deleteBlog_WithValidId_ShouldDeleteBlog() {
        // Arrange
        String blogId = "blog-id-1";
        when(blogRepository.existsById(blogId)).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> blogService.deleteBlog(blogId));

        // Assert
        verify(blogRepository).existsById(blogId);
        verify(blogRepository).deleteById(blogId);
    }

    @Test
    void deleteBlog_WithInvalidId_ShouldThrowBlogNotFoundException() {
        // Arrange
        String blogId = "invalid-id";
        when(blogRepository.existsById(blogId)).thenReturn(false);

        // Act & Assert
        assertThrows(BlogNotFoundException.class, () -> blogService.deleteBlog(blogId));
        verify(blogRepository).existsById(blogId);
        verify(blogRepository, never()).deleteById(any());
    }

    @Test
    void deleteBlog_WhenDatabaseError_ShouldThrowDataAccessException() {
        // Arrange
        String blogId = "blog-id-1";
        when(blogRepository.existsById(blogId)).thenReturn(true);
        doThrow(new DataAccessException("Database error") {}).when(blogRepository).deleteById(blogId);

        // Act & Assert
        assertThrows(DataAccessException.class, () -> blogService.deleteBlog(blogId));
        verify(blogRepository).deleteById(blogId);
    }

    @Test
    void findBlogById_WithValidId_ShouldReturnBlog() {
        // Arrange
        String blogId = "blog-id-1";
        when(blogRepository.findById(blogId)).thenReturn(Optional.of(testBlog));

        // Act
        Blog result = blogService.findBlogById(blogId);

        // Assert
        assertNotNull(result);
        assertEquals(testBlog.getId(), result.getId());
        verify(blogRepository).findById(blogId);
    }

    @Test
    void findBlogById_WithInvalidId_ShouldThrowBlogNotFoundException() {
        // Arrange
        String blogId = "invalid-id";
        when(blogRepository.findById(blogId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BlogNotFoundException.class, () -> blogService.findBlogById(blogId));
        verify(blogRepository).findById(blogId);
    }

    @Test
    void checkTitleBlogAndUserIdExists_WhenExists_ShouldThrowBlogAlreadyExistsException() {
        // Arrange
        String title = "Test Title";
        String userId = "user-id-1";
        when(blogRepository.existsByTitleAndUserId(title, userId)).thenReturn(true);

        // Act & Assert
        assertThrows(BlogAlreadyExistsException.class, 
                () -> blogService.checkTitleBlogAndUserIdExists(title, userId));
        verify(blogRepository).existsByTitleAndUserId(title, userId);
    }

    @Test
    void checkTitleBlogAndUserIdExists_WhenNotExists_ShouldNotThrow() {
        // Arrange
        String title = "Test Title";
        String userId = "user-id-1";
        when(blogRepository.existsByTitleAndUserId(title, userId)).thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> blogService.checkTitleBlogAndUserIdExists(title, userId));
        verify(blogRepository).existsByTitleAndUserId(title, userId);
    }

    @Test
    void validateUser_WithValidUserId_ShouldNotThrow() {
        // Arrange
        String userId = "user-id-1";
        when(userRepository.existsById(userId)).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> blogService.validateUser(userId));
        verify(userRepository).existsById(userId);
    }

    @Test
    void validateUser_WithInvalidUserId_ShouldThrowUserNotFoundException() {
        // Arrange
        String userId = "invalid-user-id";
        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> blogService.validateUser(userId));
        verify(userRepository).existsById(userId);
    }
}