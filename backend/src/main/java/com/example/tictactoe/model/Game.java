package com.example.tictactoe.model;

import java.time.Instant;

public class Game {

    private String gameId;
    private Player player1;
    private Player player2;
    private GameStatus status;
    private String[] board;
    private TicToe winner;
    private String currentPlayerLogin;
    private String surrenderRequesterLogin;
    private Player pendingJoinPlayer;
    private Instant createdAt;
    private Instant lastActivityAt;

    public Game() {
        this.board = new String[9];
        this.createdAt = Instant.now();
        this.lastActivityAt = Instant.now();
    }

    // Getters and Setters
    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Player getPlayer1() {
        return player1;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public String[] getBoard() {
        return board;
    }

    public void setBoard(String[] board) {
        this.board = board;
    }

    public TicToe getWinner() {
        return winner;
    }

    public void setWinner(TicToe winner) {
        this.winner = winner;
    }

    public String getCurrentPlayerLogin() {
        return currentPlayerLogin;
    }

    public void setCurrentPlayerLogin(String currentPlayerLogin) {
        this.currentPlayerLogin = currentPlayerLogin;
    }

    public String getSurrenderRequesterLogin() {
        return surrenderRequesterLogin;
    }

    public void setSurrenderRequesterLogin(String surrenderRequesterLogin) {
        this.surrenderRequesterLogin = surrenderRequesterLogin;
    }

    public Player getPendingJoinPlayer() {
        return pendingJoinPlayer;
    }

    public void setPendingJoinPlayer(Player pendingJoinPlayer) {
        this.pendingJoinPlayer = pendingJoinPlayer;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(Instant lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public void updateLastActivity() {
        this.lastActivityAt = Instant.now();
    }
}