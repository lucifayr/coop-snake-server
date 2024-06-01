package com.coopsnakeserver.app.pojo;

import java.util.concurrent.ThreadLocalRandom;

import com.coopsnakeserver.app.BinaryUtils;
import com.coopsnakeserver.app.IntoBytes;

/**
 * Coordiantes
 *
 * created: 25.05.2024
 *
 * @author June L. Gschwantner
 */
public class Coordinate implements IntoBytes {
    private short x;
    private short y;

    public Coordinate(short x, short y) {
        this.x = x;
        this.y = y;
    }

    public static Coordinate random(short boardSize) {
        var x = (short) ThreadLocalRandom.current().nextInt(0, boardSize);
        var y = (short) ThreadLocalRandom.current().nextInt(0, boardSize);
        return new Coordinate(x, y);
    }

    @Override
    public byte[] intoBytes() {
        var xBytes = BinaryUtils.int16ToBytes(this.x);
        var yBytes = BinaryUtils.int16ToBytes(this.y);
        return BinaryUtils.concat(xBytes, yBytes);
    }

    @Override
    public String toString() {
        return String.format("X: %02d, Y: %02d", this.x, this.y);
    }

    public short x() {
        return x;
    }

    public short y() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }

        if (obj instanceof Coordinate c) {
            return c.x == this.x && c.y == this.y;
        }

        return false;
    }
}
