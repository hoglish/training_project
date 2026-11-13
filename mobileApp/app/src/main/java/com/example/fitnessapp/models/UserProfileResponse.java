package com.example.fitnessapp.models;

public class UserProfileResponse {
    private String login;
    private String email;
    private String gender;
    private Integer height;
    private Float weight;
    private String goal;
    private String date_of_birthday;

    // Геттеры
    public String getLogin() { return login; }
    public String getEmail() { return email; }
    public String getGender() { return gender; }
    public Integer getHeight() { return height; }
    public Float getWeight() { return weight; }
    public String getGoal() { return goal; }
    public String getDate_of_birthday() { return date_of_birthday; }
}