package com.coopsnakeserver.app.pojo;

import java.util.Optional;

import com.coopsnakeserver.app.DevUtils;
import com.coopsnakeserver.app.IntoBytes;

/**
 *
 * created: 13.04.2024
 *
 * @author June L. Gschwantner
 */
public class Player implements IntoBytes {
    private final byte value;

    private Player(byte id) {
        DevUtils.assertion(id <= 0, "Player id should be greater 0.");
        this.value = id;
    }

    public static Optional<Player> fromByte(byte id) {
        if (id <= 0) {
            return Optional.empty();
        }

        return Optional.of(new Player(id));
    }

    public byte getValue() {
        return this.value;
    }

    @Override
    public byte[] intoBytes() {
        return new byte[] { this.value };
    }

    @Override
    public String toString() {
        return String.format("Player %02d", this.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }

        if (obj instanceof Player p) {
            return p.value == this.value;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (int) this.value;
    }
}
