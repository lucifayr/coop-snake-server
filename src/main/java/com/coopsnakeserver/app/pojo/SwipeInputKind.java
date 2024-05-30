package com.coopsnakeserver.app.pojo;

import java.util.Optional;

import com.coopsnakeserver.app.IntoBytes;

/**
 *
 * created: 30.05.2024
 *
 * @author June L. Gschwantner
 */
public enum SwipeInputKind implements IntoBytes {
    Up((byte) 0),
    Right((byte) 1),
    Down((byte) 2),
    Left((byte) 3);

    private final byte value;

    private SwipeInputKind(byte value) {
        this.value = value;
    }

    public boolean isOnSameAxis(SwipeInputKind other) {
        if (other.value >= this.value) {
            return Math.abs(other.value - this.value) == 2;
        } else {
            return Math.abs(this.value - other.value) == 2;
        }
    }

    public static Optional<SwipeInputKind> fromByte(byte value) {
        switch (value) {
            case 0:
                return Optional.of(SwipeInputKind.Up);

            case 1:
                return Optional.of(SwipeInputKind.Right);

            case 2:
                return Optional.of(SwipeInputKind.Down);

            case 3:
                return Optional.of(SwipeInputKind.Left);

            default:
                return Optional.empty();
        }
    }

    @Override
    public byte[] intoBytes() {
        return new byte[] { this.value };
    }

    @Override
    public String toString() {
        switch (this.value) {
            case 0:
                return "Up";
            case 1:
                return "Right";
            case 2:
                return "Down";
            case 3:
                return "Left";
            default:
                return "Invalid!";
        }
    }
}
