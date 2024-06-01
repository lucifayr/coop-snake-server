package com.coopsnakeserver.app;

import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.coopsnakeserver.app.debug.DebugData;

/**
 *
 * created: 03.04.2024
 *
 * @author June L. Gschwantner
 */
@SpringBootApplication
public class App {

    public static void main(String[] args) {
        initDebug();
        SpringApplication.run(App.class, args);
    }

    private static void initDebug() {
        var debug = System.getenv("SNAKE_DEBUG");

        if (debug == null || !debug.equals("true")) {
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

        var wrap = System.getenv("SNAKE_DEBUG_WRAP_ON_OUT_OF_BOUNDS");
        var wrapOnOutOfBounds = wrap != null && wrap.equals("true");

        try {
            DebugData.init(debugCoordsFile, messageInLatency, wrapOnOutOfBounds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
