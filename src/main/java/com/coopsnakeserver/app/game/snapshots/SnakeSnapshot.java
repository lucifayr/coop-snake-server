package com.coopsnakeserver.app.game.snapshots;

import java.util.ArrayDeque;
import java.util.Arrays;

import com.coopsnakeserver.app.pojo.Coordinate;
import com.coopsnakeserver.app.pojo.SnakeDirection;

/**
 * SnakeSnapshot
 *
 * A snapshot of snake data at a specific game tick. Intended to be used to
 * rewind a snake to its state during a particular tick. Rewinding can be useful
 * to account for things like input latency between the client and server.
 *
 * created: 01.06.2024
 *
 * @author June L. Gschwantner
 */
public class SnakeSnapshot {
    private ArrayDeque<Coordinate> coords;
    private SnakeDirection direction;

    public SnakeSnapshot(ArrayDeque<Coordinate> coords, SnakeDirection direction) {
        this.coords = coords;
        this.direction = direction;
    }

    public ArrayDeque<Coordinate> getCoords() {
        return coords;
    }

    public SnakeDirection getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return String.format("Snapshot (direction = %s, coords = %s)", direction.name(),
                Arrays.toString(coords.toArray()));
    }
}
