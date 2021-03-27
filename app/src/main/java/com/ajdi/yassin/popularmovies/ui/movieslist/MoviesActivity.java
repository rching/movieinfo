package com.ajdi.yassin.popularmovies.ui.movieslist;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.ajdi.yassin.popularmovies.LoadingActivity;
import com.ajdi.yassin.popularmovies.R;
import com.ajdi.yassin.popularmovies.ui.movieslist.discover.DiscoverMoviesFragment;
import com.ajdi.yassin.popularmovies.ui.movieslist.favorites.FavoritesFragment;
import com.ajdi.yassin.popularmovies.utils.ActivityUtils;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.AdSettings;

public class MoviesActivity extends AppCompatActivity implements AdListener {

    //fb ad
    private FrameLayout bannerAdContainer;
    private @Nullable
    AdView bannerAdView;

    private void loadAdView() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
            bannerAdView = null;
        }
        AdSettings.addTestDevice("7db7c889-cc72-4856-b065-8daea994e0c1");
        bannerAdContainer = (FrameLayout) findViewById(R.id.bannerAdContainer);

        bannerAdView =
                new AdView(
                        this,
                        "1580833685460344_1580834642126915", AdSize.BANNER_HEIGHT_50);

        // Reposition the ad and add it to the view hierarchy.
        bannerAdContainer.addView(bannerAdView);
        // Initiate a request to load an ad.
        bannerAdView.loadAd(bannerAdView.buildLoadAdConfig().withAdListener(this).build());
    }


    @Override
    public void onError(Ad ad, AdError error) {
        if (ad == bannerAdView) {
            Log.d("Mopvie","Ad failed to load: " + error.getErrorMessage());
        }
    }

    @Override
    public void onAdLoaded(Ad ad) {

    }

    @Override
    public void onAdClicked(Ad ad) {
        Toast.makeText(this, "Ad Clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoggingImpression(Ad ad) {
        Log.d("Movies", "onLoggingImpression");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AudienceNetworkInitializeHelper.initialize(this);
        loadAdView();
        if (savedInstanceState == null) {
            Intent intent = new Intent(this, LoadingActivity.class);
            startActivity(intent);
            setupViewFragment();
        }
        setupToolbar();
        setupBottomNavigation();
    }
    private void setupViewFragment() {
        // show discover movies fragment by default
        DiscoverMoviesFragment discoverMoviesFragment = DiscoverMoviesFragment.newInstance();
        ActivityUtils.replaceFragmentInActivity(
                getSupportFragmentManager(), discoverMoviesFragment, R.id.fragment_container);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_discover:
                        ActivityUtils.replaceFragmentInActivity(
                                getSupportFragmentManager(), DiscoverMoviesFragment.newInstance(),
                                R.id.fragment_container);
                        return true;
                    case R.id.action_favorites:
                        ActivityUtils.replaceFragmentInActivity(
                                getSupportFragmentManager(), FavoritesFragment.newInstance(),
                                R.id.fragment_container);
                        return true;
                }
                return false;
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

}
