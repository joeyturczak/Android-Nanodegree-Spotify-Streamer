/*
 * Copyright (C) 2015 Joey Turczak
 */

package com.joeyturczak.spotifystreamer.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.joeyturczak.spotifystreamer.R;
import com.joeyturczak.spotifystreamer.adapters.ArtistListAdapter;
import com.joeyturczak.spotifystreamer.models.MyArtist;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.client.Response;

/**
 * Takes a search query from the user and displays the relevant artists in a list.
 */
public class ArtistSearchFragment extends Fragment {

    private static final String LOG_TAG = ArtistSearchFragment.class.getSimpleName();

    private Context mContext;
    private ArtistListAdapter mArtistListAdapter;
    private Toast mSearchToast;

    private ArrayList<MyArtist> mMyArtists;

    public ArtistSearchFragment() {
    }

    public interface Callback {
        public void onItemSelected(MyArtist artist);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mMyArtists = new ArrayList<>();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(mContext.getString(R.string.bundle_key_artist), mMyArtists);
        outState.putBundle(mContext.getString(R.string.bundle_key_bundle), bundle);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {
            Bundle bundle = savedInstanceState.getBundle(mContext.getString(R.string.bundle_key_bundle));
            if (bundle != null) {
                mMyArtists = bundle.getParcelableArrayList(mContext.getString(R.string.bundle_key_artist));
            }
        }
        if (mMyArtists == null) {
            mMyArtists = new ArrayList<>();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mArtistListAdapter.refill(mMyArtists);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView listView = (ListView) mRootView.findViewById(R.id.artistListView);

        mArtistListAdapter = new ArtistListAdapter(mContext, R.layout.list_item_artist, mMyArtists);
        listView.setAdapter(mArtistListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((Callback) getActivity())
                        .onItemSelected(mMyArtists.get(position));
            }
        });

        EditText artistSearchField = (EditText) mRootView.findViewById(R.id.artistSearchField);

        artistSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count > 0) {
                    spotifySearch(s.toString());
                }
                else {
                    mMyArtists.clear();
                    mArtistListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return mRootView;
    }

    /** Searches Spotify via the searchArtists API and sends the relevant data to the list adapter. */
    private void spotifySearch(String query) {

        SpotifyApi api = new SpotifyApi();
        final SpotifyService spotify = api.getService();

        spotify.searchArtists(query, new SpotifyCallback<ArtistsPager>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                Log.d(LOG_TAG, spotifyError.toString());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mSearchToast != null) {
                            mSearchToast.cancel();
                        }
                        mSearchToast = Toast.makeText(mContext, R.string.toast_network_error, Toast.LENGTH_SHORT);
                        mSearchToast.show();
                    }
                });
            }

            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                final List<Artist> artists = artistsPager.artists.items;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMyArtists.clear();
                        mArtistListAdapter.notifyDataSetChanged();
                        if(artists.size() > 0) {
                            for(int i = 0; i < artists.size(); i++) {
                                String name = artists.get(i).name;
                                String imageUrl = "";
                                if(artists.get(i).images.size() > 0) {
                                    imageUrl = artists.get(i).images.get(0).url;
                                }
                                String id = artists.get(i).id;
                                mMyArtists.add(new MyArtist(name, imageUrl, id));
                            }
                        }
                        else {
                            if (mSearchToast != null) {
                                mSearchToast.cancel();
                            }
                            mSearchToast = Toast.makeText(mContext, R.string.toast_no_artists_message, Toast.LENGTH_SHORT);
                            mSearchToast.show();
                        }
                    }
                });
            }
        });
    }
}
