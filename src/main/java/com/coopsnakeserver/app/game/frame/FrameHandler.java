package com.coopsnakeserver.app.game.frame;

import java.util.ArrayDeque;
import java.util.Optional;

import com.coopsnakeserver.app.DevUtils;

/**
 * FrameHandler
 *
 * created: 01.06.2024
 *
 * @author June L. Gschwanter
 */
public class FrameHandler {
    private int maxHistoryLength;
    private ArrayDeque<PlayerGameFrame> frames;

    public FrameHandler(int maxHistoryLength) {
        DevUtils.assertion(maxHistoryLength > 0, "Frame history lenght can't be 0.");

        DevUtils.assertion(maxHistoryLength <= 64,
                "FrameHandler is not designed to handle large amounts of frame history data. If a large history is needed update you should reconsider the design.");

        this.maxHistoryLength = maxHistoryLength;
        this.frames = new ArrayDeque<>(maxHistoryLength);
    }

    public PlayerGameFrame peek() {
        var frame = this.frames.peekFirst();
        DevUtils.assertion(frame != null, "First frame in history is null. Did you call peek() before store()?");
        return frame.copy();
    }

    public void store(PlayerGameFrame frame) {
        if (this.frames.size() >= this.maxHistoryLength) {
            this.frames.removeLast();
        }
        this.frames.addFirst(frame);
    }

    /**
     * Rewind back to the frame that was taken {@code}ticks{@code} ago.
     * Mutates the history by removing all frames which were taken after the
     * returned frame.
     *
     * @param ticks The number of frames to go back. Starts at 1 for the most
     *              recent frame.
     *
     * @return The frame that is {@code}n steps (n = ticks){@code} in the
     *         past. {@code}Empty{@code} is returned if {@code}ticks > number of
     *         save frame{@code} or if {@code}ticks < 1{@code}. If
     *         {@code}Empty{@code} is returned the history is not mutated.
     */
    public Optional<PlayerGameFrame> rewind(int ticks) {
        if (ticks <= 0 || ticks > this.frames.size()) {
            return Optional.empty();
        }

        var frame = this.frames.removeFirst();
        for (var i = 1; i < ticks; i++) {
            frame = this.frames.removeFirst();
        }

        return Optional.of(frame);
    }
}
