package com.coopsnakeserver.app.game.frame;

import java.util.ArrayDeque;
import java.util.Arrays;

import com.coopsnakeserver.app.pojo.Coordinate;
import com.coopsnakeserver.app.pojo.SnakeDirection;

/**
 * PlayerGameFrame
 *
 * created: 01.06.2024
 *
 * @author June L. Gschwantner
 */
public class PlayerGameFrame {
    private ArrayDeque<Coordinate> snakeCoords;
    private SnakeDirection snakeDirection;
    private Coordinate foodCoord;

    public PlayerGameFrame(ArrayDeque<Coordinate> snakeCoords, SnakeDirection snakeDirection, Coordinate foodCoord) {
        this.snakeCoords = snakeCoords;
        this.snakeDirection = snakeDirection;
        this.foodCoord = foodCoord;
    }

    public PlayerGameFrame copy() {
        var snakeCoords = this.snakeCoords.clone();
        var snakeDirection = this.snakeDirection;
        var foodCoord = new Coordinate(this.foodCoord.x(), this.foodCoord.y());

        return new PlayerGameFrame(snakeCoords, snakeDirection, foodCoord);
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
}
