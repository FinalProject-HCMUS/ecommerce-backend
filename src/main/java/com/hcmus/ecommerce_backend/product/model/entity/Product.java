package com.hcmus.ecommerce_backend.product.model.entity;

import com.hcmus.ecommerce_backend.category.model.entity.Category;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private double cost;

    @Column(nullable = false)
    private int total;

    @Column(nullable = false)
    private double price;

    @Column(name = "discount_percent")
    private double discountPercent;

    @Column(nullable = false)
    private boolean enable;

    @Column(name = "in_stock", nullable = false)
    private boolean inStock;

    @Column(name = "main_image_url")
    private String mainImageUrl;

    @Column(name = "average_rating")
    private double averageRating;

    @Column(name = "review_count")
    private double reviewCount;

    @Column(name = "created_time", nullable = false)
    private String createdTime;

    @Column(name = "update_time", nullable = false)
    private String updateTime;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;  
}