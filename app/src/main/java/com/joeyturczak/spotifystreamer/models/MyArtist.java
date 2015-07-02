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
 * Stores the relevant artist information from the Spotify Artist model.
 */
public class MyArtist implements Parcelable {

    String mArtistName;
    String mArtistImageUrl;
    String mArtistId;

    public MyArtist(String name, String imageUrl, String id) {
        mArtistName = name;
        mArtistImageUrl = imageUrl;
        mArtistId = id;
    }

    protected MyArtist(Parcel in) {
        mArtistName = in.readString();
        mArtistImageUrl = in.readString();
        mArtistId = in.readString();
    }

    public static final Creator<MyArtist> CREATOR = new Creator<MyArtist>() {
        @Override
        public MyArtist createFromParcel(Parcel in) {
            return new MyArtist(in);
        }

        @Override
        public MyArtist[] newArray(int size) {
            return new MyArtist[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mArtistName);
        dest.writeString(mArtistImageUrl);
        dest.writeString(mArtistId);
    }

    public String getArtistName() {
        return mArtistName;
    }

    public void setArtistName(String artistName) {
        mArtistName = artistName;
    }

    public String getArtistImageUrl() {
        return mArtistImageUrl;
    }

    public void setArtistImageUrl(String artistImageUrl) {
        mArtistImageUrl = artistImageUrl;
    }

    public String getArtistId() {
        return mArtistId;
    }

    public void setArtistId(String artistId) {
        mArtistId = artistId;
    }
}
