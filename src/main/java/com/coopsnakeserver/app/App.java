package com.coopsnakeserver.app;

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
        DebugData.initFromEnv();

        SpringApplication.run(App.class, args);
    }
}
