package com.hcmus.ecommerce_backend.message.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import com.hcmus.ecommerce_backend.common.model.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Message extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "content")
    private String content;

    @Column(name = "user_id")
    private String userId; // References Users entity

    @Column(name = "message_type")
    private String messageType; // e.g., "text", "image"

    @Column(name = "content_url")
    private String contentUrl;

    @Column(name = "is_read")
    private boolean isRead;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @ManyToOne
    @JoinColumn(name = "conversation_id", referencedColumnName = "id")
    private Conversation conversation; // References Conversation entity
}
