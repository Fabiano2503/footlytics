package com.fabianoanticona.footlytics.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fabianoanticona.footlytics.R;
import com.fabianoanticona.footlytics.model.StandingItem;
import com.fabianoanticona.footlytics.model.TeamItem;

import java.util.ArrayList;
import java.util.List;

public class StandingAdapter extends RecyclerView.Adapter<StandingAdapter.StandingViewHolder> {

    public interface OnTeamClickListener {
        void onTeamClick(int teamId);
    }

    private final List<StandingItem> standings = new ArrayList<>();
    private final OnTeamClickListener listener;

    public StandingAdapter(OnTeamClickListener listener) {
        this.listener = listener;
    }

    public void updateStandings(List<StandingItem> newStandings) {
        this.standings.clear();
        if (newStandings != null) {
            this.standings.addAll(newStandings);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StandingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_standing, parent, false);
        return new StandingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StandingViewHolder holder, int position) {
        StandingItem standing = standings.get(position);
        holder.bind(standing, listener);
    }

    @Override
    public int getItemCount() {
        return standings.size();
    }

    public static class StandingViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvPosition;
        private final ImageView ivLogo;
        private final TextView tvTeam;
        private final TextView tvPlayed;
        private final TextView tvPoints;

        public StandingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            ivLogo = itemView.findViewById(R.id.ivStandingLogo);
            tvTeam = itemView.findViewById(R.id.tvStandingTeam);
            tvPlayed = itemView.findViewById(R.id.tvPlayed);
            tvPoints = itemView.findViewById(R.id.tvPoints);
        }

        public void bind(StandingItem standing, OnTeamClickListener listener) {
            if (standing == null) return;

            tvPosition.setText(String.valueOf(standing.getPosition()));
            tvPlayed.setText(String.valueOf(standing.getPlayed()));
            tvPoints.setText(String.valueOf(standing.getPoints()));

            TeamItem team = standing.getTeam();
            if (team != null) {
                tvTeam.setText(team.getName() != null ? team.getName() : "");

                if (team.getLogo() != null && !team.getLogo().trim().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(team.getLogo())
                            .placeholder(R.drawable.ic_logo_placeholder)
                            .error(R.drawable.ic_logo_placeholder)
                            .into(ivLogo);
                } else {
                    ivLogo.setImageResource(R.drawable.ic_logo_placeholder);
                }

                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onTeamClick(team.getId());
                    }
                });
            } else {
                tvTeam.setText("");
                ivLogo.setImageResource(R.drawable.ic_logo_placeholder);
                itemView.setOnClickListener(null);
            }
        }
    }
}
