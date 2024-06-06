package com.coopsnakeserver.app;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;

import com.coopsnakeserver.app.pojo.SessionInfoType;
import com.coopsnakeserver.app.pojo.GameMessageType;
import com.coopsnakeserver.app.pojo.Player;
import com.coopsnakeserver.app.pojo.SessionInfo;

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

    public GameBinaryMessage intoMsg() {
        var type = SessionInfoType.PlayerToken;
        var info = new SessionInfo(type, BinaryUtils.int32ToBytes(this.token));
        return new GameBinaryMessage(GameMessageType.SessionInfo, info.intoBytes());
    }

    public Optional<Player> tokenOwner(Map<PlayerToken, Player> tokens) {
        var player = tokens.get(this);
        if (player == null) {
            return Optional.empty();
        }

        return Optional.of(player);
    }

    @Override
    public byte[] intoBytes() {
        return BinaryUtils.int32ToBytes(this.token);
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }

        if (obj instanceof PlayerToken pt) {
            return pt.token == this.token;
        }

        return false;
    }
}
