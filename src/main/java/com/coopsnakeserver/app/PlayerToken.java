package com.coopsnakeserver.app;

import java.security.SecureRandom;
import java.util.List;
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

    public static PlayerToken genRandom(List<PlayerToken> others) {
        PlayerToken token;
        do {
            token = new PlayerToken(new SecureRandom().nextInt(0, Integer.MAX_VALUE));
        } while (others.contains(token));

        return token;
    }

    public GameBinaryMessage intoMsg() {
        var type = SessionInfoType.PlayerToken;
        var info = new SessionInfo(type, BinaryUtils.int32ToBytes(this.token));
        return new GameBinaryMessage(GameMessageType.SessionInfo, info.intoBytes());
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

    @Override
    public int hashCode() {
        return this.token;
    }
}
