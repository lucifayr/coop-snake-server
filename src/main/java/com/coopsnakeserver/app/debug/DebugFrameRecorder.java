package com.coopsnakeserver.app.debug;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import com.coopsnakeserver.app.game.frame.PlayerGameFrame;
import com.coopsnakeserver.app.pojo.Player;

/**
 * DebugFrameRecorder
 *
 * created: 05.06.2024
 *
 * @author June L. Gschwantner
 */
public class DebugFrameRecorder {
    private final int sessionKey;
    private final HashMap<Byte, FileOutputStream> recordingStreams = new HashMap<>();

    public DebugFrameRecorder(int sessionKey) {
        this.sessionKey = sessionKey;
    }

    public void record(Player player, PlayerGameFrame frame) {
        try {
            recordActual(player, frame);
        } catch (Exception e) {
            System.out.println(String.format("failed to record data for player %02d in session %s",
                    player.getValue(), this.sessionKey));
            e.printStackTrace();
        }
    }

    public void recordGameOver() {
        try {
            recordGameOverActual();
        } catch (Exception e) {
            System.out.println(String.format("failed to record game over in session %s", this.sessionKey));
            e.printStackTrace();
        }
    }

    private void recordActual(Player player, PlayerGameFrame frame) throws IOException, FileNotFoundException {
        var dirPath = String.format("src/main/resources/debug/recordings/%06d", this.sessionKey);
        Files.createDirectories(Paths.get(dirPath));

        var stream = recordingStreams.get(player.getValue());
        if (stream == null) {
            var path = String.format("%s/player_%02d", dirPath, player.getValue());
            var recordingFile = new File(path);
            recordingFile.createNewFile();

            stream = new FileOutputStream(recordingFile, false);
            recordingStreams.put(player.getValue(), stream);
        }

        stream.write(frame.intoBytes());
    }

    private void recordGameOverActual() throws IOException {
        for (var stream : recordingStreams.values()) {
            stream.write(new byte[] { 0, 0, 0, 0 });
        }
    }
}
