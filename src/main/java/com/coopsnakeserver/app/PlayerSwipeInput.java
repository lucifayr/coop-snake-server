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
    private int tickN;

    private PlayerSwipeInput(SwipeInputKind kind, int tickN) {
        this.kind = kind;
        this.tickN = tickN;
    }

    public static PlayerSwipeInput fromBytes(byte[] bytes) {
        DevUtils.assertion(bytes.length == 5,
                "Expected 5 bytes of data for player swipe input. Received " + bytes.length);

        var kindByte = bytes[0];
        var kind = SwipeInputKind.fromByte(kindByte);
        DevUtils.assertion(kind.isPresent(), String.format("Received invalid byte %d for swipe input kind.", kindByte));

        var tickNBytes = Arrays.copyOfRange(bytes, 1, 5);
        var tickN = BinaryUtils.bytesToInt32(tickNBytes);

        return new PlayerSwipeInput(kind.get(), tickN);
    }

    public SwipeInputKind getKind() {
        return kind;
    }

    public int getTickN() {
        return tickN;
    }

    @Override
    public String toString() {
        return String.format("PlayerSwipeInput (kind = %5s, frame = %010d)", this.kind, this.tickN);
    }
}
