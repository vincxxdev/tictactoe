package com.example.tictactoe.model;

public class Player {
    private String login;

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
