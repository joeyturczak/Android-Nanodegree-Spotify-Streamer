package com.joeyturczak.spotifystreamer.ui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.joeyturczak.spotifystreamer.R;
import com.joeyturczak.spotifystreamer.models.MyTrack;
import com.joeyturczak.spotifystreamer.services.MediaPlayerService;
import com.joeyturczak.spotifystreamer.utils.Utility;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Binds to the MediaPlayerService and maintains the display for the current track.
 */
public class PlayerFragment extends DialogFragment {

    private MediaPlayerService mMediaPlayerService;
    private Intent mIntent;
    private boolean mIsNewSong = true;

    private Handler mSeekHandler = new Handler();

    private MediaPlayerService.State mPlayerState;

    private int mCurrentPosition;
    ArrayList<MyTrack> mMyTracks;
    MyTrack mCurrentTrack;

    @Bind(R.id.artistName) TextView mArtistName;
    @Bind(R.id.albumName) TextView mAlbumTitle;
    @Bind(R.id.albumArtImageView) ImageView mAlbumArt;
    @Bind(R.id.trackName) TextView mTrackName;
    @Bind(R.id.previousButton) Button mPreviousButton;
    @Bind(R.id.playButton) Button mPlayButton;
    @Bind(R.id.nextButton) Button mNextButton;
    @Bind(R.id.seekBar) SeekBar mSeekBar;
    @Bind(R.id.timeElapsed) TextView mTimeElapsed;
    @Bind(R.id.duration) TextView mDuration;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;

