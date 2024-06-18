package com.coopsnakeserver.app.game;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.coopsnakeserver.app.debug.DebugMode;
import com.coopsnakeserver.app.pojo.Player;

import com.coopsnakeserver.app.GameBinaryMessage;
import com.coopsnakeserver.app.GameSessionController;
import com.coopsnakeserver.app.debug.DebugFlag;

/**
 *
 * created: 25.05.2024
 *
 * @author June L. Gschwantner
 */
public class GameSessionSocket extends BinaryWebSocketHandler {
    public static final int MAX_SESSIONS = 1_000_000;
    public static final short MAX_BOARD_SIZE = 255;
    public static final short MIN_BOARD_SIZE = 16;
    public static final byte MAX_PLAYER_COUNT = 127;
    public static final byte MIN_PLAYER_COUNT = 2;
    public static final short MIN_INTIIAL_SNAKE_SIZE = 3;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
    private final GameSessionController controler;

    private int sessionKey;
    private GameSession gameSession;

    public GameSessionSocket(int sessionKey, GameSessionConfig config, GameSessionController controler) {
        this.controler = controler;
        this.sessionKey = sessionKey;
        this.gameSession = new GameSession(this.sessionKey, config, executor);
    }

    public void customAfterConnectionEstablished(WebSocketSession ws, boolean isView) {
        try {
            if (isView) {
                gameSession.connectViewer(ws);
                return;
            }

            gameSession.connectPlayer(ws);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void customAfterConnectionClosed(WebSocketSession session, boolean isView) {
        if (isView) {
            this.gameSession.disconnectViewer(session);
            return;
        }

        if (this.gameSession.getState() == GameSessionState.WaitingForPlayers) {
            this.gameSession.disconnectPlayer(session);
        } else {
            this.gameSession.teardown();
            controler.removeSession(this.sessionKey);
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        if (DebugMode.instanceHasFlag(DebugFlag.PlaybackFrames)) {
            var bytes = message.getPayload();
            var isDebugFrameReplayEnableMsg = bytes.get(0) == GameBinaryMessage.DEBUG_MESSAGE_IDENTIFIER;

            if (isDebugFrameReplayEnableMsg) {
                var sessionKey = bytes.getInt(1);
                var player = Player.fromByte(bytes.get(5)).get();

                gameSession.enableDebugFrameReplay(sessionKey, player, session.getId());
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

    public GameSessionConfig getSessionConfig() {
        return this.gameSession.getConfig();
    }
}
