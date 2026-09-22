package com.fabianoanticona.footlytics.api;

import com.fabianoanticona.footlytics.model.CompetitionItem;
import com.fabianoanticona.footlytics.model.StandingItem;
import com.fabianoanticona.footlytics.model.TeamDetail;
import com.fabianoanticona.footlytics.model.TeamItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("competitions")
    Call<List<CompetitionItem>> obtenerCompeticiones();

    @GET("competitions/{id}/standings")
    Call<List<StandingItem>> obtenerStandings(
            @Path("id") int competitionId
    );

    @GET("teams")
    Call<List<TeamItem>> buscarEquipos(
            @Query("search") String query
    );

    @GET("teams/{id}")
    Call<TeamDetail> obtenerDetalleEquipo(
            @Path("id") int teamId
    );
}