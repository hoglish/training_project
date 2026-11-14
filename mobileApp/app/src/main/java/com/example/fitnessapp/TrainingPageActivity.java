package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnessapp.adapters.DayWeatherAdapter;
import com.example.fitnessapp.adapters.ExerciseAdapter;
import com.example.fitnessapp.adapters.HourWeatherAdapter;
import com.example.fitnessapp.adapters.TrainingAdapter;
import com.example.fitnessapp.models.ExerciseDto;
import com.example.fitnessapp.models.ExercisesPageResponse;
import com.example.fitnessapp.models.TrainingDto;
import com.example.fitnessapp.models.TrainingsPageResponse;
import com.example.fitnessapp.models.WeatherResponse;
import com.example.fitnessapp.network.ApiClient;
import com.example.fitnessapp.network.ApiService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrainingPageActivity extends AppCompatActivity {
    private static final int PAGE_SIZE = 5;
    private ApiService api;
    private TextView tvTempNow, tvCondition, tvPrecip, tvTempNight, tvCity, tvWeatherUpdated;
    private HourWeatherAdapter hourAdapter;
    private DayWeatherAdapter dayAdapter;
    private ExerciseAdapter exerciseAdapter;
    private TextInputEditText etSearchWorkout;
    private TextView tvCatalogEmpty;
    private RecyclerView rvWorkouts;
    private ChipGroup cgType;
    private String currentTypeFilter = null;
    private int currentOffset = 0;
    private boolean isLoading = false;
    private boolean noMoreData = false;
    private final Handler debounce = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;
    private TrainingAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_training_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        api = ApiClient.getApiService(this);
        setupTabs();
        setupBottomBar();
        setupWeather();
        setupCatalog();
        setupHistory();
        findViewById(R.id.fabAdd).setOnClickListener(v -> startActivity(new Intent(this, ActiveTrainingActivity.class)));
    }

    private void setupTabs() {
        TabLayout tab = findViewById(R.id.tabTraining);
        View incToday = findViewById(R.id.incToday);
        View incPlan = findViewById(R.id.incPlan);
        View incCatalog = findViewById(R.id.incCatalog);
        View incTemplates = findViewById(R.id.incTemplates);
        View incHistory = findViewById(R.id.incHistory);
        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab t) {
                incToday.setVisibility(View.GONE); incPlan.setVisibility(View.GONE);
                incCatalog.setVisibility(View.GONE); incTemplates.setVisibility(View.GONE); incHistory.setVisibility(View.GONE);
                switch (t.getPosition()) {
                    case 0: incToday.setVisibility(View.VISIBLE); break;
                    case 1: incPlan.setVisibility(View.VISIBLE); break;
                    case 2: incCatalog.setVisibility(View.VISIBLE); break;
                    case 3: incTemplates.setVisibility(View.VISIBLE); break;
                    case 4: incHistory.setVisibility(View.VISIBLE); loadHistory(); break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab t) {}
            @Override public void onTabReselected(TabLayout.Tab t) {}
        });
    }

    private void setupBottomBar() {
        View btnHome = findViewById(R.id.imageButton);
        if (btnHome != null) btnHome.setOnClickListener(v -> startActivity(new Intent(this, HomePageActivity.class)));
        View btnAi = findViewById(R.id.imageButton3);
        if (btnAi != null) btnAi.setOnClickListener(v -> Toast.makeText(this, "В разработке", Toast.LENGTH_SHORT).show());
        View btnTrackers = findViewById(R.id.imageButton4);
        if (btnTrackers != null) btnTrackers.setOnClickListener(v -> Toast.makeText(this, "В разработке", Toast.LENGTH_SHORT).show());
        View btnAccount = findViewById(R.id.imageButton5);
        if (btnAccount != null) btnAccount.setOnClickListener(v -> startActivity(new Intent(this, AccountPageActivity.class)));
    }

    private void setupWeather() {
        tvCity = findViewById(R.id.tvCity); tvTempNow = findViewById(R.id.tvTempNow);
        tvCondition = findViewById(R.id.tvCondition); tvPrecip = findViewById(R.id.tvPrecip);
        tvTempNight = findViewById(R.id.tvTempNight); tvWeatherUpdated = findViewById(R.id.tvWeatherUpdated);
        RecyclerView rvHour = findViewById(R.id.rvHourWeather); RecyclerView rvWeek = findViewById(R.id.rvWeekWeather);
        hourAdapter = new HourWeatherAdapter(); dayAdapter = new DayWeatherAdapter();
        rvHour.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)); rvHour.setAdapter(hourAdapter);
        rvWeek.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)); rvWeek.setAdapter(dayAdapter);
        tvCity.setOnClickListener(v -> loadWeather());
        findViewById(R.id.btnWeatherRetry).setOnClickListener(v -> loadWeather());
        loadWeather();
    }

    private void loadWeather() {
        tvWeatherUpdated.setText("обновление...");
        api.getWeather("Москва").enqueue(new Callback<WeatherResponse>() {
            @Override public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (!response.isSuccessful() || response.body() == null) { tvWeatherUpdated.setText("нет данных"); return; }
                WeatherResponse w = response.body();
                if (w.current != null) {
                    tvTempNow.setText(Math.round(w.current.temperature) + "°"); tvCondition.setText(w.current.weather_condition);
                    tvTempNight.setText("Ночью " + Math.round(w.current.temperature_night) + "°");
                    tvPrecip.setText("Осадки " + w.current.precipitation_probability + "%"); tvCity.setText(w.current.city_name + " ▾");
                }
                if (w.hourly != null) hourAdapter.setItems(w.hourly);
                if (w.daily != null) dayAdapter.setItems(w.daily);
                tvWeatherUpdated.setText("обновлено");
            }
            @Override public void onFailure(Call<WeatherResponse> call, Throwable t) { tvWeatherUpdated.setText("нет сети"); }
        });
    }

    private void setupCatalog() {
        View catalog = findViewById(R.id.incCatalog);
        if (catalog == null) return;
        etSearchWorkout = catalog.findViewById(R.id.etSearchWorkout); tvCatalogEmpty = catalog.findViewById(R.id.tvCatalogEmpty);
        rvWorkouts = catalog.findViewById(R.id.rvWorkouts); cgType = catalog.findViewById(R.id.cgType);
        exerciseAdapter = new ExerciseAdapter(new ExerciseAdapter.Listener() {
            @Override public void onAdd(ExerciseDto exercise) {}
            @Override public void onOpen(ExerciseDto exercise) {
                Intent intent = new Intent(TrainingPageActivity.this, WorkoutDetailActivity.class);
                intent.putExtra("exercise_id", exercise.id); startActivity(intent);
            }
        });
        rvWorkouts.setLayoutManager(new LinearLayoutManager(this)); rvWorkouts.setAdapter(exerciseAdapter);
        rvWorkouts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(RecyclerView rv, int dx, int dy) {
                if (dy <= 0 || isLoading || noMoreData) return;
                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm != null && lm.findLastVisibleItemPosition() >= exerciseAdapter.size() - 2) loadExercises(false);
            }
        });
        etSearchWorkout.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (pendingSearch != null) debounce.removeCallbacks(pendingSearch);
                pendingSearch = () -> resetAndLoad(); debounce.postDelayed(pendingSearch, 300);
            }
        });
        cgType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            currentTypeFilter = null;
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));
                if (chip != null && !"Все".equalsIgnoreCase(chip.getText().toString())) currentTypeFilter = chip.getText().toString().toLowerCase();
            }
            resetAndLoad();
        });
        View btnReset = catalog.findViewById(R.id.btnResetFilters);
        if (btnReset != null) btnReset.setOnClickListener(v -> { etSearchWorkout.setText(""); cgType.check(R.id.chipType0); resetAndLoad(); });
        View btnAddCustom = catalog.findViewById(R.id.btnAddWorkout);
        if (btnAddCustom != null) btnAddCustom.setOnClickListener(v -> startActivity(new Intent(this, EditWorkoutActivity.class)));
        resetAndLoad();
    }

    private void resetAndLoad() {
        currentOffset = 0; noMoreData = false; exerciseAdapter.setItems(new ArrayList<>()); loadExercises(true);
    }

    private void loadExercises(boolean reset) {
        if (isLoading) return; isLoading = true;
        String search = etSearchWorkout.getText() != null ? etSearchWorkout.getText().toString().trim() : null;
        if (search != null && search.isEmpty()) search = null;
        api.getExercises(search, currentTypeFilter, null, null, null, PAGE_SIZE, currentOffset).enqueue(new Callback<ExercisesPageResponse>() {
            @Override public void onResponse(Call<ExercisesPageResponse> call, Response<ExercisesPageResponse> response) {
                isLoading = false;
                if (!response.isSuccessful() || response.body() == null) { Toast.makeText(TrainingPageActivity.this, "Ошибка: " + response.code(), Toast.LENGTH_LONG).show(); return; }
                ExercisesPageResponse page = response.body();
                List<ExerciseDto> data = page.data != null ? page.data : new ArrayList<>();
                if (reset) exerciseAdapter.setItems(data); else exerciseAdapter.addItems(data);
                currentOffset += data.size();
                if (page.meta == null || data.isEmpty() || currentOffset >= page.meta.total) noMoreData = true;
                boolean empty = exerciseAdapter.size() == 0;
                tvCatalogEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            }
            @Override public void onFailure(Call<ExercisesPageResponse> call, Throwable t) { isLoading = false; Toast.makeText(TrainingPageActivity.this, "Сеть: " + t.getMessage(), Toast.LENGTH_SHORT).show(); }
        });
    }

    private void setupHistory() {
        View incHistory = findViewById(R.id.incHistory);
        if (incHistory == null) return;
        RecyclerView rvHistory = incHistory.findViewById(R.id.rvHistory);
        if (rvHistory != null) {
            rvHistory.setLayoutManager(new LinearLayoutManager(this));
            historyAdapter = new TrainingAdapter(new TrainingAdapter.Listener() {
                @Override public void onClick(TrainingDto training) {
                    Intent intent = new Intent(TrainingPageActivity.this, TrainingDetailActivity.class);
                    intent.putExtra("training_id", training.id); startActivity(intent);
                }
                @Override public void onEdit(TrainingDto training) {
                    new androidx.appcompat.app.AlertDialog.Builder(TrainingPageActivity.this)
                            .setTitle("Удалить?").setPositiveButton("Да", (d, w) -> Toast.makeText(TrainingPageActivity.this, "В разработке", Toast.LENGTH_SHORT).show())
                            .setNegativeButton("Нет", null).show();
                }
            });
            rvHistory.setAdapter(historyAdapter);
        }
    }

    private void loadHistory() {
        if (historyAdapter == null) return;
        api.getTrainings(null, null, null, 10, 0).enqueue(new Callback<TrainingsPageResponse>() {
            @Override public void onResponse(Call<TrainingsPageResponse> call, Response<TrainingsPageResponse> response) {
                if (response.isSuccessful() && response.body() != null) historyAdapter.setItems(response.body().data);
            }
            @Override public void onFailure(Call<TrainingsPageResponse> call, Throwable t) {}
        });
    }

    @Override protected void onResume() { super.onResume(); loadWeather(); }
}