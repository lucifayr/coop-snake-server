package com.coopsnakeserver.app.game;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import com.coopsnakeserver.app.GameBinaryMessage;
import com.coopsnakeserver.app.PlayerSwipeInput;
import com.coopsnakeserver.app.PlayerToken;
import com.coopsnakeserver.app.debug.DebugMode;
import com.coopsnakeserver.app.debug.DebugFlag;
import com.coopsnakeserver.app.game.frame.PlayerGameFrame;
import com.coopsnakeserver.app.pojo.Coordinate;
import com.coopsnakeserver.app.pojo.FoodCoordinate;
import com.coopsnakeserver.app.pojo.GameMessageType;
import com.coopsnakeserver.app.pojo.Player;
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
    private GameSession parentSession;
    private PlayerGameState state;
    private ArrayDeque<PlayerSwipeInput> swipeInputQueue = new ArrayDeque<>();

    private int tickN = 0;

    private Optional<Integer> debugSessionKey = Optional.empty();
    private Optional<Player> debugPlayer = Optional.empty();

    public PlayerGameLoop(GameSession parentSession, PlayerGameState state) {
        this.parentSession = parentSession;
        this.state = state;
    }

    public Optional<GameOverCause> tick() {
        this.tickN += 1;

        if (DebugMode.instanceHasFlag(DebugFlag.PlaybackFrames)) {
            var sessionKey = this.debugSessionKey.orElseGet(() -> this.state.getSessionKey());
            var player = this.debugPlayer.orElseGet(() -> this.state.getPlayer());
            var frame = DebugMode.instance().playback(sessionKey, player);

            if (frame.isPresent()) {
                this.state.newCanonicalFrame(frame.get());
                return Optional.empty();
            }
        }

        var newSnakeDirection = processSwipeInput();
        var snakeInfo = nextSnakeInfo(newSnakeDirection);

        if (GameUtils.headSelfCollision(snakeInfo.coords().getFirst(), snakeInfo.coords().stream().toList())) {
            DebugMode.recordGameOverIfEnabled(this.state.getSessionKey());
            return Optional.of(GameOverCause.CollisionSelf);
        }

        var otherSnakeCoords = this.parentSession.getOtherLoops(this.state.getPlayer()).stream()
                .flatMap(l -> {
                    var coords = l.getCanonicalFrame().getSnakeCoords();
                    return coords.stream();
                }).toList();

        if (GameUtils.headOtherCollision(snakeInfo.coords().getFirst(), otherSnakeCoords)) {
            DebugMode.recordGameOverIfEnabled(this.state.getSessionKey());
            return Optional.of(GameOverCause.CollisionOther);
        }

        var food = nextFood(snakeInfo.hasEatenFood, snakeInfo.coords());
        var frame = new PlayerGameFrame(snakeInfo.coords, snakeInfo.direction, food);
        this.state.newCanonicalFrame(frame);

        return Optional.empty();

    }

    public void updateWsClients() throws IOException {
        var foodMsg = getFoodMsg();
        var foodMsgBin = new BinaryMessage(foodMsg.intoBytes());

        var playerMsg = getPlayerMsg();
        var playerMsgBin = new BinaryMessage(playerMsg.intoBytes());

        var ws = this.state.getConnection();
        ws.sendMessage(playerMsgBin);
        ws.sendMessage(foodMsgBin);

        for (var loop : this.parentSession.getOtherLoops(this.state.getPlayer())) {
            var otherFoodMsg = loop.getFoodMsg();
            var otherFoodMsgBin = new BinaryMessage(otherFoodMsg.intoBytes());

            var otherPlayerMsg = loop.getPlayerMsg();
            var otherPlayerMsgBin = new BinaryMessage(otherPlayerMsg.intoBytes());

            ws.sendMessage(otherPlayerMsgBin);
            ws.sendMessage(otherFoodMsgBin);
        }
    }

    public GameBinaryMessage getPlayerMsg() {
        var frame = this.state.canonicalFrame();
        var coords = new Coordinate[frame.getSnakeCoords().size()];
        frame.getSnakeCoords().toArray(coords);

        var playerCoords = new PlayerCoordiantes(this.state.getPlayer(), this.tickN, coords);
        return new GameBinaryMessage(GameMessageType.PlayerPosition, playerCoords.intoBytes());
    }

    public PlayerGameFrame getCanonicalFrame() {
        return this.state.canonicalFrame();
    }

    public GameBinaryMessage getFoodMsg() {
        var frame = this.state.canonicalFrame();
        var foodCoord = new FoodCoordinate(this.state.getPlayer(), frame.getFoodCoord());
        return new GameBinaryMessage(GameMessageType.FoodPosition, foodCoord.intoBytes());
    }

    public void registerSwipeInput(PlayerSwipeInput input) {
        swipeInputQueue.addLast(input);
    }

    public PlayerToken getToken() {
        return this.state.getToken();
    }

    public WebSocketSession getConnection() {
        return this.state.getConnection();
    }

    public void enableDebugFrameReplay(int sessionKey, Player player) {
        this.debugSessionKey = Optional.of(sessionKey);
        this.debugPlayer = Optional.of(player);
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
        var snakeHeadNext = GameUtils.wrappedHead(GameUtils.nextHead(snakeHead, direction), state.getBoardSize());

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
