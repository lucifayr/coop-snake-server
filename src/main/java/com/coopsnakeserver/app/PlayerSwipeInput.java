package com.coopsnakeserver.app;

import java.util.Arrays;
import java.util.Optional;

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

    /**
     * Parse a byte array into a player swipe input. Bytes but the formatted
     * correctly.
     *
     * @param bytes The bytes to parse into an input.
     * @return {@code}Empty{@code} if the bytes are malformed otherwise parsed
     *         input.
     */
    public static Optional<PlayerSwipeInput> fromBytes(byte[] bytes) {
        var len = 5 + PlayerToken.PLAYER_TOKEN_BYTE_WIDTH;
        if (bytes.length != len) {
            App.logger().warn(
                    String.format("Expected %d bytes of data for player swipe input. Received bytes %s: %s", len,
                            bytes.length, Arrays.toString(bytes)));

            return Optional.empty();
        }

        var tokenBytes = Arrays.copyOfRange(bytes, 0, PlayerToken.PLAYER_TOKEN_BYTE_WIDTH);
        var token = PlayerToken.fromBytes(tokenBytes);

        var kindByte = bytes[PlayerToken.PLAYER_TOKEN_BYTE_WIDTH];
        var kind = SwipeInputKind.fromByte(kindByte);
        if (kind.isEmpty()) {
            App.logger().warn(String.format("Received invalid byte %d for swipe input kind.", kindByte));
            return Optional.empty();
        }

        var tickNBytesStartIdx = PlayerToken.PLAYER_TOKEN_BYTE_WIDTH + 1;
        var tickNBytesEndIdx = tickNBytesStartIdx + 4;
        var tickNBytes = Arrays.copyOfRange(bytes, tickNBytesStartIdx, tickNBytesEndIdx);
        var tickN = BinaryUtils.bytesToInt32(tickNBytes);

        var input = new PlayerSwipeInput(token, kind.get(), tickN);
        return Optional.of(input);
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
        return String.format("PlayerSwipeInput (kind = %5s, frame = %010d, token = %s)", this.kind, this.tickN,
                this.token);
    }
}
