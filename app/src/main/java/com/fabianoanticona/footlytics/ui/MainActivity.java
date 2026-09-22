package com.fabianoanticona.footlytics.ui;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.fabianoanticona.footlytics.R;
import com.fabianoanticona.footlytics.api.ApiService;
import com.fabianoanticona.footlytics.api.RetrofitClient;
import com.fabianoanticona.footlytics.model.CompetitionItem;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ApiService apiService = RetrofitClient.getApiService();
        apiService.obtenerCompeticiones().enqueue(new Callback<List<CompetitionItem>>() {
            @Override
            public void onResponse(Call<List<CompetitionItem>> call, Response<List<CompetitionItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CompetitionItem> lista = response.body();
                    Log.d("API_TEST", "Competiciones recibidas: " + lista.size());
                } else {
                    Log.e("API_TEST", "HTTP: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<CompetitionItem>> call, Throwable t) {
                Log.e("API_TEST", "Error de comunicación", t);
            }
        });
    }
}