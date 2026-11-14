package com.example.fitnessapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.adapters.ExerciseAdapter;
import com.example.fitnessapp.models.ExerciseDto;
import com.example.fitnessapp.models.ExercisesPageResponse;
import com.example.fitnessapp.network.ApiClient;
import com.example.fitnessapp.network.ApiService;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectedExerciseActivity extends AppCompatActivity {
    private ApiService api;
    private ExerciseAdapter adapter;
    private TextInputEditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_exercise); // Проверь название XML-файла!

        api = ApiClient.getApiService(this);
        etSearch = findViewById(R.id.etSearchSelect);
        RecyclerView rv = findViewById(R.id.rvSelectExercises);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ExerciseAdapter(new ExerciseAdapter.Listener() {
            @Override
            public void onAdd(ExerciseDto exercise) { /* не используется */ }

            @Override
            public void onOpen(ExerciseDto exercise) {
                // Возвращаем выбранное упражнение назад в ActiveTrainingActivity
                Intent result = new Intent();
                result.putExtra("selected_exercise_id", exercise.id);
                result.putExtra("selected_exercise_name", exercise.name);
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });
        rv.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                loadExercises(s.toString().trim());
            }
        });

        loadExercises("");
    }

    private void loadExercises(String search) {
        api.getExercises(search.isEmpty() ? null : search, null, null, null, null, 50, 0)
                .enqueue(new Callback<ExercisesPageResponse>() {
                    @Override
                    public void onResponse(Call<ExercisesPageResponse> call, Response<ExercisesPageResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<ExerciseDto> data = response.body().data != null ? response.body().data : new ArrayList<>();
                            adapter.setItems(data);
                        }
                    }
                    @Override
                    public void onFailure(Call<ExercisesPageResponse> call, Throwable t) {
                        Toast.makeText(SelectedExerciseActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}