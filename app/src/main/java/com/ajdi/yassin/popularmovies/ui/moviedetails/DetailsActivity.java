package com.ajdi.yassin.popularmovies.ui.moviedetails;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.ajdi.yassin.popularmovies.R;
import com.ajdi.yassin.popularmovies.data.local.model.MovieDetails;
import com.ajdi.yassin.popularmovies.data.local.model.Resource;
import com.ajdi.yassin.popularmovies.databinding.ActivityDetailsBinding;
import com.ajdi.yassin.popularmovies.ui.moviedetails.cast.CastAdapter;
import com.ajdi.yassin.popularmovies.ui.moviedetails.reviews.ReviewsAdapter;
import com.ajdi.yassin.popularmovies.ui.moviedetails.trailers.TrailersAdapter;
import com.ajdi.yassin.popularmovies.ui.movieslist.AudienceNetworkInitializeHelper;
import com.ajdi.yassin.popularmovies.utils.Constants;
import com.ajdi.yassin.popularmovies.utils.Injection;
import com.ajdi.yassin.popularmovies.utils.UiUtils;
import com.ajdi.yassin.popularmovies.utils.ViewModelFactory;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdExperienceType;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.RewardData;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.S2SRewardedVideoAdListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DetailsActivity extends AppCompatActivity implements S2SRewardedVideoAdListener {

    public static final String EXTRA_MOVIE_ID = "extra_movie_id";

    private static final int DEFAULT_ID = -1;

    private ActivityDetailsBinding mBinding;

    private MovieDetailsViewModel mViewModel;
    //fb ad
    private FrameLayout bannerAdContainer;
    private @Nullable
    RewardedVideoAd rewardedVideoAd;

    private void loadAdView() {
        if (rewardedVideoAd != null) {
            rewardedVideoAd.destroy();
            rewardedVideoAd = null;
        }


        rewardedVideoAd =
                    new RewardedVideoAd(this, "1580833685460344_1581236835420029");
        RewardedVideoAd.RewardedVideoLoadAdConfig loadAdConfig =
                rewardedVideoAd
                        .buildLoadAdConfig()
                        .withAdListener(this)
                        .withFailOnCacheFailureEnabled(true)
                        .withRewardData(new RewardData("YOUR_USER_ID", "YOUR_REWARD", 10))
                        .withAdExperience(
                                 AdExperienceType.AD_EXPERIENCE_TYPE_REWARDED)
                        .build();
        rewardedVideoAd.loadAd(loadAdConfig);



        // Reposition the ad and add it to the view hierarchy.
       // bannerAdContainer.addView(rewardedVideoAd);
        // Initiate a request to load an ad.
    }

    @Override
    public void onRewardedVideoCompleted() {
        Log.d("video ad","Rewarded Video View Complete");
    }

    @Override
    public void onLoggingImpression(Ad ad) {
        Log.d("video ad","Rewarded Video Impression");
    }

    @Override
    public void onRewardedVideoClosed() {
        Log.d("video ad","Rewarded Video Closed");
    }

    @Override
    public void onRewardServerFailed() {
        Log.d("video ad","Reward Video Server Failed");
    }

    @Override
    public void onRewardServerSuccess() {
        Log.d("video ad","Reward Video Server Succeeded");
    }
    @Override
    public void onError(Ad ad, AdError error) {
        if (ad == rewardedVideoAd) {
            Log.d("video ad","Ad failed to load: " + error.getErrorMessage());
        }
    }

    @Override
    public void onAdLoaded(Ad ad) {
        if (rewardedVideoAd == null
                || !rewardedVideoAd.isAdLoaded()
                || rewardedVideoAd.isAdInvalidated()) {
            Log.d("video ad","Ad not loaded. Click load to request an ad.");
        } else {
            rewardedVideoAd.show();
            Log.d("video ad","ok");
        }

    }

    @Override
    public void onAdClicked(Ad ad) {
        Toast.makeText(this, "Ad Clicked", Toast.LENGTH_SHORT).show();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppThemeLight);
        super.onCreate(savedInstanceState);
        final long movieId = getIntent().getLongExtra(EXTRA_MOVIE_ID, DEFAULT_ID);
        if (movieId == DEFAULT_ID) {
            closeOnError();
            return;
        }

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_details);
        mBinding.setLifecycleOwner(this);

        mViewModel = obtainViewModel();
        mViewModel.init(movieId);
        setupToolbar();
        setupTrailersAdapter();
        setupCastAdapter();
        setupReviewsAdapter();
        // observe result
        mViewModel.getResult().observe(this, new Observer<Resource<MovieDetails>>() {
            @Override
            public void onChanged(Resource<MovieDetails> resource) {
                if (resource.data != null &&
                        resource.data.getMovie() != null) {
                    mViewModel.setFavorite(resource.data.getMovie().isFavorite());
                    invalidateOptionsMenu();
                }
                mBinding.setResource(resource);
                mBinding.setMovieDetails(resource.data);
            }
        });
        // handle retry event in case of network failure
        mBinding.networkState.retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewModel.retry(movieId);

            }
        });
        // Observe snackbar messages
        mViewModel.getSnackbarMessage().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer message) {
                Snackbar.make(mBinding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
            }
        });

        AudienceNetworkInitializeHelper.initialize(this);
        loadAdView();



    }

    private void setupToolbar() {
        Toolbar toolbar = mBinding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            handleCollapsedToolbarTitle();
        }
    }

    private void setupTrailersAdapter() {
        RecyclerView listTrailers = mBinding.movieDetailsInfo.listTrailers;
        listTrailers.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        listTrailers.setHasFixedSize(true);
        listTrailers.setAdapter(new TrailersAdapter());
        ViewCompat.setNestedScrollingEnabled(listTrailers, false);
    }

    private void setupCastAdapter() {
        RecyclerView listCast = mBinding.movieDetailsInfo.listCast;
        listCast.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        listCast.setAdapter(new CastAdapter());
        ViewCompat.setNestedScrollingEnabled(listCast, false);
    }

    private void setupReviewsAdapter() {
        RecyclerView listReviews = mBinding.movieDetailsInfo.listReviews;
        listReviews.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        listReviews.setAdapter(new ReviewsAdapter());
        ViewCompat.setNestedScrollingEnabled(listReviews, false);
    }

    private MovieDetailsViewModel obtainViewModel() {
        ViewModelFactory factory = Injection.provideViewModelFactory(this);
        return ViewModelProviders.of(this, factory).get(MovieDetailsViewModel.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movie_details, menu);
        UiUtils.tintMenuIcon(this, menu.findItem(R.id.action_share), R.color.md_white_1000);

        MenuItem favoriteItem = menu.findItem(R.id.action_favorite);
        if (mViewModel.isFavorite()) {
            favoriteItem.setIcon(R.drawable.ic_favorite_black_24dp)
                    .setTitle(R.string.action_remove_from_favorites);
        } else {
            favoriteItem.setIcon(R.drawable.ic_favorite_border_black_24dp)
                    .setTitle(R.string.action_favorite);
        }
        UiUtils.tintMenuIcon(this, favoriteItem, R.color.md_white_1000);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share: {
                MovieDetails movieDetails = mViewModel.getResult().getValue().data;
                Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                        .setType("text/plain")
                        .setSubject(movieDetails.getMovie().getTitle() + " movie trailer")
                        .setText("Check out " + movieDetails.getMovie().getTitle() + " movie trailer at " +
                                Uri.parse(Constants.YOUTUBE_WEB_URL +
                                        movieDetails.getTrailers().get(0).getKey())
                        )
                        .createChooserIntent();

                int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
                if (Build.VERSION.SDK_INT >= 21)
                    flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;

                shareIntent.addFlags(flags);
                if (shareIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(shareIntent);
                }
                return true;
            }
            case R.id.action_favorite: {
                mViewModel.onFavoriteClicked();
                invalidateOptionsMenu();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void closeOnError() {
        throw new IllegalArgumentException("Access denied.");
    }

    /**
     * sets the title on the toolbar only if the toolbar is collapsed
     */
    private void handleCollapsedToolbarTitle() {
        mBinding.appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                // verify if the toolbar is completely collapsed and set the movie name as the title
                if (scrollRange + verticalOffset == 0) {
                    mBinding.collapsingToolbar.setTitle(
                            mViewModel.getResult().getValue().data.getMovie().getTitle());
                    isShow = true;
                } else if (isShow) {
                    // display an empty string when toolbar is expanded
                    mBinding.collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }
}
