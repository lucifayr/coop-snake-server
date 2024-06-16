package com.coopsnakeserver.app.game;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import com.coopsnakeserver.app.App;
import com.coopsnakeserver.app.BinaryUtils;
import com.coopsnakeserver.app.GameBinaryMessage;
import com.coopsnakeserver.app.PlayerSwipeInput;
import com.coopsnakeserver.app.PlayerToken;
import com.coopsnakeserver.app.pojo.GameMessageType;
import com.coopsnakeserver.app.pojo.Player;
import com.coopsnakeserver.app.pojo.SessionInfo;
import com.coopsnakeserver.app.pojo.SessionInfoType;

/**
 *
 * created: 25.05.2024
 *
 * @author June L. Gschwantner
 */
public class GameSession {
    public static int TICKS_PER_SECOND = 8;
    private static long TICK_RATE_MILLIS = 1_000 / TICKS_PER_SECOND;

    private int sessionKey;
    private GameSessionState gameState = GameSessionState.WaitingForPlayers;
    private boolean closed = false;

    private GameSessionConfig config;

    private HashMap<Player, PlayerGameLoop> loops = new HashMap<>();
    private HashMap<PlayerToken, Player> tokenOwners = new HashMap<>();
    private ScheduledFuture<?> tickFunc;

    public GameSession(int sessionKey, GameSessionConfig config, ScheduledExecutorService executor) {
        this.sessionKey = sessionKey;
        this.config = config;

        var future = executor.scheduleWithFixedDelay(() -> {
            if (this.gameState != GameSessionState.Running || this.closed) {
                return;
            }

            try {
                for (var l : this.loops.values()) {
                    var gameOver = l.tick();
                    if (gameOver.isPresent()) {
                        this.gameState = GameSessionState.GameOver;
                        notifyGameOver(gameOver.get());

                        return;
                    }

                    l.updateWsClients();
                }
            } catch (Exception e) {
                e.printStackTrace();
                App.logger().info(String.format("Uncaught exception session %06d", this.sessionKey));

                teardown();
            }
        }, 0, TICK_RATE_MILLIS, TimeUnit.MILLISECONDS);

        this.tickFunc = future;

        App.logger().info(String.format("Created session %06d", this.sessionKey));
    }

    public void disconnectPlayer(byte playerNumber) {
        if (this.gameState != GameSessionState.WaitingForPlayers) {
            App.logger().warn(
                    "Trying to disconnect from running session.Disconnect will be ignored.",
                    this.gameState);
        }

        var playerOpt = Player.fromByte(playerNumber);
        if (playerOpt.isEmpty()) {
            App.logger().warn(String.format("Received invalid player number %d", playerNumber));
            return;
        }

        var player = playerOpt.get();
        this.loops.remove(player);

        notifyWaitingFor(playerNumber);
    }

    public void connectPlayer(byte playerNumber, WebSocketSession ws)
            throws IOException {
        if (this.closed) {
            return;
        }

        if (playerNumber > this.config.getPlayerCount()) {
            App.logger()
                    .warn(String.format(
                            "Extra player tried to connect. Expected max = %d. Received player number %d",
                            this.config.getPlayerCount(), playerNumber));
            return;
        }

        var playerOpt = Player.fromByte(playerNumber);
        if (playerOpt.isEmpty()) {
            App.logger().warn(String.format("Received invalid player number %d", playerNumber));
            return;
        }

        var player = playerOpt.get();
        if (this.loops.containsKey(player)) {
            App.logger().warn(String.format("Player already connected. %s", player));
            return;
        }

        var otherTokens = this.loops.values().stream().map(l -> l.getToken()).collect(Collectors.toList());
        var token = PlayerToken.genRandom(otherTokens);
        var tokenMsgBin = new BinaryMessage(token.intoMsg().intoBytes());

        var boardInfo = new SessionInfo(SessionInfoType.BoardSize,
                BinaryUtils.int32ToBytes((int) this.config.getBoardSize()));
        var boardInfoMsg = new GameBinaryMessage(GameMessageType.SessionInfo, boardInfo.intoBytes());
        var boardInfoMsgBin = new BinaryMessage(boardInfoMsg.intoBytes());

        var playerIdInfo = new SessionInfo(SessionInfoType.PlayerId, new byte[] { 0, 0, 0, player.getValue() });
        var playerIdInfoMsg = new GameBinaryMessage(GameMessageType.SessionInfo, playerIdInfo.intoBytes());
        var playerIdInfoMsgBin = new BinaryMessage(playerIdInfoMsg.intoBytes());

        var state = new PlayerGameState(this, ws, player, token, this.config.getInitialSnakeSize());
        var loop = new PlayerGameLoop(this, state);

        this.loops.put(player, loop);
        this.tokenOwners.put(token, player);

        ws.sendMessage(playerIdInfoMsgBin);
        ws.sendMessage(tokenMsgBin);
        ws.sendMessage(boardInfoMsgBin);

        App.logger().info(String.format("%s connected to session %06d", player, sessionKey));
        if (playerNumber == this.config.getPlayerCount()) {
            this.gameState = GameSessionState.Running;
            App.logger().info(String.format("Starting game for session %06d", this.sessionKey));
        }

        notifyWaitingFor(playerNumber);
    }

