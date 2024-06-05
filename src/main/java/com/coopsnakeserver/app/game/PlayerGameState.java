package com.coopsnakeserver.app.game;

import java.io.IOException;
import java.util.Optional;

import org.springframework.web.socket.WebSocketSession;

import com.coopsnakeserver.app.DevUtils;
import com.coopsnakeserver.app.PlayerToken;
import com.coopsnakeserver.app.game.frame.FrameHandler;
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

    private FrameHandler frameHandler;

    private Player player;
    private PlayerToken token;

    public PlayerGameState(GameSession parent, WebSocketSession ws, Player player, PlayerToken token,
            short initialSankeSize)
            throws IOException {
        this.frameHandler = new FrameHandler(
                (int) GameSession.INPUT_LATENCY_GRACE_PERIOD_TICKS);

        this.session = parent;
        this.ws = ws;
        this.player = player;
        this.token = token;

        createInitialFrame(initialSankeSize);
    }

    public void newCanonicalFrame(PlayerGameFrame frame) {
        this.frameHandler.store(frame);
    }

    public PlayerGameFrame canonicalFrame() {
        return this.frameHandler.peek();
    }

    public Optional<PlayerGameFrame> rewindFrames(int ticks) {
        return this.frameHandler.rewind(ticks);
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
        var foodCoord = GameUtils.nextFood(snakeCoords.stream().toList(), this.session.getBoardSize());

        var snakeDirection = SnakeDirection.Right;
        if (goLeft) {
            snakeDirection = SnakeDirection.Left;
        }

        var frame = new PlayerGameFrame(snakeCoords, snakeDirection, foodCoord);
        frameHandler.store(frame);
    }
}
