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
        var bSize = config.getBoardSize();
        var sSize = config.getInitialSnakeSize();
        var pCount = config.getPlayerCount();

        if (bSize < GameSessionSocket.MIN_INTIIAL_SNAKE_SIZE) {
            return Optional.of(
                    ResponseEntity.badRequest().body(String.format("Board size too small: %d", bSize)));
        }
        if (bSize > GameSessionSocket.MAX_BOARD_SIZE) {
            return Optional.of(
                    ResponseEntity.badRequest().body(String.format("Board size too large: %d", bSize)));
        }
        if (sSize > GameSessionSocket.MIN_INTIIAL_SNAKE_SIZE) {
            return Optional.of(ResponseEntity.badRequest()
                    .body(String.format("Initial snake size too small: %d", sSize)));
        }
        if (sSize > bSize) {
            return Optional.of(ResponseEntity.badRequest()
                    .body(String.format("Initial snake size too large for board: board %d, snake %d",
                            bSize, sSize)));
        }
        if (pCount < GameSessionSocket.MIN_PLAYER_COUNT) {
            return Optional.of(ResponseEntity.badRequest()
                    .body(String.format("Player count too small: %d", pCount)));
        }
        if (pCount > GameSessionSocket.MAX_PLAYER_COUNT) {
            return Optional.of(ResponseEntity.badRequest()
                    .body(String.format("Player count too large: %d", pCount)));
        }
        if (pCount > bSize - 1) {
            return Optional.of(ResponseEntity.badRequest()
                    .body(String.format("Player count too large for board: board %d, players %d", bSize,
                            pCount)));
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
