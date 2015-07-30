/*
 * Copyright (C) 2015 Joey Turczak
 */

package com.joeyturczak.spotifystreamer.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.joeyturczak.spotifystreamer.R;
import com.joeyturczak.spotifystreamer.adapters.TrackListAdapter;
import com.joeyturczak.spotifystreamer.models.MyArtist;
import com.joeyturczak.spotifystreamer.models.MyTrack;
import com.joeyturczak.spotifystreamer.utils.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.client.Response;

/**
 * Shows selected artist's top ten tracks in a list view.
 */
public class ArtistDetailFragment extends Fragment {

    private static final String LOG_TAG = ArtistDetailFragment.class.getSimpleName();

    private static final String PLAYERFRAGMENT_TAG = "PFTAG";

    private Context mContext;
    private TrackListAdapter mTrackListAdapter;

    private int mPosition;

    private ArrayList<MyTrack> mMyTracks;

    private MyArtist mMyArtist;

    private boolean mIsLargeLayout;

    private String mCountryCode;

    private Toast mTopTracksToast;

    public ArtistDetailFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mMyTracks = new ArrayList<>();
        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);
        mCountryCode = Utility.getCountryCode(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(!mMyTracks.isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(getString(R.string.bundle_tracks_key), mMyTracks);
            bundle.putInt(getString(R.string.bundle_position_key), mPosition);
            outState.putBundle(getString(R.string.bundle_key_bundle), bundle);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_detail, container, false);

        if(getArguments() != null) {
            Bundle arguments = getArguments();
            mMyArtist = arguments.getParcelable(getString(R.string.bundle_key_artist));
        }

        if(savedInstanceState != null) {
            Bundle bundle = savedInstanceState.getBundle(getString(R.string.bundle_key_bundle));
            if(bundle != null) {
                mMyTracks = bundle.getParcelableArrayList(getString(R.string.bundle_tracks_key));
                mPosition = bundle.getInt(getString(R.string.bundle_position_key));
            }
        }

        ListView listView = (ListView) rootView.findViewById(R.id.artistListView);

        mTrackListAdapter = new TrackListAdapter(mContext, R.layout.list_item_track, mMyTracks);
        listView.setAdapter(mTrackListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = position;
                showDialog();
            }
        });

        if(mMyArtist != null) {
            if (mMyTracks.isEmpty()) {
                spotifySearch();
            }
        }

        return rootView;
    }

    /**  Launches the music player fragment. The music player will display in a dialog screen if the device is a tablet. */
    public void showDialog() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        PlayerFragment playerFragment = new PlayerFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(mContext.getString(R.string.bundle_tracks_key), mMyTracks);
        bundle.putInt(mContext.getString(R.string.bundle_position_key), mPosition);

        playerFragment.setArguments(bundle);

        if (mIsLargeLayout) {
            // The device is using a large layout, so show the fragment as a dialog
            playerFragment.show(fragmentManager, "dialog");
        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.artistSearchFragment, playerFragment, PLAYERFRAGMENT_TAG)
                    .addToBackStack(PLAYERFRAGMENT_TAG)
                    .commit();
        }
    }


    /** Gets the top tracks of the artist from Spotify via the getArtistTopTrack API and sends the relevant data to the list adapter. */
    private void spotifySearch() {

        SpotifyApi api = new SpotifyApi();
        final SpotifyService spotify = api.getService();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(SpotifyService.COUNTRY, mCountryCode);

        spotify.getArtistTopTrack(mMyArtist.getArtistId(), map, new SpotifyCallback<Tracks>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                Log.d(LOG_TAG, spotifyError.toString());
            }

            @Override
            public void success(Tracks tracks, Response response) {
                final List<Track> trackList = tracks.tracks;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMyTracks.clear();
                        mTrackListAdapter.notifyDataSetChanged();

                        if (trackList.size() > 0) {

                            for(int i = 0; i < trackList.size(); i++) {
                                String artistName = mMyArtist.getArtistName();
                                String trackName = trackList.get(i).name;
                                String albumName = trackList.get(i).album.name;
                                String imageUrl = "";
                                if(trackList.get(i).album.images.size() > 0) {
                                    imageUrl = trackList.get(i).album.images.get(0).url;
                                }
                                String previewUrl = trackList.get(i).preview_url;

                                mTrackListAdapter.add(new MyTrack(artistName, trackName, albumName, imageUrl, previewUrl));
                            }
                        } else {
                            if(mTopTracksToast != null) {
                                mTopTracksToast.cancel();
                            }
                            mTopTracksToast = Toast.makeText(getActivity(), R.string.toast_no_tracks, Toast.LENGTH_SHORT);
                            mTopTracksToast.show();
                        }
                    }
                });
            }
        });
    }
}
