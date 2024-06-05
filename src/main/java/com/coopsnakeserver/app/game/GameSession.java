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
import com.coopsnakeserver.app.pojo.SessionInfo;
import com.coopsnakeserver.app.pojo.SessionInfoType;

/**
 *
 * created: 25.05.2024
 *
 * @author June L. Gschwantner
 */
public class GameSession {
    public static int TICKS_PER_SECOND = 16;
    private static long TICK_RATE_MILLIS = 1_000 / TICKS_PER_SECOND;

    private static long INPUT_LATENCY_GRACE_PERIOD_MILLIS = 400;
    public static long INPUT_LATENCY_GRACE_PERIOD_TICKS = (long) ((double) TICKS_PER_SECOND
            * ((double) INPUT_LATENCY_GRACE_PERIOD_MILLIS / (double) 1_000));

    private static short GAME_BOARD_SIZE = 32;
    private static short INITIAL_SNAKE_SIZE = 3;

    private PlayerGameLoop pLoop;

    private int tickN = 0;
    private boolean gameRunning = true;

    private ScheduledFuture<?> tickFunc;

    public GameSession() {
        System.out.println("Created new session");
    }

    public void connectFirst(ScheduledExecutorService executor, WebSocketSession session) throws IOException {
        var token = PlayerToken.genRandom(Optional.empty());
        var tokenMsg = new BinaryMessage(token.intoMsg().intoBytes());
        session.sendMessage(tokenMsg);

        var boardInfo = new SessionInfo(SessionInfoType.BoardSize, GAME_BOARD_SIZE);
        var boardInfoMsg = new GameBinaryMessage(GameMessageType.SessionInfo, boardInfo.intoBytes());
        var boardInfoMsgBin = new BinaryMessage(boardInfoMsg.intoBytes());
        session.sendMessage(boardInfoMsgBin);

        var state = new PlayerGameState(this, session, Player.Player1, token, INITIAL_SNAKE_SIZE);
        this.pLoop = new PlayerGameLoop(state);

        var future = executor.scheduleWithFixedDelay(() -> {
            try {
                tickN += 1;
                var gameOver = pLoop.tick(tickN);
                if (gameOver) {
                    this.gameRunning = false;
                    return;
                }

                pLoop.updateWsClients(tickN);

            } catch (Exception e) {
                var t = Thread.currentThread();
                t.getUncaughtExceptionHandler().uncaughtException(t, e);

                teardown();
            }
        }, 0, TICK_RATE_MILLIS, TimeUnit.MILLISECONDS);

        this.tickFunc = future;
        System.out.println("Created session: " + session.getId());
    }

    public void teardown() {
        this.tickFunc.cancel(true);

        System.out.println("Closed session");
    }

    public void handleBinWsMsg(BinaryMessage message) {
        var bytes = message.getPayload().array();
        var msg = GameBinaryMessage.fromBytes(bytes);

        if (msg.getType() == GameMessageType.PlayerSwipeInput) {
            handleInput(msg);
        }
    }

    // TODO: validate tokens
    private void handleInput(GameBinaryMessage msg) {
        var input = PlayerSwipeInput.fromBytes(msg.getData());
        pLoop.registerSwipeInput(input);
    }

    public short getBoardSize() {
        return GAME_BOARD_SIZE;
    }

}
