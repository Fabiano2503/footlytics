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
import com.fabianoanticona.footlytics.model.TeamItem;

import java.util.ArrayList;
import java.util.List;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {

    public interface OnTeamClickListener {
        void onTeamClick(int teamId);
    }

    private final List<TeamItem> teams = new ArrayList<>();
    private final OnTeamClickListener listener;

    public TeamAdapter(OnTeamClickListener listener) {
        this.listener = listener;
    }

    public void updateTeams(List<TeamItem> newTeams) {
        this.teams.clear();
        if (newTeams != null) {
            this.teams.addAll(newTeams);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        TeamItem team = teams.get(position);
        holder.bind(team, listener);
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    public static class TeamViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivTeamLogo;
        private final TextView tvTeamName;
        private final TextView tvTeamCountry;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTeamLogo = itemView.findViewById(R.id.ivTeamLogo);
            tvTeamName = itemView.findViewById(R.id.tvTeamName);
            tvTeamCountry = itemView.findViewById(R.id.tvTeamCountry);
        }

        public void bind(TeamItem team, OnTeamClickListener listener) {
            if (team == null) return;

            tvTeamName.setText(team.getName() != null ? team.getName() : "");
            tvTeamCountry.setText(team.getCountry() != null ? team.getCountry() : "");

            if (team.getLogo() != null && !team.getLogo().trim().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(team.getLogo())
                        .placeholder(R.drawable.ic_logo_placeholder)
                        .error(R.drawable.ic_logo_placeholder)
                        .into(ivTeamLogo);
            } else {
                ivTeamLogo.setImageResource(R.drawable.ic_logo_placeholder);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTeamClick(team.getId());
                }
            });
        }
    }
}
