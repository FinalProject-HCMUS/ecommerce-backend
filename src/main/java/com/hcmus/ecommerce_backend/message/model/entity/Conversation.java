package com.hcmus.ecommerce_backend.message.model.entity;

import com.hcmus.ecommerce_backend.common.model.entity.BaseEntity;

import com.hcmus.ecommerce_backend.user.model.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @OneToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private User customer; // The customer involved in the conversation

    @Column(name = "is_admin_read")
    private boolean isAdminRead; // Indicates if the admin has read the conversation

    @Column(name = "is_customer_read")
    private boolean isCustomerRead; // Indicates if the customer has read the conversation

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages;
}
