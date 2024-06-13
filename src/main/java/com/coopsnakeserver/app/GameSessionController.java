package com.coopsnakeserver.app;

import java.util.HashMap;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.coopsnakeserver.app.game.GameSessionConfig;
import com.coopsnakeserver.app.game.GameSessionSocket;
import com.coopsnakeserver.app.game.GameUtils;

/**
 * GameSessionController
 *
 * created: 07.06.2024
 *
 * @author June L. Gschwantner
 */
@RestController
public class GameSessionController {
    private final HashMap<Integer, GameSessionSocket> sessions = new HashMap<>();

    @PostMapping("/game/session/new")
    private ResponseEntity<String> newEmployee(@RequestBody GameSessionConfig config) {
        var configIssue = GameSessionController.checkConfig(config);
        if (configIssue.isPresent()) {
            return configIssue.get();
        }

        var key = uniqueSessionKey();
        sessions.put(key, new GameSessionSocket(key, config));

        return ResponseEntity.ok(String.format("%06d", key));
    }

    private static Optional<ResponseEntity<String>> checkConfig(GameSessionConfig config) {
        if (config.getBoardSize() < GameSessionSocket.MIN_INTIIAL_SNAKE_SIZE) {
            return Optional.of(
                    ResponseEntity.badRequest().body(String.format("Board size too small: %d", config.getBoardSize())));
        }
        if (config.getBoardSize() > GameSessionSocket.MAX_BOARD_SIZE) {
            return Optional.of(
                    ResponseEntity.badRequest().body(String.format("Board size too large: %d", config.getBoardSize())));
        }
        if (config.getInitialSnakeSize() > GameSessionSocket.MIN_INTIIAL_SNAKE_SIZE) {
            return Optional.of(ResponseEntity.badRequest()
                    .body(String.format("Initial snake size too small: %d", config.getInitialSnakeSize())));
        }
        if (config.getPlayerCount() < GameSessionSocket.MIN_PLAYER_COUNT) {
            return Optional.of(ResponseEntity.badRequest()
                    .body(String.format("Player count too small: %d", config.getPlayerCount())));
        }
        if (config.getPlayerCount() > GameSessionSocket.MAX_PLAYER_COUNT) {
            return Optional.of(ResponseEntity.badRequest()
                    .body(String.format("Player count too large: %d", config.getPlayerCount())));
        }

        return Optional.empty();
    }

    public HashMap<Integer, GameSessionSocket> getSessions() {
        return sessions;
    }

    private int uniqueSessionKey() {
        DevUtils.assertion(sessions.keySet().size() < GameSessionSocket.MAX_SESSIONS,
                "Maximum number of sessions exeeded. Cannot created a new unique key.");

        int key;
        do {
            key = GameUtils.randomInt(0, GameSessionSocket.MAX_SESSIONS);
        } while (sessions.containsKey(key));

        return key;
    }
}
