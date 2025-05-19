package com.hcmus.ecommerce_backend.message.event;

import com.hcmus.ecommerce_backend.message.model.dto.websocket.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("WebSocketEventListener | Received a new web socket connection");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        String conversationId = (String) headerAccessor.getSessionAttributes().get("conversationId");

        if(userId != null && conversationId != null) {
            log.info("WebSocketEventListener | User disconnected: {}", userId);

            ChatMessage chatMessage = new ChatMessage(
                "User left",
                userId,
                conversationId,
                "LEAVE"
            );

            messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, chatMessage);
        }
    }
}