package com.coopsnakeserver.app.socket;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import com.coopsnakeserver.app.GameBinaryMessage;
import com.coopsnakeserver.app.PlayerSwipeInput;
import com.coopsnakeserver.app.PlayerToken;
import com.coopsnakeserver.app.pojo.GameMessageType;
import com.coopsnakeserver.app.pojo.Player;

/**
 *
 * created: 25.05.2024
 *
 * @author June L. Gschwantner
 */
public class GameSession {
    private static int TICKS_PER_SECOND = 16;
    private static long TICK_RATE_MILLIS = 1_000 / TICKS_PER_SECOND;
    private static short GAME_BOARD_SIZE = 20;

    private static short INITIAL_SNAKE_SIZE = 3;

    private GameLoop p1Loop;
    private GameLoop p2Loop;
    private WebSocketSession p1Conn;
    private WebSocketSession p2Conn;
    private PlayerToken p1Token;
    private PlayerToken p2Token;

    private int tickN = 0;
    private boolean gameRunning = true;

    private ScheduledFuture<?> tickFunc;

    public GameSession() {
        this.p1Token = PlayerToken.genRandom(Optional.empty());
        this.p2Token = PlayerToken.genRandom(Optional.of(this.p1Token));

        System.out.println("Created new session");
    }

    public void connectFirst(ScheduledExecutorService executor, WebSocketSession session) throws IOException {
        this.p1Loop = new GameLoop(this, Player.Player1, INITIAL_SNAKE_SIZE);
        this.p1Conn = session;
        session.sendMessage(this.p1Token.intoMsg());

        var future = executor.scheduleWithFixedDelay(() -> {
            if (!this.gameRunning) {
                // TODO: graceful teardown
                return;
            }

            tickN += 1;

            var p1Coords = this.p1Loop.tick(tickN);
            if (p1Coords.isEmpty()) {
                this.gameRunning = false;
                return;
            }

            var p1Msg = new GameBinaryMessage(GameMessageType.SnakePosition, p1Coords.get().intoBytes());
            try {
                p1Conn.sendMessage(new BinaryMessage(p1Msg.intoBytes()));
            } catch (IOException e) {
                e.printStackTrace();
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
        if (validToken.isPresent() && validToken.get() == Player.Player1) {
            p1Loop.setInput(Optional.of(input));
        }

    }

    public short getBoardSize() {
        return GAME_BOARD_SIZE;
    }

}
