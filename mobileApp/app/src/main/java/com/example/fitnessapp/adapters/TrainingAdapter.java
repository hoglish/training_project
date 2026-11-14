package com.example.fitnessapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnessapp.R;
import com.example.fitnessapp.models.TrainingDto;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class TrainingAdapter extends RecyclerView.Adapter<TrainingAdapter.VH> {
    public interface Listener {
        void onClick(TrainingDto training);
        void onEdit(TrainingDto training);
    }
    private final List<TrainingDto> items = new ArrayList<>();
    private final Listener listener;

    public TrainingAdapter(Listener listener) { this.listener = listener; }
    public void setItems(List<TrainingDto> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_training, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        TrainingDto item = items.get(position);
        h.tvTrainingName.setText(item.name);
        h.tvTrainingStatus.setText(item.status != null ? item.status : "завершена");

        if (item.date_time_start != null) {
            String start = item.date_time_start.length() > 16 ? item.date_time_start.substring(11, 16) : "";
            String end = (item.date_time_end != null && item.date_time_end.length() > 16) ? item.date_time_end.substring(11, 16) : "";
            h.tvTrainingTime.setText(end.isEmpty() ? start : start + "–" + end);
        }
        h.tvTrainingKcal.setText("🔥 " + (item.calories != null ? item.calories : 0) + " ккал");

        int feel = (item.feeling != null && item.feeling > 0) ? item.feeling : 4;
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < feel; i++) stars.append("★");
        h.tvTrainingFeeling.setText(stars.toString());

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(item); });
        h.btnStart.setOnClickListener(v -> { if (listener != null) listener.onClick(item); });
        h.btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEdit(item); });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTrainingName, tvTrainingTime, tvTrainingStatus, tvTrainingKcal, tvTrainingFeeling;
        MaterialButton btnStart, btnEdit;
        VH(@NonNull View itemView) {
            super(itemView);
            tvTrainingName = itemView.findViewById(R.id.tvTrainingName);
            tvTrainingTime = itemView.findViewById(R.id.tvTrainingTime);
            tvTrainingStatus = itemView.findViewById(R.id.tvTrainingStatus);
            tvTrainingKcal = itemView.findViewById(R.id.tvTrainingKcal);
            tvTrainingFeeling = itemView.findViewById(R.id.tvTrainingFeeling);
            btnStart = itemView.findViewById(R.id.btnStart);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}