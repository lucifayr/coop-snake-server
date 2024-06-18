package com.coopsnakeserver.app;

import java.util.Arrays;
import java.util.Optional;

/**
 * Content of a response message from a player after a game over. Should either
 * led to a "confirm" or a "deny" restart.
 *
 * <br>
 * <br>
 *
 * created: 16.06.2024
 *
 * @author June L. Gschwantner
 */
public class PlayerRestartAction {
    private PlayerToken token;

    private PlayerRestartAction(PlayerToken token) {
        this.token = token;
    }

    public static Optional<PlayerRestartAction> fromBytes(byte[] bytes) {
        var len = PlayerToken.PLAYER_TOKEN_BYTE_WIDTH;
        if (bytes.length != len) {
            App.logger().warn(
                    String.format("Expected %d bytes of data for restart confirm. Received bytes %s: %s", len,
                            bytes.length, Arrays.toString(bytes)));

            return Optional.empty();
        }

        var tokenBytes = Arrays.copyOfRange(bytes, 0, PlayerToken.PLAYER_TOKEN_BYTE_WIDTH);
        var tokenOpt = PlayerToken.fromBytes(tokenBytes);
        if (tokenOpt.isEmpty()) {
            App.logger().warn(String.format("Received invalid bytes %s for player token. Expected 4 bytes",
                    Arrays.toString(tokenBytes)));
            return Optional.empty();
        }

        var token = tokenOpt.get();
        return Optional.of(new PlayerRestartAction(token));
    }

    public PlayerToken getPlayerToken() {
        return token;
    }

    @Override
    public String toString() {
        return String.format("PlayerRestartAction (token = %s)", this.token);
    }
}
