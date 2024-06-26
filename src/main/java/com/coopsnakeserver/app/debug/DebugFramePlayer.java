package com.coopsnakeserver.app.debug;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Optional;

import com.coopsnakeserver.app.BinaryUtils;
import com.coopsnakeserver.app.game.frame.PlayerGameFrame;
import com.coopsnakeserver.app.pojo.Coordinate;
import com.coopsnakeserver.app.pojo.Player;
import com.coopsnakeserver.app.pojo.SnakeDirection;

/**
 * DebugFramePlayer
 *
 * Used to replay recorded sessions on demand.
 *
 * <br>
 * <br>
 *
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

    public Optional<PlayerGameFrame> playback(Player player) {
        try {
            return Optional.of(playbackActual(player));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private PlayerGameFrame playbackActual(Player player) throws IOException {
        var stream = playbackStreams.get(player.getValue());
        if (stream == null) {
            stream = openStream(player);
            playbackStreams.put(player.getValue(), stream);
        }

        return readNextFrame(stream, player);
    }

    private FileInputStream openStream(Player player) throws IOException {
        var filePath = String.format("src/main/resources/debug/recordings/%06d/player_%02d", this.sessionKey,
                player.getValue());
        var file = new File(filePath);
        return new FileInputStream(file);
    }

    private PlayerGameFrame readNextFrame(FileInputStream stream, Player player) throws IOException {
        if (stream.available() == 0) {
            stream = openStream(player);
            playbackStreams.put(player.getValue(), stream);
        }

        var dataLenBytes = stream.readNBytes(4);
        var dataLen = BinaryUtils.bytesToInt32(dataLenBytes);

        if (dataLen == 0) {
            stream = openStream(player);
            playbackStreams.put(player.getValue(), stream);
            return readNextFrame(stream, player);
        }

        var foodCoord = Coordinate.fromBytesInternal(stream.readNBytes(4));
        var snakeDirection = SnakeDirection.fromBytesInternal(stream.readNBytes(1));

        var snakeCoordsByteLen = dataLen - 5;
        var snakeCoords = new ArrayDeque<Coordinate>(snakeCoordsByteLen / 4);
        for (var i = 0; i < snakeCoordsByteLen; i += 4) {
            var coord = Coordinate.fromBytesInternal(stream.readNBytes(4));
            snakeCoords.addLast(coord);
        }

        return new PlayerGameFrame(snakeCoords, snakeDirection, foodCoord);
    }
}
