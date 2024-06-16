package com.coopsnakeserver.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.sql.Connection;
import java.sql.DriverManager;

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
    private static final Connection dbConnection = getDBConnection();

    public static void main(String[] args) {
        DebugMode.initFromEnv();
        DBUtils.initTables();

        SpringApplication.run(App.class, args);
    }

    public static Connection dbConnection() {
        return dbConnection;
    }

    public static Logger logger() {
        return logger;
    }

    private static Connection getDBConnection() {
        try {
            return DriverManager.getConnection("jdbc:sqlite:score.db");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*");
            }
        };
    }
}
