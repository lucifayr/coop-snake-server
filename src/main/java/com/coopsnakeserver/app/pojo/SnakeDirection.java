package com.coopsnakeserver.app.pojo;

import java.util.Arrays;

import com.coopsnakeserver.app.DevUtils;
import com.coopsnakeserver.app.IntoBytes;

/**
 * created: 30.05.2024
 *
 * @author June L. Gschwantner
 */
public enum SnakeDirection implements IntoBytes {
    Up((byte) 0),
    Right((byte) 1),
    Down((byte) 2),
    Left((byte) 3);

    private final byte value;

    private SnakeDirection(byte value) {
        this.value = value;
    }

    public static SnakeDirection fromBytesInternal(byte[] bytes) {
        DevUtils.assertion(bytes.length == 1,
                "Expected 1 byte long array for snake direction. Received bytes " + Arrays.toString(bytes));

        var b = bytes[0];
        switch (b) {
            case 0:
                return SnakeDirection.Up;
            case 1:
                return SnakeDirection.Right;
            case 2:
                return SnakeDirection.Down;
            case 3:
                return SnakeDirection.Left;

            default:
                throw new RuntimeException(
                        String.format("Invalid byte %d received. Byte should be 0 <= byte <= 3.", b));
        }
    }

    public SwipeInputKind intoSwipeInput() {
        switch (this) {
            case Up:
                return SwipeInputKind.Up;
            case Right:
                return SwipeInputKind.Right;
            case Down:
                return SwipeInputKind.Down;
            case Left:
                return SwipeInputKind.Left;
            default:
                return SwipeInputKind.Up;
        }
    }

    public static SnakeDirection fromSwipeInput(SwipeInputKind kind) {
        switch (kind) {
            case Up:
                return SnakeDirection.Up;
            case Right:
                return SnakeDirection.Right;
            case Down:
                return SnakeDirection.Down;
            case Left:
                return SnakeDirection.Left;
            default:
                return SnakeDirection.Up;
        }
    }

    @Override
    public byte[] intoBytes() {
        return new byte[] { this.value };
    }
}
