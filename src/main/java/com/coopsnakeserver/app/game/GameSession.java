package com.coopsnakeserver.app.game;

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

    private PlayerGameState p1State;
    private PlayerGameState p2State;

    private int tickN = 0;
    private boolean gameRunning = true;

    private ScheduledFuture<?> tickFunc;

    public GameSession() {
        // this.p1Token = PlayerToken.genRandom(Optional.empty());
        // this.p2Token = PlayerToken.genRandom(Optional.of(this.p1Token));

        System.out.println("Created new session");
    }

    public void connectFirst(ScheduledExecutorService executor, WebSocketSession session) throws IOException {
        var token = PlayerToken.genRandom(Optional.empty());
        session.sendMessage(token.intoMsg());

        this.p1State = new PlayerGameState(this, session, Player.Player1, token, INITIAL_SNAKE_SIZE);

        var future = executor.scheduleWithFixedDelay(() -> {
            if (!this.gameRunning) {
                // TODO: graceful teardown
                return;
            }

            tickN += 1;
            var gameOver = GameLoop.tick(this.p1State, tickN);
            if (gameOver) {
                this.gameRunning = false;
                return;
            }

        }, 0, TICK_RATE_MILLIS, TimeUnit.MILLISECONDS);

        this.tickFunc = future;
        System.out.println("Player 1 session: " + session.getId());
    }

    public void teardown() {
        this.tickFunc.cancel(true);

        System.out.println("Closed 1 session: " + p1State.getConnection().getId());
        System.out.println("Closed 2 session: " + p2State.getConnection().getId());
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
        // var validToken = input.getPlayerToken().tokenOwner(this.p1State.getToken(),
        // this.p2State.getToken());
        var validToken = input.getPlayerToken().tokenOwner(this.p1State.getToken(),
                PlayerToken.genRandom(Optional.of(this.p1State.getToken())));

        if (validToken.isPresent()) {
            switch (validToken.get()) {
                case Player1:
                    p1State.setInput(Optional.of(input));
                    break;
                case Player2:
                    p2State.setInput(Optional.of(input));
                    break;
            }
        }

    }

    public short getBoardSize() {
        return GAME_BOARD_SIZE;
    }

}