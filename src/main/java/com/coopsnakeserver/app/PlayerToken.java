package com.coopsnakeserver.app;

import java.security.SecureRandom;
import java.util.Optional;

import org.springframework.web.socket.TextMessage;

import com.coopsnakeserver.app.pojo.PlainTextMsgPrefix;
import com.coopsnakeserver.app.pojo.Player;

/**
 * PlayerInputToken
 *
 * created: 30.05.2024
 *
 * @author June L. Gschwantner
 */
public class PlayerToken implements IntoBytes {
    public static int PLAYER_TOKEN_BYTE_WIDTH = 4;

    private int token;

    private PlayerToken(int token) {
        this.token = token;
    }

    public static PlayerToken fromBytes(byte[] bytes) {
        var token = BinaryUtils.bytesToInt32(bytes);
        return new PlayerToken(token);
    }

    public static PlayerToken genRandom(Optional<PlayerToken> other) {
        var token = new SecureRandom().nextInt(0, Integer.MAX_VALUE);
        while (other.isPresent() && other.get().token == token) {
            token = new SecureRandom().nextInt(0, Integer.MAX_VALUE);
        }

        return new PlayerToken(token);
    }

    public TextMessage intoMsg() {
        var tokenString = String.format("%010d", this.token);
        var msg = PlainTextMsgPrefix.PlayerToken.format(tokenString);
        return new TextMessage(msg);
    }

    public Optional<Player> tokenOwner(PlayerToken p1Token, PlayerToken p2Token) {
        DevUtils.assertion(p1Token.token != p2Token.token, "PlayerTokens shouldn't be the same.");
        if (this.token == p1Token.token) {
            return Optional.of(Player.Player1);
        }

        if (this.token == p2Token.token) {
            return Optional.of(Player.Player2);
        }

        return Optional.empty();
    }

    @Override
    public byte[] intoBytes() {
        return BinaryUtils.int32ToBytes(this.token);
    }
}