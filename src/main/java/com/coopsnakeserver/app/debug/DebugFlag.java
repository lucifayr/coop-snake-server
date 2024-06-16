package com.coopsnakeserver.app.debug;

/**
 * DebugFlags
 *
 * created: 25.05.2024
 *
 * @author June L. Gschwantner
 */
public enum DebugFlag {
    PlaybackFrames(Namespace.KEY + "_PLAYBACK_FRAMES"),
    RecordFrames(Namespace.KEY + "_RECORD_FRAMES"),
    MessageInputLatency(Namespace.KEY + "_MESSAGE_INPUT_LATENCY"),
    SeedRandom(Namespace.KEY + "_SEED_RANDOM");

    private final String envKey;

    private DebugFlag(String envKey) {
        this.envKey = envKey;
    }

    public String getEnvKey() {
        return this.envKey;
    }

    public class Namespace {
        public static String KEY = "SNAKE_DEBUG";
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", this.name(), this.envKey);
    }
}
