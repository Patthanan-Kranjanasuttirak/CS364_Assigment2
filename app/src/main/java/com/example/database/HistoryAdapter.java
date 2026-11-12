package com.example.database;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class HistoryAdapter
        extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final List<HistoryItem> historyList;

    public HistoryAdapter(List<HistoryItem> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);

        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull HistoryViewHolder holder,
            int position) {

        HistoryItem item = historyList.get(position);

        holder.dateTime.setText(item.getDateTime());
        holder.duration.setText(item.getDuration());

        holder.totalSteps.setText(
                String.valueOf(item.getTotalSteps())
        );

        holder.stepRate.setText(
                String.format(
                        Locale.getDefault(),
                        "%.0f",
                        item.getStepRate()
                )
        );

        holder.totalShakes.setText(
                String.valueOf(item.getTotalShakes())
        );

        holder.shakeRate.setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f",
                        item.getShakeRate()
                )
        );

        // แสดง tag Walk
        if (item.isWalk()) {
            holder.tagWalk.setVisibility(View.VISIBLE);
        } else {
            holder.tagWalk.setVisibility(View.GONE);
        }

        // แสดง tag Shake
        if (item.isShake()) {
            holder.tagShake.setVisibility(View.VISIBLE);
        } else {
            holder.tagShake.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void removeItem(int position) {

        if (position >= 0 && position < historyList.size()) {
            historyList.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class HistoryViewHolder
            extends RecyclerView.ViewHolder {

        TextView dateTime;
        TextView duration;

        TextView totalSteps;
        TextView stepRate;

        TextView totalShakes;
        TextView shakeRate;

        TextView tagWalk;
        TextView tagShake;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            dateTime = itemView.findViewById(
                    R.id.tvHistoryDateTime
            );

            duration = itemView.findViewById(
                    R.id.tvHistoryDuration
            );

            totalSteps = itemView.findViewById(
                    R.id.tvHistoryTotalSteps
            );

            stepRate = itemView.findViewById(
                    R.id.historyStepRate
            );

            totalShakes = itemView.findViewById(
                    R.id.tvHistoryTotalShakes
            );

            shakeRate = itemView.findViewById(
                    R.id.tvHistoryShakeRate
            );

            tagWalk = itemView.findViewById(
                    R.id.tagWalk
            );

            tagShake = itemView.findViewById(
                    R.id.tagShake
            );
        }
    }
}