package com.example.fitnessapp.models;

import java.util.List;

public class CreateExerciseRequest {
    public String name;
    public String workout_type;
    public String animation;
    public List<String> muscules;
    public List<String> inventories;
    public List<String> tags;

    public CreateExerciseRequest() {}

    public CreateExerciseRequest(String name, String workoutType,
                                 List<String> muscules,
                                 List<String> inventories,
                                 List<String> tags) {
        this.name = name;
        this.workout_type = workoutType;
        this.muscules = muscules;
        this.inventories = inventories;
        this.tags = tags;
    }
}