package com.example.fitnessapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.fitnessapp.local.AppDatabase;
import com.example.fitnessapp.local.UserProfileEntity;
import com.example.fitnessapp.models.NotificationSettingsResponse;
import com.example.fitnessapp.models.UpdateNotificationsRequest;
import com.example.fitnessapp.models.UpdateProfileRequest;
import com.example.fitnessapp.models.UserProfileResponse;
import com.example.fitnessapp.network.ApiClient;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountPageActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etBirthDate, etHeight, etWeight, etQuietStart, etQuietEnd;
    private RadioGroup rgSex;
    private Spinner spMainGoal, spNormGoal, spActivity;
    private Button btnSave, btnLogout, btnBecomeTrainer, btnCalcNorm;
    private TextView tvBmiValue, tvBmiCategory, tvCalories, tvProtein, tvFat, tvCarbs;
    private SwitchCompat swRemindTraining, swRemindMeals, swRemindWater;

    private ApiService apiService;
    private SessionManager session;
    private AppDatabase db;
    private String currentUserId;

    private String[] goalsList = {"похудеть", "набрать массу", "поддерживать форму"};
    private String[] activityList = {"Минимальная", "Низкая", "Средняя", "Высокая", "Очень высокая"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_page);

        session = new SessionManager(this);
        currentUserId = session.getUserId();
        apiService = ApiClient.getApiService(this);
        db = AppDatabase.getInstance(this);

        etEmail = findViewById(R.id.etEmail);
        etBirthDate = findViewById(R.id.etBirthDate);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        etQuietStart = findViewById(R.id.etQuietStart);
        etQuietEnd = findViewById(R.id.etQuietEnd);

        rgSex = findViewById(R.id.rgSex);
        spMainGoal = findViewById(R.id.spMainGoal);
        spNormGoal = findViewById(R.id.spNormGoal);
        spActivity = findViewById(R.id.spActivity);

        btnSave = findViewById(R.id.button2);
        btnLogout = findViewById(R.id.button);
        btnBecomeTrainer = findViewById(R.id.btnBecomeTrainer);
        btnCalcNorm = findViewById(R.id.btnCalcNorm);

        swRemindTraining = findViewById(R.id.swRemindTraining);
        swRemindMeals = findViewById(R.id.swRemindMeals);
        swRemindWater = findViewById(R.id.swRemindWater);

        tvBmiValue = findViewById(R.id.tvBmiValue);
        tvBmiCategory = findViewById(R.id.tvBmiCategory);
        tvCalories = findViewById(R.id.tvCalories);
        tvProtein = findViewById(R.id.tvProtein);
        tvFat = findViewById(R.id.tvFat);
        tvCarbs = findViewById(R.id.tvCarbs);

        setupSpinners();

        etBirthDate.setOnClickListener(v -> showDatePicker());
        etQuietStart.setOnClickListener(v -> showTimePicker(etQuietStart));
        etQuietEnd.setOnClickListener(v -> showTimePicker(etQuietEnd));

        View.OnFocusChangeListener updateBmiListener = (v, hasFocus) -> calculateBmi();
        etHeight.setOnFocusChangeListener(updateBmiListener);
        etWeight.setOnFocusChangeListener(updateBmiListener);

        btnSave.setOnClickListener(v -> saveProfile());
        btnBecomeTrainer.setOnClickListener(v -> Toast.makeText(this, "Функция в разработке", Toast.LENGTH_SHORT).show());
        btnLogout.setOnClickListener(v -> logout());
        btnCalcNorm.setOnClickListener(v -> calculateNutrition());

        loadProfile();

        ImageButton btnTraining = findViewById(R.id.imageButton2);
        btnTraining.setOnClickListener(v -> {
            startActivity(new Intent(AccountPageActivity.this, TrainingPageActivity.class));
        });
    }

    private void setupSpinners() {
        ArrayAdapter<String> goalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, goalsList);
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMainGoal.setAdapter(goalAdapter);
        spNormGoal.setAdapter(goalAdapter);

        ArrayAdapter<String> activityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, activityList);
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spActivity.setAdapter(activityAdapter);
    }

    private void loadProfile() {
        new Thread(() -> {
            UserProfileEntity local = db.userProfileDao().getProfile(currentUserId);
            if (local != null) {
                runOnUiThread(() -> displayProfile(local));
            }
        }).start();

        apiService.getProfile().enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfileResponse p = response.body();
                    UserProfileEntity entity = new UserProfileEntity();
                    entity.id = currentUserId;
                    entity.login = p.getLogin();
                    entity.email = p.getEmail();
                    entity.gender = p.getGender();
                    entity.height = p.getHeight();
                    entity.weight = p.getWeight();
                    entity.goal = p.getGoal();
                    entity.dateOfBirthday = p.getDate_of_birthday();

                    new Thread(() -> {
                        db.userProfileDao().insert(entity);
                        runOnUiThread(() -> displayProfile(entity));
                    }).start();
                }
            }
            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {}
        });

        loadNotifications();
    }

    private void loadNotifications() {
        apiService.getNotifications().enqueue(new Callback<NotificationSettingsResponse>() {
            @Override
            public void onResponse(Call<NotificationSettingsResponse> call, Response<NotificationSettingsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    NotificationSettingsResponse n = response.body();
                    swRemindTraining.setChecked(n.isRemind_training());
                    swRemindMeals.setChecked(n.isRemind_meals());
                    swRemindWater.setChecked(n.isRemind_water());

                    if (n.getQuiet_mode_start() != null) {
                        String start = n.getQuiet_mode_start().length() > 5 ? n.getQuiet_mode_start().substring(0, 5) : n.getQuiet_mode_start();
                        etQuietStart.setText(start);
                    }
                    if (n.getQuiet_mode_end() != null) {
                        String end = n.getQuiet_mode_end().length() > 5 ? n.getQuiet_mode_end().substring(0, 5) : n.getQuiet_mode_end();
                        etQuietEnd.setText(end);
                    }
                }
            }
            @Override
            public void onFailure(Call<NotificationSettingsResponse> call, Throwable t) {}
        });
    }

    private void displayProfile(UserProfileEntity profile) {
        etEmail.setText(profile.email != null ? profile.email : "");
        etHeight.setText(profile.height != null ? String.valueOf(profile.height) : "");
        etWeight.setText(profile.weight != null ? String.valueOf(profile.weight) : "");

        if (profile.dateOfBirthday != null && profile.dateOfBirthday.length() > 10) {
            etBirthDate.setText(profile.dateOfBirthday.substring(0, 10));
        } else {
            etBirthDate.setText(profile.dateOfBirthday);
        }

        if ("м".equals(profile.gender)) rgSex.check(R.id.rbMale);
        else if ("ж".equals(profile.gender)) rgSex.check(R.id.rbFemale);

        if (profile.goal != null) {
            for (int i = 0; i < goalsList.length; i++) {
                if (goalsList[i].equals(profile.goal)) {
                    spMainGoal.setSelection(i);
                    spNormGoal.setSelection(i);
                    break;
                }
            }
        }
        calculateBmi();
    }

    private void calculateBmi() {
        try {
            String hStr = etHeight.getText().toString();
            String wStr = etWeight.getText().toString();
            if (hStr.isEmpty() || wStr.isEmpty()) return;

            float h = Float.parseFloat(hStr) / 100f;
            float w = Float.parseFloat(wStr);
            if (h == 0) return;

            float bmi = w / (h * h);
            tvBmiValue.setText(String.format(java.util.Locale.US, "%.1f", bmi));

            String category;
            if (bmi < 18.5) category = "Недостаточный вес";
            else if (bmi < 25) category = "Норма";
            else if (bmi < 30) category = "Избыточный вес";
            else category = "Ожирение";

            tvBmiCategory.setText(category);
        } catch (Exception ignored) {}
    }

    private void calculateNutrition() {
        try {
            float weight = Float.parseFloat(etWeight.getText().toString());
            float height = Float.parseFloat(etHeight.getText().toString());

            int age = 25;
            String birth = etBirthDate.getText().toString();
            if (birth.length() == 10) {
                int year = Integer.parseInt(birth.substring(0, 4));
                age = Calendar.getInstance().get(Calendar.YEAR) - year;
            }

            boolean isMale = rgSex.getCheckedRadioButtonId() == R.id.rbMale;
            double bmr;
            if (isMale) {
                bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
            } else {
                bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
            }

            double[] activityCoef = {1.2, 1.375, 1.55, 1.725, 1.9};
            int actIndex = spActivity.getSelectedItemPosition();
            if (actIndex < 0) actIndex = 2;

            double calories = bmr * activityCoef[actIndex];
            String goal = spNormGoal.getSelectedItem().toString();
            if (goal.equals("похудеть")) calories -= 500;
            else if (goal.equals("набрать массу")) calories += 500;

            double protein = (calories * 0.3) / 4;
            double fat = (calories * 0.3) / 9;
            double carbs = (calories * 0.4) / 4;

            tvCalories.setText(String.format(java.util.Locale.US, "%.0f", calories));
            tvProtein.setText(String.format(java.util.Locale.US, "%.0f", protein));
            tvFat.setText(String.format(java.util.Locale.US, "%.0f", fat));
            tvCarbs.setText(String.format(java.util.Locale.US, "%.0f", carbs));

        } catch (Exception e) {
            Toast.makeText(this, "Заполните рост, вес и пол", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfile() {
        UpdateProfileRequest request = new UpdateProfileRequest();

        String email = etEmail.getText().toString().trim();
        if (!email.isEmpty()) request.setEmail(email);

        String birthDate = etBirthDate.getText().toString().trim();
        if (!birthDate.isEmpty()) request.setDate_of_birthday(birthDate);

        int selectedSex = rgSex.getCheckedRadioButtonId();
        if (selectedSex == R.id.rbMale) request.setGender("м");
        else if (selectedSex == R.id.rbFemale) request.setGender("ж");

        String heightStr = etHeight.getText().toString().trim();
        if (!heightStr.isEmpty()) request.setHeight(Integer.parseInt(heightStr));

        String weightStr = etWeight.getText().toString().trim();
        if (!weightStr.isEmpty()) request.setWeight(Float.parseFloat(weightStr));

        if (spMainGoal.getSelectedItem() != null) {
            request.setGoal(spMainGoal.getSelectedItem().toString());
        }

        btnSave.setEnabled(false);
        btnSave.setText("Сохранение...");

        apiService.updateProfile(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnSave.setEnabled(true);
                btnSave.setText("Сохранить изменения");

                if (response.isSuccessful()) {
                    new Thread(() -> {
                        UserProfileEntity entity = db.userProfileDao().getProfile(currentUserId);
                        if (entity == null) entity = new UserProfileEntity();
                        entity.id = currentUserId;
                        entity.email = request.getEmail();
                        entity.height = request.getHeight();
                        entity.weight = request.getWeight();
                        entity.goal = request.getGoal();
                        entity.gender = request.getGender();
                        entity.dateOfBirthday = request.getDate_of_birthday();
                        db.userProfileDao().insert(entity);
                        runOnUiThread(() -> {
                            Toast.makeText(AccountPageActivity.this, "Профиль обновлен!", Toast.LENGTH_SHORT).show();
                            saveNotifications();
                        });
                    }).start();
                } else {
                    Toast.makeText(AccountPageActivity.this, "Ошибка сервера", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnSave.setEnabled(true);
                btnSave.setText("Сохранить изменения");
                Toast.makeText(AccountPageActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveNotifications() {
        UpdateNotificationsRequest request = new UpdateNotificationsRequest();
        request.setRemind_training(swRemindTraining.isChecked());
        request.setRemind_meals(swRemindMeals.isChecked());
        request.setRemind_water(swRemindWater.isChecked());

        String start = etQuietStart.getText().toString().trim();
        String end = etQuietEnd.getText().toString().trim();
        if (!start.isEmpty()) request.setQuiet_mode_start(start);
        if (!end.isEmpty()) request.setQuiet_mode_end(end);

        apiService.updateNotifications(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(AccountPageActivity.this, "Уведомления сохранены!", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(AccountPageActivity.this, "Ошибка сохранения уведомлений", Toast.LENGTH_SHORT).show());
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void logout() {
        session.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            String date = String.format(java.util.Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            etBirthDate.setText(date);
        }, year, month, day);
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void showTimePicker(TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            String time = String.format(java.util.Locale.US, "%02d:%02d", hourOfDay, minute1);
            editText.setText(time);
        }, hour, minute, true);
        timePickerDialog.show();
    }


}