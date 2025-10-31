package com.yebur.backendorderly.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final OrdersTabletWebSocketHandler ordersTabletWebSocketHandler;

    public WebSocketConfig(OrdersTabletWebSocketHandler ordersTabletWebSocketHandler) {
        this.ordersTabletWebSocketHandler = ordersTabletWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(ordersTabletWebSocketHandler, "/ws/overview/tablet")
                .setAllowedOrigins("*");
    }
}