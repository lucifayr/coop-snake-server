package com.coopsnakeserver.app.pojo;

import java.util.Optional;

import com.coopsnakeserver.app.BinaryUtils;
import com.coopsnakeserver.app.IntoBytes;

/**
 * GameMessageType
 *
 * created: 21.05.2024
 *
 * @author June L. Gshwantner
 */
public enum GameMessageType implements IntoBytes {
    PlayerPosition(0),
    PlayerSwipeInput(1),
    SessionInfo(2),
    FoodPosition(3),
    PlayerRestartConfirm(4),
    PlayerRestartDeny(5),
    ErrorInvalidVersion(20),
    ErrorInvalidType(21),
    ErrorInvalidDataLength(22);

    private final int tag;

    private GameMessageType(int typeTag) {
        this.tag = typeTag;
    }

    public static Optional<GameMessageType> fromTag(int typeTag) {
        switch (typeTag) {
            case 0:
                return Optional.of(GameMessageType.PlayerPosition);

            case 1:
                return Optional.of(GameMessageType.PlayerSwipeInput);

            case 2:
                return Optional.of(GameMessageType.SessionInfo);

            case 3:
                return Optional.of(GameMessageType.FoodPosition);

            case 4:
                return Optional.of(GameMessageType.PlayerRestartConfirm);

            case 5:
                return Optional.of(GameMessageType.PlayerRestartDeny);

            case 20:
                return Optional.of(GameMessageType.ErrorInvalidVersion);

            case 21:
                return Optional.of(GameMessageType.ErrorInvalidType);

            case 22:
                return Optional.of(GameMessageType.ErrorInvalidDataLength);

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

    public static byte[] validBytes() {
        return BinaryUtils.iteratorToBytes(GameMessageType.values());

    }

    @Override
    public String toString() {
        return String.format("%s (tag = %d) ", this.name(), this.tag);
    }
}
