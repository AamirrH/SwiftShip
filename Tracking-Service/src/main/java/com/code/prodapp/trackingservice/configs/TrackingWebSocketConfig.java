package com.code.prodapp.trackingservice.configs;

import com.code.prodapp.trackingservice.websocket.TrackingWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class TrackingWebSocketConfig implements WebSocketConfigurer {

    private final TrackingWebSocketHandler trackingWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(trackingWebSocketHandler, "/ws/tracking/orders/{orderNumber}")
                .setAllowedOriginPatterns("*");
    }
}
