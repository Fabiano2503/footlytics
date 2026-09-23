package com.fabianoanticona.footlytics.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fabianoanticona.footlytics.R;
import com.fabianoanticona.footlytics.model.CompetitionItem;

import java.util.ArrayList;
import java.util.List;

public class CompetitionAdapter extends RecyclerView.Adapter<CompetitionAdapter.CompetitionViewHolder> {

    public interface OnCompetitionClickListener {
        void onCompetitionClick(CompetitionItem competition, int position);
    }

    private final List<CompetitionItem> competitions = new ArrayList<>();
    private final OnCompetitionClickListener listener;
    private int selectedPosition = 0;

    public CompetitionAdapter(OnCompetitionClickListener listener) {
        this.listener = listener;
    }

    public void setCompetitions(List<CompetitionItem> newCompetitions) {
        this.competitions.clear();
        if (newCompetitions != null) {
            this.competitions.addAll(newCompetitions);
        }
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        if (position >= 0 && position < competitions.size()) {
            int previousPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    @NonNull
    @Override
    public CompetitionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_competition, parent, false);
        return new CompetitionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompetitionViewHolder holder, int position) {
        CompetitionItem competition = competitions.get(position);
        boolean isSelected = (position == selectedPosition);
        holder.bind(competition, isSelected, listener, position);
    }

    @Override
    public int getItemCount() {
        return competitions.size();
    }

    public static class CompetitionViewHolder extends RecyclerView.ViewHolder {

        private final View layoutChip;
        private final ImageView ivLogo;
        private final TextView tvName;

        public CompetitionViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutChip = itemView.findViewById(R.id.layoutChip);
            ivLogo = itemView.findViewById(R.id.ivCompetitionLogo);
            tvName = itemView.findViewById(R.id.tvCompetitionName);
        }

        public void bind(CompetitionItem competition, boolean isSelected, OnCompetitionClickListener listener, int position) {
            if (competition == null) return;

            tvName.setText(competition.getName() != null ? competition.getName() : "");

            if (isSelected) {
                layoutChip.setBackgroundResource(R.drawable.bg_chip_active);
                tvName.setTextColor(Color.parseColor("#FFFFFF"));
            } else {
                layoutChip.setBackgroundResource(R.drawable.bg_chip_inactive);
                tvName.setTextColor(Color.parseColor("#17202A"));
            }

            if (competition.getLogo() != null && !competition.getLogo().trim().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(competition.getLogo())
                        .placeholder(R.drawable.ic_logo_footlytics)
                        .error(R.drawable.ic_logo_footlytics)
                        .into(ivLogo);
            } else {
                ivLogo.setImageResource(R.drawable.ic_logo_footlytics);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCompetitionClick(competition, position);
                }
            });
        }
    }
}
