package com.hcmus.ecommerce_backend.product.service.impl;

import com.hcmus.ecommerce_backend.product.exception.CartItemNotFoundException;
import com.hcmus.ecommerce_backend.product.model.dto.request.CreateCartItemRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateCartItemRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.CartItemResponse;
import com.hcmus.ecommerce_backend.product.model.entity.CartItem;
import com.hcmus.ecommerce_backend.product.model.mapper.CartItemMapper;
import com.hcmus.ecommerce_backend.product.repository.CartItemRepository;
import com.hcmus.ecommerce_backend.product.service.CartItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartItemMapper cartItemMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<CartItemResponse> getAllCartItems(Pageable pageable) {
        log.info("CartItemServiceImpl | getAllCartItems | Retrieving cart items with pagination - Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            Page<CartItem> cartItemPage = cartItemRepository.findAll(pageable);
            Page<CartItemResponse> cartItemResponsePage = cartItemPage.map(cartItemMapper::toResponse);
            log.info("CartItemServiceImpl | getAllCartItems | Found {} cart items on page {} of {}",
                    cartItemResponsePage.getNumberOfElements(),
                    cartItemResponsePage.getNumber() + 1,
                    cartItemResponsePage.getTotalPages());
            return cartItemResponsePage;
        } catch (DataAccessException e) {
            log.error("CartItemServiceImpl | getAllCartItems | Database error retrieving paginated cart items: {}", e.getMessage(), e);
            return Page.empty(pageable);
        } catch (Exception e) {
            log.error("CartItemServiceImpl | getAllCartItems | Unexpected error retrieving paginated cart items: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CartItemResponse getCartItemById(String id) {
        log.info("CartItemServiceImpl | getCartItemById | id: {}", id);
        try {
            CartItem cartItem = findCartItemById(id);
            log.info("CartItemServiceImpl | getCartItemById | Cart item found with id: {}", cartItem.getId());
            return cartItemMapper.toResponse(cartItem);
        } catch (CartItemNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("CartItemServiceImpl | getCartItemById | Database error for id {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("CartItemServiceImpl | getCartItemById | Unexpected error for id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponse> getCartItemsByUserId(String userId) {
        log.info("CartItemServiceImpl | getCartItemsByUserId | userId: {}", userId);
        try {
            List<CartItemResponse> cartItems = cartItemRepository.findByUserId(userId).stream()
                    .map(cartItemMapper::toResponse)
                    .collect(Collectors.toList());
            log.info("CartItemServiceImpl | getCartItemsByUserId | Found {} cart items for user {}", cartItems.size(), userId);
            return cartItems;
        } catch (DataAccessException e) {
            log.error("CartItemServiceImpl | getCartItemsByUserId | Database error for userId {}: {}", userId, e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("CartItemServiceImpl | getCartItemsByUserId | Unexpected error for userId {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CartItemResponse createCartItem(CreateCartItemRequest request) {
        log.info("CartItemServiceImpl | createCartItem | Creating cart item for user: {}, item: {}", request.getUserId(), request.getItemId());
        try {
            CartItem cartItem = cartItemMapper.toEntity(request);
            CartItem savedCartItem = cartItemRepository.save(cartItem);
            log.info("CartItemServiceImpl | createCartItem | Created cart item with id: {}", savedCartItem.getId());
            return cartItemMapper.toResponse(savedCartItem);
        } catch (DataAccessException e) {
            log.error("CartItemServiceImpl | createCartItem | Database error creating cart item for user {}, item {}: {}", 
                    request.getUserId(), request.getItemId(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("CartItemServiceImpl | createCartItem | Unexpected error creating cart item for user {}, item {}: {}", 
                    request.getUserId(), request.getItemId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public CartItemResponse updateCartItem(String id, UpdateCartItemRequest request) {
        log.info("CartItemServiceImpl | updateCartItem | Updating cart item with id: {}", id);
        try {
            CartItem cartItem = findCartItemById(id);
            cartItemMapper.updateEntity(request, cartItem);
            CartItem updatedCartItem = cartItemRepository.save(cartItem);
            log.info("CartItemServiceImpl | updateCartItem | Updated cart item with id: {}", updatedCartItem.getId());
            return cartItemMapper.toResponse(updatedCartItem);
        } catch (CartItemNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("CartItemServiceImpl | updateCartItem | Database error updating cart item with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("CartItemServiceImpl | updateCartItem | Unexpected error updating cart item with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteCartItem(String id) {
        log.info("CartItemServiceImpl | deleteCartItem | Deleting cart item with id: {}", id);
        try {
            // Check existence first in a separate transaction
            if (!doesCartItemExistById(id)) {
                log.error("CartItemServiceImpl | deleteCartItem | Cart item not found with id: {}", id);
                throw new CartItemNotFoundException(id);
            }
            
            // Then delete in the current transaction
            cartItemRepository.deleteById(id);
            log.info("CartItemServiceImpl | deleteCartItem | Deleted cart item with id: {}", id);
        } catch (CartItemNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("CartItemServiceImpl | deleteCartItem | Database error deleting cart item with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("CartItemServiceImpl | deleteCartItem | Unexpected error deleting cart item with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void deleteCartItemByUserIdAndItemId(String userId, String itemId) {
        log.info("CartItemServiceImpl | deleteCartItemByUserIdAndItemId | Deleting cart item for user: {}, item: {}", userId, itemId);
        try {
            // Check if the cart item exists
            if (!cartItemRepository.existsByUserIdAndItemId(userId, itemId)) {
                log.error("CartItemServiceImpl | deleteCartItemByUserIdAndItemId | Cart item not found for user: {}, item: {}", userId, itemId);
                throw new CartItemNotFoundException("User ID: " + userId + ", Item ID: " + itemId);
            }
            
            cartItemRepository.deleteByUserIdAndItemId(userId, itemId);
            log.info("CartItemServiceImpl | deleteCartItemByUserIdAndItemId | Deleted cart item for user: {}, item: {}", userId, itemId);
        } catch (CartItemNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("CartItemServiceImpl | deleteCartItemByUserIdAndItemId | Database error deleting cart item for user: {}, item: {}: {}", 
                    userId, itemId, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("CartItemServiceImpl | deleteCartItemByUserIdAndItemId | Unexpected error deleting cart item for user: {}, item: {}: {}", 
                    userId, itemId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Helper method to find a cart item by ID.
     * Uses a separate transaction to avoid issues with the main transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private CartItem findCartItemById(String id) {
        return cartItemRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("CartItemServiceImpl | findCartItemById | Cart item not found with id: {}", id);
                    return new CartItemNotFoundException(id);
                });
    }
    
    /**
     * Helper method to check if a cart item exists by ID.
     * Uses a separate transaction to avoid issues with the main transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private boolean doesCartItemExistById(String id) {
        return cartItemRepository.existsById(id);
    }
}