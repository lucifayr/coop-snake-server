package com.coopsnakeserver.app.debug;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import com.coopsnakeserver.app.BinaryUtils;
import com.coopsnakeserver.app.pojo.Coordinate;

/**
 * DebugData
 *
 * created: 25.05.2024
 *
 * @author June L. Gschwantner
 */
public class DebugData {
    private final ByteBuffer DEBUG_PLAYER_COORDS;

    private final HashSet<DebugFlag> enabledFlags = new HashSet<>();

    private static DebugData INSTANCE;

    private DebugData(Optional<String> debugCoordDataFile) throws IOException {
        if (debugCoordDataFile.isPresent()) {
            enabledFlags.add(DebugFlag.PlayerCoordinateDataFromFile);
            this.DEBUG_PLAYER_COORDS = ByteBuffer.wrap(this.getClass().getResourceAsStream(debugCoordDataFile.get())
                    .readAllBytes());

            this.DEBUG_PLAYER_COORDS.mark();

        } else {
            this.DEBUG_PLAYER_COORDS = null;
        }
    }

    public static void init(Optional<String> debugPosDataFile) throws IOException {
        if (INSTANCE != null) {
            return;
        }

        INSTANCE = new DebugData(debugPosDataFile);
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
