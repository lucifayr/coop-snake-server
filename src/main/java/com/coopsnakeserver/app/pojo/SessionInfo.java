package com.coopsnakeserver.app.pojo;

import com.coopsnakeserver.app.BinaryUtils;
import com.coopsnakeserver.app.IntoBytes;

/**
 *
 * created: 31.05.2024
 *
 * @author June L. Gschwantner
 */
public class SessionInfo implements IntoBytes {
    private SessionInfoType type;
    private byte[] value;

    public SessionInfo(SessionInfoType infoType, byte[] value) {
        this.type = infoType;
        this.value = value;
    }

    @Override
    public byte[] intoBytes() {
        return BinaryUtils.concat(this.type.intoBytes(), this.value);
    }

    @Override
    public String toString() {
        return String.format("Session info (type = %s, value = %d) ", this.type, this.value);
    }
}
