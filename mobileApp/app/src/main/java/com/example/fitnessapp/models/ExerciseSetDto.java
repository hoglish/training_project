package com.example.fitnessapp.models;

public class ExerciseSetDto {
    public String id;
    public String workout_id;
    public int set_number;
    public int number_of_repeat;
    public double weight;

    public ExerciseSetDto() {}

    public ExerciseSetDto(String workoutId, int setNumber, int reps, double weight) {
        this.workout_id = workoutId;
        this.set_number = setNumber;
        this.number_of_repeat = reps;
        this.weight = weight;
    }
}