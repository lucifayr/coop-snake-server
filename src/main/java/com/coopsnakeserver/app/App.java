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
        var debug = System.getenv("SNAKE_DEBUG");
        if (debug != null && debug.equals("true")) {

            Optional<String> debug_coords_file = Optional.empty();
            var path = System.getenv("SNAKE_DEBUG_COORDS_FILE");
            if (path != null) {
                debug_coords_file = Optional.of("/debug/" + path);
            }

            try {
                DebugData.init(debug_coords_file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        SpringApplication.run(App.class, args);
    }
}
