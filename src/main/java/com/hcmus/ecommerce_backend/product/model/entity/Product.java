package com.hcmus.ecommerce_backend.product.model.entity;

import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.common.model.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "products")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}