package com.coopsnakeserver.app.socket;

import java.io.IOException;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 *
 * created: 13.04.2024
 *
 * @author June L. Gschwantner
 */
public class EchoWebsocket extends TextWebSocketHandler {

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        System.out.println(message);

        session.sendMessage(message);
    }
}
