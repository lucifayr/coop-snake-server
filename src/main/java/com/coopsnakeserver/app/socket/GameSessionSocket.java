package com.coopsnakeserver.app.socket;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.coopsnakeserver.app.GameBinaryMessage;
import com.coopsnakeserver.app.GameMessageType;
import com.coopsnakeserver.app.pojo.Player;
import com.coopsnakeserver.app.pojo.PlayerCoordiante;

/**
 *
 * created: 25.05.2024
 *
 * @author June L. Gschwantner
 */
public class GameSessionSocket extends BinaryWebSocketHandler {
    public static int FPS = 30;
    public static long TICK_RATE_MILLIS = 1_000 / FPS;

    public static int GAME_BOARD_WIDTH = 40;
    public static int GAME_BOARD_HEIGHT = 40;

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);
    HashMap<String, ScheduledFuture<?>> connections = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        var id = session.getId();
        var future = executor.scheduleWithFixedDelay(() -> {
            try {
                session.sendMessage(placeholderData());
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }, 0, TICK_RATE_MILLIS, TimeUnit.MILLISECONDS);

        connections.put(id, future);

        System.out.println("Opened session: " + id);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        var id = session.getId();
        var future = connections.remove(id);

        if (future == null) {
            return;
        }

        future.cancel(true);

        System.out.println("Closed session: " + id);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        var bytes = message.getPayload().array();
        System.out.println(String.format("Message from session %s: %s", session.getId(), Arrays.toString(bytes)));
        // var msg = GameBinaryMessage.fromBytes(bytes);
    }

    private static BinaryMessage placeholderData() {
        var randX = ThreadLocalRandom.current().nextInt(0, GAME_BOARD_WIDTH);
        var randY = ThreadLocalRandom.current().nextInt(0, GAME_BOARD_HEIGHT);

        var playerCoord = new PlayerCoordiante(Player.Player1, randX, randY);
        System.out.println(playerCoord);
        System.out.println(Arrays.toString(playerCoord.intoBytes()));

        var placeholderGameState = new GameBinaryMessage(GameMessageType.SnakePosition, playerCoord.intoBytes());
        return new BinaryMessage(placeholderGameState.intoBytes());
    }
}
