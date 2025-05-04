package com.hcmus.ecommerce_backend.order.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import com.hcmus.ecommerce_backend.common.model.entity.BaseEntity;
import com.hcmus.ecommerce_backend.order.model.enums.Status;

@Entity
@Table(name = "order_track")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderTrack extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "notes")
    private String notes;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status status = Status.NEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
}