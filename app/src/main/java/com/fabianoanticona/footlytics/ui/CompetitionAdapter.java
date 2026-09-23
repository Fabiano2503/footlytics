package com.fabianoanticona.footlytics.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.fabianoanticona.footlytics.R;
import com.fabianoanticona.footlytics.model.CompetitionItem;

import java.util.List;

public class CompetitionAdapter extends ArrayAdapter<CompetitionItem> {

    public CompetitionAdapter(@NonNull Context context, @NonNull List<CompetitionItem> competitions) {
        super(context, 0, competitions);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_competition, parent, false);
        }

        CompetitionItem competition = getItem(position);

        ImageView ivLogo = convertView.findViewById(R.id.ivCompetitionLogo);
        TextView tvName = convertView.findViewById(R.id.tvCompetitionName);
        TextView tvCountry = convertView.findViewById(R.id.tvCompetitionCountry);

        if (competition != null) {
            tvName.setText(competition.getName() != null ? competition.getName() : "");

            if (competition.getCountry() != null && !competition.getCountry().trim().isEmpty()) {
                tvCountry.setText(competition.getCountry());
                tvCountry.setVisibility(View.VISIBLE);
            } else {
                tvCountry.setVisibility(View.GONE);
            }

            if (competition.getLogo() != null && !competition.getLogo().trim().isEmpty()) {
                Glide.with(getContext())
                        .load(competition.getLogo())
                        .placeholder(R.drawable.ic_logo_placeholder)
                        .error(R.drawable.ic_logo_placeholder)
                        .into(ivLogo);
            } else {
                ivLogo.setImageResource(R.drawable.ic_logo_placeholder);
            }
        }

        return convertView;
    }
}
