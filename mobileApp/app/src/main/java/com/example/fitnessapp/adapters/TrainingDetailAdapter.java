package com.example.fitnessapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnessapp.models.ExerciseSetDto;
import com.example.fitnessapp.models.TrainingExerciseGroup;
import java.util.ArrayList;
import java.util.List;

public class TrainingDetailAdapter extends RecyclerView.Adapter<TrainingDetailAdapter.VH> {
    private final List<TrainingExerciseGroup> items = new ArrayList<>();
    public void setItems(List<TrainingExerciseGroup> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }
    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new VH(v);
    }
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TrainingExerciseGroup ex = items.get(position);
        StringBuilder sb = new StringBuilder("🏋️ " + ex.workout_name + "\n");
        if (ex.sets != null) {
            for (ExerciseSetDto set : ex.sets) {
                sb.append("   • Подход ").append(set.set_number).append(": ").append(set.number_of_repeat).append(" раз х ").append(set.weight).append(" кг\n");
            }
        }
        h.tv.setText(sb.toString());
    }
    @Override public int getItemCount() { return items.size(); }
    static class VH extends RecyclerView.ViewHolder {
        TextView tv;
        VH(@NonNull View itemView) { super(itemView); tv = itemView.findViewById(android.R.id.text1); tv.setTextSize(15f); }
    }
}