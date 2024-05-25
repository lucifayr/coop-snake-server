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
import com.coopsnakeserver.app.pojo.Coordinate;
import com.coopsnakeserver.app.pojo.Player;
import com.coopsnakeserver.app.pojo.PlayerCoordiantes;

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
                session.sendMessage(placeholderData(Player.Player1));
                session.sendMessage(placeholderData(Player.Player2));
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

    private static BinaryMessage placeholderData(Player player) {

        var coords = new Coordinate[ThreadLocalRandom.current().nextInt(0, 40)];
        for (int i = 0; i < coords.length; i++) {
            var randX = ThreadLocalRandom.current().nextInt(0, GAME_BOARD_WIDTH);
            var randY = ThreadLocalRandom.current().nextInt(0, GAME_BOARD_HEIGHT);
            coords[i] = new Coordinate(randX, randY);
        }

        var playerCoords = new PlayerCoordiantes(player, coords);
        System.out.println(playerCoords);
        var placeholderGameState = new GameBinaryMessage(GameMessageType.SnakePosition, playerCoords.intoBytes());
        return new BinaryMessage(placeholderGameState.intoBytes());
    }
}
