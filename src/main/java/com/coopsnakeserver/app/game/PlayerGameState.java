package com.coopsnakeserver.app.game;

import java.util.ArrayDeque;

import org.springframework.web.socket.WebSocketSession;

import com.coopsnakeserver.app.DevUtils;
import com.coopsnakeserver.app.PlayerToken;
import com.coopsnakeserver.app.game.frame.PlayerGameFrame;
import com.coopsnakeserver.app.pojo.Coordinate;
import com.coopsnakeserver.app.pojo.Player;
import com.coopsnakeserver.app.pojo.SnakeDirection;

/**
 * PlayerGameState
 *
 * created: 30.05.2024
 *
 * @author June L. Gschwantner
 */
public class PlayerGameState {
    private GameSession session;
    private WebSocketSession ws;

    private short initialSnakeSize;

    private Player player;
    private PlayerToken token;

    private PlayerGameFrame canonicalFrame;

    public PlayerGameState(GameSession parent, WebSocketSession ws, Player player, PlayerToken token,
            short initialSnakeSize) {
        this.session = parent;
        this.ws = ws;
        this.player = player;
        this.token = token;

        this.initialSnakeSize = initialSnakeSize;
        createInitialFrame();
    }

    public void newCanonicalFrame(PlayerGameFrame frame) {
        var maxSnakeSize = this.getBoardSize() * this.getBoardSize() - 1;
        if (frame.getSnakeCoords().size() >= maxSnakeSize) {
            var coords = frame.getSnakeCoords();
            var snakeCoords = new ArrayDeque<Coordinate>(this.initialSnakeSize);
            for (var i = 0; i < initialSnakeSize; i++) {
                snakeCoords.addFirst(coords.pollFirst());
            }

            this.canonicalFrame = new PlayerGameFrame(snakeCoords, frame.getSnakeDirection(), frame.getFoodCoord());
        } else {
            this.canonicalFrame = frame;
        }
    }

    public PlayerGameFrame canonicalFrame() {
        return this.canonicalFrame;
    }

    public short getBoardSize() {
        return this.session.getBoardSize();
    }

    public PlayerToken getToken() {
        return this.token;
    }

    public Player getPlayer() {
        return this.player;
    }

    public WebSocketSession getConnection() {
        return this.ws;
    }

    private void createInitialFrame() {
        DevUtils.assertion(this.initialSnakeSize > 0,
                "Snake has to have more than 0 segemnts at the start of the game. Received " + this.initialSnakeSize);

        short yOffset = player.getValue();
        var goLeft = yOffset % 2 == 0;

        var snakeCoords = GameUtils.initialCoords(this.initialSnakeSize, session.getBoardSize(), yOffset, goLeft);
        var foodCoord = GameUtils.findFoodCoord(snakeCoords.getFirst(), snakeCoords.stream().toList(),
                this.session.getBoardSize());

        var snakeDirection = SnakeDirection.Right;
        if (goLeft) {
            snakeDirection = SnakeDirection.Left;
        }

        var frame = new PlayerGameFrame(snakeCoords, snakeDirection, foodCoord);
        this.canonicalFrame = frame;
    }
}
