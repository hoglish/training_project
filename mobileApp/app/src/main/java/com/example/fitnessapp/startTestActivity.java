package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

        RadioGroup rgGender = findViewById(R.id.rgGender);
        EditText etHeight = findViewById(R.id.etHeight);
        EditText etWeight = findViewById(R.id.etWeight);
        Button btnBodyNext = findViewById(R.id.btnBodyNext);
        Button btnFinish = findViewById(R.id.btnFinish);

        EditText etLogin = findViewById(R.id.etLogin);
        EditText etPassword = findViewById(R.id.etPassword);


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

            int height = Integer.parseInt(heightStr);
            float weight = Float.parseFloat(weightStr);

            if (height < 40) {
                Toast.makeText(startTestActivity.this, "Рост должен быть не менее 40 см", Toast.LENGTH_SHORT).show();
                return;
            }

            if (weight <= 0) {
                Toast.makeText(startTestActivity.this, "Вес должен быть больше 0", Toast.LENGTH_SHORT).show();
                return;
            }
            requestData.gender = (selectedGender == R.id.rbMale) ? "м" : "ж";
            requestData.height = height;
            requestData.weight = weight;


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





        });




    }
}