package com.coopsnakeserver.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.coopsnakeserver.app.debug.DebugMode;

/**
 *
 * created: 03.04.2024
 *
 * @author June L. Gschwantner
 */
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        DebugMode.initFromEnv();

        SpringApplication.run(App.class, args);
    }
}
