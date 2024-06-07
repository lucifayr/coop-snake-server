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

    private Optional<Byte> playerCount;
    private Optional<Short> boardSize;
    private Optional<Short> initialSnakeSize;

    public GameSessionConfig(Optional<Byte> playerCount, Optional<Short> boardSize, Optional<Short> initialSnakeSize) {
        this.playerCount = playerCount;
        this.boardSize = boardSize;
        this.initialSnakeSize = initialSnakeSize;
    }

    public short getBoardSize() {
        return this.boardSize.orElse(DEFAULT_BOARD_SIZE);
    }

    public short getInitialSnakeSize() {
        return this.initialSnakeSize.orElse(DEFAULT_INITIAL_SNAKE_SIZE);
    }

    public byte getPlayerCount() {
        return this.playerCount.orElse(DEFAULT_PLAYER_COUNT);
    }
}
