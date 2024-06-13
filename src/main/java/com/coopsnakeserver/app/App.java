package com.coopsnakeserver.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.coopsnakeserver.app.debug.DebugMode;

/**
 *
 * created: 03.04.2024
 *
 * @author June L. Gschwantner
 */
@SpringBootApplication
public class App {
    private static final Logger logger = LogManager.getLogger(App.class);

    public static void main(String[] args) {
        DebugMode.initFromEnv();

        SpringApplication.run(App.class, args);
    }

    public static Logger logger() {
        return logger;
    }
}
