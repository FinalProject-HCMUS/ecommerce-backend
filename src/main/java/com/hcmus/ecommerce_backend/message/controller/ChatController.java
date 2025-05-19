package com.hcmus.ecommerce_backend.message.controller;

import com.hcmus.ecommerce_backend.message.model.dto.request.CreateMessageRequest;
import com.hcmus.ecommerce_backend.message.model.dto.response.MessageResponse;
import com.hcmus.ecommerce_backend.message.model.dto.websocket.ChatMessage;
import com.hcmus.ecommerce_backend.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage/{conversationId}")
    public void sendMessage(@DestinationVariable String conversationId, @Payload ChatMessage chatMessage) {
        log.info("ChatController | sendMessage | Received message in conversation: {}", conversationId);

        // Create and save the message
        CreateMessageRequest messageRequest = new CreateMessageRequest(
                chatMessage.getContent(),
                chatMessage.getUserId(),
                conversationId,
                chatMessage.getMessageType(),
                chatMessage.getContentUrl()
        );

        MessageResponse savedMessage = messageService.createMessage(messageRequest);

        // Send the message to subscribers of this conversation
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId,
                savedMessage
        );
    }

    @MessageMapping("/chat.join/{conversationId}")
    public void joinConversation(@DestinationVariable String conversationId,
                                @Payload ChatMessage chatMessage,
                                SimpMessageHeaderAccessor headerAccessor) {

        log.info("ChatController | joinConversation | User {} joined conversation: {}",
                chatMessage.getUserId(), conversationId);

        // Store user info in WebSocket session
        headerAccessor.getSessionAttributes().put("userId", chatMessage.getUserId());
        headerAccessor.getSessionAttributes().put("conversationId", conversationId);

        // Notify others that user has joined
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId,
                new ChatMessage("User joined", chatMessage.getUserId(), conversationId, "JOIN")
        );
    }
}