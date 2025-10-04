package com.example.tictactoe.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class Move {

    @NotBlank(message = "Player login is required")
    private String playerLogin;

    @Min(value = 0, message = "Square index must be between 0 and 8")
    @Max(value = 8, message = "Square index must be between 0 and 8")
    private int squareIndex;

    @NotBlank(message = "Game ID is required")
    private String gameId;

    public String getPlayerLogin() {
        return playerLogin;
    }

    public void setPlayerLogin(String playerLogin) {
        this.playerLogin = playerLogin;
    }

    public int getSquareIndex() {
        return squareIndex;
    }

    public void setSquareIndex(int squareIndex) {
        this.squareIndex = squareIndex;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
}