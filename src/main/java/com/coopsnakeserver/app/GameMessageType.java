package com.coopsnakeserver.app;

import java.util.Optional;

/**
 * GameMessageType
 *
 * created: 21.05.2024
 *
 * @author June L. Gshwantner
 */
public enum GameMessageType implements IntoBytes {
    SnakePosition(0),
    PlayerInput(1);

    private final int tag;

    private GameMessageType(int typeTag) {
        this.tag = typeTag;
    }

    public static Optional<GameMessageType> fromTag(int typeTag) {
        switch (typeTag) {
            case 0:
                return Optional.of(GameMessageType.SnakePosition);

            case 1:
                return Optional.of(GameMessageType.PlayerInput);

            default:
                return Optional.empty();
        }
    }

    @Override
    public byte[] intoBytes() {
        return BinaryUtils.int32ToBytes(this.tag);
    }

    public int tag() {
        return this.tag;
    }

    @Override
    public String toString() {
        return String.format("%s (tag = %d) ", this.name(), this.tag);
    }
}
