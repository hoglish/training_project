package com.example.fitnessapp.network;

import com.example.fitnessapp.models.AuthResponse;
import com.example.fitnessapp.models.CreateExerciseRequest;
import com.example.fitnessapp.models.CreateTrainingRequest;
import com.example.fitnessapp.models.ExerciseDto;
import com.example.fitnessapp.models.ExercisesPageResponse;
import com.example.fitnessapp.models.LoginRequest;
import com.example.fitnessapp.models.NotificationSettingsResponse;
import com.example.fitnessapp.models.RefreshRequest;
import com.example.fitnessapp.models.RegisterRequest;
import com.example.fitnessapp.models.TrainingDto;
import com.example.fitnessapp.models.TrainingsPageResponse;
import com.example.fitnessapp.models.UpdateNotificationsRequest;
import com.example.fitnessapp.models.UpdateProfileRequest;
import com.example.fitnessapp.models.UserProfileResponse;
import com.example.fitnessapp.models.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ---------- AUTH ----------
    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/refresh")
    Call<AuthResponse> refresh(@Body RefreshRequest request);

    // ---------- PROFILE ----------
    @GET("users/me")
    Call<UserProfileResponse> getProfile();

    @PATCH("users/me")
    Call<Void> updateProfile(@Body UpdateProfileRequest request);

    @PATCH("users/me/notifications")
    Call<Void> updateNotifications(@Body UpdateNotificationsRequest request);

    @GET("users/me/notifications")
    Call<NotificationSettingsResponse> getNotifications();

    // ---------- WEATHER ----------
    @GET("weather")
    Call<WeatherResponse> getWeather(@Query("city") String city);

    // ---------- EXERCISES ----------
    @GET("workouts/exercises")
    Call<ExercisesPageResponse> getExercises(
            @Query("search") String search,
            @Query("workout_type") String workoutType,
            @Query("muscules") String muscules,
            @Query("inventories") String inventories,
            @Query("tags") String tags,
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @GET("workouts/exercises/{id}")
    Call<ExerciseDto> getExerciseById(@Path("id") String id);

    @POST("workouts/exercises")
    Call<ExerciseDto> createExercise(@Body CreateExerciseRequest body);

    @PATCH("workouts/exercises/{id}")
    Call<ExerciseDto> updateExercise(@Path("id") String id, @Body CreateExerciseRequest body);

    @DELETE("workouts/exercises/{id}")
    Call<Void> deleteExercise(@Path("id") String id);

    // ---------- TRAININGS ----------
    @GET("workouts/trainings")
    Call<TrainingsPageResponse> getTrainings(
            @Query("date_from") String dateFrom,
            @Query("date_to") String dateTo,
            @Query("search") String search,
            @Query("limit") int limit,
            @Query("offset") int offset
    );

    @GET("workouts/trainings/{id}")
    Call<TrainingDto> getTrainingById(@Path("id") String id);

    @POST("workouts/trainings")
    Call<TrainingDto> createTraining(@Body CreateTrainingRequest body);
}