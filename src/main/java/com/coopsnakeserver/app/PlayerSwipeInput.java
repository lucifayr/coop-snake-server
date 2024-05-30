package com.coopsnakeserver.app;

import java.util.Arrays;

import com.coopsnakeserver.app.pojo.SwipeInputKind;

/**
 * PlayerSwipeInput
 *
 * created: 30.05.2024
 *
 * @author June L. Gschwantner
 */
public class PlayerSwipeInput {
    private SwipeInputKind kind;
    private int frameTimestamp;

    private PlayerSwipeInput(SwipeInputKind kind, int frameTimestamp) {
        this.kind = kind;
        this.frameTimestamp = frameTimestamp;
    }

    public static PlayerSwipeInput fromBytes(byte[] bytes) {
        DevUtils.assertion(bytes.length == 5,
                "Expected 5 bytes of data for player swipe input. Received " + bytes.length);

        var kindByte = bytes[0];
        var kind = SwipeInputKind.fromByte(kindByte);
        DevUtils.assertion(kind.isPresent(), String.format("Received invalid byte %d for swipe input kind.", kindByte));

        var frameTimestampBytes = Arrays.copyOfRange(bytes, 1, 5);
        var frameTimestamp = BinaryUtils.bytesToInt32(frameTimestampBytes);

        return new PlayerSwipeInput(kind.get(), frameTimestamp);
    }

    public SwipeInputKind getKind() {
        return kind;
    }

    public int getFrameTimestamp() {
        return frameTimestamp;
    }

    @Override
    public String toString() {
        return String.format("PlayerSwipeInput (kind = %5s, frame = %010d)", this.kind, this.frameTimestamp);
    }
}
