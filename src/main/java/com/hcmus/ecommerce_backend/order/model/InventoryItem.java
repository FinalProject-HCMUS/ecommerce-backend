package com.hcmus.ecommerce_backend.order.model;

import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.product.model.entity.Product;
import com.hcmus.ecommerce_backend.product.model.entity.ProductColorSize;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryItem {
    private ProductColorSize productItem;
    private Product product;
    private Category category;
    private int quantity;
    private double unitPrice;
    private double cost;
}