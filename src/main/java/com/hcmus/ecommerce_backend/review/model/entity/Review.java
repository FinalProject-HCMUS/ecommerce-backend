package com.hcmus.ecommerce_backend.review.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "review")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

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

    @Builder.Default
    @Column(name = "review_time")
    private LocalDateTime reviewTime = LocalDateTime.now();

    @Column(name = "order_id")
    private String orderId; //Reference to Order entity
}
