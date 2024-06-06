package com.coopsnakeserver.app.game;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.coopsnakeserver.app.debug.DebugMode;
import com.coopsnakeserver.app.pojo.Player;

import com.coopsnakeserver.app.GameBinaryMessage;
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
        var gameSession = gameSessions.get(session.getId());
        if (gameSession == null) {
            return;
        }

        if (DebugMode.instanceHasFlag(DebugFlag.PlaybackFrames)) {
            var bytes = message.getPayload();
            var isDebugFrameReplayEnableMsg = bytes.get(0) == GameBinaryMessage.DEBUG_MESSAGE_IDENTIFIER;

            if (isDebugFrameReplayEnableMsg) {
                var sessionKey = bytes.getInt(1);
                var player = bytes.get(5);
                // TODO
                gameSession.enableDebugFrameReplay(sessionKey, Player.Player1);

                return;
            }
        }

        if (DebugMode.instanceHasFlag(DebugFlag.MessageInputLatency)) {
            var latency = DebugMode.instance().messageInLatency();
            try {
                Thread.sleep(latency);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        gameSession.handleBinWsMsg(message);
    }
}
