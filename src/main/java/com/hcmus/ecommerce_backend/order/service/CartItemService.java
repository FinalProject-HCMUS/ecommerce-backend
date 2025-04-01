package com.hcmus.ecommerce_backend.order.service;

import com.hcmus.ecommerce_backend.order.model.dto.request.CreateCartItemRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateCartItemRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.CartItemResponse;

import java.util.List;

public interface CartItemService {
    
    List<CartItemResponse> getAllCartItems();
    
    CartItemResponse getCartItemById(String id);
    
    List<CartItemResponse> getCartItemsByUserId(String userId);
    
    List<CartItemResponse> getCartItemsByOrderId(String orderId);
    
    CartItemResponse createCartItem(CreateCartItemRequest request);
    
    CartItemResponse updateCartItem(String id, UpdateCartItemRequest request);
    
    void deleteCartItem(String id);
    
    void deleteCartItemByUserIdAndItemId(String userId, String itemId);
}