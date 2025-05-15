package com.hcmus.ecommerce_backend.review.model.entity;

import com.hcmus.ecommerce_backend.common.model.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "review")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "comment")
    private String comment;

    @Column(name = "headline")
    private String headline;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "order_detail_id")
    private String orderDetailId;

    @Column(name = "user_name")
    private String userName;
}
