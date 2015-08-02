/*
 * Copyright (C) 2015 Joey Turczak
 */

package com.joeyturczak.spotifystreamer.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;

import com.joeyturczak.spotifystreamer.R;
import com.joeyturczak.spotifystreamer.models.MyArtist;
import com.joeyturczak.spotifystreamer.services.MediaPlayerService;
import com.joeyturczak.spotifystreamer.utils.Utility;

public class MainActivity extends AppCompatActivity implements ArtistSearchFragment.Callback, android.support.v4.app.FragmentManager.OnBackStackChangedListener {

    private static final String SEARCHFRAGMENT_TAG = "SFTAG";
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static final String PLAYERFRAGMENT_TAG = "PFTAG";

    private boolean mIsLargeLayout;

    private MyArtist mMyArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        displayUp();

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.artistSearchFragment, new ArtistSearchFragment(), SEARCHFRAGMENT_TAG)
                    .commit();
        } else {
            Bundle bundle = savedInstanceState.getBundle(getString(R.string.bundle_key_bundle));
            if(bundle != null) {
                mMyArtist = bundle.getParcelable(getString(R.string.bundle_key_artist));
                if(mMyArtist != null) {
                    getSupportActionBar().setSubtitle(mMyArtist.getArtistName());
                }
            }
        }

        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);

        if (mIsLargeLayout) {
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.artistDetailFragment, new ArtistDetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.bundle_key_artist), mMyArtist);
        outState.putBundle(getString(R.string.bundle_key_bundle), bundle);
    }

    @Override
    public void onBackStackChanged() {
        displayUp();
    }

    public void displayUp() {
        boolean up = getSupportFragmentManager().getBackStackEntryCount() > 0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(up);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().popBackStack();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fragment, menu);

        if(Utility.isServiceRunning(this, MediaPlayerService.class)) {
            MenuItem menuItem = menu.findItem(R.id.action_now_playing);
            menuItem.setVisible(true);
            menuItem = menu.findItem(R.id.action_share);
            menuItem.setVisible(true);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String shareUrl = sharedPreferences.getString(getString(R.string.share_key), "");
            ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
                shareActionProvider.setShareIntent(createShareIntent(shareUrl));
        }
        return true;
    }

    /** Creates an intent for sharing the currently playing track's URL */
    private Intent createShareIntent(String shareUrl) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.action_now_playing) {
            showDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(MyArtist artist) {
        mMyArtist = artist;
        getSupportActionBar().setSubtitle(artist.getArtistName());

        Utility.hideKeyboard(this);

        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.intent_key_artist), artist);

        ArtistDetailFragment fragment = new ArtistDetailFragment();
        fragment.setArguments(bundle);
        if (mIsLargeLayout) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.artistDetailFragment, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.artistSearchFragment, fragment, DETAILFRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
        }
    }

    /**  Launches the music player from the now playing button in the menu bar with dummy data to be replaced with data from the media service. */
    public void showDialog() {
        Utility.hideKeyboard(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        PlayerFragment playerFragment = new PlayerFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(getString(R.string.bundle_position_key), -1);
        playerFragment.setArguments(bundle);

        if (mIsLargeLayout) {
            // The device is using a large layout, so show the fragment as a dialog
            playerFragment.show(fragmentManager, "dialog");
        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.artistSearchFragment, playerFragment, PLAYERFRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
        }
    }


}
