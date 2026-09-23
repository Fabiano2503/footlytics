package com.fabianoanticona.footlytics.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fabianoanticona.footlytics.R;
import com.fabianoanticona.footlytics.api.ApiService;
import com.fabianoanticona.footlytics.api.RetrofitClient;
import com.fabianoanticona.footlytics.data.DataCache;
import com.fabianoanticona.footlytics.model.CompetitionItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {

    private static final long MIN_SPLASH_TIME = 1500L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        fetchCompetitionsAndNavigate();
    }

    private void fetchCompetitionsAndNavigate() {
        ApiService apiService = RetrofitClient.getApiService();
        long startTime = System.currentTimeMillis();

        apiService.getCompetitions().enqueue(new Callback<List<CompetitionItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<CompetitionItem>> call,
                                   @NonNull Response<List<CompetitionItem>> response) {
                List<CompetitionItem> competitions = (response.isSuccessful() && response.body() != null)
                        ? response.body()
                        : null;
                scheduleNavigation(competitions, startTime);
            }

            @Override
            public void onFailure(@NonNull Call<List<CompetitionItem>> call, @NonNull Throwable t) {
                scheduleNavigation(null, startTime);
            }
        });
    }

    private void scheduleNavigation(List<CompetitionItem> competitions, long startTime) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long timeToWait = MIN_SPLASH_TIME - elapsedTime;

        Runnable navigationTask = () -> {
            DataCache.getInstance().setCompetitions(competitions);
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        };

        if (timeToWait > 0) {
            new Handler(Looper.getMainLooper()).postDelayed(navigationTask, timeToWait);
        } else {
            navigationTask.run();
        }
    }
}
