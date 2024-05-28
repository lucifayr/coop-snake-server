package com.coopsnakeserver.app.debug;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import com.coopsnakeserver.app.BinaryUtils;
import com.coopsnakeserver.app.DevUtils;
import com.coopsnakeserver.app.pojo.Coordinate;

/**
 * DebugData
 *
 * created: 25.05.2024
 *
 * @author June L. Gschwantner
 */
public class DebugData {
    private final InputStream DEBUG_PLAYER_COORDS_FROM_FILE;
    private final HashSet<DebugFlag> enabledFlags = new HashSet<>();

    private static DebugData INSTANCE;

    private DebugData(Optional<String> debugCoordDataFile) {
        if (debugCoordDataFile.isPresent()) {
            enabledFlags.add(DebugFlag.PlayerCoordinateDataFromFile);
            this.DEBUG_PLAYER_COORDS_FROM_FILE = this.getClass().getResourceAsStream(debugCoordDataFile.get());
        } else {
            this.DEBUG_PLAYER_COORDS_FROM_FILE = null;
        }
    }

    public static void init(Optional<String> debugPosDataFile) {
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
        if (this.DEBUG_PLAYER_COORDS_FROM_FILE == null) {
            return Optional.empty();
        }

        var termiator = new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255 };
        var coords = new ArrayList<Coordinate>();
        try {
            while (this.DEBUG_PLAYER_COORDS_FROM_FILE.available() > 0) {
                var xBytes = this.DEBUG_PLAYER_COORDS_FROM_FILE.readNBytes(4);
                if (Arrays.equals(xBytes, termiator)) {
                    break;
                }

                var yBytes = this.DEBUG_PLAYER_COORDS_FROM_FILE.readNBytes(4);
                if (Arrays.equals(yBytes, termiator)) {
                    break;
                }

                var x = BinaryUtils.bytesToInt32(xBytes);
                var y = BinaryUtils.bytesToInt32(yBytes);
                coords.add(new Coordinate(x, y));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }

        Coordinate[] arr = new Coordinate[coords.size()];
        return Optional.of(coords.toArray(arr));
    }
}
