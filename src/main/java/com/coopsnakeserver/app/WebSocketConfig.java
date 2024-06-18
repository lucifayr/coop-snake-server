package com.coopsnakeserver.app;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final GameSessionGateway gateway;

    public WebSocketConfig(GameSessionGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gateway, String.format("%s/*", GameSessionGateway.URL_PREFIX_SESSION_JOIN))
                .setAllowedOrigins("*");
        registry.addHandler(gateway, String.format("%s/*", GameSessionGateway.URL_PREFIX_SESSION_VIEW))
                .setAllowedOrigins("*");
    }
}
