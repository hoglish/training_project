package com.example.fitnessapp.models;

public class RegisterRequest {
    private String login;
    private String password;
    private String email;

    private String goal;
    private String gender;
    private Integer height;
    private Float weight;
    private String date_of_birthday;

    // Конструктор
    public RegisterRequest(String login, String password, String email) {
        this.login = login;
        this.password = password;
        this.email = email;
    }

    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getGoal() { return goal; }
    public String getGender() { return gender; }
    public Integer getHeight() { return height; }
    public Float getWeight() { return weight; }

    public void setGoal(String goal) { this.goal = goal; }
    public void setGender(String gender) { this.gender = gender; }
    public void setHeight(Integer height) { this.height = height; }
    public void setWeight(Float weight) { this.weight = weight; }
    public void setDate_of_birthday(String date_of_birthday) {
        this.date_of_birthday = date_of_birthday;
    }
}