    /** Turns off the progress spinner when the media player enters the prepared state. */
    private BroadcastReceiver mPreparedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mProgressBar.setVisibility(View.INVISIBLE);
            updatePlayButtonImage();
        }
    };

    /** Updates the current track and updates the display when a notification button is pressed. */
    private BroadcastReceiver mNotificationButtonPressedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mCurrentPosition = mMediaPlayerService.getPosition();
            mCurrentTrack = mMyTracks.get(mCurrentPosition);
            updateDisplay(mCurrentTrack);
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem menuItem = menu.findItem(R.id.action_now_playing);
        menuItem.setVisible(false);
        menuItem = menu.findItem(R.id.action_share);
        menuItem.setVisible(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle bundle = getArguments();
        mCurrentPosition = bundle.getInt(getString(R.string.bundle_position_key));
        mMyTracks = bundle.getParcelableArrayList(getString(R.string.bundle_tracks_key));
        if(mMyTracks != null) {
            mCurrentTrack = mMyTracks.get(mCurrentPosition);
        }

        if(mIntent == null) {
            mIntent = new Intent(getActivity(), MediaPlayerService.class);
            getActivity().bindService(mIntent, mediaConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(mIntent);
        }

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mPreparedReceiver,
                new IntentFilter(getString(R.string.broadcast_prepared)));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mNotificationButtonPressedReceiver,
                new IntentFilter(getString(R.string.broadcast_notification_button_pressed)));
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            mIsNewSong = false;
        }
    }

    /** The system calls this to get the DialogFragment's layout, regardless
     of whether it's being displayed as a dialog or an embedded fragment. */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        final View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        ButterKnife.bind(this, rootView);

        setupListeners();

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        mSeekHandler.removeCallbacks(mRunnable);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mPreparedReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mNotificationButtonPressedReceiver);
        getActivity().unbindService(mediaConnection);
        mMediaPlayerService = null;
        ButterKnife.unbind(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSeekBar();
    }

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private ServiceConnection mediaConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.MediaBinder binder = (MediaPlayerService.MediaBinder) service;

            mMediaPlayerService = binder.getService();
            if(mCurrentPosition < 0) {
                mMyTracks = mMediaPlayerService.getTrackList();
                mCurrentPosition = mMediaPlayerService.getPosition();
                mCurrentTrack = mMyTracks.get(mCurrentPosition);
                mIsNewSong = false;
            }

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.share_key), mCurrentTrack.getTrackPreviewUrl());
            editor.apply();

            mPlayerState = mMediaPlayerService.getPlayerState();

            updateDisplay(mCurrentTrack);

            mMediaPlayerService.setTrackList(mMyTracks);
            if(mPlayerState == MediaPlayerService.State.IDLE || mIsNewSong) {
                playSong();
            } else if(mPlayerState == MediaPlayerService.State.STARTED) {
                resumeSong();
            } else if(mPlayerState == MediaPlayerService.State.PAUSED) {
                pauseSong();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMediaPlayerService.unbindService(mediaConnection);
            getActivity().stopService(mIntent);
            mMediaPlayerService = null;
        }
    };

    /** Updates the play/pause button to the correct image depending on the state of the media player. */
    public void updatePlayButtonImage() {
        if(mPlayerState.equals(MediaPlayerService.State.STARTED)) {
            mPlayButton.setBackgroundResource(android.R.drawable.ic_media_pause);
        } else {
            mPlayButton.setBackgroundResource(android.R.drawable.ic_media_play);
        }
    }

    /** Sets the listeners for the buttons and the seek bar. */
    private void setupListeners() {

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayerService != null) {
                    mPlayerState = mMediaPlayerService.getPlayerState();
                    if (mPlayerState == MediaPlayerService.State.STARTED) {
                        pauseSong();
                    } else if (mPlayerState == MediaPlayerService.State.PAUSED) {
                        resumeSong();
                    } else {
                        playSong();
                    }
                }
            }
        });

        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayerService != null) {
                    if (mCurrentPosition > 0) {
                        mCurrentPosition--;
                    } else {
                        mCurrentPosition = (--mCurrentPosition + mMyTracks.size()) % mMyTracks.size();
                    }
                    mCurrentTrack = mMyTracks.get(mCurrentPosition);
                    updateDisplay(mCurrentTrack);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mMediaPlayerService.playPrevious();

                }
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayerService != null) {
                    if (mCurrentPosition < mMyTracks.size() - 1) {
                        mCurrentPosition++;
                    } else {
                        mCurrentPosition = ++mCurrentPosition % mMyTracks.size();
                    }
                    mCurrentTrack = mMyTracks.get(mCurrentPosition);
                    updateDisplay(mCurrentTrack);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mMediaPlayerService.playNext();

                }
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!mMediaPlayerService.getPlayerState().equals(MediaPlayerService.State.IDLE)
                        && !mMediaPlayerService.getPlayerState().equals(MediaPlayerService.State.STOPPED)) {
                    mMediaPlayerService.seek(seekBar.getProgress());
                }
            }
        });
    }

    /** Updates the seek bar continuously. */
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            updateSeekBar();
        }
    };

    private void updateSeekBar() {
        if (mMediaPlayerService != null) {
            mPlayerState = mMediaPlayerService.getPlayerState();
            int timeElapsed = mMediaPlayerService.getSeekPosition();
            mSeekBar.setProgress(timeElapsed);
            mTimeElapsed.setText(Utility.formatTime(timeElapsed));
            if(mPlayerState != MediaPlayerService.State.IDLE) {
                int duration = mMediaPlayerService.getDuration();
                mSeekBar.setMax(duration);
                mDuration.setText(Utility.formatTime(duration));
            }
            updatePlayButtonImage();
        }
        if(mPlayerState == MediaPlayerService.State.STARTED) {
            mSeekHandler.post(mRunnable);
        } else {
            mSeekHandler.postDelayed(mRunnable, 200);
        }
    }

    /** Updates the views with the relevant information from the current track. */
    private void updateDisplay(MyTrack currentTrack) {

        mArtistName.setText(currentTrack.getArtistName());
        mAlbumTitle.setText(currentTrack.getTrackAlbumName());

        String imageUrl = currentTrack.getTrackAlbumImageUrl();

        if(!imageUrl.isEmpty()) {
            Picasso.with(getActivity()).load(imageUrl).into(mAlbumArt);
        }
        else {
            mAlbumArt.setImageResource(android.R.drawable.picture_frame);
        }

        mTrackName.setText(currentTrack.getTrackName());

        updatePlayButtonImage();
    }

    /** Prepares and plays a new song */
    private void playSong() {
        if(mMediaPlayerService != null) {
            mMediaPlayerService.setPosition(mCurrentPosition);
            mMediaPlayerService.prepareTrack();
            updatePlayButtonImage();
            updateSeekBar();
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    /** Pauses current song */
    private void pauseSong() {
        if(mMediaPlayerService != null) {
            mMediaPlayerService.pause();
            updatePlayButtonImage();
            updateSeekBar();
        }
    }

    /** Resumes playback of current song */
    private void resumeSong() {
        if(mMediaPlayerService != null) {
            mMediaPlayerService.start();
            updatePlayButtonImage();
            updateSeekBar();
        }
    }
}