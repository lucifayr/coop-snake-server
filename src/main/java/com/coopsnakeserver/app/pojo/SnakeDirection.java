package com.coopsnakeserver.app.pojo;

import com.coopsnakeserver.app.IntoBytes;

/**
 *
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
