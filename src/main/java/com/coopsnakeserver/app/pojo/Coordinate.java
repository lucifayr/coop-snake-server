package com.coopsnakeserver.app.pojo;

import java.util.concurrent.ThreadLocalRandom;

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
        this.x = x;
        this.y = y;
    }

    public static Coordinate randomWeighted(float weight, short boardSize, short clusterSize, short clusterX,
            short clusterY) {
        DevUtils.assertion(weight <= 1 && weight >= 0, "Weight should be between 0 and 1. Received " + weight);
        DevUtils.assertion(boardSize >= clusterX + clusterSize,
                String.format(
                        "Cluster should not be outside the board area. board size = %02d, cluster size = %02d, cluster x = %02d",
                        boardSize, clusterSize, clusterX));
        DevUtils.assertion(boardSize >= clusterY + clusterSize,
                String.format(
                        "Cluster should not be outside the board area. board size = %02d, cluster size = %02d, cluster y = %02d",
                        boardSize, clusterSize, clusterY));

        var w = ThreadLocalRandom.current().nextFloat();
        var inCluster = w < weight;
        if (inCluster) {
            var x = (short) ThreadLocalRandom.current().nextInt(clusterX, clusterX + clusterSize);
            var y = (short) ThreadLocalRandom.current().nextInt(clusterY, clusterY + clusterSize);
            return new Coordinate(x, y);
        } else {
            var x = (short) ThreadLocalRandom.current().nextInt(0, boardSize);
            var y = (short) ThreadLocalRandom.current().nextInt(0, boardSize);
            return new Coordinate(x, y);
        }

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
