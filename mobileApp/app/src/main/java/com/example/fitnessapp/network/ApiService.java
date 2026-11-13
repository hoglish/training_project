package com.example.fitnessapp.network;

import com.example.fitnessapp.models.AuthResponse;
import com.example.fitnessapp.models.LoginRequest;
import com.example.fitnessapp.models.NotificationSettingsResponse;
import com.example.fitnessapp.models.RefreshRequest;
import com.example.fitnessapp.models.RegisterRequest;
import com.example.fitnessapp.models.UpdateNotificationsRequest;
import com.example.fitnessapp.models.UpdateProfileRequest;
import com.example.fitnessapp.models.UserProfileResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;

public interface ApiService {
    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/refresh")
    Call<AuthResponse> refresh(@Body RefreshRequest request);

    @GET("users/me")
    Call<UserProfileResponse> getProfile();

    @PATCH("users/me")
    Call<Void> updateProfile(@Body UpdateProfileRequest request);

    @PATCH("users/me/notifications")
    Call<Void> updateNotifications(@Body UpdateNotificationsRequest request);

    @GET("users/me/notifications")
    Call<NotificationSettingsResponse> getNotifications();
}