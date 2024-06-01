package com.coopsnakeserver.app.game;

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
    public static boolean tick(PlayerGameState state, int tickN) {
        var gameOver = state.processTick(tickN);
        if (gameOver) {
            return true;
        }

        state.sendCoordUpdates(tickN);

        return false;
    }

}
