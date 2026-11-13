package com.example.fitnessapp.models;

public class UpdateProfileRequest {
    private String email;
    private String date_of_birthday;
    private String gender;
    private Integer height;
    private Float weight;
    private String goal;

    public void setEmail(String email) { this.email = email; }
    public void setDate_of_birthday(String date_of_birthday) { this.date_of_birthday = date_of_birthday; }
    public void setGender(String gender) { this.gender = gender; }
    public void setHeight(Integer height) { this.height = height; }
    public void setWeight(Float weight) { this.weight = weight; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getEmail() { return email; }
    public String getDate_of_birthday() { return date_of_birthday; }
    public String getGender() { return gender; }
    public Integer getHeight() { return height; }
    public Float getWeight() { return weight; }
    public String getGoal() { return goal; }
}