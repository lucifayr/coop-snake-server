package com.coopsnakeserver.app.game;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.coopsnakeserver.app.debug.DebugMode;
import com.coopsnakeserver.app.debug.DebugFlag;

/**
 *
 * created: 25.05.2024
 *
 * @author June L. Gschwantner
 */
public class GameSessionSocket extends BinaryWebSocketHandler {
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

    // TODO: figure out how to handle sharing sessions
    HashMap<String, GameSession> gameSessions = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        var id = session.getId();
        try {

            var gameSession = new GameSession();
            gameSession.connectFirst(executor, session);

            gameSessions.put(id, gameSession);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        var id = session.getId();
        var gameSession = gameSessions.remove(id);

        if (gameSession == null) {
            return;
        }

        gameSession.teardown();
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        if (DebugMode.instanceHasFlag(DebugFlag.MessageInputLatency)) {
            var latency = DebugMode.instance().messageInLatency();
            try {
                Thread.sleep(latency);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        var gameSession = gameSessions.get(session.getId());
        if (gameSession == null) {
            return;
        }

        gameSession.handleBinWsMsg(message);
    }
}
