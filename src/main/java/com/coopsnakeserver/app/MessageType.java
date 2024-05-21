package com.coopsnakeserver.app;

import java.util.Optional;

/**
 * MessageType
 *
 * created: 21.05.2024
 *
 * @author June L. Gshwantner
 */
public enum MessageType {
    SnakePosition(0),
    PlayerInput(1);

    private final int tag;

    private MessageType(int typeTag) {
        this.tag = typeTag;
    }

    public static Optional<MessageType> fromTag(int typeTag) {
        switch (typeTag) {
            case 0:
                return Optional.of(MessageType.SnakePosition);

            case 1:
                return Optional.of(MessageType.PlayerInput);

            default:
                return Optional.empty();
        }
    }

    public int tag() {
        return this.tag;
    }

    public byte[] tagBytes() {
        return BinaryUtils.int32ToBytes(this.tag);
    }
}
