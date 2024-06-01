package com.coopsnakeserver.app.pojo;

import com.coopsnakeserver.app.BinaryUtils;
import com.coopsnakeserver.app.IntoBytes;

/**
 * FoodCoordinate
 *
 * created: 31.06.2024
 *
 * @author June L. Gschwantner
 */
public class FoodCoordinate implements IntoBytes {
    private Player player;
    private Coordinate coordinate;

    public FoodCoordinate(Player player, Coordinate coordinate) {
        this.player = player;
        this.coordinate = coordinate;
    }

    @Override
    public byte[] intoBytes() {
        return BinaryUtils.concat(this.player.intoBytes(), this.coordinate.intoBytes());
    }

    @Override
    public String toString() {
        return String.format("%s: %s", this.player, this.coordinate);
    }
}
