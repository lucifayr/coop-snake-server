package com.coopsnakeserver.app.pojo;

/**
 *
 * created: 30.05.2024
 *
 * @author June L. Gschwantner
 */
public enum PlainTextMsgPrefix {
    PlayerToken("player-token"),
    SessionKey("session-key");

    private final String key;

    private PlainTextMsgPrefix(String key) {
        this.key = key;
    }

    public String format(String value) {
        return this.key + ":" + value;
    }
}
