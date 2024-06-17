package com.coopsnakeserver.app.game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import com.coopsnakeserver.app.App;
import com.coopsnakeserver.app.BinaryUtils;
import com.coopsnakeserver.app.DBUtils;
import com.coopsnakeserver.app.GameBinaryMessage;
import com.coopsnakeserver.app.PlayerRestartAction;
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

    private List<WebSocketSession> viewers = new ArrayList<>();
    private HashMap<Player, PlayerGameLoop> loops = new HashMap<>();
    private HashMap<PlayerToken, Player> tokenOwners = new HashMap<>();
    private HashSet<PlayerToken> restartConfirmations = new HashSet<>();
    private ScheduledFuture<?> tickFunc;

    public GameSession(int sessionKey, GameSessionConfig config, ScheduledExecutorService executor) {
        this.sessionKey = sessionKey;
        this.config = config;

        var future = executor.scheduleWithFixedDelay(() -> {
            if (this.gameState != GameSessionState.Running || this.closed) {
                return;
            }

            try {
                for (var loop : this.loops.values()) {
                    var gameOver = loop.tick();
                    if (gameOver.isPresent()) {
                        this.gameState = GameSessionState.GameOver;

                        var gameOverMsg = new SessionInfo(SessionInfoType.GameOver, gameOver.get().intoBytes());
                        var gameOverMsgBin = new GameBinaryMessage(GameMessageType.SessionInfo,
                                gameOverMsg.intoBytes());

                        var score = getScore();
                        var scoreMsg = new SessionInfo(SessionInfoType.Score, BinaryUtils.int32ToBytes(score));
                        var scoreMsgBin = new GameBinaryMessage(GameMessageType.SessionInfo,
                                scoreMsg.intoBytes());

                        DBUtils.saveScore(this.config.getTeamName(), score);

                        notifyConnections(scoreMsgBin);
                        notifyConnections(gameOverMsgBin);

                        return;
                    }
                }

                for (var loop : this.loops.values()) {
                    notifyGameStateUpdate(loop.getConnection());
                }

                for (var ws : this.viewers) {
                    notifyGameStateUpdate(ws);
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

        var playerCountInfo = new SessionInfo(SessionInfoType.PlayerCount,
                BinaryUtils.int32ToBytes((int) this.config.getPlayerCount()));
        var playerCountInfoMsg = new GameBinaryMessage(GameMessageType.SessionInfo, playerCountInfo.intoBytes());
        var playerCountInfoMsgBin = new BinaryMessage(playerCountInfoMsg.intoBytes());

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
        ws.sendMessage(playerCountInfoMsgBin);

        App.logger().info(String.format("%s connected to session %06d", player, sessionKey));
        if (playerNumber == this.config.getPlayerCount()) {
            this.gameState = GameSessionState.Running;
            App.logger().info(String.format("Starting game for session %06d", this.sessionKey));
        }

        notifyWaitingFor(playerNumber);
    }

    public void connectViewer(WebSocketSession ws) throws IOException {
        if (this.closed) {
            return;
        }

        var boardInfo = new SessionInfo(SessionInfoType.BoardSize,
                BinaryUtils.int32ToBytes((int) this.config.getBoardSize()));
        var boardInfoMsg = new GameBinaryMessage(GameMessageType.SessionInfo, boardInfo.intoBytes());
        var boardInfoMsgBin = new BinaryMessage(boardInfoMsg.intoBytes());

        var playerCountInfo = new SessionInfo(SessionInfoType.PlayerCount,
                BinaryUtils.int32ToBytes((int) this.config.getPlayerCount()));
        var playerCountInfoMsg = new GameBinaryMessage(GameMessageType.SessionInfo, playerCountInfo.intoBytes());
        var playerCountInfoMsgBin = new BinaryMessage(playerCountInfoMsg.intoBytes());

        ws.sendMessage(boardInfoMsgBin);
        ws.sendMessage(playerCountInfoMsgBin);

        this.viewers.add(ws);
        App.logger().info(String.format("Viewer(%s) connected to session %06d", ws.getId(), sessionKey));
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

        if (msg.getType() == GameMessageType.PlayerRestartConfirm) {
            var restartAction = PlayerRestartAction.fromBytes(msg.getData());
            if (restartAction.isPresent()) {
                handleRestartConfirmation(restartAction.get());
            }
        }

        if (msg.getType() == GameMessageType.PlayerRestartDeny) {
            var restartAction = PlayerRestartAction.fromBytes(msg.getData());
            if (restartAction.isPresent()) {
                handleRestartDenial(restartAction.get());
            }
        }
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

    private int getScore() {
        var score = this.loops.values().stream().mapToInt(loop -> {
            return loop.getFoodEaten();
        }).sum();

        return score;
    }

    private void notifyGameStateUpdate(WebSocketSession ws) throws IOException {
        for (var loop : this.loops.values()) {
            var foodMsg = loop.getFoodMsg();
            var foodMsgBin = new BinaryMessage(foodMsg.intoBytes());

            var playerMsg = loop.getPlayerMsg();
            var playerMsgBin = new BinaryMessage(playerMsg.intoBytes());

            ws.sendMessage(playerMsgBin);
            ws.sendMessage(foodMsgBin);
        }
    }

    private void notifyConnections(GameBinaryMessage msg) {
        for (var l : this.loops.values()) {
            try {
                l.getConnection().sendMessage(new BinaryMessage(msg.intoBytes()));
            } catch (Exception e) {
                e.printStackTrace();
                App.logger().warn(String.format("Failed to notify connection %s. Message = %s",
                        l.getConnection().getId(), msg));
            }
        }
    }

    private void notifyWaitingFor(byte playerNumber) {
        var waitingFor = this.config.getPlayerCount() - playerNumber;
        var waitingForInfo = new SessionInfo(SessionInfoType.WaitingFor, BinaryUtils.int32ToBytes(waitingFor));
        var waitingForInfoMsg = new GameBinaryMessage(GameMessageType.SessionInfo, waitingForInfo.intoBytes());

        notifyConnections(waitingForInfoMsg);
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

    private void restartGame() {
        for (var loop : this.loops.values()) {
            loop.reset();
        }

        this.gameState = GameSessionState.Running;
    }

    private void handleRestartConfirmation(PlayerRestartAction action) {
        if (this.gameState != GameSessionState.GameOver) {
            return;
        }

        if (tokenOwners.containsKey(action.getPlayerToken())) {
            restartConfirmations.add(action.getPlayerToken());
        }

        var waitingFor = tokenOwners.size() - restartConfirmations.size();
        if (waitingFor <= 0) {
            restartGame();
            restartConfirmations.clear();
            var restartInfo = new SessionInfo(SessionInfoType.RestartConfirmed, new byte[0]);
            var restartInfoMsg = new GameBinaryMessage(GameMessageType.SessionInfo, restartInfo.intoBytes());
            notifyConnections(restartInfoMsg);
        } else {
            var waitingForInfo = new SessionInfo(SessionInfoType.WaitingFor, BinaryUtils.int32ToBytes(waitingFor));
            var waitingForInfoMsg = new GameBinaryMessage(GameMessageType.SessionInfo, waitingForInfo.intoBytes());
            notifyConnections(waitingForInfoMsg);
        }
    }

    private void handleRestartDenial(PlayerRestartAction action) {
        if (this.gameState != GameSessionState.GameOver) {
            return;
        }

        if (!restartConfirmations.contains(action.getPlayerToken())
                && tokenOwners.containsKey(action.getPlayerToken())) {
            var restartInfo = new SessionInfo(SessionInfoType.RestartDenied, new byte[0]);
            var restartInfoMsg = new GameBinaryMessage(GameMessageType.SessionInfo, restartInfo.intoBytes());
            notifyConnections(restartInfoMsg);

            teardown();
        }
    }
}
