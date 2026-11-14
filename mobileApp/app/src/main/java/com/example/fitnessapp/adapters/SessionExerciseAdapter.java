package com.example.fitnessapp.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.models.ExerciseDto;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class SessionExerciseAdapter extends RecyclerView.Adapter<SessionExerciseAdapter.VH> {

    private final List<ExerciseDto> items;

    public SessionExerciseAdapter(List<ExerciseDto> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session_exercise, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ExerciseDto item = items.get(position);
        h.tvExerciseName.setText(item.name);

        // Если подходов еще нет — добавляем первый
        if (h.llSets.getChildCount() == 0) {
            addSetRow(h.llSets, 1);
        }

        // Кнопка "+ подход"
        h.btnAddSet.setOnClickListener(v -> {
            int newSetNumber = h.llSets.getChildCount() + 1;
            addSetRow(h.llSets, newSetNumber);
        });

        // Удаление упражнения
        h.btnRemoveExercise.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                items.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, items.size());
            }
        });
    }

    // Создает строку для ввода веса и повторений
    private void addSetRow(LinearLayout container, int setNumber) {
        View row = LayoutInflater.from(container.getContext())
                .inflate(R.layout.item_exercise_set, container, false);

        TextView tvSetNumber = row.findViewById(R.id.tvSetNumber);
        tvSetNumber.setText(String.valueOf(setNumber));

        TextView btnRemoveSet = row.findViewById(R.id.btnRemoveSet);
        btnRemoveSet.setOnClickListener(v -> container.removeView(row));

        container.addView(row);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvExerciseName;
        TextView btnRemoveExercise;
        LinearLayout llSets;
        MaterialButton btnAddSet;

        VH(@NonNull View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            btnRemoveExercise = itemView.findViewById(R.id.btnRemoveExercise);
            llSets = itemView.findViewById(R.id.llSets);
            btnAddSet = itemView.findViewById(R.id.btnAddSet);
        }
    }
}