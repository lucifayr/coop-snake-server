package com.coopsnakeserver.app;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

// TODO/WARNING: this is a massive hack. Why. Because I don't have time to
// change the architecture of GameSessionSocket. So this is now the glue that
// allows multiple sessions to connect with a key. Not great.

/**
 * GameSessionGateway
 *
 * created: 07.06.2024
 *
 * @author June L. Gschwantner
 */
@Controller
public class GameSessionGateway extends BinaryWebSocketHandler {
    private final GameSessionController controller;

    public GameSessionGateway(GameSessionController controller) {
        this.controller = controller;
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        var key = keyFromUri(session);
        if (!key.isPresent()) {
            return;
        }
        var handler = controller.getSessions().get(key.get());

        if (handler == null) {
            return;
        }

        handler.handleMessage(session, message);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        var key = keyFromUri(session);
        if (!key.isPresent()) {
            return;
        }
        var handler = controller.getSessions().get(key.get());

        if (handler == null) {
            return;
        }

        handler.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        var key = keyFromUri(session);
        if (!key.isPresent()) {
            return;
        }
        var handler = controller.getSessions().get(key.get());

        if (handler == null) {
            return;
        }

        handler.afterConnectionClosed(session, status);
    }

    private Optional<Integer> keyFromUri(WebSocketSession session) {
        try {
            var uri = session.getUri().getPath();
            var keyString = uri.substring(uri.lastIndexOf('/') + 1);
            var key = Integer.parseInt(keyString);

            if (key < 0 || key >= 100_000) {
                return Optional.empty();
            }

            return Optional.of(key);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
