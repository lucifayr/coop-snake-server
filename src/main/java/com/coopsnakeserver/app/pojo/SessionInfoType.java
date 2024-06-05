package com.coopsnakeserver.app.pojo;

import com.coopsnakeserver.app.IntoBytes;

/**
 *
 * created: 31.05.2024
 *
 * @author June L. Gschwantner
 */
public enum SessionInfoType implements IntoBytes {
    SessionKey((byte) 0),
    PlayerToken((byte) 1),
    BoardSize((byte) 2),
    GameOver((byte) 3);

    private final byte value;

    private SessionInfoType(byte value) {
        this.value = value;
    }

    @Override
    public byte[] intoBytes() {
        return new byte[] { this.value };
    }

    @Override
    public String toString() {
        return String.format("%s (byte value = %d) ", this.name(), this.value);
    }
}
