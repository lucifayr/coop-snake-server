package com.coopsnakeserver.app.game;

import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayDeque;
import java.util.List;

import com.coopsnakeserver.app.DevUtils;
import com.coopsnakeserver.app.debug.DebugMode;
import com.coopsnakeserver.app.debug.DebugFlag;
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
    public static float randomFloat() {
        return GameUtils.randomFloat(Float.MIN_VALUE, Float.MAX_VALUE);
    }

    public static float randomFloat(float bound) {
        return GameUtils.randomFloat(Float.MIN_VALUE, Float.MAX_VALUE);
    }

    public static float randomFloat(float origin, float bound) {
        if (DebugMode.instanceHasFlag(DebugFlag.SeedRandom)) {
            return DebugMode.instance().randomFloat(origin, bound);
        }

        return ThreadLocalRandom.current().nextFloat(origin, bound);
    }

    public static int randomInt() {
        return GameUtils.randomInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static int randomInt(int bound) {
        return GameUtils.randomInt(Integer.MIN_VALUE, bound);
    }

    public static int randomInt(int origin, int bound) {
        if (DebugMode.instanceHasFlag(DebugFlag.SeedRandom)) {
            return DebugMode.instance().randomInt(origin, bound);
        }

        return ThreadLocalRandom.current().nextInt(origin, bound);
    }

    public static boolean headOutOfBounds(Coordinate head, short boardSize) {
        var outUp = head.y() < 0;
        var outRight = head.x() >= boardSize;
        var outDown = head.y() >= boardSize;
        var outLeft = head.x() < 0;

        return outUp || outRight || outDown || outLeft;
    }

    public static boolean headSelfCollision(Coordinate head, List<Coordinate> bodyAndHead) {
        for (var i = 1; i < bodyAndHead.size(); i++) {
            var segment = bodyAndHead.get(i);
            if (head.equals(segment)) {
                return true;
            }
        }

        return false;
    }

    public static boolean headOtherCollision(Coordinate head, List<Coordinate> otherBodies) {
        for (var i = 0; i < otherBodies.size(); i++) {
            var segment = otherBodies.get(i);
            if (head.equals(segment)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Find a non-occupied coordinate to place new food on. Prefers placing food
     * far away from the player/snake head.
     *
     * @param occupied  Coordinates taken up by snake segments.
     * @param boardSize The size of the game board.
     * @return A non-occupied coordinate that food can be placed on.
     */
    public static Coordinate findFoodCoord(Coordinate snakeHead, List<Coordinate> occupied, short boardSize) {
        var clusterCount = 2;
        var clusterSize = (short) (boardSize / clusterCount);
        var maxIdx = boardSize / clusterSize - 1;

        var clusterIdxX = snakeHead.x() / clusterSize;
        var clusterIdxY = snakeHead.y() / clusterSize;
        var clusterX = (short) ((maxIdx - clusterIdxX) * clusterSize);
        var clusterY = (short) ((maxIdx - clusterIdxY) * clusterSize);

        var coord = Coordinate.randomWeighted(0.8f, boardSize, clusterSize, clusterX, clusterY);
        while (occupied.contains(coord)) {
            coord = Coordinate.randomWeighted(0.8f, boardSize, clusterSize, clusterX, clusterY);
        }

        return coord;
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

    public static Coordinate wrappedHead(Coordinate head, short boardSize) {
        var x = head.x();
        if (x < 0) {
            x = (short) (boardSize - 1);
        }

        if (x >= boardSize) {
            x = 0;
        }

        var y = head.y();
        if (y < 0) {
            y = (short) (boardSize - 1);
        }

        if (y >= boardSize) {
            y = 0;
        }

        return new Coordinate(x, y);
    }

    public static ArrayDeque<Coordinate> initialCoords(short snakeSize, short boardSize, byte playerNumber) {
        DevUtils.assertion(boardSize >= snakeSize, String.format(
                "Board is too small to initialize a snake. Board size %d. Snake size %d", boardSize, snakeSize));
        DevUtils.assertion(playerNumber < boardSize - 1, String.format(
                "Board is too small to initialize a snake. Board size %d. Player number %d", boardSize, playerNumber));

        var coords = new ArrayDeque<Coordinate>(snakeSize);

        var goLeft = playerNumber % 2 == 0;

        var x0Offset = Math.max(snakeSize - boardSize / 2, 0);
        var x0 = boardSize / 2 - x0Offset;
        if (goLeft) {
            x0 = boardSize / 2 + x0Offset;
        }

        var y0Offset = (playerNumber + 1) / 2;
        if (goLeft) {
            y0Offset *= -1;
        }
        var y0 = boardSize / 2 + y0Offset;

        var c0 = new Coordinate((short) x0, (short) y0);
        coords.addFirst(c0);

        for (var i = 1; i < snakeSize; i++) {
            var x = x0 + i;
            if (goLeft) {
                x = x0 - i;
            }

            var c = new Coordinate((short) x, (short) y0);
            coords.addFirst(c);
        }

        return coords;
    };
}
