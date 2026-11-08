package com.example.fitnessapp.network;

import com.example.fitnessapp.models.AuthResponse;
import com.example.fitnessapp.models.LoginRequest;
import com.example.fitnessapp.models.RefreshRequest;
import com.example.fitnessapp.models.RegisterRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/refresh")
    Call<AuthResponse> refresh(@Body RefreshRequest request);

    @GET("auth/check-login")
    Call<Void> checkLogin(@Query("login") String login);
}