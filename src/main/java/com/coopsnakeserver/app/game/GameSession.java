package com.coopsnakeserver.app.game;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import com.coopsnakeserver.app.BinaryUtils;
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
    public static int TICKS_PER_SECOND = 8;
    private static long TICK_RATE_MILLIS = 1_000 / TICKS_PER_SECOND;

    private static short GAME_BOARD_SIZE = 32;
    private static short INITIAL_SNAKE_SIZE = 3;

    private int sessionKey;
    private boolean gameRunning = true;

    private PlayerGameLoop pLoop;
    private ScheduledFuture<?> tickFunc;

    public GameSession() {
        // TODO: validate that the key is unique
        this.sessionKey = ThreadLocalRandom.current().nextInt(0, 100_000);
        System.out.println("Created new session");
    }

    public void connectFirst(ScheduledExecutorService executor, WebSocketSession session) throws IOException {
        var token = PlayerToken.genRandom(Optional.empty());
        var tokenMsg = new BinaryMessage(token.intoMsg().intoBytes());
        session.sendMessage(tokenMsg);

        var boardInfo = new SessionInfo(SessionInfoType.BoardSize, BinaryUtils.int32ToBytes((int) GAME_BOARD_SIZE));
        var boardInfoMsg = new GameBinaryMessage(GameMessageType.SessionInfo, boardInfo.intoBytes());
        var boardInfoMsgBin = new BinaryMessage(boardInfoMsg.intoBytes());
        session.sendMessage(boardInfoMsgBin);

        var state = new PlayerGameState(this, session, Player.Player1, token, INITIAL_SNAKE_SIZE);
        this.pLoop = new PlayerGameLoop(state);

        var future = executor.scheduleWithFixedDelay(() -> {
            try {
                var gameOver = pLoop.tick();
                if (gameOver.isPresent()) {
                    this.gameRunning = false;
                    notifyGameOver(gameOver.get());
                    teardown();

                    return;
                }

                pLoop.updateWsClients();
            } catch (Exception e) {
                var t = Thread.currentThread();
                t.getUncaughtExceptionHandler().uncaughtException(t, e);

                teardown();
            }
        }, 0, TICK_RATE_MILLIS, TimeUnit.MILLISECONDS);

        this.tickFunc = future;
        System.out.println("Connection opened: " + session.getId());
    }

    public void notifyGameOver(GameOverCause cause) throws IOException {
        var gameOverMsg = new SessionInfo(SessionInfoType.GameOver, cause.intoBytes());
        var gameOverMsgBin = new GameBinaryMessage(GameMessageType.SessionInfo, gameOverMsg.intoBytes());

        this.pLoop.getConnection().sendMessage(new BinaryMessage(gameOverMsgBin.intoBytes()));
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

    public int getKey() {
        return this.sessionKey;
    }

    public short getBoardSize() {
        return GAME_BOARD_SIZE;
    }

}
