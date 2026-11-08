package com.example.fitnessapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fitnessapp.models.AuthResponse;
import com.example.fitnessapp.models.RegisterRequest;
import com.example.fitnessapp.network.ApiClient;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.utils.SessionManager;

import java.util.Calendar;

import retrofit2.Callback;

import retrofit2.Call;
import retrofit2.Response;

public class startTestActivity extends AppCompatActivity {
    private UserRegistrationData requestData = new UserRegistrationData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start_test);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout stepWelcome = findViewById(R.id.stepWelcome);
        LinearLayout stepGoal = findViewById(R.id.stepGoal);
        LinearLayout stepBody = findViewById(R.id.stepBody);
        LinearLayout stepAccount = findViewById(R.id.stepAccount);

        Button btnWelcomeNext = findViewById(R.id.btnWelcomeNext);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        ImageButton btnBack = findViewById(R.id.btnBack);
        Button btnGoalNext = findViewById(R.id.btnGoalNext);
        EditText etGoal = findViewById(R.id.etGoal);
        EditText etBirthday = findViewById(R.id.etBirthday);

        RadioGroup rgGender = findViewById(R.id.rgGender);
        EditText etHeight = findViewById(R.id.etHeight);
        EditText etWeight = findViewById(R.id.etWeight);
        Button btnBodyNext = findViewById(R.id.btnBodyNext);
        Button btnFinish = findViewById(R.id.btnFinish);

        EditText etLogin = findViewById(R.id.etLogin);
        EditText etPassword = findViewById(R.id.etPassword);


        etBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        startTestActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                                String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                                etBirthday.setText(date);
                            }
                        }, year, month, day);

                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        btnWelcomeNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stepWelcome.setVisibility(View.GONE);
                stepGoal.setVisibility(View.VISIBLE);
                progressBar.setProgress(2);
            }
        });

        btnGoalNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String goalText = etGoal.getText().toString().trim();
                if(goalText.isEmpty()){
                    Toast.makeText(startTestActivity.this, "Пожалуйста, напишите свою цель", Toast.LENGTH_SHORT).show();
                    return;
                }

                requestData.goal = goalText;
                stepGoal.setVisibility(View.GONE);
                stepBody.setVisibility(View.VISIBLE);
                progressBar.setProgress(3);
            }
        });

        btnBodyNext.setOnClickListener(v ->{
            int selectedGender = rgGender.getCheckedRadioButtonId();
            String heightStr = etHeight.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim();

            if (selectedGender == -1){
                Toast.makeText(this, "Выберите пол", Toast.LENGTH_SHORT).show();
                return;
            }

            if (heightStr.isEmpty() || weightStr.isEmpty()) {
                Toast.makeText(this, "Введите рост и вес", Toast.LENGTH_SHORT).show();
                return;
            }

            int height;
            float weight;
            try {
                height = Integer.parseInt(heightStr);
                weight = Float.parseFloat(weightStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Неверный формат чисел", Toast.LENGTH_SHORT).show();
                return;
            }

            if (height < 100 || height > 250) {
                Toast.makeText(this, "Рост должен быть от 100 до 250 см", Toast.LENGTH_SHORT).show();
                return;
            }

            if (weight < 20 || weight > 300) {
                Toast.makeText(this, "Вес должен быть от 20 до 300 кг", Toast.LENGTH_SHORT).show();
                return;
            }

            String weightFormatted = String.format(java.util.Locale.US, "%.1f", weight);
            weight = Float.parseFloat(weightFormatted);
            String birthday = etBirthday.getText().toString().trim();

            if (birthday.isEmpty()) {
                Toast.makeText(startTestActivity.this, "Выберите дату рождения", Toast.LENGTH_SHORT).show();
                return;
            }

            requestData.gender = (selectedGender == R.id.rbMale) ? "м" : "ж";
            requestData.height = height;
            requestData.weight = weight;
            requestData.date_of_birthday = birthday;


            stepBody.setVisibility(View.GONE);
            stepAccount.setVisibility(View.VISIBLE);
            progressBar.setProgress(4);
        });

        btnFinish.setOnClickListener(v -> {
            String login = etLogin.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (login.isEmpty()) {
                Toast.makeText(startTestActivity.this, "Введите логин", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(startTestActivity.this, "Введите пароль", Toast.LENGTH_SHORT).show();
                return;
            }

            if (login.length() > 30) {
                Toast.makeText(startTestActivity.this, "Логин не должен превышать 30 символов", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(startTestActivity.this, "Пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show();
                return;
            }

            requestData.login = login;
            requestData.password = password;
            RegisterRequest registerRequest = new RegisterRequest(login, password, null);

            registerRequest.setGoal(requestData.goal);
            registerRequest.setGender(requestData.gender);
            registerRequest.setHeight(requestData.height);
            registerRequest.setWeight(requestData.weight);
            registerRequest.setDate_of_birthday(requestData.date_of_birthday);

            btnFinish.setEnabled(false);
            btnFinish.setText("Регистрация...");

            ApiService apiService = ApiClient.getApiService(this);
            apiService.register(registerRequest).enqueue(new Callback<AuthResponse>() {
                @Override
                public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                    btnFinish.setEnabled(true);
                    btnFinish.setText("Начать");

                    if (response.isSuccessful() && response.body() != null) {
                        SessionManager session = new SessionManager(startTestActivity.this);
                        session.saveTokens(
                                response.body().getAccessToken(),
                                response.body().getRefreshToken()
                        );
                        Toast.makeText(startTestActivity.this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(startTestActivity.this, MainActivity.class));
                        finish();
                    } else if (response.code() == 409) {
                        Toast.makeText(startTestActivity.this, "Этот логин уже занят", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(startTestActivity.this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<AuthResponse> call, Throwable t) {
                    btnFinish.setEnabled(true);
                    btnFinish.setText("Начать");
                    Toast.makeText(startTestActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });




        });




    }
}