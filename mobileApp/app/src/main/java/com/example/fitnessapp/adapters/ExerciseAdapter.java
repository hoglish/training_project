package com.example.fitnessapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.models.ExerciseDto;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.VH> {

    public interface Listener {
        void onAdd(ExerciseDto exercise);
        void onOpen(ExerciseDto exercise);
    }

    private final List<ExerciseDto> items = new ArrayList<>();
    private final Listener listener;

    public ExerciseAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<ExerciseDto> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void addItems(List<ExerciseDto> more) {
        if (more == null || more.isEmpty()) return;
        int start = items.size();
        items.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    public int size() {
        return items.size();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ExerciseDto item = items.get(position);

        h.tvName.setText(item.name);
        h.tvMeta.setText(item.workout_type != null ? item.workout_type : "");
        h.tvInventory.setVisibility(View.GONE);
        h.tvOutdoor.setVisibility(View.GONE);

        // Скрываем кнопку плюса
        h.btnAdd.setVisibility(View.GONE);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOpen(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvMeta, tvInventory, tvOutdoor;
        MaterialButton btnAdd;

        VH(@NonNull View itemView) {
            super(itemView);
            tvName      = itemView.findViewById(R.id.tvWorkoutName);
            tvMeta      = itemView.findViewById(R.id.tvWorkoutMeta);
            tvInventory = itemView.findViewById(R.id.tvWorkoutInventory);
            tvOutdoor   = itemView.findViewById(R.id.tvWorkoutOutdoor);
            btnAdd      = itemView.findViewById(R.id.btnAddToTraining);
        }
    }
}