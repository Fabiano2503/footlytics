package com.fabianoanticona.footlytics.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.fabianoanticona.footlytics.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ExploreFragment exploreFragment;
    private FavoritesFragment favoritesFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        if (savedInstanceState == null) {
            exploreFragment = ExploreFragment.newInstance();
            favoritesFragment = FavoritesFragment.newInstance();

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentContainer, exploreFragment, "EXPLORE")
                    .add(R.id.fragmentContainer, favoritesFragment, "FAVORITES")
                    .hide(favoritesFragment)
                    .commit();

            activeFragment = exploreFragment;
        } else {
            exploreFragment = (ExploreFragment) getSupportFragmentManager().findFragmentByTag("EXPLORE");
            favoritesFragment = (FavoritesFragment) getSupportFragmentManager().findFragmentByTag("FAVORITES");

            if (favoritesFragment != null && favoritesFragment.isVisible()) {
                activeFragment = favoritesFragment;
            } else {
                activeFragment = exploreFragment;
            }
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_explore) {
                if (exploreFragment != null && activeFragment != exploreFragment) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .hide(activeFragment)
                            .show(exploreFragment)
                            .commit();
                    activeFragment = exploreFragment;
                }
                return true;
            } else if (itemId == R.id.nav_favorites) {
                if (favoritesFragment != null && activeFragment != favoritesFragment) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .hide(activeFragment)
                            .show(favoritesFragment)
                            .commit();
                    activeFragment = favoritesFragment;
                }
                return true;
            }
            return false;
        });
    }
}
