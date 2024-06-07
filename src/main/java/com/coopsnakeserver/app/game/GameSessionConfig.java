package com.coopsnakeserver.app.game;

import java.util.Optional;

/**
 * GameSessionConfig
 *
 * created: 07.06.2024
 *
 * @author June L. Gschwantner
 */
public class GameSessionConfig {
    private static byte DEFAULT_PLAYER_COUNT = 2;
    private static short DEFAULT_BOARD_SIZE = 32;
    private static short DEFAULT_INITIAL_SNAKE_SIZE = 3;

    private byte playerCount;
    private short boardSize;
    private short initialSnakeSize;

    public GameSessionConfig(Optional<Byte> playerCount, Optional<Short> boardSize, Optional<Short> initialSnakeSize) {
        this.playerCount = playerCount.orElse(DEFAULT_PLAYER_COUNT);
        this.boardSize = boardSize.orElse(DEFAULT_BOARD_SIZE);
        this.initialSnakeSize = initialSnakeSize.orElse(DEFAULT_INITIAL_SNAKE_SIZE);
    }

    public short getBoardSize() {
        return boardSize;
    }

    public short getInitialSnakeSize() {
        return initialSnakeSize;
    }

    public byte getPlayerCount() {
        return playerCount;
    }
}
