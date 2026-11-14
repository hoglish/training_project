package com.example.fitnessapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.models.DailyWeather;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DayWeatherAdapter extends RecyclerView.Adapter<DayWeatherAdapter.VH> {

    private List<DailyWeather> items = new ArrayList<>();

    public void setItems(List<DailyWeather> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_day, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        DailyWeather item = items.get(position);
        h.tvDayName.setText(dayName(item.date));
        h.tvDayIcon.setText(HourWeatherAdapter.iconFor(item.condition));
        h.tvDayTemp.setText(Math.round(item.temp_max) + "°");
        h.tvDayNight.setText(Math.round(item.temp_min) + "°");
        h.tvDayPrecip.setText(item.precipitation_probability > 0
                ? item.precipitation_probability + "%"
                : "");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDayName, tvDayIcon, tvDayTemp, tvDayNight, tvDayPrecip;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDayName   = itemView.findViewById(R.id.tvDayName);
            tvDayIcon   = itemView.findViewById(R.id.tvDayIcon);
            tvDayTemp   = itemView.findViewById(R.id.tvDayTemp);
            tvDayNight  = itemView.findViewById(R.id.tvDayNight);
            tvDayPrecip = itemView.findViewById(R.id.tvDayPrecip);
        }
    }

    /** "2026-11-14" → "сегодня" / "завтра" / "пт" */
    private String dayName(String isoDate) {
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date d = in.parse(isoDate);
            if (d == null) return "";

            Calendar today = Calendar.getInstance();
            Calendar target = Calendar.getInstance();
            target.setTime(d);

            if (today.get(Calendar.YEAR) == target.get(Calendar.YEAR)
                    && today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)) {
                return "сегодня";
            }

            today.add(Calendar.DAY_OF_YEAR, 1);
            if (today.get(Calendar.YEAR) == target.get(Calendar.YEAR)
                    && today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)) {
                return "завтра";
            }

            String[] days = {"вс", "пн", "вт", "ср", "чт", "пт", "сб"};
            return days[target.get(Calendar.DAY_OF_WEEK) - 1];
        } catch (Exception e) {
            return "";
        }
    }
}