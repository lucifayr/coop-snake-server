package com.coopsnakeserver.app.game;

import java.util.ArrayDeque;

import com.coopsnakeserver.app.DevUtils;
import com.coopsnakeserver.app.PlayerSwipeInput;
import com.coopsnakeserver.app.game.snapshots.SnakeSnapshotHandler;

/**
 * GameLoop
 *
 * Wrapper around calling state updates individually.
 *
 * created: 30.05.2024
 *
 * @author June L. Gschwantner
 */
public class GameLoop {
    PlayerGameState state;

    private int lastTick = 0;
    private ArrayDeque<PlayerSwipeInput> inputQueue = new ArrayDeque<>();
    private SnakeSnapshotHandler snapshotHandler = new SnakeSnapshotHandler(
            (int) GameSession.INPUT_LATENCY_GRACE_PERIOD_TICKS);

    public GameLoop(PlayerGameState state) {
        this.state = state;
    }

    public boolean tick(int tickN) {
        var gameOver = state.processTick(tickN);
        if (gameOver) {
            return true;
        }

        // TODO:
        // this.snapshotHandler.takeSnapshot(this.coords.clone(),this.direction);

        state.sendCoordUpdates(tickN);

        return false;
    }

    public void registerInput(PlayerSwipeInput input) {
        DevUtils.assertion(this.lastTick >= input.getTickN(), String.format(
                "Attempted to register input in the future. Current tick %d, Target tick %d.", this.lastTick,
                input.getTickN()));

        inputQueue.addLast(input);
    }

    private void processInput(int tickN) {
        var input = this.inputQueue.getFirst();
        if (input == null) {
            return;
        }

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

}
