package com.coopsnakeserver.app.pojo;

import com.coopsnakeserver.app.BinaryUtils;
import com.coopsnakeserver.app.DevUtils;
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
        DevUtils.assertion(x >= 0, String.format("Expected x coordiante to be 0 or greater. Received x = %d", x));
        DevUtils.assertion(y >= 0, String.format("Expected y coordiante to be 0 or greater. Received y = %d", y));

        this.x = x;
        this.y = y;
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
}
