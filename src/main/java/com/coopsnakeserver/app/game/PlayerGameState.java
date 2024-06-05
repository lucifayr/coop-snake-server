package com.coopsnakeserver.app.game;

import org.springframework.web.socket.WebSocketSession;

import com.coopsnakeserver.app.DevUtils;
import com.coopsnakeserver.app.PlayerToken;
import com.coopsnakeserver.app.game.frame.PlayerGameFrame;
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

    private Player player;
    private PlayerToken token;

    private PlayerGameFrame canonicalFrame;

    public PlayerGameState(GameSession parent, WebSocketSession ws, Player player, PlayerToken token,
            short initialSankeSize) {
        this.session = parent;
        this.ws = ws;
        this.player = player;
        this.token = token;

        createInitialFrame(initialSankeSize);
    }

    public void newCanonicalFrame(PlayerGameFrame frame) {
        this.canonicalFrame = frame;
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

    private void createInitialFrame(short initialSankeSize) {
        DevUtils.assertion(initialSankeSize > 0,
                "Snake has to have more than 0 segemnts at the start of the game. Received " + initialSankeSize);

        short yOffset = player.getValue();
        var goLeft = yOffset % 2 == 0;

        var snakeCoords = GameUtils.initialCoords(initialSankeSize, session.getBoardSize(), yOffset, goLeft);
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
