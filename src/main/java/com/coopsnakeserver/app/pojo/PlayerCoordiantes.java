package com.coopsnakeserver.app.pojo;

import java.util.Arrays;

import com.coopsnakeserver.app.BinaryUtils;
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
    private Coordinate[] coordinates;

    public PlayerCoordiantes(Player player, Coordinate[] coordinates) {
        this.player = player;
        this.coordinates = coordinates;
    }

    public byte[] intoBytes() {
        var playerBytes = new byte[] { this.player.id() };
        var coordinatesBytes = BinaryUtils.iteratorToBytes(this.coordinates);

        return BinaryUtils.concat(playerBytes, coordinatesBytes);
    }

    @Override
    public String toString() {
        return String.format("%s: %s", this.player, Arrays.toString(this.coordinates));
    }
}
