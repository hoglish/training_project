package com.example.fitnessapp.models;

public class LoginRequest {
    private String login;
    private String password;

    public LoginRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }
}