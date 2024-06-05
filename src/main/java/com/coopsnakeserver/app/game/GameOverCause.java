package com.coopsnakeserver.app.game;

import com.coopsnakeserver.app.IntoBytes;

/**
 * GameOverCause
 *
 * created 05.06.2024
 *
 *
 * @author June L. Gschwantner
 */
public enum GameOverCause implements IntoBytes {
    CollisionBounds((byte) 0),
    CollisionSelf((byte) 1),
    CollisionOther((byte) 2);

    private final byte value;

    private GameOverCause(byte value) {
        this.value = value;
    }

    @Override
    public byte[] intoBytes() {
        return new byte[] { this.value };
    }

}
