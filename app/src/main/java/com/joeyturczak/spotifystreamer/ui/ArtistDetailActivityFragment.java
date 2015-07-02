/*
 * Copyright (C) 2015 Joey Turczak
 */

package com.joeyturczak.spotifystreamer.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.joeyturczak.spotifystreamer.R;
import com.joeyturczak.spotifystreamer.adapters.TrackListAdapter;
import com.joeyturczak.spotifystreamer.models.MyArtist;
import com.joeyturczak.spotifystreamer.models.MyTrack;

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
 * A placeholder fragment containing a simple view.
 */
public class ArtistDetailActivityFragment extends Fragment {

    private static final String LOG_TAG = ArtistDetailActivityFragment.class.getSimpleName();

    private Context mContext;
    private TrackListAdapter mTrackListAdapter;

    private ArrayList<MyTrack> mMyTracks;

    private MyArtist mMyArtist;

    public ArtistDetailActivityFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mMyTracks = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist_detail, container, false);

        Intent intent = getActivity().getIntent();
        if(intent != null && intent.hasExtra(mContext.getString(R.string.intent_key_artist))) {
            mMyArtist = intent.getParcelableExtra(mContext.getString(R.string.intent_key_artist));
        }

        ListView artistListView = (ListView) rootView.findViewById(R.id.artistListView);

        mTrackListAdapter = new TrackListAdapter(mContext, R.layout.list_item_track, mMyTracks);
        artistListView.setAdapter(mTrackListAdapter);

        spotifySearch();

        return rootView;
    }

    /** Gets the top tracks of the artist from Spotify via the getArtistTopTrack API and sends the relevant data to the list adapter. */
    private void spotifySearch() {

        SpotifyApi api = new SpotifyApi();
        final SpotifyService spotify = api.getService();

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(SpotifyService.COUNTRY, mContext.getString(R.string.country_code));

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
                                String trackName = trackList.get(i).name;
                                String albumName = trackList.get(i).album.name;
                                String imageUrl = "";
                                if(trackList.get(i).album.images.size() > 0) {
                                    imageUrl = trackList.get(i).album.images.get(0).url;
                                }
                                mTrackListAdapter.add(new MyTrack(trackName, albumName, imageUrl));
                            }
                        }
                    }
                });
            }
        });
    }
}
