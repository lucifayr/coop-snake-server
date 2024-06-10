package com.coopsnakeserver.app;

import java.util.HashMap;

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
    private String newEmployee(@RequestBody GameSessionConfig config) {
        var key = uniqueSessionKey();
        sessions.put(key, new GameSessionSocket(key, config));

        return String.format("%06d", key);
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
