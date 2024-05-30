package com.coopsnakeserver.app.pojo;

/**
 *
 * created: 30.05.2024
 *
 * @author June L. Gschwantner
 */
public enum SnakeDirection {
    Up,
    Right,
    Down,
    Left;

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
}
