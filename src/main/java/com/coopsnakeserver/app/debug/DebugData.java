package com.coopsnakeserver.app.debug;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import com.coopsnakeserver.app.DevUtils;
import com.coopsnakeserver.app.game.frame.PlayerGameFrame;
import com.coopsnakeserver.app.pojo.Player;

/**
 * DebugData
 *
 * created: 25.05.2024
 *
 * @author June L. Gschwantner
 */
public class DebugData {
    private final Long DEBUG_MESSAGE_IN_LATENCY;

    private final HashMap<Integer, DebugFramePlayer> framePlayers = new HashMap<>();
    private final HashMap<Integer, DebugFrameRecorder> frameRecorders = new HashMap<>();
    private final HashSet<DebugFlag> enabledFlags = new HashSet<>();

    private static DebugData INSTANCE;

    private DebugData(Optional<Long> messageInLatency,
            boolean wrapOnOutOfBounds, boolean playbackFrames, boolean recordFrames) throws IOException {
        if (messageInLatency.isPresent()) {
            this.enabledFlags.add(DebugFlag.MessageInputLatency);
            this.DEBUG_MESSAGE_IN_LATENCY = messageInLatency.get();
        } else {
            this.DEBUG_MESSAGE_IN_LATENCY = null;
        }

        if (wrapOnOutOfBounds) {
            this.enabledFlags.add(DebugFlag.WrapAroundOnOutOfBounds);
        }

        if (recordFrames) {
            this.enabledFlags.add(DebugFlag.RecordFrames);
        }

        if (playbackFrames) {
            this.enabledFlags.add(DebugFlag.PlaybackFrames);
        }

    }

    public static void initFromEnv() {
        var debug = System.getenv("SNAKE_DEBUG");
        var debugEnabled = debug == null || !debug.equals("true");
        if (INSTANCE != null || debugEnabled) {
            return;
        }

        Optional<Long> messageInLatency = Optional.empty();
        var latency = System.getenv("SNAKE_DEBUG_MSG_IN_LATENCY");
        if (latency != null) {
            try {
                messageInLatency = Optional.of(Long.parseUnsignedLong(latency));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        var wrapEnv = System.getenv("SNAKE_DEBUG_WRAP_ON_OUT_OF_BOUNDS");
        var wrapOnOutOfBounds = wrapEnv != null && wrapEnv.equals("true");

        var recordEnv = System.getenv("SNAKE_DEBUG_RECORD_FRAMES");
        var recordFrames = recordEnv != null && recordEnv.equals("true");

        var playbacbEnv = System.getenv("SNAKE_DEBUG_PLAYBACK_FRAMES");
        var playbackFrames = playbacbEnv != null && playbacbEnv.equals("true");

        try {
            INSTANCE = new DebugData(messageInLatency, wrapOnOutOfBounds, playbackFrames,
                    recordFrames);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DebugData instance() {
        return INSTANCE;
    }

    public static boolean instanceHasFlag(DebugFlag flag) {
        if (INSTANCE == null) {
            return false;
        }

        return INSTANCE.enabledFlags.contains(flag);
    }

    public static void recordGameOverIfEnabled(int sessionKey) {
        if (INSTANCE == null || !INSTANCE.enabledFlags.contains(DebugFlag.RecordFrames)) {
            return;
        }

        var recorder = INSTANCE.frameRecorders.get(sessionKey);
        DevUtils.assertion(recorder != null, "record should be called before recodGameOver");
        recorder.recordGameOver();
    }

    public static void recordIfEnabled(int sessionKey, Player player, PlayerGameFrame frame) {
        if (INSTANCE == null || !INSTANCE.enabledFlags.contains(DebugFlag.RecordFrames)) {
            return;
        }

        var recorder = INSTANCE.frameRecorders.get(sessionKey);
        if (recorder == null) {
            recorder = new DebugFrameRecorder(sessionKey);
            INSTANCE.frameRecorders.put(sessionKey, recorder);
        }

        recorder.record(player, frame);
    }

    public PlayerGameFrame playback(int sessionKey, Player player) {
        var playback = INSTANCE.framePlayers.get(sessionKey);
        if (playback == null) {
            playback = new DebugFramePlayer(sessionKey);
            INSTANCE.framePlayers.put(sessionKey, playback);
        }

        return playback.playback(player);
    }

    public Long messageInLatency() {
        return this.DEBUG_MESSAGE_IN_LATENCY;
    }
}
