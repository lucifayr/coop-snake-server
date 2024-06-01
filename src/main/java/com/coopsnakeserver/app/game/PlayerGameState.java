package com.coopsnakeserver.app.game;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Optional;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import com.coopsnakeserver.app.DevUtils;
import com.coopsnakeserver.app.GameBinaryMessage;
import com.coopsnakeserver.app.PlayerSwipeInput;
import com.coopsnakeserver.app.PlayerToken;
import com.coopsnakeserver.app.debug.DebugData;
import com.coopsnakeserver.app.debug.DebugFlag;
import com.coopsnakeserver.app.game.snapshots.SnakeSnapshotHandler;
import com.coopsnakeserver.app.pojo.Coordinate;
import com.coopsnakeserver.app.pojo.FoodCoordinate;
import com.coopsnakeserver.app.pojo.GameMessageType;
import com.coopsnakeserver.app.pojo.Player;
import com.coopsnakeserver.app.pojo.PlayerCoordiantes;
import com.coopsnakeserver.app.pojo.SnakeDirection;

/**
 * PlayerGameState
 *
 * created: 30.05.2024
 *
 * @author June L. Gschwantner
 */
public class PlayerGameState {
    private SnakeSnapshotHandler snapshotHandler;
    private GameSession session;
    private WebSocketSession ws;

    private Player player;
    private PlayerToken token;

    private ArrayDeque<Coordinate> coords;
    private SnakeDirection direction = SnakeDirection.Right;
    private Optional<PlayerSwipeInput> input = Optional.empty();
    private Coordinate food;

    private boolean gameOverConditionHit = false;
    private int lastTick = 0;

    public PlayerGameState(GameSession parent, WebSocketSession ws, Player player, PlayerToken token,
            short initialSankeSize)
            throws IOException {
        DevUtils.assertion(initialSankeSize > 0, "Snake has have more than 0 segemnts. Received " + initialSankeSize);

        this.session = parent;
        this.ws = ws;
        this.player = player;
        this.token = token;

        short yOffset = player.getValue();
        var goLeft = yOffset % 2 == 0;
        this.coords = GameUtils.initialCoords(initialSankeSize, session.getBoardSize(), yOffset, goLeft);

        if (goLeft) {
            this.direction = SnakeDirection.Left;
        } else {
            this.direction = SnakeDirection.Right;
        }

        this.snapshotHandler = new SnakeSnapshotHandler((int) GameSession.INPUT_LATENCY_GRACE_PERIOD_TICKS);
        this.snapshotHandler.takeSnapshot(this.coords.clone(), this.direction);

        updateAndSendFood();
    }

    /**
     * Set player input. Will be processed the next time {@code}tick{@code} is
     * called. Calling this function multiple times before a tick will only
     * result in only the last input being process.
     *
     * @param input The input to process in the next tick. Set to
     *              {@code}Empty{@code} to explicitly remove any input from
     *              processing.
     */
    public void setInput(Optional<PlayerSwipeInput> input) {
        this.input = input;
    }

    /**
     * Called by the {@code}GameLoop{@code} once every tick.
     */
    public boolean processTick(int tickN) {
        if (tickN == this.lastTick) {
            return false;
        }

        this.lastTick = tickN;

        this.processInput(tickN);
        this.updateCoords();

        this.snapshotHandler.takeSnapshot(this.coords.clone(), this.direction);

        return this.gameOverConditionHit;
    }

    public void sendCoordUpdates(int tickN) {
        DevUtils.assertion(tickN == this.lastTick,
                "tickN doesn't match lastTick. Make sure to call processTick before calling this function!");

        var coords = this.getCurrentPlayerCoords(tickN);
        var msg = new GameBinaryMessage(GameMessageType.PlayerPosition, coords.intoBytes());

        try {
            ws.sendMessage(new BinaryMessage(msg.intoBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlayerToken getToken() {
        return this.token;
    }

    public WebSocketSession getConnection() {
        return this.ws;
    }

    private void updateCoords() {
        var snakeHead = this.coords.peekFirst();
        var snakeHeadNext = GameUtils.nextHead(snakeHead, this.direction);
        if (DebugData.instanceHasFlag(DebugFlag.WrapAroundOnOutOfBounds)) {
            snakeHeadNext = GameUtils.wrappedHead(snakeHeadNext, this.session.getBoardSize());
        }

        var isOutOfBounds = GameUtils.headOutOfBounds(snakeHeadNext, this.session.getBoardSize());
        if (isOutOfBounds) {
            this.gameOverConditionHit = true;
            return;
        }

        this.coords.addFirst(snakeHeadNext);
        this.coords.removeLast();
    }

    private void processInput(int tickN) {
        if (this.input.isEmpty()) {
            return;
        }

        var input = this.input.get();

        var tickOnClientInput = input.getTickN();
        var ticksDueToLatencyDelta = tickN - tickOnClientInput;
        var snapshotAtClientTick = this.snapshotHandler.rewind(ticksDueToLatencyDelta);
        if (snapshotAtClientTick.isEmpty()) {
            return;
        }

        var snapshot = snapshotAtClientTick.get();
        var swipeIsNoop = input.getKind().isOnSameAxis(snapshot.getDirection().intoSwipeInput());
        if (swipeIsNoop) {
            return;
        }

        this.direction = SnakeDirection.fromSwipeInput(input.getKind());
        this.coords = snapshot.getCoords();
        this.input = Optional.empty();
    }

    private void updateAndSendFood() {
        this.food = GameUtils.nextFood(this.coords.stream().toList(), this.session.getBoardSize());

        var foodMsg = new FoodCoordinate(this.player, this.food);
        var msg = new GameBinaryMessage(GameMessageType.FoodPosition, foodMsg.intoBytes());
        try {
            ws.sendMessage(new BinaryMessage(msg.intoBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PlayerCoordiantes getCurrentPlayerCoords(int tickN) {

        if (DebugData.instanceHasFlag(DebugFlag.PlayerCoordinateDataFromFile)) {
            var coords = DebugData.instance().nextDebugCoords().orElseGet(() -> new Coordinate[0]);
            return new PlayerCoordiantes(this.player, tickN, coords);
        }

        var coords = new Coordinate[this.coords.size()];
        this.coords.toArray(coords);
        return new PlayerCoordiantes(this.player, tickN, coords);
    }
}
