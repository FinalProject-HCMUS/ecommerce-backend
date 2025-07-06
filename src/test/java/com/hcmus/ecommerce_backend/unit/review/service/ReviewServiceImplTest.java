package com.hcmus.ecommerce_backend.unit.review.service;

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

import com.hcmus.ecommerce_backend.order.exception.OrderDetailNotFoundException;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderDetailResponse;
import com.hcmus.ecommerce_backend.order.model.entity.OrderDetail;
import com.hcmus.ecommerce_backend.order.model.mapper.OrderDetailMapper;
import com.hcmus.ecommerce_backend.order.repository.OrderDetailRepository;
import com.hcmus.ecommerce_backend.product.exception.ProductColorSizeNotFoundException;
import com.hcmus.ecommerce_backend.product.model.dto.response.ColorResponse;
import com.hcmus.ecommerce_backend.product.model.dto.response.SizeResponse;
import com.hcmus.ecommerce_backend.product.model.entity.Color;
import com.hcmus.ecommerce_backend.product.model.entity.Product;
import com.hcmus.ecommerce_backend.product.model.entity.ProductColorSize;
import com.hcmus.ecommerce_backend.product.model.entity.Size;
import com.hcmus.ecommerce_backend.product.model.mapper.ColorMapper;
import com.hcmus.ecommerce_backend.product.model.mapper.SizeMapper;
import com.hcmus.ecommerce_backend.product.repository.ProductColorSizeRepository;
import com.hcmus.ecommerce_backend.product.repository.ProductRepository;
import com.hcmus.ecommerce_backend.review.exception.ReviewAlreadyExistsException;
import com.hcmus.ecommerce_backend.review.exception.ReviewNotFoundException;
import com.hcmus.ecommerce_backend.review.model.dto.request.CreateReviewRequest;
import com.hcmus.ecommerce_backend.review.model.dto.response.ReviewResponse;
import com.hcmus.ecommerce_backend.review.model.entity.Review;
import com.hcmus.ecommerce_backend.review.model.mapper.ReviewMapper;
import com.hcmus.ecommerce_backend.review.repository.ReviewRepository;
import com.hcmus.ecommerce_backend.review.service.impl.ReviewServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private ProductColorSizeRepository productColorSizeRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private OrderDetailMapper orderDetailMapper;

    @Mock
    private ColorMapper colorMapper;

    @Mock
    private SizeMapper sizeMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review review;
    private ReviewResponse reviewResponse;
    private CreateReviewRequest createReviewRequest;
    private OrderDetail orderDetail;
    private OrderDetailResponse orderDetailResponse;
    private ProductColorSize productColorSize;
    private Product product;
    private Color color;
    private ColorResponse colorResponse;
    private Size size;
    private SizeResponse sizeResponse;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Setup basic entities
        product = new Product();
        product.setId("product-1");
        product.setName("Test Product");
        product.setReviewCount(5);
        product.setAverageRating(4.0);

        color = new Color();
        color.setId("color-1");
        color.setName("Red");

        size = new Size();
        size.setId("size-1");
        size.setName("Medium");

        productColorSize = new ProductColorSize();
        productColorSize.setId("pcs-1");
        productColorSize.setProduct(product);
        productColorSize.setColor(color);
        productColorSize.setSize(size);

        orderDetail = new OrderDetail();
        orderDetail.setId("order-detail-1");
        orderDetail.setItemId("pcs-1");
        orderDetail.setReviewed(false);

        review = new Review();
        review.setId("review-1");
        review.setComment("Great product!");
        review.setHeadline("Excellent");
        review.setRating(5);
        review.setOrderDetailId("order-detail-1");
        review.setUserName("John Doe");

        // Setup response DTOs
        orderDetailResponse = OrderDetailResponse.builder()
                .itemId("pcs-1")
                .isReviewed(false)
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

        reviewResponse = ReviewResponse.builder()
                .id("review-1")
                .comment("Great product!")
                .headline("Excellent")
                .rating(5)
                .orderDetailId("order-detail-1")
                .userName("John Doe")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .orderDetail(orderDetailResponse)
                .color(colorResponse)
                .size(sizeResponse)
                .build();

        createReviewRequest = CreateReviewRequest.builder()
                .comment("Great product!")
                .headline("Excellent")
                .rating(5)
                .orderDetailId("order-detail-1")
                .userName("John Doe")
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void searchReviews_Success() {
        // Given
        String keyword = "great";
        Integer minRating = 4;
        Integer maxRating = 5;
        String productId = "product-1";

        Object[] reviewData = {review, orderDetail, productColorSize, color, size};
        List<Object[]> resultList = Collections.singletonList(reviewData);

        Page<Object[]> resultPage = new PageImpl<>(resultList, pageable, 1);

        when(reviewRepository.findReviewsWithDetails(keyword, minRating, maxRating, null, productId, pageable))
                .thenReturn(resultPage);
        when(reviewMapper.toResponse(review)).thenReturn(reviewResponse);
        when(orderDetailMapper.toResponse(orderDetail)).thenReturn(orderDetailResponse);
        when(colorMapper.toResponse(color)).thenReturn(colorResponse);
        when(sizeMapper.toResponse(size)).thenReturn(sizeResponse);

        // When
        Page<ReviewResponse> result = reviewService.searchReviews(keyword, minRating, maxRating, productId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(reviewResponse, result.getContent().get(0));
        verify(reviewRepository).findReviewsWithDetails(keyword, minRating, maxRating, null, productId, pageable);
    }

    @Test
    void searchReviews_WithNullKeyword_ProcessesCorrectly() {
        // Given
        Object[] reviewData = {review, orderDetail, productColorSize, color, size};
        List<Object[]> resultList = Collections.singletonList(reviewData);
        Page<Object[]> resultPage = new PageImpl<>(resultList, pageable, 1);

        when(reviewRepository.findReviewsWithDetails(null, null, null, null, null, pageable))
                .thenReturn(resultPage);
        when(reviewMapper.toResponse(review)).thenReturn(reviewResponse);
        when(orderDetailMapper.toResponse(orderDetail)).thenReturn(orderDetailResponse);
        when(colorMapper.toResponse(color)).thenReturn(colorResponse);
        when(sizeMapper.toResponse(size)).thenReturn(sizeResponse);

        // When
        Page<ReviewResponse> result = reviewService.searchReviews(null, null, null, null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reviewRepository).findReviewsWithDetails(null, null, null, null, null, pageable);
    }

    @Test
    void searchReviews_WithEmptyKeyword_ProcessesCorrectly() {
        // Given
        String emptyKeyword = "   ";
        Object[] reviewData = {review, orderDetail, productColorSize, color, size};
        List<Object[]> resultList = Collections.singletonList(reviewData);
        Page<Object[]> resultPage = new PageImpl<>(resultList, pageable, 1);

        when(reviewRepository.findReviewsWithDetails(null, null, null, null, null, pageable))
                .thenReturn(resultPage);
        when(reviewMapper.toResponse(review)).thenReturn(reviewResponse);
        when(orderDetailMapper.toResponse(orderDetail)).thenReturn(orderDetailResponse);
        when(colorMapper.toResponse(color)).thenReturn(colorResponse);
        when(sizeMapper.toResponse(size)).thenReturn(sizeResponse);

        // When
        Page<ReviewResponse> result = reviewService.searchReviews(emptyKeyword, null, null, null, pageable);

        // Then
        assertNotNull(result);
        verify(reviewRepository).findReviewsWithDetails(null, null, null, null, null, pageable);
    }

    @Test
    void searchReviews_DatabaseError_ThrowsException() {
        // Given
        when(reviewRepository.findReviewsWithDetails(any(), any(), any(), any(), any(), any()))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, 
                () -> reviewService.searchReviews("keyword", 1, 5, "product-1", pageable));
        verify(reviewRepository).findReviewsWithDetails(any(), any(), any(), any(), any(), any());
    }

    @Test
    void searchReviews_UnexpectedError_ThrowsException() {
        // Given
        when(reviewRepository.findReviewsWithDetails(any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> reviewService.searchReviews("keyword", 1, 5, "product-1", pageable));
        verify(reviewRepository).findReviewsWithDetails(any(), any(), any(), any(), any(), any());
    }

    @Test
    void searchReviewsByOrderDetailId_Success() {
        // Given
        String orderDetailId = "order-detail-1";
        String keyword = "great";
        Integer minRating = 4;
        Integer maxRating = 5;

        Object[] reviewData = {review, orderDetail, productColorSize, color, size};
        List<Object[]> resultList = Collections.singletonList(reviewData);
        Page<Object[]> resultPage = new PageImpl<>(resultList, pageable, 1);

        when(reviewRepository.findReviewsWithDetails(keyword, minRating, maxRating, orderDetailId, null, pageable))
                .thenReturn(resultPage);
        when(reviewMapper.toResponse(review)).thenReturn(reviewResponse);
        when(orderDetailMapper.toResponse(orderDetail)).thenReturn(orderDetailResponse);
        when(colorMapper.toResponse(color)).thenReturn(colorResponse);
        when(sizeMapper.toResponse(size)).thenReturn(sizeResponse);

        // When
        Page<ReviewResponse> result = reviewService.searchReviewsByOrderDetailId(orderDetailId, keyword, minRating, maxRating, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(reviewResponse, result.getContent().get(0));
        verify(reviewRepository).findReviewsWithDetails(keyword, minRating, maxRating, orderDetailId, null, pageable);
    }

    @Test
    void searchReviewsByOrderDetailId_DatabaseError_ThrowsException() {
        // Given
        String orderDetailId = "order-detail-1";
        when(reviewRepository.findReviewsWithDetails(any(), any(), any(), eq(orderDetailId), any(), any()))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, 
                () -> reviewService.searchReviewsByOrderDetailId(orderDetailId, "keyword", 1, 5, pageable));
        verify(reviewRepository).findReviewsWithDetails(any(), any(), any(), eq(orderDetailId), any(), any());
    }

    @Test
    void searchReviewsByOrderDetailId_UnexpectedError_ThrowsException() {
        // Given
        String orderDetailId = "order-detail-1";
        when(reviewRepository.findReviewsWithDetails(any(), any(), any(), eq(orderDetailId), any(), any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> reviewService.searchReviewsByOrderDetailId(orderDetailId, "keyword", 1, 5, pageable));
        verify(reviewRepository).findReviewsWithDetails(any(), any(), any(), eq(orderDetailId), any(), any());
    }

    @Test
    void getReviewById_Success() {
        // Given
        String id = "review-1";
        Object[] reviewData = {review, orderDetail, productColorSize, color, size};
        List<Object[]> resultList = Collections.singletonList(reviewData);
        Page<Object[]> resultPage = new PageImpl<>(resultList, Pageable.unpaged(), 1);

        when(reviewRepository.findReviewsWithDetails(null, null, null, null, null, Pageable.unpaged()))
                .thenReturn(resultPage);
        when(reviewMapper.toResponse(review)).thenReturn(reviewResponse);
        when(orderDetailMapper.toResponse(orderDetail)).thenReturn(orderDetailResponse);
        when(colorMapper.toResponse(color)).thenReturn(colorResponse);
        when(sizeMapper.toResponse(size)).thenReturn(sizeResponse);

        // When
        ReviewResponse result = reviewService.getReviewById(id);

        // Then
        assertNotNull(result);
        assertEquals(reviewResponse, result);
        verify(reviewRepository).findReviewsWithDetails(null, null, null, null, null, Pageable.unpaged());
    }

    @Test
    void getReviewById_NotFound() {
        // Given
        String id = "non-existent";
        Page<Object[]> emptyPage = new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), 0);

        when(reviewRepository.findReviewsWithDetails(null, null, null, null, null, Pageable.unpaged()))
                .thenReturn(emptyPage);

        // When & Then
        assertThrows(ReviewNotFoundException.class, () -> reviewService.getReviewById(id));
        verify(reviewRepository).findReviewsWithDetails(null, null, null, null, null, Pageable.unpaged());
    }

    @Test
    void getReviewById_DatabaseError_ThrowsException() {
        // Given
        String id = "review-1";
        when(reviewRepository.findReviewsWithDetails(null, null, null, null, null, Pageable.unpaged()))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> reviewService.getReviewById(id));
        verify(reviewRepository).findReviewsWithDetails(null, null, null, null, null, Pageable.unpaged());
    }

    @Test
    void getReviewById_UnexpectedError_ThrowsException() {
        // Given
        String id = "review-1";
        when(reviewRepository.findReviewsWithDetails(null, null, null, null, null, Pageable.unpaged()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> reviewService.getReviewById(id));
        verify(reviewRepository).findReviewsWithDetails(null, null, null, null, null, Pageable.unpaged());
    }

    @Test
    void createReview_Success() {
        // Given
        when(reviewRepository.existsByOrderDetailId("order-detail-1")).thenReturn(false);
        when(reviewMapper.toEntity(createReviewRequest)).thenReturn(review);
        when(reviewRepository.save(review)).thenReturn(review);
        when(orderDetailRepository.findById("order-detail-1")).thenReturn(Optional.of(orderDetail));
        when(orderDetailRepository.save(orderDetail)).thenReturn(orderDetail);
        when(productColorSizeRepository.findById("pcs-1")).thenReturn(Optional.of(productColorSize));
        when(productRepository.save(product)).thenReturn(product);
        when(reviewMapper.toResponse(review)).thenReturn(reviewResponse);

        // When
        ReviewResponse result = reviewService.createReview(createReviewRequest);

        // Then
        assertNotNull(result);
        assertEquals(reviewResponse, result);
        assertEquals(6, product.getReviewCount()); // 5 + 1
        assertEquals(4.2, product.getAverageRating(), 0.05); // ((4.0 * 5) + 5) / 6
        assertTrue(orderDetail.isReviewed());
        
        verify(reviewRepository).existsByOrderDetailId("order-detail-1");
        verify(reviewMapper).toEntity(createReviewRequest);
        verify(reviewRepository).save(review);
        verify(orderDetailRepository).findById("order-detail-1");
        verify(orderDetailRepository).save(orderDetail);
        verify(productColorSizeRepository).findById("pcs-1");
        verify(productRepository).save(product);
        verify(reviewMapper).toResponse(review);
    }

    @Test
    void createReview_AlreadyExists() {
        // Given
        when(reviewRepository.existsByOrderDetailId("order-detail-1")).thenReturn(true);

        // When & Then
        assertThrows(ReviewAlreadyExistsException.class, () -> reviewService.createReview(createReviewRequest));
        verify(reviewRepository).existsByOrderDetailId("order-detail-1");
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_OrderDetailNotFound() {
        // Given
        when(reviewRepository.existsByOrderDetailId("order-detail-1")).thenReturn(false);
        when(reviewMapper.toEntity(createReviewRequest)).thenReturn(review);
        when(reviewRepository.save(review)).thenReturn(review);
        when(orderDetailRepository.findById("order-detail-1")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderDetailNotFoundException.class, () -> reviewService.createReview(createReviewRequest));
        verify(orderDetailRepository).findById("order-detail-1");
    }

    @Test
    void createReview_ProductColorSizeNotFound() {
        // Given
        when(reviewRepository.existsByOrderDetailId("order-detail-1")).thenReturn(false);
        when(reviewMapper.toEntity(createReviewRequest)).thenReturn(review);
        when(reviewRepository.save(review)).thenReturn(review);
        when(orderDetailRepository.findById("order-detail-1")).thenReturn(Optional.of(orderDetail));
        when(orderDetailRepository.save(orderDetail)).thenReturn(orderDetail);
        when(productColorSizeRepository.findById("pcs-1")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductColorSizeNotFoundException.class, () -> reviewService.createReview(createReviewRequest));
        verify(productColorSizeRepository).findById("pcs-1");
    }

    @Test
    void createReview_DatabaseError_ThrowsException() {
        // Given
        when(reviewRepository.existsByOrderDetailId("order-detail-1")).thenReturn(false);
        when(reviewMapper.toEntity(createReviewRequest)).thenReturn(review);
        when(reviewRepository.save(review)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> reviewService.createReview(createReviewRequest));
        verify(reviewRepository).save(review);
    }

    @Test
    void createReview_UnexpectedError_ThrowsException() {
        // Given
        when(reviewRepository.existsByOrderDetailId("order-detail-1")).thenReturn(false);
        when(reviewMapper.toEntity(createReviewRequest)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> reviewService.createReview(createReviewRequest));
        verify(reviewMapper).toEntity(createReviewRequest);
    }

    @Test
    void createReview_WithZeroInitialReviewCount() {
        // Given
        product.setReviewCount(0);
        product.setAverageRating(0.0);
        
        when(reviewRepository.existsByOrderDetailId("order-detail-1")).thenReturn(false);
        when(reviewMapper.toEntity(createReviewRequest)).thenReturn(review);
        when(reviewRepository.save(review)).thenReturn(review);
        when(orderDetailRepository.findById("order-detail-1")).thenReturn(Optional.of(orderDetail));
        when(orderDetailRepository.save(orderDetail)).thenReturn(orderDetail);
        when(productColorSizeRepository.findById("pcs-1")).thenReturn(Optional.of(productColorSize));
        when(productRepository.save(product)).thenReturn(product);
        when(reviewMapper.toResponse(review)).thenReturn(reviewResponse);

        // When
        ReviewResponse result = reviewService.createReview(createReviewRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, product.getReviewCount());
        assertEquals(5.0, product.getAverageRating()); // First review, so average = rating
        verify(productRepository).save(product);
    }

    @Test
    void deleteReview_Success() {
        // Given
        String id = "review-1";
        // Mock the separate transaction method
        ReviewServiceImpl spyService = spy(reviewService);
        doReturn(true).when(spyService).doesReviewExistById(id);
        
        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));
        when(orderDetailRepository.findById("order-detail-1")).thenReturn(Optional.of(orderDetail));
        when(orderDetailRepository.save(orderDetail)).thenReturn(orderDetail);
        when(productColorSizeRepository.findById("pcs-1")).thenReturn(Optional.of(productColorSize));
        when(productRepository.save(product)).thenReturn(product);

        // When
        spyService.deleteReview(id);

        // Then
        assertFalse(orderDetail.isReviewed());
        assertEquals(4, product.getReviewCount()); // 5 - 1
        assertEquals(3.75, product.getAverageRating(), 0.01); // ((4.0 * 6) - 5) / 4
        
        verify(spyService).doesReviewExistById(id);
        verify(reviewRepository).findById(id);
        verify(orderDetailRepository).findById("order-detail-1");
        verify(orderDetailRepository).save(orderDetail);
        verify(productColorSizeRepository).findById("pcs-1");
        verify(productRepository).save(product);
        verify(reviewRepository).deleteById(id);
    }

    @Test
    void deleteReview_NotFound() {
        // Given
        String id = "non-existent";
        ReviewServiceImpl spyService = spy(reviewService);
        doReturn(false).when(spyService).doesReviewExistById(id);

        // When & Then
        assertThrows(ReviewNotFoundException.class, () -> spyService.deleteReview(id));
        verify(spyService).doesReviewExistById(id);
        verify(reviewRepository, never()).deleteById(id);
    }

    @Test
    void deleteReview_OrderDetailNotFound() {
        // Given
        String id = "review-1";
        ReviewServiceImpl spyService = spy(reviewService);
        doReturn(true).when(spyService).doesReviewExistById(id);
        
        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));
        when(orderDetailRepository.findById("order-detail-1")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(OrderDetailNotFoundException.class, () -> spyService.deleteReview(id));
        verify(orderDetailRepository).findById("order-detail-1");
    }

    @Test
    void deleteReview_ProductColorSizeNotFound() {
        // Given
        String id = "review-1";
        ReviewServiceImpl spyService = spy(reviewService);
        doReturn(true).when(spyService).doesReviewExistById(id);
        
        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));
        when(orderDetailRepository.findById("order-detail-1")).thenReturn(Optional.of(orderDetail));
        when(orderDetailRepository.save(orderDetail)).thenReturn(orderDetail);
        when(productColorSizeRepository.findById("pcs-1")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductColorSizeNotFoundException.class, () -> spyService.deleteReview(id));
        verify(productColorSizeRepository).findById("pcs-1");
    }

    @Test
    void deleteReview_DatabaseError_ThrowsException() {
        // Given
        String id = "review-1";
        ReviewServiceImpl spyService = spy(reviewService);
        doReturn(true).when(spyService).doesReviewExistById(id);
        
        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));
        when(orderDetailRepository.findById("order-detail-1")).thenReturn(Optional.of(orderDetail));
        when(orderDetailRepository.save(orderDetail)).thenReturn(orderDetail);
        when(productColorSizeRepository.findById("pcs-1")).thenReturn(Optional.of(productColorSize));
        when(productRepository.save(product)).thenReturn(product);
        doThrow(new DataIntegrityViolationException("Database error")).when(reviewRepository).deleteById(id);

        // When & Then
        assertThrows(DataAccessException.class, () -> spyService.deleteReview(id));
        verify(reviewRepository).deleteById(id);
    }

    @Test
    void deleteReview_UnexpectedError_ThrowsException() {
        // Given
        String id = "review-1";
        ReviewServiceImpl spyService = spy(reviewService);
        doReturn(true).when(spyService).doesReviewExistById(id);
        
        when(reviewRepository.findById(id)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> spyService.deleteReview(id));
        verify(reviewRepository).findById(id);
    }

    @Test
    void deleteReview_WithSingleReview_SetsAverageToZero() {
        // Given
        String id = "review-1";
        product.setReviewCount(1);
        product.setAverageRating(5.0);
        
        ReviewServiceImpl spyService = spy(reviewService);
        doReturn(true).when(spyService).doesReviewExistById(id);
        
        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));
        when(orderDetailRepository.findById("order-detail-1")).thenReturn(Optional.of(orderDetail));
        when(orderDetailRepository.save(orderDetail)).thenReturn(orderDetail);
        when(productColorSizeRepository.findById("pcs-1")).thenReturn(Optional.of(productColorSize));
        when(productRepository.save(product)).thenReturn(product);

        // When
        spyService.deleteReview(id);

        // Then
        assertEquals(0, product.getReviewCount());
        assertEquals(0.0, product.getAverageRating());
        verify(productRepository).save(product);
    }

    @Test
    void getReviewsByRatingRange_Success() {
        // Given
        Integer minRating = 4;
        Integer maxRating = 5;
        List<Review> reviews = Arrays.asList(review);
        
        when(reviewRepository.findByRatingBetweenOrderByCreatedAtDesc(minRating, maxRating)).thenReturn(reviews);
        when(reviewMapper.toResponse(review)).thenReturn(reviewResponse);

        // When
        List<ReviewResponse> result = reviewService.getReviewsByRatingRange(minRating, maxRating);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(reviewResponse, result.get(0));
        verify(reviewRepository).findByRatingBetweenOrderByCreatedAtDesc(minRating, maxRating);
        verify(reviewMapper).toResponse(review);
    }

    @Test
    void getReviewsByRatingRange_EmptyResult() {
        // Given
        Integer minRating = 4;
        Integer maxRating = 5;
        
        when(reviewRepository.findByRatingBetweenOrderByCreatedAtDesc(minRating, maxRating)).thenReturn(Collections.emptyList());

        // When
        List<ReviewResponse> result = reviewService.getReviewsByRatingRange(minRating, maxRating);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reviewRepository).findByRatingBetweenOrderByCreatedAtDesc(minRating, maxRating);
    }

    @Test
    void getReviewsByRatingRange_DatabaseError_ReturnsEmptyList() {
        // Given
        Integer minRating = 4;
        Integer maxRating = 5;
        
        when(reviewRepository.findByRatingBetweenOrderByCreatedAtDesc(minRating, maxRating))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When
        List<ReviewResponse> result = reviewService.getReviewsByRatingRange(minRating, maxRating);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(reviewRepository).findByRatingBetweenOrderByCreatedAtDesc(minRating, maxRating);
    }

    @Test
    void getReviewsByRatingRange_UnexpectedError_ThrowsException() {
        // Given
        Integer minRating = 4;
        Integer maxRating = 5;
        
        when(reviewRepository.findByRatingBetweenOrderByCreatedAtDesc(minRating, maxRating))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> reviewService.getReviewsByRatingRange(minRating, maxRating));
        verify(reviewRepository).findByRatingBetweenOrderByCreatedAtDesc(minRating, maxRating);
    }

    @Test
    void getReviewsByRatingRange_MapperError_ThrowsException() {
        // Given
        Integer minRating = 4;
        Integer maxRating = 5;
        List<Review> reviews = Arrays.asList(review);
        
        when(reviewRepository.findByRatingBetweenOrderByCreatedAtDesc(minRating, maxRating)).thenReturn(reviews);
        when(reviewMapper.toResponse(review)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> reviewService.getReviewsByRatingRange(minRating, maxRating));
        verify(reviewMapper).toResponse(review);
    }

    @Test
    void findReviewById_Success() {
        // Given
        String id = "review-1";
        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));

        // When
        Review result = reviewService.findReviewById(id);

        // Then
        assertNotNull(result);
        assertEquals(review, result);
        verify(reviewRepository).findById(id);
    }

    @Test
    void findReviewById_NotFound() {
        // Given
        String id = "non-existent";
        when(reviewRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ReviewNotFoundException.class, () -> reviewService.findReviewById(id));
        verify(reviewRepository).findById(id);
    }

    @Test
    void doesReviewExistById_True() {
        // Given
        String id = "review-1";
        when(reviewRepository.existsById(id)).thenReturn(true);

        // When
        boolean result = reviewService.doesReviewExistById(id);

        // Then
        assertTrue(result);
        verify(reviewRepository).existsById(id);
    }

    @Test
    void doesReviewExistById_False() {
        // Given
        String id = "non-existent";
        when(reviewRepository.existsById(id)).thenReturn(false);

        // When
        boolean result = reviewService.doesReviewExistById(id);

        // Then
        assertFalse(result);
        verify(reviewRepository).existsById(id);
    }

    @Test
    void mapToFullReviewResponse_Success() {
        // Given
        Object[] reviewData = {review, orderDetail, productColorSize, color, size};
        List<Object[]> resultList = Collections.singletonList(reviewData);
        
        when(reviewMapper.toResponse(review)).thenReturn(reviewResponse);
        when(orderDetailMapper.toResponse(orderDetail)).thenReturn(orderDetailResponse);
        when(colorMapper.toResponse(color)).thenReturn(colorResponse);
        when(sizeMapper.toResponse(size)).thenReturn(sizeResponse);

        Page<Object[]> resultPage = new PageImpl<>(resultList, pageable, 1);
        when(reviewRepository.findReviewsWithDetails(null, null, null, null, null, pageable))
                .thenReturn(resultPage);

        // When
        Page<ReviewResponse> result = reviewService.searchReviews(null, null, null, null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        ReviewResponse mappedResponse = result.getContent().get(0);
        assertEquals(reviewResponse, mappedResponse);
        
        verify(reviewMapper).toResponse(review);
        verify(orderDetailMapper).toResponse(orderDetail);
        verify(colorMapper).toResponse(color);
        verify(sizeMapper).toResponse(size);
    }

    @Test
    void searchReviews_MultipleResults() {
        // Given
        Review review2 = new Review();
        review2.setId("review-2");
        review2.setComment("Good product");
        review2.setRating(4);

        ReviewResponse reviewResponse2 = ReviewResponse.builder()
                .id("review-2")
                .comment("Good product")
                .rating(4)
                .build();

        Object[] reviewData1 = {review, orderDetail, productColorSize, color, size};
        Object[] reviewData2 = {review2, orderDetail, productColorSize, color, size};
        List<Object[]> resultList = Arrays.asList(reviewData1, reviewData2);
        Page<Object[]> resultPage = new PageImpl<>(resultList, pageable, 2);

        when(reviewRepository.findReviewsWithDetails(any(), any(), any(), any(), any(), any()))
                .thenReturn(resultPage);
        when(reviewMapper.toResponse(review)).thenReturn(reviewResponse);
        when(reviewMapper.toResponse(review2)).thenReturn(reviewResponse2);
        when(orderDetailMapper.toResponse(orderDetail)).thenReturn(orderDetailResponse);
        when(colorMapper.toResponse(color)).thenReturn(colorResponse);
        when(sizeMapper.toResponse(size)).thenReturn(sizeResponse);

        // When
        Page<ReviewResponse> result = reviewService.searchReviews("keyword", 1, 5, "product-1", pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(reviewMapper, times(2)).toResponse(any(Review.class));
    }

    @Test
    void getReviewById_WithDifferentId_NotFound() {
        // Given
        String searchId = "review-2";
        review.setId("review-1"); // Different ID
        
        Object[] reviewData = {review, orderDetail, productColorSize, color, size};
        List<Object[]> resultList = Collections.singletonList(reviewData);
        Page<Object[]> resultPage = new PageImpl<>(resultList, Pageable.unpaged(), 1);

        when(reviewRepository.findReviewsWithDetails(null, null, null, null, null, Pageable.unpaged()))
                .thenReturn(resultPage);

        // When & Then
        assertThrows(ReviewNotFoundException.class, () -> reviewService.getReviewById(searchId));
        verify(reviewRepository).findReviewsWithDetails(null, null, null, null, null, Pageable.unpaged());
    }

    @Test
    void searchReviews_EmptyResults() {
        // Given
        Page<Object[]> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(reviewRepository.findReviewsWithDetails(any(), any(), any(), any(), any(), any()))
                .thenReturn(emptyPage);

        // When
        Page<ReviewResponse> result = reviewService.searchReviews("keyword", 1, 5, "product-1", pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(reviewRepository).findReviewsWithDetails(any(), any(), any(), any(), any(), any());
    }

    @Test
    void createReview_MapperError_ThrowsException() {
        // Given
        when(reviewRepository.existsByOrderDetailId("order-detail-1")).thenReturn(false);
        when(reviewMapper.toResponse(review)).thenThrow(new RuntimeException("Response mapping error"));
        when(reviewMapper.toEntity(createReviewRequest)).thenReturn(review);
        when(reviewRepository.save(review)).thenReturn(review);
        when(orderDetailRepository.findById("order-detail-1")).thenReturn(Optional.of(orderDetail));
        when(orderDetailRepository.save(orderDetail)).thenReturn(orderDetail);
        when(productColorSizeRepository.findById("pcs-1")).thenReturn(Optional.of(productColorSize));
        when(productRepository.save(product)).thenReturn(product);

        // When & Then
        assertThrows(RuntimeException.class, () -> reviewService.createReview(createReviewRequest));
        verify(reviewMapper).toResponse(review);
    }
}
