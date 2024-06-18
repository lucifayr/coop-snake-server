package com.coopsnakeserver.app;

import java.util.ArrayList;
import java.util.List;

/**
 * DBUtils
 *
 * created: 16.06.2024
 *
 * @author June L. Gschwantner
 */
public class DBUtils {
    /**
     * Create any tables that are needed but that don't yet exist. If all tables
     * already exist this function does nothing.
     */
    public static final void initTables() {
        try {
            var statment = App.dbConnection().createStatement();
            var sql = """
                            CREATE TABLE IF NOT EXISTS scores (
                                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                                    team_name VARCHAR(255) NOT NULL,
                                    score INTEGER NOT NULL,
                                    timestamp INTEGER NOT NULL
                            );
                    """;

            statment.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
            App.logger().error("Failed to initiazlie SQLite tables");
        }
    }

    public static final void saveScore(String teamName, Integer score) {
        try {
            var statement = App.dbConnection().prepareStatement("""
                    INSERT INTO scores
                        (team_name, score, timestamp) VALUES (?, ?, ?);
                                """);

            statement.setString(1, teamName.trim());
            statement.setInt(2, score);
            statement.setLong(3, System.currentTimeMillis() / 1000L);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            App.logger().error(String.format("Failed to save score %d for team %s", score, teamName));
        }
    }

    public record HighScore(String teamName, Integer score) {
    }

    public static final List<HighScore> getHighScores24h() {
        var millisIn24h = 24 * 60 * 60 * 1000;
        var minTimestamp = (System.currentTimeMillis() / 1000L) - millisIn24h;

        return getHighScoresAfterTimestamp(minTimestamp);
    }

    public static final List<HighScore> getHighScoresAllTime() {
        return getHighScoresAfterTimestamp(0);
    }

    private static final List<HighScore> getHighScoresAfterTimestamp(long timestampMillis) {
        try {
            var millisIn24h = 24 * 60 * 60 * 1000;
            var minTimestamp = (System.currentTimeMillis() / 1000L) - millisIn24h;

            var sql = """
                    SELECT DISTINCT team_name, MAX(score) as score
                    FROM scores
                    WHERE timestamp > ?
                    GROUP BY team_name
                    ORDER BY MAX(score) desc, team_name, timestamp desc
                    LIMIT 50;
                    """;

            var statement = App.dbConnection().prepareStatement(sql);
            statement.setLong(1, minTimestamp);

            var results = statement.executeQuery();
            var scores = new ArrayList<HighScore>();
            while (results.next()) {
                var score = results.getInt("score");
                var team = results.getString("team_name");

                DevUtils.assertion(team != null, "Team name should never be null.");

                scores.add(new HighScore(team, score));
            }

            return scores;
        } catch (Exception e) {
            e.printStackTrace();
            App.logger().error("Failed to get high scores");

            return new ArrayList<>();
        }
    }
}
