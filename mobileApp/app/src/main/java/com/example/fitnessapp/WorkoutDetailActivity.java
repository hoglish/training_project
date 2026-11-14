package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.fitnessapp.models.ExerciseDto;
import com.example.fitnessapp.models.InventoryDto;
import com.example.fitnessapp.models.MuscleDto;
import com.example.fitnessapp.models.TagDto;
import com.example.fitnessapp.network.ApiClient;
import com.example.fitnessapp.network.ApiService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkoutDetailActivity extends AppCompatActivity {

    private ApiService api;
    private String exerciseId;

    private ImageView ivAnimation;
    private TextView tvDetailName, tvDetailType, tvDetailMuscles, tvDetailInventory, tvAnimationNote;
    private ChipGroup cgDetailTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_workout_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        api = ApiClient.getApiService(this);
        exerciseId = getIntent().getStringExtra("exercise_id");

        ivAnimation      = findViewById(R.id.ivAnimation);
        tvDetailName     = findViewById(R.id.tvDetailName);
        tvDetailType     = findViewById(R.id.tvDetailType);
        tvDetailMuscles  = findViewById(R.id.tvDetailMuscles);
        tvDetailInventory= findViewById(R.id.tvDetailInventory);
        tvAnimationNote  = findViewById(R.id.tvAnimationNote);
        cgDetailTags     = findViewById(R.id.cgDetailTags);

        // Кнопки
        findViewById(R.id.btnStartWithWorkout).setOnClickListener(v -> {
            Intent intent = new Intent(this, ActiveTrainingActivity.class);
            intent.putExtra("exercise_id", exerciseId); // передаём ID, чтобы начать тренировку именно с него
            startActivity(intent);
        });

        findViewById(R.id.btnAddToTemplate).setOnClickListener(v -> {
            Toast.makeText(this, "Добавление в шаблон в разработке", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnEditWorkout).setOnClickListener(v -> {
            Toast.makeText(this, "Редактирование в разработке", Toast.LENGTH_SHORT).show();
        });

        if (exerciseId != null) {
            loadExerciseDetails();
        } else {
            Toast.makeText(this, "Ошибка: ID упражнения не передан", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadExerciseDetails() {
        api.getExerciseById(exerciseId).enqueue(new Callback<ExerciseDto>() {
            @Override
            public void onResponse(Call<ExerciseDto> call, Response<ExerciseDto> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(WorkoutDetailActivity.this, "Не удалось загрузить данные", Toast.LENGTH_SHORT).show();
                    return;
                }

                ExerciseDto ex = response.body();

                // Основное
                tvDetailName.setText(ex.name);
                tvDetailType.setText(ex.workout_type != null ? ex.workout_type : "—");

                // Мышцы
                if (ex.muscules != null && !ex.muscules.isEmpty()) {
                    List<String> names = new ArrayList<>();
                    for (MuscleDto m : ex.muscules) names.add(m.name);
                    tvDetailMuscles.setText(String.join(", ", names));
                } else {
                    tvDetailMuscles.setText("Не указаны");
                }

                // Инвентарь
                if (ex.inventories != null && !ex.inventories.isEmpty()) {
                    List<String> names = new ArrayList<>();
                    for (InventoryDto i : ex.inventories) names.add(i.name);
                    tvDetailInventory.setText(String.join(", ", names));
                } else {
                    tvDetailInventory.setText("Не требуется");
                }

                // Теги (создаём чипсы программно)
                cgDetailTags.removeAllViews();
                if (ex.tags != null && !ex.tags.isEmpty()) {
                    for (TagDto tag : ex.tags) {
                        Chip chip = new Chip(WorkoutDetailActivity.this);
                        chip.setText(tag.name);
                        chip.setCheckable(false);
                        cgDetailTags.addView(chip);
                    }
                }

                // Анимация
                if (ex.animation != null && !ex.animation.isEmpty()) {
                    tvAnimationNote.setText("Анимация: " + ex.animation);
                } else {
                    tvAnimationNote.setText("Анимация не загружена");
                }
                ivAnimation.setImageResource(R.drawable.gym); // Заглушка картинки
            }

            @Override
            public void onFailure(Call<ExerciseDto> call, Throwable t) {
                Toast.makeText(WorkoutDetailActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}