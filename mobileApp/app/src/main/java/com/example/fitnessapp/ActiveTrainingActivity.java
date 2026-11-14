package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.adapters.SessionExerciseAdapter;
import com.example.fitnessapp.models.CreateTrainingRequest;
import com.example.fitnessapp.models.ExerciseDto;
import com.example.fitnessapp.models.ExerciseSetDto;
import com.example.fitnessapp.models.TrainingDto;
import com.example.fitnessapp.network.ApiClient;
import com.example.fitnessapp.network.ApiService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActiveTrainingActivity extends AppCompatActivity {
    private ApiService api;
    private TextView tvSessionTitle, tvSessionTimer, tvSessionSets;
    private Button btnAddExercise, btnFinish;
    private EditText etSessionNote;
    private RecyclerView rvSessionExercises;
    private SessionExerciseAdapter sessionAdapter;

    private int secondsElapsed = 0;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    private final List<ExerciseSetDto> allSets = new ArrayList<>();
    private final List<ExerciseDto> addedExercises = new ArrayList<>();

    private final ActivityResultLauncher<Intent> pickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String id = result.getData().getStringExtra("selected_exercise_id");
                    String name = result.getData().getStringExtra("selected_exercise_name");
                    if (id != null && name != null) addExerciseToScreen(id, name);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_training);

        api = ApiClient.getApiService(this);
        tvSessionTitle = findViewById(R.id.tvSessionTitle);
        tvSessionTimer = findViewById(R.id.tvSessionTimer);
        tvSessionSets = findViewById(R.id.tvSessionSets);
        etSessionNote = findViewById(R.id.etSessionNote);
        btnAddExercise = findViewById(R.id.btnAddExercise);
        btnFinish = findViewById(R.id.btnFinish);
        rvSessionExercises = findViewById(R.id.rvSessionExercises);

        rvSessionExercises.setLayoutManager(new LinearLayoutManager(this));
        sessionAdapter = new SessionExerciseAdapter(addedExercises);
        rvSessionExercises.setAdapter(sessionAdapter);

        startTimer();
        btnAddExercise.setOnClickListener(v -> pickerLauncher.launch(new Intent(this, SelectedExerciseActivity.class)));
        btnFinish.setOnClickListener(v -> finishTraining());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                secondsElapsed++;
                tvSessionTimer.setText(String.format(Locale.US, "⏱ %02d:%02d", secondsElapsed / 60, secondsElapsed % 60));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void addExerciseToScreen(String id, String name) {
        ExerciseDto exercise = new ExerciseDto();
        exercise.id = id;
        exercise.name = name;
        addedExercises.add(exercise);
        for (int i = 1; i <= 3; i++) allSets.add(new ExerciseSetDto(exercise.id, i, 10, 50.0));
        sessionAdapter.notifyDataSetChanged();
        tvSessionSets.setText(allSets.size() + " подходов");
    }

    private void finishTraining() {
        if (allSets.isEmpty()) {
            Toast.makeText(this, "Добавьте упражнение", Toast.LENGTH_SHORT).show();
            return;
        }
        btnFinish.setEnabled(false);
        btnFinish.setText("Сохранение...");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String now = sdf.format(new Date());
        long startMillis = System.currentTimeMillis() - (secondsElapsed * 1000L);
        String startTime = sdf.format(new Date(startMillis));

        // Считаем калории: минуты * 7
        int calories = Math.max(7, (secondsElapsed / 60) * 7);

        CreateTrainingRequest request = new CreateTrainingRequest(
                "Тренировка " + new SimpleDateFormat("dd.MM", Locale.US).format(new Date()),
                startTime,
                now,
                "завершена",
                4,
                etSessionNote.getText().toString(),
                allSets
        );
        request.calories = calories; // Передаём калории

        api.createTraining(request).enqueue(new Callback<TrainingDto>() {
            @Override
            public void onResponse(Call<TrainingDto> call, Response<TrainingDto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ActiveTrainingActivity.this, "Тренировка сохранена!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    btnFinish.setEnabled(true);
                    btnFinish.setText("Завершить");
                    Toast.makeText(ActiveTrainingActivity.this, "Ошибка: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<TrainingDto> call, Throwable t) {
                btnFinish.setEnabled(true);
                btnFinish.setText("Завершить");
                Toast.makeText(ActiveTrainingActivity.this, "Сеть: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}