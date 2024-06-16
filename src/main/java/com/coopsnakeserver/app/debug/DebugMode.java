package com.coopsnakeserver.app.debug;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;

import com.coopsnakeserver.app.App;
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
public class DebugMode {
    private final Long DEBUG_MESSAGE_IN_LATENCY;

    private final HashMap<Integer, DebugFramePlayer> framePlayers = new HashMap<>();
    private final HashMap<Integer, DebugFrameRecorder> frameRecorders = new HashMap<>();
    private final HashSet<DebugFlag> enabledFlags = new HashSet<>();
    private final Random random;

    private static DebugMode INSTANCE;

    private DebugMode(Optional<Long> messageInLatency, Optional<Long> seed, boolean playbackFrames,
            boolean recordFrames) throws IOException {
        if (messageInLatency.isPresent()) {
            this.enabledFlags.add(DebugFlag.MessageInputLatency);
            this.DEBUG_MESSAGE_IN_LATENCY = messageInLatency.get();
        } else {
            this.DEBUG_MESSAGE_IN_LATENCY = null;
        }

        if (seed.isPresent()) {
            this.enabledFlags.add(DebugFlag.SeedRandom);
            this.random = new Random(seed.get());
        } else {
            this.random = null;
        }

        if (recordFrames) {
            this.enabledFlags.add(DebugFlag.RecordFrames);
        }

        if (playbackFrames) {
            this.enabledFlags.add(DebugFlag.PlaybackFrames);
        }

    }

    public static void initFromEnv() {
        var debug = System.getenv(DebugFlag.Namespace.KEY);
        var debugEnabled = debug == null || !debug.equals("true");
        if (INSTANCE != null || debugEnabled) {
            return;
        }

        Optional<Long> randomSeed = Optional.empty();
        var seedEnv = System.getenv(DebugFlag.SeedRandom.getEnvKey());
        if (seedEnv != null) {
            try {
                randomSeed = Optional.of(Long.parseUnsignedLong(seedEnv));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Optional<Long> messageInLatency = Optional.empty();
        var latency = System.getenv(DebugFlag.MessageInputLatency.getEnvKey());
        if (latency != null) {
            try {
                messageInLatency = Optional.of(Long.parseUnsignedLong(latency));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        var recordEnv = System.getenv(DebugFlag.RecordFrames.getEnvKey());
        var recordFrames = recordEnv != null && recordEnv.equals("true");

        var playbacbEnv = System.getenv(DebugFlag.PlaybackFrames.getEnvKey());
        var playbackFrames = playbacbEnv != null && playbacbEnv.equals("true");

        try {
            INSTANCE = new DebugMode(messageInLatency, randomSeed, playbackFrames,
                    recordFrames);

            App.logger().info("Initialized debug mode from env.\nEnabled flags: "
                    + Arrays.toString(INSTANCE.enabledFlags.toArray()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static DebugMode instance() {
        return INSTANCE;
    }

    public static boolean instanceHasFlag(DebugFlag flag) {
        if (INSTANCE == null) {
            return false;
        }

        return INSTANCE.enabledFlags.contains(flag);
    }

    public float randomFloat(float origin, float bound) {
        if (INSTANCE == null || !INSTANCE.enabledFlags.contains(DebugFlag.SeedRandom)) {
            return -1;
        }

        return INSTANCE.random.nextFloat(origin, bound);
    }

    public int randomInt(int origin, int bound) {
        if (INSTANCE == null || !INSTANCE.enabledFlags.contains(DebugFlag.SeedRandom)) {
            return -1;
        }

        return INSTANCE.random.nextInt(origin, bound);
    }

    public int randomInt(int bound) {
        return randomInt(Integer.MIN_VALUE, bound);
    }

    public int randomInt() {
        return randomInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public float randomFloat(float bound) {
        return randomFloat(Float.MIN_VALUE, bound);
    }

    public float randomFloat() {
        return randomFloat(Float.MIN_VALUE, Float.MAX_VALUE);
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

    public Optional<PlayerGameFrame> playback(int sessionKey, Player player) {
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
