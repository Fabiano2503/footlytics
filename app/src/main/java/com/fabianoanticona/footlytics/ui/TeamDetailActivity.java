package com.fabianoanticona.footlytics.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.fabianoanticona.footlytics.R;
import com.fabianoanticona.footlytics.api.ApiService;
import com.fabianoanticona.footlytics.api.RetrofitClient;
import com.fabianoanticona.footlytics.data.AppDatabase;
import com.fabianoanticona.footlytics.data.FavoriteTeamDao;
import com.fabianoanticona.footlytics.data.FavoriteTeamEntity;
import com.fabianoanticona.footlytics.model.TeamDetail;
import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamDetailActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private View layoutContent;
    private ImageView ivDetailLogo;
    private TextView tvDetailName;
    private TextView tvDetailCountry;
    private TextView tvDetailStadium;
    private TextView tvDetailStadiumCity;
    private ImageButton btnFavorite;
    private ProgressBar pbLoading;
    private TextView tvMessage;
    private Button btnRetry;

    private int teamId = -1;
    private boolean isFavorite = false;
    private TeamDetail teamDetail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_detail);

        teamId = getIntent().getIntExtra("TEAM_ID", -1);
        if (teamId == -1) {
            finish();
            return;
        }

        initViews();
        setupListeners();
        checkFavoriteStatus();
        loadTeamDetail(teamId);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        layoutContent = findViewById(R.id.layoutContent);
        ivDetailLogo = findViewById(R.id.ivDetailLogo);
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailCountry = findViewById(R.id.tvDetailCountry);
        tvDetailStadium = findViewById(R.id.tvDetailStadium);
        tvDetailStadiumCity = findViewById(R.id.tvDetailStadiumCity);
        btnFavorite = findViewById(R.id.btnFavorite);
        pbLoading = findViewById(R.id.pbLoading);
        tvMessage = findViewById(R.id.tvMessage);
        btnRetry = findViewById(R.id.btnRetry);
    }

    private void setupListeners() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        if (btnRetry != null) {
            btnRetry.setOnClickListener(v -> loadTeamDetail(teamId));
        }

        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> toggleFavorite());
        }
    }

    private void checkFavoriteStatus() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            boolean favorite = AppDatabase.getInstance(TeamDetailActivity.this)
                    .favoriteTeamDao()
                    .isFavorite(teamId);
            runOnUiThread(() -> {
                isFavorite = favorite;
                updateFavoriteButtonUI();
            });
        });
    }

    private void updateFavoriteButtonUI() {
        if (btnFavorite != null) {
            btnFavorite.setImageResource(isFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        }
    }

    private void toggleFavorite() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            FavoriteTeamDao dao = AppDatabase.getInstance(TeamDetailActivity.this).favoriteTeamDao();
            if (isFavorite) {
                dao.deleteById(teamId);
                runOnUiThread(() -> {
                    isFavorite = false;
                    updateFavoriteButtonUI();
                    Toast.makeText(TeamDetailActivity.this, R.string.msg_removed_favorite, Toast.LENGTH_SHORT).show();
                });
            } else {
                String name = teamDetail != null ? teamDetail.getName() : "";
                String country = teamDetail != null ? teamDetail.getCountry() : "";
                String logo = teamDetail != null ? teamDetail.getLogo() : "";

                FavoriteTeamEntity entity = new FavoriteTeamEntity(teamId, name, country, logo);
                dao.insert(entity);
                runOnUiThread(() -> {
                    isFavorite = true;
                    updateFavoriteButtonUI();
                    Toast.makeText(TeamDetailActivity.this, R.string.msg_added_favorite, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadTeamDetail(int id) {
        showLoading();

        ApiService apiService = RetrofitClient.getApiService();
        apiService.getTeamDetail(id).enqueue(new Callback<TeamDetail>() {
            @Override
            public void onResponse(@NonNull Call<TeamDetail> call,
                                   @NonNull Response<TeamDetail> response) {
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    showSuccess(response.body());
                } else if (response.code() == 404) {
                    showError(getString(R.string.error_team_not_found));
                } else {
                    showError(getString(R.string.error_team_detail_failed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<TeamDetail> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showError(getString(R.string.error_team_detail_failed));
            }
        });
    }

    private void showLoading() {
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (layoutContent != null) layoutContent.setVisibility(View.GONE);
        if (tvMessage != null) tvMessage.setVisibility(View.GONE);
        if (btnRetry != null) btnRetry.setVisibility(View.GONE);
    }

    private void showSuccess(@NonNull TeamDetail teamDetail) {
        this.teamDetail = teamDetail;

        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (tvMessage != null) tvMessage.setVisibility(View.GONE);
        if (btnRetry != null) btnRetry.setVisibility(View.GONE);
        if (layoutContent != null) layoutContent.setVisibility(View.VISIBLE);

        if (tvDetailName != null) {
            tvDetailName.setText(teamDetail.getName() != null ? teamDetail.getName() : "");
        }

        if (ivDetailLogo != null) {
            if (teamDetail.getLogo() != null && !teamDetail.getLogo().trim().isEmpty()) {
                Glide.with(this)
                        .load(teamDetail.getLogo())
                        .placeholder(R.drawable.ic_logo_placeholder)
                        .error(R.drawable.ic_logo_placeholder)
                        .into(ivDetailLogo);
            } else {
                ivDetailLogo.setImageResource(R.drawable.ic_logo_placeholder);
            }
        }

        if (tvDetailCountry != null) {
            if (teamDetail.getCountry() != null && !teamDetail.getCountry().trim().isEmpty()) {
                tvDetailCountry.setText(teamDetail.getCountry());
                tvDetailCountry.setVisibility(View.VISIBLE);
            } else {
                tvDetailCountry.setVisibility(View.GONE);
            }
        }

        if (tvDetailStadium != null) {
            if (teamDetail.getStadium() != null && !teamDetail.getStadium().trim().isEmpty()) {
                tvDetailStadium.setText(teamDetail.getStadium());
                tvDetailStadium.setVisibility(View.VISIBLE);
            } else {
                tvDetailStadium.setVisibility(View.GONE);
            }
        }

        if (tvDetailStadiumCity != null) {
            if (teamDetail.getStadiumCity() != null && !teamDetail.getStadiumCity().trim().isEmpty()) {
                tvDetailStadiumCity.setText(teamDetail.getStadiumCity());
                tvDetailStadiumCity.setVisibility(View.VISIBLE);
            } else {
                tvDetailStadiumCity.setVisibility(View.GONE);
            }
        }
    }

    private void showError(String message) {
        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (layoutContent != null) layoutContent.setVisibility(View.GONE);
        if (tvMessage != null) {
            tvMessage.setText(message);
            tvMessage.setVisibility(View.VISIBLE);
        }
        if (btnRetry != null) btnRetry.setVisibility(View.VISIBLE);
    }
}
