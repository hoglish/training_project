package com.example.fitnessapp.models;

import java.util.List;

public class WeatherResponse {
    public CurrentWeather current;
    public List<HourlyWeather> hourly;
    public List<DailyWeather> daily;
}