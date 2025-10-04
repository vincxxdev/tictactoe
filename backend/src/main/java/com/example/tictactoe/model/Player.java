package com.example.tictactoe.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class Player {
    @NotBlank(message = "Player login cannot be empty")
    @Size(min = 2, max = 50, message = "Player login must be between 2 and 50 characters")
    private String login;

    public Player() {
    }

    public Player(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
