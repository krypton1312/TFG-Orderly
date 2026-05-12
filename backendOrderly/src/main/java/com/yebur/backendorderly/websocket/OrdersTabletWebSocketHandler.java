package com.yebur.backendorderly.websocket;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class OrdersTabletWebSocketHandler extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("✅ Client connected: " + session.getId());
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        System.out.println("❌ Client disconnected: " + session.getId());
        sessions.remove(session);
    }

    public void broadcast(Object messageObject) {
        String json;
        try {
            json = mapper.writeValueAsString(messageObject);
        } catch (Exception e) {
            System.err.println("[WS] Failed to serialize event: " + e.getMessage());
            return;
        }
        synchronized (sessions) {
            for (WebSocketSession session : sessions) {
                if (!session.isOpen()) continue;
                try {
                    session.sendMessage(new TextMessage(json));
                } catch (Exception e) {
                    System.err.println("[WS] Failed to send to session " + session.getId() + ": " + e.getMessage());
                }
            }
        }
    }
}
