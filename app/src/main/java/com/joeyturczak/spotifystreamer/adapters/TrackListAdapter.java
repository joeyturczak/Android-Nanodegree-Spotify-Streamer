/*
 * Copyright (C) 2015 Joey Turczak
 */

package com.joeyturczak.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.joeyturczak.spotifystreamer.R;
import com.joeyturczak.spotifystreamer.models.MyTrack;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by joeyturczak on 6/20/15.
 */

/**
 * Populates the list view for the ArtistDetailFragment with top ten tracks from the artist.
 */
public class TrackListAdapter extends ArrayAdapter<MyTrack> {

    private ArrayList<MyTrack> mMyTracks;
    private Context mContext;

    public TrackListAdapter(Context context, int resource, ArrayList<MyTrack> tracks) {
        super(context, resource, tracks);
        mContext = context;
        mMyTracks = tracks;
    }

    @Override
    public int getCount() {
        return mMyTracks.size();
    }

    @Override
    public MyTrack getItem(int position) {
        return mMyTracks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_track, parent, false);
        }
        ImageView albumArtImageView = (ImageView) convertView.findViewById(R.id.albumArtImageView);
        TextView trackName = (TextView) convertView.findViewById(R.id.trackName);
        TextView albumName = (TextView) convertView.findViewById(R.id.albumName);

        MyTrack track;

        if (mMyTracks.size() > 0) {
            track = getItem(position);
            trackName.setText(track.getTrackName());
            albumName.setText(track.getTrackAlbumName());
            String imageUrl = track.getTrackAlbumImageUrl();
            if(!imageUrl.isEmpty()) {
                Picasso.with(mContext).load(imageUrl).into(albumArtImageView);
            }
            else {
                albumArtImageView.setImageResource(android.R.drawable.picture_frame);
            }
        }

        return convertView;
    }
}