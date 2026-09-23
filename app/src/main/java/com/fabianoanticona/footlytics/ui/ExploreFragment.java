package com.fabianoanticona.footlytics.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fabianoanticona.footlytics.R;
import com.fabianoanticona.footlytics.api.ApiService;
import com.fabianoanticona.footlytics.api.RetrofitClient;
import com.fabianoanticona.footlytics.model.CompetitionItem;
import com.fabianoanticona.footlytics.model.StandingItem;
import com.fabianoanticona.footlytics.model.TeamItem;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExploreFragment extends Fragment {

    private View cardSelector;
    private View layoutTableHeader;
    private Spinner spinnerCompetitions;
    private RecyclerView rvStandings;
    private RecyclerView rvSearchResults;
    private TextInputEditText etSearch;
    private ProgressBar pbLoading;
    private TextView tvMessage;
    private Button btnRetry;

    private CompetitionAdapter competitionAdapter;
    private StandingAdapter standingAdapter;
    private TeamAdapter teamAdapter;

    private final List<CompetitionItem> competitions = new ArrayList<>();
    private int selectedCompetitionId = -1;
    private boolean isInitialSelection = true;

    private boolean isSearchMode = false;
    private String currentSearchQuery = "";
    private boolean isInitialized = false;

    public ExploreFragment() {
        // Required empty public constructor
    }

    public static ExploreFragment newInstance() {
        return new ExploreFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();

        if (!isInitialized) {
            isInitialized = true;
            loadCompetitions();
        }
    }

    private void initViews(View view) {
        cardSelector = view.findViewById(R.id.cardSelector);
        layoutTableHeader = view.findViewById(R.id.layoutTableHeader);
        spinnerCompetitions = view.findViewById(R.id.spinnerCompetitions);
        rvStandings = view.findViewById(R.id.rvStandings);
        rvSearchResults = view.findViewById(R.id.rvSearchResults);
        etSearch = view.findViewById(R.id.etSearch);
        pbLoading = view.findViewById(R.id.pbLoading);
        tvMessage = view.findViewById(R.id.tvMessage);
        btnRetry = view.findViewById(R.id.btnRetry);
    }

    private void setupRecyclerView() {
        standingAdapter = new StandingAdapter(teamId -> {
            if (getContext() != null) {
                Intent intent = new Intent(requireContext(), TeamDetailActivity.class);
                intent.putExtra("TEAM_ID", teamId);
                startActivity(intent);
            }
        });
        rvStandings.setLayoutManager(new LinearLayoutManager(getContext()));
        rvStandings.setAdapter(standingAdapter);

        teamAdapter = new TeamAdapter(teamId -> {
            if (getContext() != null) {
                Intent intent = new Intent(requireContext(), TeamDetailActivity.class);
                intent.putExtra("TEAM_ID", teamId);
                startActivity(intent);
            }
        });
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSearchResults.setAdapter(teamAdapter);
    }

    private void setupListeners() {
        btnRetry.setOnClickListener(v -> {
            if (isSearchMode) {
                if (!currentSearchQuery.isEmpty()) {
                    performSearch(currentSearchQuery);
                }
            } else if (competitions.isEmpty()) {
                loadCompetitions();
            } else if (selectedCompetitionId != -1) {
                fetchStandings(selectedCompetitionId);
            } else {
                loadCompetitions();
            }
        });

        spinnerCompetitions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < competitions.size()) {
                    CompetitionItem selectedItem = competitions.get(position);
                    int newCompetitionId = selectedItem.getId();

                    if (isInitialSelection) {
                        isInitialSelection = false;
                        return;
                    }

                    if (newCompetitionId != selectedCompetitionId) {
                        selectedCompetitionId = newCompetitionId;
                        fetchStandings(selectedCompetitionId);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        if (etSearch != null) {
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
                    if (!query.isEmpty()) {
                        performSearch(query);
                    }
                    return true;
                }
                return false;
            });

            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.toString().trim().isEmpty() && isSearchMode) {
                        switchToExploreMode();
                    }
                }
            });
        }
    }

    private void performSearch(String query) {
        isSearchMode = true;
        currentSearchQuery = query;

        showSearchLoading();

        ApiService apiService = RetrofitClient.getApiService();
        apiService.searchTeams(query).enqueue(new Callback<List<TeamItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<TeamItem>> call,
                                   @NonNull Response<List<TeamItem>> response) {
                if (!isAdded() || !isSearchMode) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<TeamItem> teams = response.body();
                    if (teams.isEmpty()) {
                        showSearchEmpty(getString(R.string.error_search_empty));
                    } else {
                        teamAdapter.updateTeams(teams);
                        showSearchSuccess();
                    }
                } else {
                    showSearchError(getString(R.string.error_search_failed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<TeamItem>> call, @NonNull Throwable t) {
                if (!isAdded() || !isSearchMode) return;
                showSearchError(getString(R.string.error_network));
            }
        });
    }

    private void switchToExploreMode() {
        isSearchMode = false;
        currentSearchQuery = "";

        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);
        if (cardSelector != null) cardSelector.setVisibility(View.VISIBLE);

        if (standingAdapter != null && standingAdapter.getItemCount() > 0) {
            if (layoutTableHeader != null) layoutTableHeader.setVisibility(View.VISIBLE);
            if (rvStandings != null) rvStandings.setVisibility(View.VISIBLE);
            if (pbLoading != null) pbLoading.setVisibility(View.GONE);
            if (tvMessage != null) tvMessage.setVisibility(View.GONE);
            if (btnRetry != null) btnRetry.setVisibility(View.GONE);
        } else {
            if (pbLoading != null && pbLoading.getVisibility() == View.VISIBLE) {
                showLoading();
            } else if (tvMessage != null && tvMessage.getVisibility() == View.VISIBLE) {
                if (layoutTableHeader != null) layoutTableHeader.setVisibility(View.GONE);
                if (rvStandings != null) rvStandings.setVisibility(View.GONE);
            } else {
                showSuccess();
            }
        }
    }

    private void showSearchLoading() {
        if (cardSelector != null) cardSelector.setVisibility(View.GONE);
        if (layoutTableHeader != null) layoutTableHeader.setVisibility(View.GONE);
        if (rvStandings != null) rvStandings.setVisibility(View.GONE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);

        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (tvMessage != null) tvMessage.setVisibility(View.GONE);
        if (btnRetry != null) btnRetry.setVisibility(View.GONE);
    }

    private void showSearchSuccess() {
        if (cardSelector != null) cardSelector.setVisibility(View.GONE);
        if (layoutTableHeader != null) layoutTableHeader.setVisibility(View.GONE);
        if (rvStandings != null) rvStandings.setVisibility(View.GONE);

        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (tvMessage != null) tvMessage.setVisibility(View.GONE);
        if (btnRetry != null) btnRetry.setVisibility(View.GONE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.VISIBLE);
    }

    private void showSearchEmpty(String message) {
        if (cardSelector != null) cardSelector.setVisibility(View.GONE);
        if (layoutTableHeader != null) layoutTableHeader.setVisibility(View.GONE);
        if (rvStandings != null) rvStandings.setVisibility(View.GONE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);

        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (tvMessage != null) {
            tvMessage.setText(message);
            tvMessage.setVisibility(View.VISIBLE);
        }
        if (btnRetry != null) btnRetry.setVisibility(View.GONE);
    }

    private void showSearchError(String message) {
        if (cardSelector != null) cardSelector.setVisibility(View.GONE);
        if (layoutTableHeader != null) layoutTableHeader.setVisibility(View.GONE);
        if (rvStandings != null) rvStandings.setVisibility(View.GONE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);

        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (tvMessage != null) {
            tvMessage.setText(message);
            tvMessage.setVisibility(View.VISIBLE);
        }
        if (btnRetry != null) btnRetry.setVisibility(View.VISIBLE);
    }

    private void loadCompetitions() {
        showLoading();

        ApiService apiService = RetrofitClient.getApiService();
        apiService.getCompetitions().enqueue(new Callback<List<CompetitionItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<CompetitionItem>> call,
                                   @NonNull Response<List<CompetitionItem>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    competitions.clear();
                    competitions.addAll(response.body());

                    competitionAdapter = new CompetitionAdapter(requireContext(), competitions);
                    spinnerCompetitions.setAdapter(competitionAdapter);

                    int targetIndex = findCompetitionIndexByName("LaLiga");
                    if (targetIndex != -1) {
                        selectedCompetitionId = competitions.get(targetIndex).getId();
                        isInitialSelection = true;
                        spinnerCompetitions.setSelection(targetIndex);
                    } else {
                        selectedCompetitionId = competitions.get(0).getId();
                        isInitialSelection = true;
                        spinnerCompetitions.setSelection(0);
                    }

                    fetchStandings(selectedCompetitionId);
                } else {
                    showError(getString(R.string.error_server));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CompetitionItem>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showError(getString(R.string.error_network));
            }
        });
    }

    private int findCompetitionIndexByName(String competitionName) {
        if (competitionName == null || competitions.isEmpty()) return -1;
        for (int i = 0; i < competitions.size(); i++) {
            CompetitionItem item = competitions.get(i);
            if (item != null && item.getName() != null && item.getName().equalsIgnoreCase(competitionName)) {
                return i;
            }
        }
        return -1;
    }

    private void fetchStandings(int competitionId) {
        showLoading();

        ApiService apiService = RetrofitClient.getApiService();
        apiService.getStandings(competitionId).enqueue(new Callback<List<StandingItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<StandingItem>> call,
                                   @NonNull Response<List<StandingItem>> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<StandingItem> standings = response.body();
                    if (standings.isEmpty()) {
                        showEmpty(getString(R.string.empty_standings_message));
                    } else {
                        standingAdapter.updateStandings(standings);
                        showSuccess();
                    }
                } else {
                    showError(getString(R.string.error_server));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<StandingItem>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showError(getString(R.string.error_network));
            }
        });
    }

    // State machine methods
    public void showLoading() {
        if (isSearchMode) return;
        if (cardSelector != null) cardSelector.setVisibility(View.VISIBLE);
        if (layoutTableHeader != null) layoutTableHeader.setVisibility(View.GONE);
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (rvStandings != null) rvStandings.setVisibility(View.GONE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);
        if (tvMessage != null) tvMessage.setVisibility(View.GONE);
        if (btnRetry != null) btnRetry.setVisibility(View.GONE);
    }

    public void showSuccess() {
        if (isSearchMode) return;
        if (cardSelector != null) cardSelector.setVisibility(View.VISIBLE);
        if (layoutTableHeader != null) layoutTableHeader.setVisibility(View.VISIBLE);
        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (rvStandings != null) rvStandings.setVisibility(View.VISIBLE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);
        if (tvMessage != null) tvMessage.setVisibility(View.GONE);
        if (btnRetry != null) btnRetry.setVisibility(View.GONE);
    }

    public void showError(String message) {
        if (isSearchMode) return;
        if (cardSelector != null) cardSelector.setVisibility(View.VISIBLE);
        if (layoutTableHeader != null) layoutTableHeader.setVisibility(View.GONE);
        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (rvStandings != null) rvStandings.setVisibility(View.GONE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);
        if (tvMessage != null) {
            tvMessage.setText(message);
            tvMessage.setVisibility(View.VISIBLE);
        }
        if (btnRetry != null) btnRetry.setVisibility(View.VISIBLE);
    }

    public void showEmpty(String message) {
        if (isSearchMode) return;
        if (cardSelector != null) cardSelector.setVisibility(View.VISIBLE);
        if (layoutTableHeader != null) layoutTableHeader.setVisibility(View.GONE);
        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (rvStandings != null) rvStandings.setVisibility(View.GONE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);
        if (tvMessage != null) {
            tvMessage.setText(message);
            tvMessage.setVisibility(View.VISIBLE);
        }
        if (btnRetry != null) btnRetry.setVisibility(View.GONE);
    }
}
