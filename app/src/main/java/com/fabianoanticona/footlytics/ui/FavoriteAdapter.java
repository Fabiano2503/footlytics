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
import com.fabianoanticona.footlytics.data.FavoriteTeamEntity;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {

    public interface OnFavoriteClickListener {
        void onFavoriteClick(int teamId);
    }

    private final List<FavoriteTeamEntity> favorites = new ArrayList<>();
    private final OnFavoriteClickListener listener;

    public FavoriteAdapter(OnFavoriteClickListener listener) {
        this.listener = listener;
    }

    public void updateFavorites(List<FavoriteTeamEntity> newFavorites) {
        this.favorites.clear();
        if (newFavorites != null) {
            this.favorites.addAll(newFavorites);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteTeamEntity favorite = favorites.get(position);
        holder.bind(favorite, listener);
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivTeamLogo;
        private final TextView tvTeamName;
        private final TextView tvTeamCountry;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivTeamLogo = itemView.findViewById(R.id.ivTeamLogo);
            tvTeamName = itemView.findViewById(R.id.tvTeamName);
            tvTeamCountry = itemView.findViewById(R.id.tvTeamCountry);
        }

        public void bind(FavoriteTeamEntity favorite, OnFavoriteClickListener listener) {
            if (favorite == null) return;

            tvTeamName.setText(favorite.getName() != null ? favorite.getName() : "");
            tvTeamCountry.setText(favorite.getCountry() != null ? favorite.getCountry() : "");

            if (favorite.getLogo() != null && !favorite.getLogo().trim().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(favorite.getLogo())
                        .placeholder(R.drawable.ic_logo_placeholder)
                        .error(R.drawable.ic_logo_placeholder)
                        .into(ivTeamLogo);
            } else {
                ivTeamLogo.setImageResource(R.drawable.ic_logo_placeholder);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(favorite.getId());
                }
            });
        }
    }
}
