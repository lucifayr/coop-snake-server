package com.coopsnakeserver.app.game.frame;

import java.util.ArrayDeque;
import java.util.Arrays;

import com.coopsnakeserver.app.BinaryUtils;
import com.coopsnakeserver.app.IntoBytes;
import com.coopsnakeserver.app.pojo.Coordinate;
import com.coopsnakeserver.app.pojo.SnakeDirection;

/**
 * PlayerGameFrame
 *
 * created: 01.06.2024
 *
 * @author June L. Gschwantner
 */
public class PlayerGameFrame implements IntoBytes {
    private ArrayDeque<Coordinate> snakeCoords;
    private SnakeDirection snakeDirection;
    private Coordinate foodCoord;

    public PlayerGameFrame(ArrayDeque<Coordinate> snakeCoords, SnakeDirection snakeDirection, Coordinate foodCoord) {
        this.snakeCoords = snakeCoords;
        this.snakeDirection = snakeDirection;
        this.foodCoord = foodCoord;
    }

    public ArrayDeque<Coordinate> getSnakeCoords() {
        return snakeCoords;
    }

    public SnakeDirection getSnakeDirection() {
        return snakeDirection;
    }

    public Coordinate getFoodCoord() {
        return foodCoord;
    }

    @Override
    public String toString() {
        return String.format("Frame:" +
                "\tfood  : coord = %s" +
                "\tsnake : direction = %s, coords = %s",
                foodCoord, snakeDirection.name(), Arrays.toString(snakeCoords.toArray()));
    }

    @Override
    public byte[] intoBytes() {
        var foodCordBytes = foodCoord.intoBytes(); // 4 bytes
        var snakeDirectionByte = snakeDirection.intoBytes(); // 1 byte

        var snakeCoordsArr = new Coordinate[snakeCoords.size()];
        snakeCoords.toArray(snakeCoordsArr);
        var snakeCoordBytes = BinaryUtils.iteratorToBytes(snakeCoordsArr);

        var len = foodCordBytes.length + snakeDirectionByte.length + snakeCoordBytes.length;
        var lenBytes = BinaryUtils.int32ToBytes(len);

        return BinaryUtils.concat(lenBytes, foodCordBytes, snakeDirectionByte, snakeCoordBytes);
    }
}
