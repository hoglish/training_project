package com.example.fitnessapp.models;

import java.util.List;

public class TrainingDto {
    public String id;
    public String name;
    public String date_time_start;
    public String date_time_end;
    public String status;
    public Integer feeling;
    public String description;
    public Integer calories;
    public String user_id;
    public List<TrainingExerciseGroup> exercises;
}