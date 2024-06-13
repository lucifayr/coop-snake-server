package com.coopsnakeserver.app.pojo;

import java.util.Arrays;

import com.coopsnakeserver.app.BinaryUtils;
import com.coopsnakeserver.app.DevUtils;
import com.coopsnakeserver.app.IntoBytes;
import com.coopsnakeserver.app.game.GameUtils;

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

        var w = GameUtils.randomFloat();
        var inCluster = w < weight;
        if (inCluster) {
            var x = (short) GameUtils.randomInt(clusterX, clusterX + clusterSize);
            var y = (short) GameUtils.randomInt(clusterY, clusterY + clusterSize);
            return new Coordinate(x, y);
        } else {
            var x = (short) GameUtils.randomInt(0, boardSize);
            var y = (short) GameUtils.randomInt(0, boardSize);
            return new Coordinate(x, y);
        }
    }

    public static Coordinate fromBytes(byte[] bytes) {
        // TODO: assert
        DevUtils.assertion(bytes.length == 4,
                "Expected 4 byte long array for coordinate. Received bytes " + Arrays.toString(bytes));

        var xBytes = Arrays.copyOfRange(bytes, 0, 2);
        var yBytes = Arrays.copyOfRange(bytes, 2, 4);
        var x = BinaryUtils.bytesToInt16(xBytes);
        var y = BinaryUtils.bytesToInt16(yBytes);

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