    public void notifyWaitingFor(byte playerNumber) {
        var waitingFor = this.config.getPlayerCount() - playerNumber;
        var waitingForInfo = new SessionInfo(SessionInfoType.WaitingFor, BinaryUtils.int32ToBytes(waitingFor));
        var waitingForInfoMsg = new GameBinaryMessage(GameMessageType.SessionInfo, waitingForInfo.intoBytes());
        var waitingForInfoMsgBin = new BinaryMessage(waitingForInfoMsg.intoBytes());

        for (var l : this.loops.values()) {
            try {
                l.getConnection().sendMessage(waitingForInfoMsgBin);
            } catch (Exception e) {
                e.printStackTrace();
                App.logger().warn(String.format("Failed to notify connection %s of updated wait list",
                        l.getConnection().getId()));
            }
        }
    }

    public void notifyGameOver(GameOverCause cause) throws IOException {
        var gameOverMsg = new SessionInfo(SessionInfoType.GameOver, cause.intoBytes());
        var gameOverMsgBin = new GameBinaryMessage(GameMessageType.SessionInfo, gameOverMsg.intoBytes());

        for (var l : this.loops.values()) {
            l.getConnection().sendMessage(new BinaryMessage(gameOverMsgBin.intoBytes()));
        }
    }

    public void teardown() {
        if (this.closed) {
            return;
        }

        this.closed = true;
        this.tickFunc.cancel(true);

        for (var loop : this.loops.values()) {
            try {
                loop.getConnection().close();
            } catch (Exception e) {
                App.logger().warn(String.format("Failed to send close signal to connection %s in session %06d",
                        loop.getConnection().getId(), this.sessionKey));
            }
        }

        App.logger().info(String.format("Closed session %06d", this.sessionKey));
    }

    public List<PlayerGameLoop> getOtherLoops(Player me) {
        return this.loops.entrySet().stream().filter(e -> {
            return e.getKey() != me;
        }).map(e -> {
            return e.getValue();
        }).toList();
    }

    public void handleBinWsMsg(BinaryMessage message) {
        var bytes = message.getPayload().array();
        var msg = GameBinaryMessage.fromBytes(bytes);

        if (msg.getType() == GameMessageType.PlayerSwipeInput) {
            handleInput(msg);
        }
    }

    private void handleInput(GameBinaryMessage msg) {
        var inputOpt = PlayerSwipeInput.fromBytes(msg.getData());
        if (inputOpt.isEmpty()) {
            return;
        }

        var input = inputOpt.get();
        var tokenOwner = tokenOwners.get(input.getPlayerToken());
        if (tokenOwner == null) {
            return;
        }

        var loop = this.loops.get(tokenOwner);
        if (loop == null) {
            App.logger().warn(
                    String.format("Received input for player that doesn't have a running loop. Input = %s", input));
            return;
        }
        loop.registerSwipeInput(input);
    }

    public GameSessionState getState() {
        return this.gameState;
    }

    public int getKey() {
        return this.sessionKey;
    }

    public short getBoardSize() {
        return this.config.getBoardSize();
    }

    public void enableDebugFrameReplay(int sessionKey, Player player, Player lastConnectedPlayer) {
        var loop = this.loops.get(lastConnectedPlayer);
        if (loop == null) {
            return;
        }

        loop.enableDebugFrameReplay(sessionKey, player);
    }
}
