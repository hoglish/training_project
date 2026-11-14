package com.example.fitnessapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnessapp.adapters.TrainingDetailAdapter;
import com.example.fitnessapp.models.TrainingDto;
import com.example.fitnessapp.network.ApiClient;
import com.example.fitnessapp.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrainingDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_detail);
        ApiService api = ApiClient.getApiService(this);
        String id = getIntent().getStringExtra("training_id");

        RecyclerView rv = findViewById(R.id.rvTrainingDetails);
        rv.setLayoutManager(new LinearLayoutManager(this));
        TrainingDetailAdapter adapter = new TrainingDetailAdapter();
        rv.setAdapter(adapter);

        TextView tvName = findViewById(R.id.tvDetailName);
        TextView tvStats = findViewById(R.id.tvDetailStats);

        if (id != null) {
            api.getTrainingById(id).enqueue(new Callback<TrainingDto>() {
                @Override
                public void onResponse(Call<TrainingDto> call, Response<TrainingDto> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        TrainingDto t = response.body();
                        tvName.setText(t.name);
                        tvStats.setText("🔥 " + (t.calories != null ? t.calories : 0) + " ккал");
                        adapter.setItems(t.exercises);
                    }
                }
                @Override
                public void onFailure(Call<TrainingDto> call, Throwable t) {
                    Toast.makeText(TrainingDetailActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}