package com.coopsnakeserver.app.socket;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.coopsnakeserver.app.GameBinaryMessage;
import com.coopsnakeserver.app.PlayerSwipeInput;
import com.coopsnakeserver.app.PlayerToken;
import com.coopsnakeserver.app.pojo.GameMessageType;
import com.coopsnakeserver.app.debug.DebugData;
import com.coopsnakeserver.app.debug.DebugFlag;
import com.coopsnakeserver.app.pojo.Coordinate;
import com.coopsnakeserver.app.pojo.Player;
import com.coopsnakeserver.app.pojo.PlayerCoordiantes;

/**
 *
 * created: 25.05.2024
 *
 * @author June L. Gschwantner
 */
public class GameSession {
    public static int TICKS_PER_SECOND = 16;
    public static long TICK_RATE_MILLIS = 1_000 / TICKS_PER_SECOND;
    public static int GAME_BOARD_SIZE = 20;
    public static int INITIAL_SNAKE_SIZE = 3;

    private PlayerToken p1Token;
    private PlayerToken p2Token;

    private WebSocketSession p1Conn;
    private WebSocketSession p2Conn;

    private int tickN = 0;
    private ScheduledFuture<?> tickFunc;

    public GameSession() {
        this.p1Token = PlayerToken.genRandom(Optional.empty());
        this.p2Token = PlayerToken.genRandom(Optional.of(this.p1Token));

        System.out.println("Created new session");
    }

    public void connectFirst(ScheduledExecutorService executor, WebSocketSession session) throws IOException {

        this.p1Conn = session;
        this.p1Conn.sendMessage(this.p1Token.intoMsg());

        var future = executor.scheduleWithFixedDelay(() -> {
            tickN += 1;

            try {
                this.p1Conn.sendMessage(placeholderData(Player.Player1, tickN));
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }, 0, TICK_RATE_MILLIS, TimeUnit.MILLISECONDS);

        this.tickFunc = future;

        System.out.println("Player 1 session: " + session.getId());
    }

    public void teardown() {
        this.tickFunc.cancel(true);
        System.out.println("Closed session: " + p1Conn.getId());
    }

    public void handleBinWsMsg(BinaryMessage message) {
        var bytes = message.getPayload().array();
        var msg = GameBinaryMessage.fromBytes(bytes);

        if (msg.getType() == GameMessageType.PlayerSwipeInput) {
            handleInput(msg);
        }
    }

    private void handleInput(GameBinaryMessage msg) {
        var input = PlayerSwipeInput.fromBytes(msg.getData());
        var validToken = input.getPlayerToken().tokenOwner(this.p1Token, this.p2Token);
        if (validToken.isEmpty()) {
            return;
        }

        System.out.println(input);
    }

    private static BinaryMessage placeholderData(Player player, int tickN) {
        var coords = randomCoords();
        if (DebugData.instanceHasFlag(DebugFlag.PlayerCoordinateDataFromFile)) {
            coords = DebugData.instance().nextDebugCoords().orElseGet(() -> new Coordinate[0]);
        }

        var playerCoords = new PlayerCoordiantes(player, tickN, coords);
        var placeholderGameState = new GameBinaryMessage(GameMessageType.SnakePosition, playerCoords.intoBytes());
        return new BinaryMessage(placeholderGameState.intoBytes());
    }

    private static Coordinate[] randomCoords() {
        var coords = new Coordinate[ThreadLocalRandom.current().nextInt(0, 40)];
        for (int i = 0; i < coords.length; i++) {
            var randX = ThreadLocalRandom.current().nextInt(0, GAME_BOARD_SIZE);
            var randY = ThreadLocalRandom.current().nextInt(0, GAME_BOARD_SIZE);
            coords[i] = new Coordinate((short) randX, (short) randY);
        }

        return coords;
    }
}
