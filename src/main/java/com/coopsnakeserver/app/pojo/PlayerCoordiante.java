package com.coopsnakeserver.app.pojo;

import com.coopsnakeserver.app.BinaryUtils;

/**
 *
 * created: 13.04.2024
 *
 * @author June L. Gschwantner
 */
public class PlayerCoordiante {
    private Player player;
    private int x;
    private int y;

    public PlayerCoordiante(Player player, int x, int y) {
        this.player = player;
        this.x = x;
        this.y = y;
    }

    public byte[] intoBytes() {
        var playerBytes = new byte[] { this.player.id() };
        var xBytes = BinaryUtils.int32ToBytes(this.x);
        var yBytes = BinaryUtils.int32ToBytes(this.y);

        return BinaryUtils.concat(playerBytes, xBytes, yBytes);
    }

    @Override
    public String toString() {
        return String.format("%s (X: %d, Y: %d)", this.player, this.x, this.y);
    }
}
