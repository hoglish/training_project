package com.example.fitnessapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.models.HourlyWeather;

import java.util.ArrayList;
import java.util.List;

public class HourWeatherAdapter extends RecyclerView.Adapter<HourWeatherAdapter.VH> {

    private List<HourlyWeather> items = new ArrayList<>();

    public void setItems(List<HourlyWeather> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_hour, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        HourlyWeather item = items.get(position);
        h.tvTime.setText(item.time);
        h.tvTemp.setText(Math.round(item.temperature) + "°");
        h.tvIcon.setText(iconFor(item.weather_condition));
        h.tvPrecip.setText(item.precipitation_probability > 0
                ? item.precipitation_probability + "%"
                : "");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTime, tvIcon, tvTemp, tvPrecip;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTime   = itemView.findViewById(R.id.tvHourTime);
            tvIcon   = itemView.findViewById(R.id.tvHourIcon);
            tvTemp   = itemView.findViewById(R.id.tvHourTemp);
            tvPrecip = itemView.findViewById(R.id.tvHourPrecip);
        }
    }

    /** Общий маппинг condition → emoji (используется и в DayWeatherAdapter) */
    public static String iconFor(String cond) {
        if (cond == null) return "❓";
        switch (cond) {
            case "ясно":                    return "☀️";
            case "малооблачно":             return "🌤";
            case "облачно с прояснениями":  return "⛅";
            case "пасмурно":                return "☁️";
            case "небольшой дождь":
            case "дождь":
            case "ливень":                  return "🌧";
            case "гроза":                   return "⛈";
            case "снег":
            case "небольшой снег":          return "❄️";
            case "мокрый снег":             return "🌨";
            case "туман":                   return "🌫";
            default:                        return "❓";
        }
    }
}