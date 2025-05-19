package com.hcmus.ecommerce_backend.message.model.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String content;
    private String userId;
    private String conversationId;
    private String messageType;
    private String contentUrl;

    public ChatMessage(String content, String userId, String conversationId, String messageType) {
        this.content = content;
        this.userId = userId;
        this.conversationId = conversationId;
        this.messageType = messageType;
        this.contentUrl = null;
    }
}