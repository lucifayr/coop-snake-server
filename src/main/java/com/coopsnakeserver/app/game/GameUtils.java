package com.coopsnakeserver.app.game;

import java.util.ArrayDeque;

import com.coopsnakeserver.app.DevUtils;
import com.coopsnakeserver.app.pojo.Coordinate;
import com.coopsnakeserver.app.pojo.SnakeDirection;

/**
 * GameUtils
 *
 * created: 30.05.2024
 *
 * @author June L. Gschwantner
 */
public class GameUtils {
    public static boolean headOutOfBounds(Coordinate head, short boardSize) {
        var outUp = head.x() < 0;
        var outRight = head.y() >= boardSize;
        var outDown = head.x() >= boardSize;
        var outLeft = head.y() < 0;

        return outUp || outRight || outDown || outLeft;
    }

    public static Coordinate nextHead(Coordinate head, SnakeDirection direction) {
        switch (direction) {
            case Up:
                return new Coordinate(head.x(), (short) (head.y() - 1));
            case Right:
                return new Coordinate((short) (head.x() + 1), head.y());
            case Down:
                return new Coordinate(head.x(), (short) (head.y() + 1));
            case Left:
                return new Coordinate((short) (head.x() - 1), head.y());
            default:
                throw new RuntimeException("Invalid direction " + direction);
        }
    }

    public static ArrayDeque<Coordinate> initialCoords(short snakeSize, short boardSize, short yOffset,
            boolean reverse) {
        DevUtils.assertion(boardSize / 2 > snakeSize, "Board is too small to initialize a snake.");
        var coords = new ArrayDeque<Coordinate>(snakeSize);

        var x0 = boardSize / 2;
        var y0 = boardSize / 2 + yOffset;

        var c0 = new Coordinate((short) x0, (short) y0);
        coords.addLast(c0);

        for (var i = 1; i < snakeSize; i++) {
            var x = 0;
            if (reverse) {
                x = x0 - i;
            } else {
                x = x0 - i;
            }

            var c = new Coordinate((short) x, (short) y0);
            coords.addLast(c);
        }

        return coords;
    };
}
