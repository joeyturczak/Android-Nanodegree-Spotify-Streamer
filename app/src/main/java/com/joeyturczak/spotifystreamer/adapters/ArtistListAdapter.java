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

import com.joeyturczak.spotifystreamer.models.MyArtist;
import com.joeyturczak.spotifystreamer.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by joeyturczak on 6/20/15.
 */

/**
 * Populates the list view for the ArtistSearchFragment with artists.
 */
public class ArtistListAdapter extends ArrayAdapter<MyArtist> {

    private ArrayList<MyArtist> mMyArtists;
    private Context mContext;

    public ArtistListAdapter(Context context, int resource, ArrayList<MyArtist> artists) {
        super(context, resource, artists);
        mContext = context;
        mMyArtists = artists;
    }

    @Override
    public int getCount() {
        return mMyArtists.size();
    }

    @Override
    public MyArtist getItem(int position) {
        return mMyArtists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_artist, parent, false);
        }
        ImageView artistImageView = (ImageView) convertView.findViewById(R.id.artistImageView);
        TextView artistName = (TextView) convertView.findViewById(R.id.artistName);

        MyArtist artist;

        if(mMyArtists.size() > 0) {
            artist = getItem(position);
            artistName.setText(artist.getArtistName());
            String imageUrl = artist.getArtistImageUrl();
            if(!imageUrl.isEmpty()) {
                Picasso.with(mContext).load(imageUrl).into(artistImageView);
            }
            else {
                artistImageView.setImageResource(android.R.drawable.gallery_thumb);
            }
        }

        return convertView;
    }

    /** Repopulates the list with new data. */
    public void refill(ArrayList<MyArtist> artists) {
        mMyArtists = artists;
        notifyDataSetChanged();
    }
}
