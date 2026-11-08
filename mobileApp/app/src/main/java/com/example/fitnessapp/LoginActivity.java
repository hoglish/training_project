package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessapp.models.AuthResponse;
import com.example.fitnessapp.models.LoginRequest;
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
                            session.saveTokens(
                                    response.body().getAccessToken(),
                                    response.body().getRefreshToken()
                            );

                            Toast.makeText(LoginActivity.this, "Успешный вход!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();

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