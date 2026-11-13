package com.example.fitnessapp.models;

public class NotificationSettingsResponse {
    private boolean remind_training;
    private boolean remind_meals;
    private boolean remind_water;
    private String quiet_mode_start;
    private String quiet_mode_end;

    public boolean isRemind_training() { return remind_training; }
    public boolean isRemind_meals() { return remind_meals; }
    public boolean isRemind_water() { return remind_water; }
    public String getQuiet_mode_start() { return quiet_mode_start; }
    public String getQuiet_mode_end() { return quiet_mode_end; }
}