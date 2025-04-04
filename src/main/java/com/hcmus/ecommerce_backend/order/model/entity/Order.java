package com.hcmus.ecommerce_backend.order.model.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.hcmus.ecommerce_backend.common.model.entity.BaseEntity;
import com.hcmus.ecommerce_backend.order.model.enums.PaymentMethod;
import com.hcmus.ecommerce_backend.order.model.enums.Status;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "orders")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status status = Status.NEW;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Builder.Default
    @Column(name = "order_date")
    private LocalDateTime orderDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "shipping_cost")
    private Double shippingCost;

    @Column(name = "product_cost")
    private Double productCost;

    @Column(name = "sub_total")
    private Double subTotal;

    @Column(name = "total")
    private Double total;

    @Column(name = "customer_id")
    private String customerId; // References Users entity

    // Relationships
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderTrack> orderTracks;
}
