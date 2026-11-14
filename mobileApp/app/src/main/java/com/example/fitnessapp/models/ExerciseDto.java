package com.example.fitnessapp.models;

import java.util.List;

public class ExerciseDto {
    public String id;
    public String name;
    public String workout_type;
    public String animation;
    public List<MuscleDto> muscules;
    public List<InventoryDto> inventories;
    public List<TagDto> tags;
}