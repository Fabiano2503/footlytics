package com.fabianoanticona.footlytics.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fabianoanticona.footlytics.R;
import com.fabianoanticona.footlytics.api.ApiService;
import com.fabianoanticona.footlytics.api.RetrofitClient;
import com.fabianoanticona.footlytics.data.DataCache;
import com.fabianoanticona.footlytics.model.CompetitionItem;
import com.fabianoanticona.footlytics.model.StandingItem;
import com.fabianoanticona.footlytics.model.TeamItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExploreFragment extends Fragment {

    private TextView tvAppTitle;
    private ImageView ivSearchIcon;
    private View layoutSearch;
    private EditText etSearch;
    private RecyclerView rvCompetitions;
    private View layoutTableHeader;
    private RecyclerView rvStandings;
    private RecyclerView rvSearchResults;
    private ProgressBar pbLoading;
    private TextView tvMessage;
    private Button btnRetry;

    private CompetitionAdapter competitionAdapter;
    private StandingAdapter standingAdapter;
    private TeamAdapter teamAdapter;

    private final List<CompetitionItem> competitions = new ArrayList<>();
    private int selectedCompetitionId = -1;

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
        setupBackNavigation();

        if (!isInitialized) {
            isInitialized = true;
            loadCompetitions();
        }
    }

    private void initViews(View view) {
        tvAppTitle = view.findViewById(R.id.tvAppTitle);
        ivSearchIcon = view.findViewById(R.id.ivSearchIcon);
        layoutSearch = view.findViewById(R.id.layoutSearch);
        etSearch = view.findViewById(R.id.etSearch);
        rvCompetitions = view.findViewById(R.id.rvCompetitions);
        layoutTableHeader = view.findViewById(R.id.layoutTableHeader);
        rvStandings = view.findViewById(R.id.rvStandings);
        rvSearchResults = view.findViewById(R.id.rvSearchResults);
        pbLoading = view.findViewById(R.id.pbLoading);
        tvMessage = view.findViewById(R.id.tvMessage);
        btnRetry = view.findViewById(R.id.btnRetry);
    }

    private void setupRecyclerView() {
        competitionAdapter = new CompetitionAdapter((competition, position) -> {
            if (competition != null && competition.getId() != selectedCompetitionId) {
                selectedCompetitionId = competition.getId();
                competitionAdapter.setSelectedPosition(position);
                if (isSearchMode) {
                    showExploreState();
                }
                fetchStandings(selectedCompetitionId);
            }
        });
        rvCompetitions.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCompetitions.setAdapter(competitionAdapter);

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
        if (ivSearchIcon != null) {
            ivSearchIcon.setOnClickListener(v -> showSearchState());
        }

        if (btnRetry != null) {
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
        }

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().trim();
                    if (query.isEmpty()) {
                        if (teamAdapter != null) {
                            teamAdapter.updateTeams(new ArrayList<>());
                        }
                    } else if (query.length() >= 2) {
                        performSearch(query);
                    } else {
                        if (teamAdapter != null) {
                            teamAdapter.updateTeams(new ArrayList<>());
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    private void setupBackNavigation() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (layoutSearch != null && layoutSearch.getVisibility() == View.VISIBLE) {
                    showExploreState(); // Exit search mode
                } else {
                    this.setEnabled(false); // Let the system handle the normal back press
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void showExploreState() {
        isSearchMode = false;
        currentSearchQuery = "";

        if (tvAppTitle != null) tvAppTitle.setVisibility(View.VISIBLE);
        if (ivSearchIcon != null) ivSearchIcon.setVisibility(View.VISIBLE);
        if (rvCompetitions != null) rvCompetitions.setVisibility(View.VISIBLE);
        if (layoutTableHeader != null) layoutTableHeader.setVisibility(View.VISIBLE);
        if (rvStandings != null) rvStandings.setVisibility(View.VISIBLE);

        if (layoutSearch != null) layoutSearch.setVisibility(View.GONE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);

        hideKeyboard();

        if (etSearch != null) {
            etSearch.setText("");
        }
    }

    private void showSearchState() {
        isSearchMode = true;

        if (tvAppTitle != null) tvAppTitle.setVisibility(View.GONE);
        if (ivSearchIcon != null) ivSearchIcon.setVisibility(View.GONE);
        if (rvCompetitions != null) rvCompetitions.setVisibility(View.GONE);
        if (layoutTableHeader != null) layoutTableHeader.setVisibility(View.GONE);
        if (rvStandings != null) rvStandings.setVisibility(View.GONE);

        if (layoutSearch != null) layoutSearch.setVisibility(View.VISIBLE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.VISIBLE);

        if (teamAdapter != null) {
            teamAdapter.updateTeams(new ArrayList<>());
        }

        if (etSearch != null) {
            etSearch.requestFocus();
            etSearch.post(() -> showKeyboard(etSearch));
        }
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
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

    private void showSearchLoading() {
        if (rvCompetitions != null) rvCompetitions.setVisibility(View.GONE);
        if (layoutTableHeader != null) layoutTableHeader.setVisibility(View.GONE);
        if (rvStandings != null) rvStandings.setVisibility(View.GONE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);

        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (tvMessage != null) tvMessage.setVisibility(View.GONE);
        if (btnRetry != null) btnRetry.setVisibility(View.GONE);
    }

    private void showSearchSuccess() {
        if (rvCompetitions != null) rvCompetitions.setVisibility(View.GONE);
        if (layoutTableHeader != null) layoutTableHeader.setVisibility(View.GONE);
        if (rvStandings != null) rvStandings.setVisibility(View.GONE);

        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (tvMessage != null) tvMessage.setVisibility(View.GONE);
        if (btnRetry != null) btnRetry.setVisibility(View.GONE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.VISIBLE);
    }

    private void showSearchEmpty(String message) {
        if (rvCompetitions != null) rvCompetitions.setVisibility(View.GONE);
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
        if (rvCompetitions != null) rvCompetitions.setVisibility(View.GONE);
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
        List<CompetitionItem> cachedCompetitions = DataCache.getInstance().getCompetitions();
        if (cachedCompetitions != null && !cachedCompetitions.isEmpty()) {
            competitions.clear();
            competitions.addAll(cachedCompetitions);
            DataCache.getInstance().clear();

            competitionAdapter.setCompetitions(competitions);

            int targetIndex = findCompetitionIndexByName("LaLiga");
            if (targetIndex != -1) {
                selectedCompetitionId = competitions.get(targetIndex).getId();
                competitionAdapter.setSelectedPosition(targetIndex);
            } else {
                selectedCompetitionId = competitions.get(0).getId();
                competitionAdapter.setSelectedPosition(0);
            }

            fetchStandings(selectedCompetitionId);
            return;
        }

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

                    competitionAdapter.setCompetitions(competitions);

                    int targetIndex = findCompetitionIndexByName("LaLiga");
                    if (targetIndex != -1) {
                        selectedCompetitionId = competitions.get(targetIndex).getId();
                        competitionAdapter.setSelectedPosition(targetIndex);
                    } else {
                        selectedCompetitionId = competitions.get(0).getId();
                        competitionAdapter.setSelectedPosition(0);
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
        if (rvCompetitions != null) rvCompetitions.setVisibility(View.VISIBLE);
        if (layoutTableHeader != null) layoutTableHeader.setVisibility(View.GONE);
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (rvStandings != null) rvStandings.setVisibility(View.GONE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);
        if (tvMessage != null) tvMessage.setVisibility(View.GONE);
        if (btnRetry != null) btnRetry.setVisibility(View.GONE);
    }

    public void showSuccess() {
        if (isSearchMode) return;
        if (rvCompetitions != null) rvCompetitions.setVisibility(View.VISIBLE);
        if (layoutTableHeader != null) layoutTableHeader.setVisibility(View.VISIBLE);
        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (rvStandings != null) rvStandings.setVisibility(View.VISIBLE);
        if (rvSearchResults != null) rvSearchResults.setVisibility(View.GONE);
        if (tvMessage != null) tvMessage.setVisibility(View.GONE);
        if (btnRetry != null) btnRetry.setVisibility(View.GONE);
    }

    public void showError(String message) {
        if (isSearchMode) return;
        if (rvCompetitions != null) rvCompetitions.setVisibility(View.VISIBLE);
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
        if (rvCompetitions != null) rvCompetitions.setVisibility(View.VISIBLE);
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
