package com.example.fitnessapp.models;

import java.util.List;

public class CreateTrainingRequest {
    public String name;
    public String date_time_start;
    public String date_time_end;
    public String status;
    public Integer feeling;
    public String description;
    public Integer calories;
    public List<ExerciseSetDto> sets;

    public CreateTrainingRequest(String name,
                                 String start,
                                 String end,
                                 String status,
                                 Integer feeling,
                                 String description,
                                 List<ExerciseSetDto> sets) {
        this.name = name;
        this.date_time_start = start;
        this.date_time_end = end;
        this.status = status;
        this.feeling = feeling;
        this.description = description;
        this.sets = sets;
    }
}