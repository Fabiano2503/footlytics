package com.fabianoanticona.footlytics.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fabianoanticona.footlytics.R;
import com.fabianoanticona.footlytics.data.AppDatabase;
import com.fabianoanticona.footlytics.data.FavoriteTeamEntity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private View layoutEmptyFavorites;
    private Button btnExploreTeams;

    private FavoriteAdapter favoriteAdapter;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    public static FavoritesFragment newInstance() {
        return new FavoritesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            loadFavorites();
        }
    }

    private void initViews(View view) {
        rvFavorites = view.findViewById(R.id.rvFavorites);
        layoutEmptyFavorites = view.findViewById(R.id.layoutEmptyState);
        btnExploreTeams = view.findViewById(R.id.btnExploreTeams);
    }

    private void setupRecyclerView() {
        favoriteAdapter = new FavoriteAdapter(teamId -> {
            if (getContext() != null) {
                Intent intent = new Intent(requireContext(), TeamDetailActivity.class);
                intent.putExtra("TEAM_ID", teamId);
                startActivity(intent);
            }
        });
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavorites.setAdapter(favoriteAdapter);
    }

    private void setupListeners() {
        if (btnExploreTeams != null) {
            btnExploreTeams.setOnClickListener(v -> {
                if (getActivity() != null) {
                    BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigation);
                    if (bottomNav != null) {
                        bottomNav.setSelectedItemId(R.id.nav_explore);
                    }
                }
            });
        }
    }

    private void loadFavorites() {
        Context context = getContext();
        if (context == null) return;
        Context appContext = context.getApplicationContext();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<FavoriteTeamEntity> favorites = AppDatabase.getInstance(appContext)
                    .favoriteTeamDao()
                    .getAll();

            if (isAdded() && getActivity() != null) {
                requireActivity().runOnUiThread(() -> {
                    if (favoriteAdapter != null) {
                        favoriteAdapter.updateFavorites(favorites);
                    }

                    if (favorites == null || favorites.isEmpty()) {
                        if (rvFavorites != null) rvFavorites.setVisibility(View.GONE);
                        if (layoutEmptyFavorites != null) layoutEmptyFavorites.setVisibility(View.VISIBLE);
                    } else {
                        if (rvFavorites != null) rvFavorites.setVisibility(View.VISIBLE);
                        if (layoutEmptyFavorites != null) layoutEmptyFavorites.setVisibility(View.GONE);
                    }
                });
            }
        });
    }
}
