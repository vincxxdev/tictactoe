package com.example.tictactoe.model;

public class Game {

    private String gameId;
    private Player player1;
    private Player player2;
    private GameStatus status;
    private String[] board;
    private TicToe winner;
    private String currentPlayerLogin;
    private String surrenderRequesterLogin; // New field

    public Game() {
        this.board = new String[9];
    }

    // Getters and Setters for all fields, including the new one

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
}