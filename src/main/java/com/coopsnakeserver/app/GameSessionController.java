package com.coopsnakeserver.app;

import java.util.HashMap;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.coopsnakeserver.app.game.GameSessionConfig;
import com.coopsnakeserver.app.game.GameSessionSocket;
import com.coopsnakeserver.app.game.GameUtils;

/**
 * GameSessionController
 *
 * Rest API endpoints to create new sessions and query session info.
 *
 * <br>
 * <br>
 *
 * created: 07.06.2024
 *
 * @author June L. Gschwantner
 */
@RestController
public class GameSessionController {
    private final HashMap<Integer, GameSessionSocket> sessions = new HashMap<>();

    @GetMapping("/game/session/{key}/config")
    private ResponseEntity<GameSessionConfig> getConfig(@PathVariable Integer key) {
        var session = sessions.get(key);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }

        var config = session.getSessionConfig();
        return ResponseEntity.ok(config);
    }

    @PostMapping("/game/session/new")
    private ResponseEntity<String> newEmployee(@RequestBody GameSessionConfig config) {
        var configIssue = GameSessionController.validateConfig(config);
        if (configIssue.isPresent()) {
            return configIssue.get();
        }

        var key = uniqueSessionKey();
        sessions.put(key, new GameSessionSocket(key, config, this));

        return ResponseEntity.ok(String.format("%06d", key));
    }

    private static Optional<ResponseEntity<String>> validateConfig(GameSessionConfig config) {
        var bSize = config.getBoardSize();
        var sSize = config.getInitialSnakeSize();
        var pCount = config.getPlayerCount();
        var tName = config.getTeamName();

        if (tName == null) {
            return Optional.of(
                    ResponseEntity.badRequest().body("Missing team name"));
        }

        if (tName.length() <= 0) {
            return Optional.of(
                    ResponseEntity.badRequest().body(String.format("Team name too short: %s", tName)));
        }

        if (tName.length() > 255) {
            return Optional.of(
                    ResponseEntity.badRequest().body(String.format("Team name too long: %s", tName)));
        }

        if (bSize < GameSessionSocket.MIN_BOARD_SIZE) {
            return Optional.of(
                    ResponseEntity.badRequest().body(String.format("Board size too small: %d", bSize)));
        }
        if (bSize > GameSessionSocket.MAX_BOARD_SIZE) {
            return Optional.of(
                    ResponseEntity.badRequest().body(String.format("Board size too large: %d", bSize)));
        }
        if (sSize < GameSessionSocket.MIN_INTIIAL_SNAKE_SIZE) {
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

    public void removeSession(int key) {
        this.sessions.remove(key);
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
