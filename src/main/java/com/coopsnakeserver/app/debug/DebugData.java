package com.coopsnakeserver.app.debug;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import com.coopsnakeserver.app.BinaryUtils;
import com.coopsnakeserver.app.game.frame.PlayerGameFrame;
import com.coopsnakeserver.app.pojo.Coordinate;
import com.coopsnakeserver.app.pojo.Player;

/**
 * DebugData
 *
 * created: 25.05.2024
 *
 * @author June L. Gschwantner
 */
public class DebugData {
    private final ByteBuffer DEBUG_PLAYER_COORDS;
    private final Long DEBUG_MESSAGE_IN_LATENCY;

    private final HashMap<Integer, DebugFrameRecorder> frameRecorders = new HashMap<>();
    private final HashSet<DebugFlag> enabledFlags = new HashSet<>();
    private static DebugData INSTANCE;

    private DebugData(Optional<String> debugCoordDataFile, Optional<Long> messageInLatency,
            boolean wrapOnOutOfBounds, boolean recordFrames) throws IOException {
        if (debugCoordDataFile.isPresent()) {
            this.enabledFlags.add(DebugFlag.PlayerCoordinateDataFromFile);
            this.DEBUG_PLAYER_COORDS = ByteBuffer.wrap(this.getClass().getResourceAsStream(debugCoordDataFile.get())
                    .readAllBytes());

            this.DEBUG_PLAYER_COORDS.mark();
        } else {
            this.DEBUG_PLAYER_COORDS = null;
        }

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
    }

    public static void initFromEnv() {
        var debug = System.getenv("SNAKE_DEBUG");
        var debugEnabled = debug == null || !debug.equals("true");
        if (INSTANCE != null || debugEnabled) {
            return;
        }

        Optional<String> debugCoordsFile = Optional.empty();
        var path = System.getenv("SNAKE_DEBUG_COORDS_FILE");
        if (path != null) {
            debugCoordsFile = Optional.of("/debug/" + path);
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

        try {
            INSTANCE = new DebugData(debugCoordsFile, messageInLatency, wrapOnOutOfBounds, recordFrames);
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

    public Long messageInLatency() {
        return this.DEBUG_MESSAGE_IN_LATENCY;
    }

    public Optional<Coordinate[]> nextDebugCoords() {
        if (this.DEBUG_PLAYER_COORDS == null) {
            return Optional.empty();
        }

        var terminator = new byte[] { (byte) 255, (byte) 255 };
        var coords = new ArrayList<Coordinate>();

        while (this.DEBUG_PLAYER_COORDS.remaining() > 0) {
            var xBytes = new byte[2];
            this.DEBUG_PLAYER_COORDS.get(xBytes);
            if (Arrays.equals(xBytes, terminator)) {
                break;
            }

            var yBytes = new byte[2];
            this.DEBUG_PLAYER_COORDS.get(yBytes);
            if (Arrays.equals(yBytes, terminator)) {
                break;
            }

            var x = BinaryUtils.bytesToInt16(xBytes);
            var y = BinaryUtils.bytesToInt16(yBytes);
            coords.add(new Coordinate(x, y));
        }

        if (this.DEBUG_PLAYER_COORDS.remaining() == 0) {
            this.DEBUG_PLAYER_COORDS.reset();
        }

        Coordinate[] arr = new Coordinate[coords.size()];
        return Optional.of(coords.toArray(arr));
    }
}
