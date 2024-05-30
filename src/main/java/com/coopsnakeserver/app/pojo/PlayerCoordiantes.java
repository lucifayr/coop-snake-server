package com.coopsnakeserver.app.pojo;

import java.util.Arrays;

import com.coopsnakeserver.app.BinaryUtils;
import com.coopsnakeserver.app.DevUtils;
import com.coopsnakeserver.app.IntoBytes;

/**
 * PlayerCoordiantes
 *
 * created: 25.05.2024
 *
 * @author June L. Gschwantner
 */
public class PlayerCoordiantes implements IntoBytes {
    private Player player;
    private int tickN;
    private Coordinate[] coordinates;

    public PlayerCoordiantes(Player player, int tickN, Coordinate[] coordinates) {
        DevUtils.assertion(tickN >= 0, "tick number should always be >= 0. Received " + tickN);

        this.player = player;
        this.tickN = tickN;
        this.coordinates = coordinates;
    }

    @Override
    public byte[] intoBytes() {
        var tickNBytes = BinaryUtils.int32ToBytes(tickN);
        var coordinatesBytes = BinaryUtils.iteratorToBytes(this.coordinates);
        return BinaryUtils.concat(this.player.intoBytes(), tickNBytes, coordinatesBytes);
    }

    @Override
    public String toString() {
        return String.format("%s: %s", this.player, Arrays.toString(this.coordinates));
    }
}
