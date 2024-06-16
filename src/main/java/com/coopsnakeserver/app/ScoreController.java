package com.coopsnakeserver.app;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.coopsnakeserver.app.DBUtils.HighScore;

/**
 *
 * created: 16.06.2024
 *
 * @author June L. Gschwantner
 */
@RestController
public class ScoreController {
    @GetMapping("/score/today")
    private ResponseEntity<List<HighScore>> getHighScores24h() {
        return ResponseEntity.ok(DBUtils.getHighScores24h());
    }

    @GetMapping("/score/allTime")
    private ResponseEntity<List<HighScore>> getHighScoresAllTime() {
        return ResponseEntity.ok(DBUtils.getHighScoresAllTime());
    }
}
