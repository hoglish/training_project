package com.example.fitnessapp.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_profile")
public class UserProfileEntity {
    @NonNull
    @PrimaryKey
    public String id;

    public String login;
    public String email;
    public String gender;
    public Integer height;
    public Float weight;
    public String goal;
    public String dateOfBirthday;
}