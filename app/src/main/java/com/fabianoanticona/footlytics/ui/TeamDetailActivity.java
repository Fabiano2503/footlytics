package com.fabianoanticona.footlytics.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamDetailActivity extends AppCompatActivity {

    private ImageView ivBack;
    private ImageView ivFavorite;
    private View layoutContent;
    private ImageView ivTeamLogo;
    private TextView tvTeamName;
    private TextView tvTeamDetails;
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
        ivBack = findViewById(R.id.ivBack);
        ivFavorite = findViewById(R.id.ivFavorite);
        layoutContent = findViewById(R.id.layoutContent);
        ivTeamLogo = findViewById(R.id.ivTeamLogo);
        tvTeamName = findViewById(R.id.tvTeamName);
        tvTeamDetails = findViewById(R.id.tvTeamDetails);
        pbLoading = findViewById(R.id.pbLoading);
        tvMessage = findViewById(R.id.tvMessage);
        btnRetry = findViewById(R.id.btnRetry);
    }

    private void setupListeners() {
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        if (ivFavorite != null) {
            ivFavorite.setOnClickListener(v -> toggleFavorite());
        }

        if (btnRetry != null) {
            btnRetry.setOnClickListener(v -> loadTeamDetail(teamId));
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
        if (ivFavorite != null) {
            ivFavorite.setImageResource(isFavorite ? R.drawable.ic_star_large : R.drawable.ic_star_outline);
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

        if (tvTeamName != null && teamDetail.getName() != null && !teamDetail.getName().trim().isEmpty()) {
            tvTeamName.setText(teamDetail.getName());
        }

        if (ivTeamLogo != null && teamDetail.getLogo() != null && !teamDetail.getLogo().trim().isEmpty()) {
            Glide.with(this)
                    .load(teamDetail.getLogo())
                    .placeholder(R.drawable.ic_logo_footlytics)
                    .error(R.drawable.ic_logo_footlytics)
                    .into(ivTeamLogo);
        }

        if (tvTeamDetails != null) {
            StringBuilder sb = new StringBuilder();
            if (teamDetail.getCountry() != null && !teamDetail.getCountry().trim().isEmpty()) {
                sb.append("🇪🇸 ").append(teamDetail.getCountry());
            }

            StringBuilder stadiumSb = new StringBuilder();
            if (teamDetail.getStadium() != null && !teamDetail.getStadium().trim().isEmpty()) {
                stadiumSb.append(teamDetail.getStadium());
            }
            if (teamDetail.getStadiumCity() != null && !teamDetail.getStadiumCity().trim().isEmpty()) {
                if (stadiumSb.length() > 0) {
                    stadiumSb.append(", ");
                }
                stadiumSb.append(teamDetail.getStadiumCity());
            }

            if (stadiumSb.length() > 0) {
                if (sb.length() > 0) {
                    sb.append(" • ");
                }
                sb.append("🏟️ ").append(stadiumSb);
            }

            tvTeamDetails.setText(sb.toString());
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
