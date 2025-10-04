package com.example.tictactoe.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class JoinResponse {
    @NotBlank(message = "Game ID is required")
    private String gameId;

    @NotBlank(message = "Player login is required (responder)")
    private String responderLogin;

    @NotBlank(message = "Requester login is required")
    private String requesterLogin;

    @NotNull(message = "Accepted status is required")
    private Boolean accepted;

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getResponderLogin() {
        return responderLogin;
    }

    public void setResponderLogin(String responderLogin) {
        this.responderLogin = responderLogin;
    }

    public String getRequesterLogin() {
        return requesterLogin;
    }

    public void setRequesterLogin(String requesterLogin) {
        this.requesterLogin = requesterLogin;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }
}
