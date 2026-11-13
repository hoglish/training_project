package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessapp.local.AppDatabase;
import com.example.fitnessapp.local.UserProfileEntity;
import com.example.fitnessapp.models.AuthResponse;
import com.example.fitnessapp.models.LoginRequest;
import com.example.fitnessapp.models.UserProfileResponse;
import com.example.fitnessapp.network.ApiClient;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etLogin, etPassword;
    private Button btnLogin;
    private ImageButton btnBack;
    private ApiService apiService;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnBack = findViewById(R.id.btnBack);

        session = new SessionManager(this);
        apiService = ApiClient.getApiService(this);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = etLogin.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (login.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Введите логин и пароль", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnLogin.setEnabled(false);
                btnLogin.setText("Загрузка...");

                apiService.login(new LoginRequest(login, password)).enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Войти");

                        if (response.isSuccessful() && response.body() != null) {
                            AuthResponse auth = response.body();
                            session.saveTokens(auth.getAccessToken(), auth.getRefreshToken());
                            session.saveUserId(auth.getUserId());

                            apiService.getProfile().enqueue(new Callback<UserProfileResponse>() {
                                @Override
                                public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> resp) {
                                    if (resp.isSuccessful() && resp.body() != null) {
                                        UserProfileEntity entity = new UserProfileEntity();
                                        entity.id = auth.getUserId();
                                        entity.login = resp.body().getLogin();
                                        entity.email = resp.body().getEmail();
                                        entity.gender = resp.body().getGender();
                                        entity.height = resp.body().getHeight();
                                        entity.weight = resp.body().getWeight();
                                        entity.goal = resp.body().getGoal();
                                        entity.dateOfBirthday = resp.body().getDate_of_birthday();

                                        new Thread(() -> {
                                            AppDatabase.getInstance(LoginActivity.this).userProfileDao().insert(entity);
                                            runOnUiThread(() -> {
                                                startActivity(new Intent(LoginActivity.this, AccountPageActivity.class));
                                                finish();
                                            });
                                        }).start();
                                    } else {
                                        startActivity(new Intent(LoginActivity.this, AccountPageActivity.class));
                                        finish();
                                    }
                                }

                                @Override
                                public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                                    startActivity(new Intent(LoginActivity.this, AccountPageActivity.class));
                                    finish();
                                }
                            });
                        } else {
                            Toast.makeText(LoginActivity.this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Войти");
                        Toast.makeText(LoginActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}