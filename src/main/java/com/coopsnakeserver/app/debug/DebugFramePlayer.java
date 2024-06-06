package com.coopsnakeserver.app.debug;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;

import com.coopsnakeserver.app.BinaryUtils;
import com.coopsnakeserver.app.game.frame.PlayerGameFrame;
import com.coopsnakeserver.app.pojo.Coordinate;
import com.coopsnakeserver.app.pojo.Player;
import com.coopsnakeserver.app.pojo.SnakeDirection;

/**
 * created: 05.06.2024
 *
 * @author June L. Gschwantner
 */
public class DebugFramePlayer {

    private final int sessionKey;
    private final HashMap<Byte, FileInputStream> playbackStreams = new HashMap<>();

    public DebugFramePlayer(int sessionKey) {
        this.sessionKey = sessionKey;
    }

    public PlayerGameFrame playback(Player player) {
        try {
            return playbackActual(player);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private PlayerGameFrame playbackActual(Player player) throws IOException {
        var stream = playbackStreams.get(player.getValue());
        if (stream == null) {

            var filePath = String.format("src/main/resources/debug/recordings/%06d/player_%02d", this.sessionKey,
                    player.getValue());
            var file = new File(filePath);
            stream = new FileInputStream(file);
            playbackStreams.put(player.getValue(), stream);

            stream.mark(Integer.MAX_VALUE);
        }

        return readNextFrame(stream);
    }

    private PlayerGameFrame readNextFrame(FileInputStream stream) throws IOException {
        if (stream.available() == 0) {
            stream.reset();
        }

        var dataLenBytes = stream.readNBytes(4);
        var dataLen = BinaryUtils.bytesToInt32(dataLenBytes);

        if (dataLen == 0) {
            stream.reset();
            return readNextFrame(stream);
        }

        var foodCoord = Coordinate.fromBytes(stream.readNBytes(4));
        var snakeDirection = SnakeDirection.fromBytes(stream.readNBytes(1));

        var snakeCoordsByteLen = dataLen - 5;
        var snakeCoords = new ArrayDeque<Coordinate>(snakeCoordsByteLen / 4);
        for (var i = 0; i < snakeCoordsByteLen; i += 4) {
            var coord = Coordinate.fromBytes(stream.readNBytes(4));
            snakeCoords.addLast(coord);
        }

        return new PlayerGameFrame(snakeCoords, snakeDirection, foodCoord);
    }
}
