package com.coopsnakeserver.app.game;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Optional;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import com.coopsnakeserver.app.GameBinaryMessage;
import com.coopsnakeserver.app.PlayerSwipeInput;
import com.coopsnakeserver.app.debug.DebugMode;
import com.coopsnakeserver.app.debug.DebugFlag;
import com.coopsnakeserver.app.game.frame.PlayerGameFrame;
import com.coopsnakeserver.app.pojo.Coordinate;
import com.coopsnakeserver.app.pojo.FoodCoordinate;
import com.coopsnakeserver.app.pojo.GameMessageType;
import com.coopsnakeserver.app.pojo.PlayerCoordiantes;
import com.coopsnakeserver.app.pojo.SnakeDirection;

/**
 * PlayerGameLoop
 *
 * created: 30.05.2024
 *
 * @author June L. Gschwantner
 */
public class PlayerGameLoop {
    private PlayerGameState state;
    private ArrayDeque<PlayerSwipeInput> swipeInputQueue = new ArrayDeque<>();

    private int tickN = 0;

    public PlayerGameLoop(PlayerGameState state) {
        this.state = state;
    }

    // TODO: handle debug frame replay
    // if (DebugData.instanceHasFlag(DebugFlag.PlayerCoordinateDataFromFile)) {
    // var coords = DebugData.instance().nextDebugCoords().orElseGet(() -> new
    // Coordinate[0]);
    // return new PlayerCoordiantes(this.player, tickN, coords);
    // }

    public Optional<GameOverCause> tick() {
        this.tickN += 1;

        if (DebugMode.instanceHasFlag(DebugFlag.PlaybackFrames)) {
            var frame = DebugMode.instance().playback(state.getSessionKey(), state.getPlayer());
            this.state.newCanonicalFrame(frame);
            return Optional.empty();
        }

        var newSnakeDirection = processSwipeInput();
        var snakeInfo = nextSnakeInfo(newSnakeDirection);

        if (GameUtils.headOutOfBounds(snakeInfo.coords().getFirst(), this.state.getBoardSize())) {
            DebugMode.recordGameOverIfEnabled(this.state.getSessionKey());
            return Optional.of(GameOverCause.CollisionBounds);
        }

        if (GameUtils.headSelfCollision(snakeInfo.coords().getFirst(), snakeInfo.coords().stream().toList())) {
            DebugMode.recordGameOverIfEnabled(this.state.getSessionKey());
            return Optional.of(GameOverCause.CollisionSelf);
        }

        var food = nextFood(snakeInfo.hasEatenFood, snakeInfo.coords());
        var frame = new PlayerGameFrame(snakeInfo.coords, snakeInfo.direction, food);
        this.state.newCanonicalFrame(frame);

        return Optional.empty();

    }

    public void updateWsClients() throws IOException {
        var frame = this.state.canonicalFrame();

        var foodCoord = new FoodCoordinate(this.state.getPlayer(), frame.getFoodCoord());
        var foodMsg = new GameBinaryMessage(GameMessageType.FoodPosition, foodCoord.intoBytes());
        var foodMsgBin = new BinaryMessage(foodMsg.intoBytes());

        var coords = new Coordinate[frame.getSnakeCoords().size()];
        frame.getSnakeCoords().toArray(coords);

        var playerCoords = new PlayerCoordiantes(this.state.getPlayer(), this.tickN, coords);
        var playerMsg = new GameBinaryMessage(GameMessageType.PlayerPosition, playerCoords.intoBytes());
        var playerMsgBin = new BinaryMessage(playerMsg.intoBytes());

        var ws = this.state.getConnection();
        ws.sendMessage(playerMsgBin);
        ws.sendMessage(foodMsgBin);
    }

    public void registerSwipeInput(PlayerSwipeInput input) {
        swipeInputQueue.addLast(input);
    }

    public WebSocketSession getConnection() {
        return this.state.getConnection();
    }

    private Optional<SnakeDirection> processSwipeInput() {
        var input = this.swipeInputQueue.pollFirst();
        if (input == null) {
            return Optional.empty();
        }

        var tickOnClientInput = input.getTickN();
        var ticksDueToLatencyDelta = this.tickN - tickOnClientInput;
        if (ticksDueToLatencyDelta < 0) {
            return Optional.empty();
        }

        if ((double) ticksDueToLatencyDelta / (double) GameSession.TICKS_PER_SECOND > 0.3) {
            System.out.println("WARNING: latency is very high. delay in ticks = " + ticksDueToLatencyDelta);
        }

        var frame = this.state.canonicalFrame();
        var swipeIsNoop = input.getKind().isOnSameAxis(frame.getSnakeDirection().intoSwipeInput());
        if (swipeIsNoop) {
            return Optional.empty();
        }

        var direction = SnakeDirection.fromSwipeInput(input.getKind());
        return Optional.of(direction);
    }

    private SnakeInfo nextSnakeInfo(Optional<SnakeDirection> newDirection) {
        var lastFrame = this.state.canonicalFrame();
        var coords = lastFrame.getSnakeCoords();
        var direction = newDirection.orElseGet(() -> lastFrame.getSnakeDirection());

        var snakeHead = coords.peekFirst();
        var snakeHeadNext = GameUtils.nextHead(snakeHead, direction);
        if (DebugMode.instanceHasFlag(DebugFlag.WrapAroundOnOutOfBounds)) {
            snakeHeadNext = GameUtils.wrappedHead(snakeHeadNext, state.getBoardSize());
        }

        var hasEatenFood = isEatingFood(snakeHeadNext);
        coords.addFirst(snakeHeadNext);
        if (!hasEatenFood) {
            coords.removeLast();
        }

        return new SnakeInfo(coords, direction, hasEatenFood);
    }

    private Coordinate nextFood(boolean snakeHasEatenFood, ArrayDeque<Coordinate> snakeCoords) {
        var lastFrame = this.state.canonicalFrame();
        if (!snakeHasEatenFood) {
            return lastFrame.getFoodCoord();
        }

        var newCoord = GameUtils.findFoodCoord(snakeCoords.getFirst(), snakeCoords.stream().toList(),
                this.state.getBoardSize());
        return newCoord;
    }

    private boolean isEatingFood(Coordinate snakeHead) {
        var lastFrame = this.state.canonicalFrame();
        var foodCoord = lastFrame.getFoodCoord();
        return foodCoord.equals(snakeHead);
    }

    // private boolean isCollidingWithOtherSnake() {}

    private record SnakeInfo(ArrayDeque<Coordinate> coords, SnakeDirection direction, boolean hasEatenFood) {
    }
}
