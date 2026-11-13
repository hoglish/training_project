package com.example.fitnessapp.models;

public class UpdateNotificationsRequest {
    private Boolean remind_training;
    private Boolean remind_meals;
    private Boolean remind_water;
    private String quiet_mode_start;
    private String quiet_mode_end;

    public void setRemind_training(Boolean remind_training) {
        this.remind_training = remind_training;
    }

    public void setRemind_meals(Boolean remind_meals) {
        this.remind_meals = remind_meals;
    }

    public void setRemind_water(Boolean remind_water) {
        this.remind_water = remind_water;
    }

    public void setQuiet_mode_start(String quiet_mode_start) {
        this.quiet_mode_start = quiet_mode_start;
    }

    public void setQuiet_mode_end(String quiet_mode_end) {
        this.quiet_mode_end = quiet_mode_end;
    }
}