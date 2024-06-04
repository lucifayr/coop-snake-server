package com.coopsnakeserver.app.game.snapshots;

import java.util.ArrayDeque;
import java.util.Optional;

import com.coopsnakeserver.app.pojo.Coordinate;
import com.coopsnakeserver.app.pojo.SnakeDirection;

/**
 * A handler that keeps a list of snapshots (i.e. A history) of snake data per
 * tick. The history is {@code}maxSnapshots{@code} long, so any data stored more
 * than {@code}maxSnapshots{@code} ticks ago is removed from the snapshot list.
 *
 * <br>
 * <br>
 *
 * created: 01.06.2024
 *
 * @author June L. Gschwanter
 */
public class SnakeSnapshotHandler {
    // TODO: update this entire API, it is not good
    private int maxSnapshots;
    private ArrayDeque<SnakeSnapshot> snapshots;

    public SnakeSnapshotHandler(int maxSnapshots) {
        this.maxSnapshots = maxSnapshots;
        this.snapshots = new ArrayDeque<>(maxSnapshots);
    }

    /**
     * Save a snapshot of the state of a snake for later use. The data passed to
     * this function will be treated as the most recent snapshot (i.e. the
     * snapshot that is returned by calling {@code}rewind(ticks){@code} with
     * {@code}ticks = 1{@code}).
     *
     * @param coords    The coordinate data of the snapshot.
     * @param direction The direction data of the snapshot.
     */
    public void takeSnapshot(ArrayDeque<Coordinate> coords, SnakeDirection direction) {
        this.snapshots.addFirst(new SnakeSnapshot(coords, direction));

        if (this.snapshots.size() > this.maxSnapshots) {
            this.snapshots.removeLast();
        }
    }

    /**
     * Rewind back to the snapshot that was taken {@code}ticks{@code} ago.
     * Mutates the history by removing all snapshots which were taken after the
     * returned snapshot.
     *
     * @param ticks The number of snapshots to go back. Starts at 1 for the most
     *              recent snapshot.
     *
     * @return The snapshot that is {@code}n steps (n = ticks){@code} in the
     *         past. {@code}Empty{@code} is returned if {@code}ticks > number of
     *         save snapshots{@code} or if {@code}ticks < 1{@code}. If
     *         {@code}Empty{@code} is returned the history is not mutated.
     */
    public Optional<SnakeSnapshot> rewind(int ticks) {
        if (ticks <= 0 || ticks > this.snapshots.size()) {
            return Optional.empty();
        }

        var snapshot = this.snapshots.removeFirst();
        for (var i = 1; i < ticks; i++) {
            snapshot = this.snapshots.removeFirst();
        }

        return Optional.of(snapshot);
    }
}
