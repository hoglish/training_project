package com.example.fitnessapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessapp.models.CreateExerciseRequest;
import com.example.fitnessapp.models.ExerciseDto;
import com.example.fitnessapp.network.ApiClient;
import com.example.fitnessapp.network.ApiService;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditWorkoutActivity extends AppCompatActivity {

    private ApiService api;

    private TextInputEditText etWorkoutName;
    private Spinner spWorkoutType;
    private Button btnSaveWorkout;
    private TextView tvEditError;

    private final String[] types = {
            "базовое", "изолирующее", "калистеника", "кардио",
            "бег", "йога", "растяжка"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_workout);

        api = ApiClient.getApiService(this);

        etWorkoutName = findViewById(R.id.etWorkoutName);
        spWorkoutType = findViewById(R.id.spWorkoutType);
        btnSaveWorkout = findViewById(R.id.btnSaveWorkout);
        tvEditError = findViewById(R.id.tvEditError);

        // Настраиваем выпадающий список типов
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spWorkoutType.setAdapter(adapter);

        // Кнопка сохранить
        btnSaveWorkout.setOnClickListener(v -> saveWorkout());

        // Кнопка удалить (скрываем, так как мы создаем новое)
        findViewById(R.id.btnDeleteWorkout).setVisibility(android.view.View.GONE);
    }

    private void saveWorkout() {
        String name = etWorkoutName.getText() != null ? etWorkoutName.getText().toString().trim() : "";

        if (name.isEmpty()) {
            showError("Введите название упражнения");
            return;
        }

        String type = spWorkoutType.getSelectedItem().toString();

        btnSaveWorkout.setEnabled(false);
        btnSaveWorkout.setText("Сохранение...");
        tvEditError.setVisibility(android.view.View.GONE);

        // Собираем запрос (мышцы и инвентарь пока передаем пустыми массивами)
        CreateExerciseRequest request = new CreateExerciseRequest(
                name,
                type,
                new ArrayList<>(), // muscules
                new ArrayList<>(), // inventories
                new ArrayList<>()  // tags
        );

        api.createExercise(request).enqueue(new Callback<ExerciseDto>() {
            @Override
            public void onResponse(Call<ExerciseDto> call, Response<ExerciseDto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditWorkoutActivity.this, "Упражнение создано!", Toast.LENGTH_SHORT).show();
                    finish(); // Закрываем экран и возвращаемся в каталог
                } else {
                    btnSaveWorkout.setEnabled(true);
                    btnSaveWorkout.setText("Сохранить");
                    showError("Ошибка: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ExerciseDto> call, Throwable t) {
                btnSaveWorkout.setEnabled(true);
                btnSaveWorkout.setText("Сохранить");
                showError("Сеть: " + t.getMessage());
            }
        });
    }

    private void showError(String msg) {
        tvEditError.setText(msg);
        tvEditError.setVisibility(android.view.View.VISIBLE);
    }
}