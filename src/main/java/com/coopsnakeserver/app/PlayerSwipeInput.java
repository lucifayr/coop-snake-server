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
    private PlayerToken token;
    private SwipeInputKind kind;
    private int tickN;

    private PlayerSwipeInput(PlayerToken token, SwipeInputKind kind, int tickN) {
        this.token = token;
        this.kind = kind;
        this.tickN = tickN;
    }

    public static PlayerSwipeInput fromBytes(byte[] bytes) {
        var len = 5 + PlayerToken.PLAYER_TOKEN_BYTE_WIDTH;
        DevUtils.assertion(bytes.length == len,
                String.format("Expected %d bytes of data for player swipe input. Received bytes %s: %s", len,
                        bytes.length, Arrays.toString(bytes)));

        var tokenBytes = Arrays.copyOfRange(bytes, 0, PlayerToken.PLAYER_TOKEN_BYTE_WIDTH);
        var token = PlayerToken.fromBytes(tokenBytes);

        var kindByte = bytes[PlayerToken.PLAYER_TOKEN_BYTE_WIDTH];
        var kind = SwipeInputKind.fromByte(kindByte);
        DevUtils.assertion(kind.isPresent(), String.format("Received invalid byte %d for swipe input kind.", kindByte));

        var tickNBytesStartIdx = PlayerToken.PLAYER_TOKEN_BYTE_WIDTH + 1;
        var tickNBytesEndIdx = tickNBytesStartIdx + 4;
        var tickNBytes = Arrays.copyOfRange(bytes, tickNBytesStartIdx, tickNBytesEndIdx);
        var tickN = BinaryUtils.bytesToInt32(tickNBytes);

        return new PlayerSwipeInput(token, kind.get(), tickN);
    }

    public PlayerToken getPlayerToken() {
        return token;
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
