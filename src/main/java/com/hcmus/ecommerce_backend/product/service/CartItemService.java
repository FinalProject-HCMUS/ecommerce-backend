package com.hcmus.ecommerce_backend.product.service;

import com.hcmus.ecommerce_backend.product.model.dto.request.CreateCartItemRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateCartItemRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.CartItemResponse;

import java.util.List;

public interface CartItemService {
    
    List<CartItemResponse> getAllCartItems();
    
    CartItemResponse getCartItemById(String id);
    
    List<CartItemResponse> getCartItemsByUserId(String userId);
    
    CartItemResponse createCartItem(CreateCartItemRequest request);
    
    CartItemResponse updateCartItem(String id, UpdateCartItemRequest request);
    
    void deleteCartItem(String id);
    
    void deleteCartItemByUserIdAndItemId(String userId, String itemId);
}