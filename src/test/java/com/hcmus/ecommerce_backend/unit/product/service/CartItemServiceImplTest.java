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
import com.hcmus.ecommerce_backend.product.exception.CartItemNotFoundException;
import com.hcmus.ecommerce_backend.product.model.dto.request.cart_item.CreateCartItemRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.cart_item.UpdateCartItemRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.CartItemResponse;
import com.hcmus.ecommerce_backend.product.model.dto.response.CartItemWithProductResponse;
import com.hcmus.ecommerce_backend.product.model.entity.CartItem;
import com.hcmus.ecommerce_backend.product.model.mapper.CartItemMapper;
import com.hcmus.ecommerce_backend.product.repository.CartItemRepository;
import com.hcmus.ecommerce_backend.product.service.impl.CartItemServiceImpl;

@ExtendWith(MockitoExtension.class)
class CartItemServiceImplTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CartItemMapper cartItemMapper;

    @InjectMocks
    private CartItemServiceImpl cartItemService;

    private CartItem cartItem;
    private CartItemResponse cartItemResponse;
    private CartItemWithProductResponse cartItemWithProductResponse;
    private CreateCartItemRequest createCartItemRequest;
    private UpdateCartItemRequest updateCartItemRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Setup CartItem entity
        cartItem = new CartItem();
        cartItem.setId("cart-item-1");
        cartItem.setUserId("user-1");
        cartItem.setItemId("item-1");
        cartItem.setQuantity(2);

        // Setup CartItemResponse
        cartItemResponse = CartItemResponse.builder()
                .id("cart-item-1")
                .userId("user-1")
                .itemId("item-1")
                .quantity(2)
                .build();

        // Setup CartItemWithProductResponse
        cartItemWithProductResponse = CartItemWithProductResponse.builder()
                .id("cart-item-1")
                .userId("user-1")
                .itemId("item-1")
                .quantity(2)
                .build();

        // Setup CreateCartItemRequest
        createCartItemRequest = CreateCartItemRequest.builder()
                .userId("user-1")
                .itemId("item-1")
                .quantity(2)
                .build();

        // Setup UpdateCartItemRequest
        updateCartItemRequest = UpdateCartItemRequest.builder()
                .quantity(3)
                .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAllCartItems_Success() {
        // Given
        List<CartItem> cartItems = Arrays.asList(cartItem);
        Page<CartItem> cartItemPage = new PageImpl<>(cartItems, pageable, 1);
        when(cartItemRepository.findAll(pageable)).thenReturn(cartItemPage);
        when(cartItemMapper.toResponse(cartItem)).thenReturn(cartItemResponse);

        // When
        Page<CartItemResponse> result = cartItemService.getAllCartItems(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(cartItemResponse, result.getContent().get(0));
        verify(cartItemRepository).findAll(pageable);
        verify(cartItemMapper).toResponse(cartItem);
    }

    @Test
    void getAllCartItems_EmptyResult() {
        // Given
        Page<CartItem> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(cartItemRepository.findAll(pageable)).thenReturn(emptyPage);

        // When
        Page<CartItemResponse> result = cartItemService.getAllCartItems(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(cartItemRepository).findAll(pageable);
    }

    @Test
    void getAllCartItems_DatabaseError_ReturnsEmptyPage() {
        // Given
        when(cartItemRepository.findAll(pageable)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When
        Page<CartItemResponse> result = cartItemService.getAllCartItems(pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cartItemRepository).findAll(pageable);
    }

    @Test
    void getAllCartItems_UnexpectedError_ThrowsException() {
        // Given
        when(cartItemRepository.findAll(pageable)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> cartItemService.getAllCartItems(pageable));
        verify(cartItemRepository).findAll(pageable);
    }

    @Test
    void getCartItemById_Success() {
        // Given
        String id = "cart-item-1";
        when(cartItemRepository.findById(id)).thenReturn(Optional.of(cartItem));
        when(cartItemMapper.toResponse(cartItem)).thenReturn(cartItemResponse);

        // When
        CartItemResponse result = cartItemService.getCartItemById(id);

        // Then
        assertNotNull(result);
        assertEquals(cartItemResponse, result);
        verify(cartItemRepository).findById(id);
        verify(cartItemMapper).toResponse(cartItem);
    }

    @Test
    void getCartItemById_NotFound() {
        // Given
        String id = "non-existent";
        when(cartItemRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CartItemNotFoundException.class, () -> cartItemService.getCartItemById(id));
        verify(cartItemRepository).findById(id);
    }

    @Test
    void getCartItemById_DatabaseError() {
        // Given
        String id = "cart-item-1";
        when(cartItemRepository.findById(id)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> cartItemService.getCartItemById(id));
        verify(cartItemRepository).findById(id);
    }

    @Test
    void getCartItemById_UnexpectedError() {
        // Given
        String id = "cart-item-1";
        when(cartItemRepository.findById(id)).thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> cartItemService.getCartItemById(id));
        verify(cartItemRepository).findById(id);
    }

    @Test
    void getCartItemsByUserId_Success() {
        // Given
        String userId = "user-1";
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("id", "cart-item-1");
        rawData.put("user_id", "user-1");
        
        List<Map<String, Object>> rawResults = Arrays.asList(rawData);
        when(cartItemRepository.findCartItemWithProductByUserId(userId)).thenReturn(rawResults);
        when(cartItemMapper.toCartItemWithProductReponse(rawData)).thenReturn(cartItemWithProductResponse);

        // When
        List<CartItemWithProductResponse> result = cartItemService.getCartItemsByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(cartItemWithProductResponse, result.get(0));
        verify(cartItemRepository).findCartItemWithProductByUserId(userId);
        verify(cartItemMapper).toCartItemWithProductReponse(rawData);
    }

    @Test
    void getCartItemsByUserId_EmptyResult() {
        // Given
        String userId = "user-1";
        when(cartItemRepository.findCartItemWithProductByUserId(userId)).thenReturn(Collections.emptyList());

        // When
        List<CartItemWithProductResponse> result = cartItemService.getCartItemsByUserId(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cartItemRepository).findCartItemWithProductByUserId(userId);
    }

    @Test
    void getCartItemsByUserId_DatabaseError_ReturnsEmptyList() {
        // Given
        String userId = "user-1";
        when(cartItemRepository.findCartItemWithProductByUserId(userId))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When
        List<CartItemWithProductResponse> result = cartItemService.getCartItemsByUserId(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cartItemRepository).findCartItemWithProductByUserId(userId);
    }

    @Test
    void getCartItemsByUserId_UnexpectedError_ThrowsException() {
        // Given
        String userId = "user-1";
        when(cartItemRepository.findCartItemWithProductByUserId(userId))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> cartItemService.getCartItemsByUserId(userId));
        verify(cartItemRepository).findCartItemWithProductByUserId(userId);
    }

    @Test
    void createCartItem_Success() {
        // Given
        when(cartItemMapper.toEntity(createCartItemRequest)).thenReturn(cartItem);
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
        when(cartItemMapper.toResponse(cartItem)).thenReturn(cartItemResponse);

        // When
        CartItemResponse result = cartItemService.createCartItem(createCartItemRequest);

        // Then
        assertNotNull(result);
        assertEquals(cartItemResponse, result);
        verify(cartItemMapper).toEntity(createCartItemRequest);
        verify(cartItemRepository).save(cartItem);
        verify(cartItemMapper).toResponse(cartItem);
    }

    @Test
    void createCartItem_DatabaseError() {
        // Given
        when(cartItemMapper.toEntity(createCartItemRequest)).thenReturn(cartItem);
        when(cartItemRepository.save(cartItem)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> cartItemService.createCartItem(createCartItemRequest));
        verify(cartItemMapper).toEntity(createCartItemRequest);
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    void createCartItem_UnexpectedError() {
        // Given
        when(cartItemMapper.toEntity(createCartItemRequest)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> cartItemService.createCartItem(createCartItemRequest));
        verify(cartItemMapper).toEntity(createCartItemRequest);
    }

    @Test
    void updateCartItem_Success() {
        // Given
        String id = "cart-item-1";
        when(cartItemRepository.findById(id)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
        when(cartItemMapper.toResponse(cartItem)).thenReturn(cartItemResponse);

        // When
        CartItemResponse result = cartItemService.updateCartItem(id, updateCartItemRequest);

        // Then
        assertNotNull(result);
        assertEquals(cartItemResponse, result);
        verify(cartItemRepository).findById(id);
        verify(cartItemMapper).updateEntity(updateCartItemRequest, cartItem);
        verify(cartItemRepository).save(cartItem);
        verify(cartItemMapper).toResponse(cartItem);
    }

    @Test
    void updateCartItem_NotFound() {
        // Given
        String id = "non-existent";
        when(cartItemRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CartItemNotFoundException.class, () -> cartItemService.updateCartItem(id, updateCartItemRequest));
        verify(cartItemRepository).findById(id);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void updateCartItem_DatabaseError() {
        // Given
        String id = "cart-item-1";
        when(cartItemRepository.findById(id)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(cartItem)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> cartItemService.updateCartItem(id, updateCartItemRequest));
        verify(cartItemRepository).findById(id);
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    void updateCartItem_UnexpectedError() {
        // Given
        String id = "cart-item-1";
        when(cartItemRepository.findById(id)).thenReturn(Optional.of(cartItem));
        doThrow(new RuntimeException("Mapping error")).when(cartItemMapper).updateEntity(updateCartItemRequest, cartItem);

        // When & Then
        assertThrows(RuntimeException.class, () -> cartItemService.updateCartItem(id, updateCartItemRequest));
        verify(cartItemRepository).findById(id);
        verify(cartItemMapper).updateEntity(updateCartItemRequest, cartItem);
    }

    @Test
    void deleteCartItem_Success() {
        // Given
        String id = "cart-item-1";
        when(cartItemRepository.existsById(id)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> cartItemService.deleteCartItem(id));

        // Then
        verify(cartItemRepository).existsById(id);
        verify(cartItemRepository).deleteById(id);
    }

    @Test
    void deleteCartItem_NotFound() {
        // Given
        String id = "non-existent";
        when(cartItemRepository.existsById(id)).thenReturn(false);

        // When & Then
        assertThrows(CartItemNotFoundException.class, () -> cartItemService.deleteCartItem(id));
        verify(cartItemRepository).existsById(id);
        verify(cartItemRepository, never()).deleteById(id);
    }

    @Test
    void deleteCartItem_DatabaseError() {
        // Given
        String id = "cart-item-1";
        when(cartItemRepository.existsById(id)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("Database error")).when(cartItemRepository).deleteById(id);

        // When & Then
        assertThrows(DataAccessException.class, () -> cartItemService.deleteCartItem(id));
        verify(cartItemRepository).existsById(id);
        verify(cartItemRepository).deleteById(id);
    }

    @Test
    void deleteCartItem_UnexpectedError() {
        // Given
        String id = "cart-item-1";
        when(cartItemRepository.existsById(id)).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(cartItemRepository).deleteById(id);

        // When & Then
        assertThrows(RuntimeException.class, () -> cartItemService.deleteCartItem(id));
        verify(cartItemRepository).existsById(id);
        verify(cartItemRepository).deleteById(id);
    }

    @Test
    void deleteCartItemByUserIdAndItemId_Success() {
        // Given
        String userId = "user-1";
        String itemId = "item-1";
        when(cartItemRepository.existsByUserIdAndItemId(userId, itemId)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> cartItemService.deleteCartItemByUserIdAndItemId(userId, itemId));

        // Then
        verify(cartItemRepository).existsByUserIdAndItemId(userId, itemId);
        verify(cartItemRepository).deleteByUserIdAndItemId(userId, itemId);
    }

    @Test
    void deleteCartItemByUserIdAndItemId_NotFound() {
        // Given
        String userId = "user-1";
        String itemId = "non-existent";
        when(cartItemRepository.existsByUserIdAndItemId(userId, itemId)).thenReturn(false);

        // When & Then
        assertThrows(CartItemNotFoundException.class, 
                () -> cartItemService.deleteCartItemByUserIdAndItemId(userId, itemId));
        verify(cartItemRepository).existsByUserIdAndItemId(userId, itemId);
        verify(cartItemRepository, never()).deleteByUserIdAndItemId(userId, itemId);
    }

    @Test
    void deleteCartItemByUserIdAndItemId_DatabaseError() {
        // Given
        String userId = "user-1";
        String itemId = "item-1";
        when(cartItemRepository.existsByUserIdAndItemId(userId, itemId)).thenReturn(true);
        doThrow(new DataIntegrityViolationException("Database error"))
                .when(cartItemRepository).deleteByUserIdAndItemId(userId, itemId);

        // When & Then
        assertThrows(DataAccessException.class, 
                () -> cartItemService.deleteCartItemByUserIdAndItemId(userId, itemId));
        verify(cartItemRepository).existsByUserIdAndItemId(userId, itemId);
        verify(cartItemRepository).deleteByUserIdAndItemId(userId, itemId);
    }

    @Test
    void deleteCartItemByUserIdAndItemId_UnexpectedError() {
        // Given
        String userId = "user-1";
        String itemId = "item-1";
        when(cartItemRepository.existsByUserIdAndItemId(userId, itemId)).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error"))
                .when(cartItemRepository).deleteByUserIdAndItemId(userId, itemId);

        // When & Then
        assertThrows(RuntimeException.class, 
                () -> cartItemService.deleteCartItemByUserIdAndItemId(userId, itemId));
        verify(cartItemRepository).existsByUserIdAndItemId(userId, itemId);
        verify(cartItemRepository).deleteByUserIdAndItemId(userId, itemId);
    }

    @Test
    void findCartItemById_Success() {
        // Given
        String id = "cart-item-1";
        when(cartItemRepository.findById(id)).thenReturn(Optional.of(cartItem));

        // When
        CartItem result = cartItemService.findCartItemById(id);

        // Then
        assertNotNull(result);
        assertEquals(cartItem, result);
        verify(cartItemRepository).findById(id);
    }

    @Test
    void findCartItemById_NotFound() {
        // Given
        String id = "non-existent";
        when(cartItemRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CartItemNotFoundException.class, () -> cartItemService.findCartItemById(id));
        verify(cartItemRepository).findById(id);
    }

    @Test
    void doesCartItemExistById_True() {
        // Given
        String id = "cart-item-1";
        when(cartItemRepository.existsById(id)).thenReturn(true);

        // When
        boolean result = cartItemService.doesCartItemExistById(id);

        // Then
        assertTrue(result);
        verify(cartItemRepository).existsById(id);
    }

    @Test
    void doesCartItemExistById_False() {
        // Given
        String id = "non-existent";
        when(cartItemRepository.existsById(id)).thenReturn(false);

        // When
        boolean result = cartItemService.doesCartItemExistById(id);

        // Then
        assertFalse(result);
        verify(cartItemRepository).existsById(id);
    }

    @Test
    void getAllCartItems_MultipleItems() {
        // Given
        CartItem cartItem2 = new CartItem();
        cartItem2.setId("cart-item-2");
        cartItem2.setUserId("user-2");
        cartItem2.setItemId("item-2");

        CartItemResponse response2 = CartItemResponse.builder()
                .id("cart-item-2")
                .userId("user-2")
                .itemId("item-2")
                .build();

        List<CartItem> cartItems = Arrays.asList(cartItem, cartItem2);
        Page<CartItem> cartItemPage = new PageImpl<>(cartItems, pageable, 2);
        
        when(cartItemRepository.findAll(pageable)).thenReturn(cartItemPage);
        when(cartItemMapper.toResponse(cartItem)).thenReturn(cartItemResponse);
        when(cartItemMapper.toResponse(cartItem2)).thenReturn(response2);

        // When
        Page<CartItemResponse> result = cartItemService.getAllCartItems(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        verify(cartItemMapper, times(2)).toResponse(any(CartItem.class));
    }

    @Test
    void getCartItemsByUserId_MultipleItems() {
        // Given
        String userId = "user-1";
        
        Map<String, Object> rawData1 = new HashMap<>();
        rawData1.put("id", "cart-item-1");
        rawData1.put("user_id", "user-1");
        
        Map<String, Object> rawData2 = new HashMap<>();
        rawData2.put("id", "cart-item-2");
        rawData2.put("user_id", "user-1");
        
        CartItemWithProductResponse response2 = CartItemWithProductResponse.builder()
                .id("cart-item-2")
                .userId("user-1")
                .build();

        List<Map<String, Object>> rawResults = Arrays.asList(rawData1, rawData2);
        when(cartItemRepository.findCartItemWithProductByUserId(userId)).thenReturn(rawResults);
        when(cartItemMapper.toCartItemWithProductReponse(rawData1)).thenReturn(cartItemWithProductResponse);
        when(cartItemMapper.toCartItemWithProductReponse(rawData2)).thenReturn(response2);

        // When
        List<CartItemWithProductResponse> result = cartItemService.getCartItemsByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(cartItemMapper, times(2)).toCartItemWithProductReponse(any(Map.class));
    }

    @Test
    void createCartItem_MapperResponseError() {
        // Given
        when(cartItemMapper.toEntity(createCartItemRequest)).thenReturn(cartItem);
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
        when(cartItemMapper.toResponse(cartItem)).thenThrow(new RuntimeException("Response mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> cartItemService.createCartItem(createCartItemRequest));
        verify(cartItemMapper).toResponse(cartItem);
    }

    @Test
    void updateCartItem_MapperResponseError() {
        // Given
        String id = "cart-item-1";
        when(cartItemRepository.findById(id)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
        when(cartItemMapper.toResponse(cartItem)).thenThrow(new RuntimeException("Response mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> cartItemService.updateCartItem(id, updateCartItemRequest));
        verify(cartItemMapper).toResponse(cartItem);
    }

    @Test
    void getCartItemsByUserId_MapperError() {
        // Given
        String userId = "user-1";
        Map<String, Object> rawData = new HashMap<>();
        rawData.put("id", "cart-item-1");
        
        List<Map<String, Object>> rawResults = Arrays.asList(rawData);
        when(cartItemRepository.findCartItemWithProductByUserId(userId)).thenReturn(rawResults);
        when(cartItemMapper.toCartItemWithProductReponse(rawData))
                .thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> cartItemService.getCartItemsByUserId(userId));
        verify(cartItemMapper).toCartItemWithProductReponse(rawData);
    }

    @Test
    void deleteCartItemByUserIdAndItemId_ExistsCheckDatabaseError() {
        // Given
        String userId = "user-1";
        String itemId = "item-1";
        when(cartItemRepository.existsByUserIdAndItemId(userId, itemId))
                .thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, 
                () -> cartItemService.deleteCartItemByUserIdAndItemId(userId, itemId));
        verify(cartItemRepository).existsByUserIdAndItemId(userId, itemId);
        verify(cartItemRepository, never()).deleteByUserIdAndItemId(userId, itemId);
    }

    @Test
    void deleteCartItem_ExistsCheckDatabaseError() {
        // Given
        String id = "cart-item-1";
        when(cartItemRepository.existsById(id)).thenThrow(new DataIntegrityViolationException("Database error"));

        // When & Then
        assertThrows(DataAccessException.class, () -> cartItemService.deleteCartItem(id));
        verify(cartItemRepository).existsById(id);
        verify(cartItemRepository, never()).deleteById(id);
    }

    @Test
    void getAllCartItems_MapperError() {
        // Given
        List<CartItem> cartItems = Arrays.asList(cartItem);
        Page<CartItem> cartItemPage = new PageImpl<>(cartItems, pageable, 1);
        when(cartItemRepository.findAll(pageable)).thenReturn(cartItemPage);
        when(cartItemMapper.toResponse(cartItem)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> cartItemService.getAllCartItems(pageable));
        verify(cartItemMapper).toResponse(cartItem);
    }

    @Test
    void getCartItemById_MapperError() {
        // Given
        String id = "cart-item-1";
        when(cartItemRepository.findById(id)).thenReturn(Optional.of(cartItem));
        when(cartItemMapper.toResponse(cartItem)).thenThrow(new RuntimeException("Mapping error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> cartItemService.getCartItemById(id));
        verify(cartItemMapper).toResponse(cartItem);
    }
}
