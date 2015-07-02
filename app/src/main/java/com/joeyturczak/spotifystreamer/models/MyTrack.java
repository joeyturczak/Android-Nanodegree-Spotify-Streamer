/*
 * Copyright (C) 2015 Joey Turczak
 */

package com.joeyturczak.spotifystreamer.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by joeyturczak on 6/25/15.
 */

/**
 * Stores the relevant track information from the Spotify Track model.
 */
public class MyTrack implements Parcelable {

    String mTrackName;
    String mTrackAlbumName;
    String mTrackAlbumImageUrl;

    public MyTrack(String name, String albumName, String imageUrl) {
        mTrackName = name;
        mTrackAlbumName = albumName;
        mTrackAlbumImageUrl = imageUrl;
    }

    protected MyTrack(Parcel in) {
        mTrackName = in.readString();
        mTrackAlbumName = in.readString();
        mTrackAlbumImageUrl = in.readString();
    }

    public static final Creator<MyTrack> CREATOR = new Creator<MyTrack>() {
        @Override
        public MyTrack createFromParcel(Parcel in) {
            return new MyTrack(in);
        }

        @Override
        public MyTrack[] newArray(int size) {
            return new MyTrack[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTrackName);
        dest.writeString(mTrackAlbumName);
        dest.writeString(mTrackAlbumImageUrl);
    }

    public String getTrackName() {
        return mTrackName;
    }

    public void setTrackName(String trackName) {
        mTrackName = trackName;
    }

    public String getTrackAlbumName() {
        return mTrackAlbumName;
    }

    public void setTrackAlbumName(String trackAlbumName) {
        mTrackAlbumName = trackAlbumName;
    }

    public String getTrackAlbumImageUrl() {
        return mTrackAlbumImageUrl;
    }

    public void setTrackAlbumImageUrl(String trackAlbumImageUrl) {
        mTrackAlbumImageUrl = trackAlbumImageUrl;
    }
}
