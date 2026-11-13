package com.example.fitnessapp.models;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private int expiresIn;
    private String userId;
    public String getUserId() { return userId; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public int getExpiresIn() { return expiresIn; }
}
