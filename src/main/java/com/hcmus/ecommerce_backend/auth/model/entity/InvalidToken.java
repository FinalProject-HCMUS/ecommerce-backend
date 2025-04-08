package com.hcmus.ecommerce_backend.auth.model.entity;


import com.hcmus.ecommerce_backend.common.model.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "INVALID_TOKEN")
public class InvalidToken extends BaseEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "TOKEN_ID")
    private String tokenId;

}
