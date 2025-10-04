package com.example.tictactoe.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class ConnectRequest {
    @NotNull(message = "Player information is required")
    @Valid
    private Player player;

    private String gameId;

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
}
