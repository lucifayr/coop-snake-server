package com.coopsnakeserver.app.game;

import java.io.IOException;
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
    public static final int MAX_SESSIONS = 1_000_000;
    public static final short MAX_BOARD_SIZE = 255;
    public static final short MIN_BOARD_SIZE = 16;
    public static final byte MAX_PLAYER_COUNT = 127;
    public static final byte MIN_PLAYER_COUNT = 2;
    public static final short MIN_INTIIAL_SNAKE_SIZE = 3;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

    private int sessionKey;
    private GameSession gameSession;

    private byte nextPlayer = 1;

    public GameSessionSocket(int sessionKey, GameSessionConfig config) {
        this.sessionKey = sessionKey;
        this.gameSession = new GameSession(this.sessionKey, config, executor);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession ws) {
        try {
            gameSession.connectPlayer(this.nextPlayer, ws);
            this.nextPlayer += 1;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // TODO: handle disconnects properly
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        if (this.gameSession.getState() == GameSessionState.WaitingForPlayers) {
            this.gameSession.disconnectPlayer((byte) (this.nextPlayer - 1));
            nextPlayer -= 1;
        } else {
            this.gameSession.teardown();
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

                // massive hack :)
                var lastConnectedPlayer = Player.fromByte((byte) (this.nextPlayer - 1)).get();

                gameSession.enableDebugFrameReplay(sessionKey, player, lastConnectedPlayer);
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
