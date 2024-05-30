package com.coopsnakeserver.app.socket;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Optional;

import com.coopsnakeserver.app.DevUtils;
import com.coopsnakeserver.app.PlayerSwipeInput;
import com.coopsnakeserver.app.debug.DebugData;
import com.coopsnakeserver.app.debug.DebugFlag;
import com.coopsnakeserver.app.pojo.Coordinate;
import com.coopsnakeserver.app.pojo.Player;
import com.coopsnakeserver.app.pojo.PlayerCoordiantes;
import com.coopsnakeserver.app.pojo.SnakeDirection;

// /**
//  *
//  * created: 30.05.2024
//  *
//  * @author June L. Gschwantner
//  */
public class GameLoop {
    private GameSession session;

    private Player player;
    private short initialSankeSize;

    private SnakeDirection direction = SnakeDirection.Right;
    private ArrayDeque<Coordinate> coords;

    private Optional<PlayerSwipeInput> input = Optional.empty();

    public GameLoop(GameSession parent, Player player, short initialSankeSize)
            throws IOException {
        DevUtils.assertion(initialSankeSize > 0, "Snake has have more than 0 segemnts. Received " + initialSankeSize);
        this.session = parent;
        this.player = player;
        this.initialSankeSize = initialSankeSize;

        this.coords = initialCoords(this.initialSankeSize);
    }

    /**
     * Calculate the next position for the snake. Returns {@code}Empty{@code} if
     * a game over state has been triggered.
     */
    public Optional<PlayerCoordiantes> tick(int tickN) {
        if (DebugData.instanceHasFlag(DebugFlag.PlayerCoordinateDataFromFile)) {
            var coords = DebugData.instance().nextDebugCoords().orElseGet(() -> new Coordinate[0]);
            return Optional.of(new PlayerCoordiantes(player, tickN, coords));
        }

        if (this.input.isPresent()) {
            // TODO: handle tickN
            this.direction = SnakeDirection.fromSwipeInput(this.input.get().getKind());

            this.input = Optional.empty();
        }

        var snakeHead = this.coords.peekFirst();
        var snakeHeadNext = nextHead(snakeHead, this.direction);
        var isOutOfBounds = headOutOfBounds(snakeHeadNext, this.session.getBoardSize());

        if (isOutOfBounds) {
            return Optional.empty();
        }

        this.coords.addFirst(snakeHeadNext);
        this.coords.removeLast();

        var coords = new Coordinate[this.coords.size()];
        this.coords.toArray(coords);
        return Optional.of(new PlayerCoordiantes(player, tickN, coords));
    }

    /**
     * Set player input. Will be processed the next time {@code}tick{@code} is
     * called. Calling this function multiple times before a tick will only
     * result in only the last input being process.
     *
     * @param input The input to process in the next tick. Set to
     *              {@code}Empty{@code} to explicitly remove any input from
     *              processing.
     */
    public void setInput(Optional<PlayerSwipeInput> input) {
        this.input = input;
    }

    private static boolean headOutOfBounds(Coordinate head, short boardSize) {
        var outUp = head.x() < 0;
        var outRight = head.y() >= boardSize;
        var outDown = head.x() >= boardSize;
        var outLeft = head.y() < 0;

        return outUp || outRight || outDown || outLeft;
    }

    private static Coordinate nextHead(Coordinate head, SnakeDirection direction) {
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

    private ArrayDeque<Coordinate> initialCoords(short snakeSize) {
        DevUtils.assertion(this.session.getBoardSize() / 2 > snakeSize, "Board is too small to initialize a snake.");
        var coords = new ArrayDeque<Coordinate>(snakeSize);

        var x0 = this.session.getBoardSize() / 2;
        var y0 = this.session.getBoardSize() / 2;

        var c0 = new Coordinate((short) x0, (short) y0);
        coords.addLast(c0);

        for (var i = 1; i < snakeSize; i++) {
            var x = x0 + i;
            var c = new Coordinate((short) x, (short) y0);
            coords.addLast(c);
        }

        return coords;
    };
}
