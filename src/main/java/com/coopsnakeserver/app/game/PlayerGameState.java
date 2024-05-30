package com.coopsnakeserver.app.game;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import com.coopsnakeserver.app.DevUtils;
import com.coopsnakeserver.app.GameBinaryMessage;
import com.coopsnakeserver.app.PlayerSwipeInput;
import com.coopsnakeserver.app.PlayerToken;
import com.coopsnakeserver.app.debug.DebugData;
import com.coopsnakeserver.app.debug.DebugFlag;
import com.coopsnakeserver.app.pojo.Coordinate;
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
    private GameSession session;
    private WebSocketSession ws;

    private Player player;
    private PlayerToken token;

    private ArrayDeque<Coordinate> coords;
    private SnakeDirection direction = SnakeDirection.Right;
    private Optional<PlayerSwipeInput> input = Optional.empty();

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
        var reverse = yOffset % 2 == 0;
        this.coords = GameUtils.initialCoords(initialSankeSize, session.getBoardSize(), yOffset, reverse);

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

        this.processInput();
        this.updateCoords();

        return this.gameOverConditionHit;
    }

    public void sendClientUpdates(int tickN) {
        DevUtils.assertion(tickN == this.lastTick,
                "tickN doesn't match lastTick. Make sure to call processTick before calling this function!");

        var coords = this.getCurrentPlayerCoords(tickN);
        var msg = new GameBinaryMessage(GameMessageType.SnakePosition, coords.intoBytes());

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
        var isOutOfBounds = GameUtils.headOutOfBounds(snakeHeadNext, this.session.getBoardSize());

        if (isOutOfBounds) {
            this.gameOverConditionHit = true;
            return;
        }

        this.coords.addFirst(snakeHeadNext);
        this.coords.removeLast();
    }

    private void processInput() {
        if (this.input.isPresent()) {
            var inputKind = this.input.get().getKind();
            if (inputKind.isOnSameAxis(this.direction.intoSwipeInput())) {
                return;
            }

            // TODO: handle tickN and add grace period
            this.direction = SnakeDirection.fromSwipeInput(inputKind);
            this.input = Optional.empty();
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
