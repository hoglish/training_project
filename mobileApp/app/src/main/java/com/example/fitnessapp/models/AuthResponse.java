package com.example.fitnessapp.models;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private int expiresIn;

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public int getExpiresIn() { return expiresIn; }
}
