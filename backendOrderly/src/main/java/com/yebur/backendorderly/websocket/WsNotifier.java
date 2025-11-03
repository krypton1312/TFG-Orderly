package com.yebur.backendorderly.websocket;

import org.springframework.stereotype.Component;

@Component
public class WsNotifier {
    private final OrdersTabletWebSocketHandler handler;

    public WsNotifier(OrdersTabletWebSocketHandler handler) {
        this.handler = handler;
    }

    public void send(WsEvent event) {
        handler.broadcast(event);
    }
}