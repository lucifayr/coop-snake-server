package com.coopsnakeserver.app.game;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Optional;

import org.springframework.web.socket.BinaryMessage;

import com.coopsnakeserver.app.GameBinaryMessage;
import com.coopsnakeserver.app.PlayerSwipeInput;
import com.coopsnakeserver.app.debug.DebugData;
import com.coopsnakeserver.app.debug.DebugFlag;
import com.coopsnakeserver.app.game.frame.PlayerGameFrame;
import com.coopsnakeserver.app.pojo.Coordinate;
import com.coopsnakeserver.app.pojo.FoodCoordinate;
import com.coopsnakeserver.app.pojo.GameMessageType;
import com.coopsnakeserver.app.pojo.PlayerCoordiantes;
import com.coopsnakeserver.app.pojo.SnakeDirection;

// Note to self: every time canonicalFrame() is called, the entire frame is
// copied. It's a lot easier to manage mutations that way, but if RAM/CPU usage
// is too heavy, this should be checked ASAP.

/**
 * PlayerGameLoop
 *
 * created: 30.05.2024
 *
 * @author June L. Gschwantner
 */
public class PlayerGameLoop {
    PlayerGameState state;
    private ArrayDeque<PlayerSwipeInput> swipeInputQueue = new ArrayDeque<>();

    public PlayerGameLoop(PlayerGameState state) {
        this.state = state;
    }

    // TODO: handle debug frame replay
    // if (DebugData.instanceHasFlag(DebugFlag.PlayerCoordinateDataFromFile)) {
    // var coords = DebugData.instance().nextDebugCoords().orElseGet(() -> new
    // Coordinate[0]);
    // return new PlayerCoordiantes(this.player, tickN, coords);
    // }

    public boolean tick(int tickN) {
        var newSnakeDirection = processSwipeInput(tickN);
        var snakeInfo = nextSnakeInfo(newSnakeDirection);

        var food = nextFood(snakeInfo.hasEatenFood, snakeInfo.coords());

        // TODO: handle game over
        var frame = new PlayerGameFrame(snakeInfo.coords, snakeInfo.direction, food);
        this.state.newCanonicalFrame(frame);

        return false;
    }

    public void updateWsClients(int tickN) throws IOException {
        var frame = this.state.canonicalFrame();

        var foodCoord = new FoodCoordinate(this.state.getPlayer(), frame.getFoodCoord());
        var foodMsg = new GameBinaryMessage(GameMessageType.FoodPosition, foodCoord.intoBytes());
        var foodMsgBin = new BinaryMessage(foodMsg.intoBytes());

        var coords = new Coordinate[frame.getSnakeCoords().size()];
        frame.getSnakeCoords().toArray(coords);

        var playerCoords = new PlayerCoordiantes(this.state.getPlayer(), tickN, coords);
        var playerMsg = new GameBinaryMessage(GameMessageType.PlayerPosition, playerCoords.intoBytes());
        var playerMsgBin = new BinaryMessage(playerMsg.intoBytes());

        var ws = this.state.getConnection();
        ws.sendMessage(playerMsgBin);
        ws.sendMessage(foodMsgBin);
    }

    public void registerSwipeInput(PlayerSwipeInput input) {
        swipeInputQueue.addLast(input);
    }

    private Optional<SnakeDirection> processSwipeInput(int tickN) {
        var input = this.swipeInputQueue.pollFirst();
        if (input == null) {
            return Optional.empty();
        }

        var tickOnClientInput = input.getTickN();
        var ticksDueToLatencyDelta = tickN - tickOnClientInput;
        var frameAtClientTick = this.state.rewindFrames(ticksDueToLatencyDelta);
        if (frameAtClientTick.isEmpty()) {
            return Optional.empty();
        }

        var frame = frameAtClientTick.get();
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
        if (DebugData.instanceHasFlag(DebugFlag.WrapAroundOnOutOfBounds)) {
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

        var newCoord = GameUtils.nextFood(snakeCoords.stream().toList(), this.state.getBoardSize());
        return newCoord;
    }

    // TODO:
    private boolean isOutOfBounds(Coordinate snakeHead) {
        return false;

        // var isOutOfBounds = GameUtils.headOutOfBounds(snakeHeadNext,
        // this.session.getBoardSize());
        // if (isOutOfBounds) {
        // this.gameOverConditionHit = true;
        // return;
        // }
    }

    // TODO:
    private boolean isEatingFood(Coordinate snakeHead) {
        return false;

        // var isOutOfBounds = GameUtils.headOutOfBounds(snakeHeadNext,
        // this.session.getBoardSize());
        // if (isOutOfBounds) {
        // this.gameOverConditionHit = true;
        // return;
        // }
    }

    // private boolean isCollidingWithOtherSnake() {}

    private record SnakeInfo(ArrayDeque<Coordinate> coords, SnakeDirection direction, boolean hasEatenFood) {
    }
}
