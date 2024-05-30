package com.coopsnakeserver.app.game;

/**
 * GameLoop
 *
 * created: 30.05.2024
 *
 * @author June L. Gschwantner
 */
public class GameLoop {
    // TODO: is this needed?
    public static boolean tick(PlayerGameState state, int tickN) {
        var gameOver = state.processTick(tickN);
        if (gameOver) {
            return true;
        }

        state.sendClientUpdates(tickN);

        return false;
    }

}
