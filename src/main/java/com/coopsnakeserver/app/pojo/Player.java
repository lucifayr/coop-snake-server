package com.coopsnakeserver.app.pojo;

import com.coopsnakeserver.app.IntoBytes;

/**
 *
 * created: 13.04.2024
 *
 * @author June L. Gschwantner
 */
public enum Player implements IntoBytes {
    Player1((byte) 1),
    Player2((byte) 2);

    private final byte id;

    private Player(byte id) {
        this.id = id;
    }

    @Override
    public byte[] intoBytes() {
        return new byte[] { this.id };
    }

    @Override
    public String toString() {
        switch (this.id) {
            case 1:
                return "Player 1";
            case 2:
                return "Player 2";
            default:
                return "Invalid Player";
        }
    }
}
