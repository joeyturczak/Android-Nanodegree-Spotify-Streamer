package com.joeyturczak.spotifystreamer.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.joeyturczak.spotifystreamer.R;
import com.joeyturczak.spotifystreamer.models.MyTrack;
import com.joeyturczak.spotifystreamer.ui.MainActivity;
import com.joeyturczak.spotifystreamer.utils.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by joeyturczak on 7/16/15.
 */

/**
 * Starts a media player service that maintains a list of tracks and playback controls.
 */
public class MediaPlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private static final String PREVIOUS = "PREVIOUS";
    private static final String PLAY = "PLAY";
    private static final String NEXT = "NEXT";
    private static final String RETURN = "RETURN";

    private MediaPlayer mMediaPlayer;
    private ArrayList<MyTrack> mMyTracks;
    private int mPosition;

    public enum State {
        IDLE,
        PREPARED,
        STARTED,
        PAUSED,
        STOPPED
    }

    private State mPlayerState;

    private final IBinder mIBinder = new MediaBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        mPosition = 0;
        mMediaPlayer = new MediaPlayer();
        initMediaPlayer();
        setPlayerState(State.IDLE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }

    /** Intializes the media player instance. */
    public void initMediaPlayer() {
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    public void setTrackList(ArrayList<MyTrack> tracks) {
        mMyTracks = tracks;
    }

    public ArrayList<MyTrack> getTrackList() {
        return mMyTracks;
    }

    public MyTrack getCurrentTrack() {
        return mMyTracks.get(mPosition);
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        mPosition = position;
    }

    public void setPlayerState(State state) {
        mPlayerState = state;
    }

    public State getPlayerState() {
        return mPlayerState;
    }

    /** Prepares the currently selected song for playback asynchronously. */
    public void prepareTrack() {
        reset();
        MyTrack currentTrack = mMyTracks.get(mPosition);
        try {
            mMediaPlayer.setDataSource(currentTrack.getTrackPreviewUrl());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.prepareAsync();
    }

    /** Gets the current time the song is at. */
    public int getSeekPosition() {
        if(mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    /** Gets the length of the current song */
    public int getDuration() {
        if (!mPlayerState.equals(State.IDLE)) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    /** Pauses the media player. */
    public void pause() {
        setPlayerState(State.PAUSED);
        mMediaPlayer.pause();
        notifyPlaying();
    }

    /** Starts playback of the current song at the position seeked to. */
    public void seek(int position) {
        mMediaPlayer.seekTo(position);
    }

    /** Starts playing the current song. */
    public void start() {
        setPlayerState(State.STARTED);
        mMediaPlayer.start();
        notifyPlaying();
    }

    /** Resets the media player to the Idle state so a new song can be prepared. */
    public void reset() {
        mMediaPlayer.reset();
        setPlayerState(State.IDLE);
    }

    /** Stops the song and places the media player in the Stopped state. */
    public void stop() {
        mMediaPlayer.stop();
        setPlayerState(State.STOPPED);
        notifyPlaying();
    }

    /** Plays the next song in the list. */
    public void playNext() {
        if(mPosition < mMyTracks.size() - 1) {
            mPosition++;
            prepareTrack();
        }
    }

    /** Plays the previous song in the list. */
    public void playPrevious() {
        if(mPosition > 0) {
            mPosition--;
            prepareTrack();
        }
    }

    public class MediaBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction() != null) {
            if (intent.getAction().equals(PREVIOUS)) {
                playPrevious();
            } else if (intent.getAction().equals(PLAY)) {
                if(mPlayerState.equals(State.STARTED)) {
                    pause();
                } else if (!mPlayerState.equals(State.IDLE)) {
                    start();
                }
            } else if (intent.getAction().equals(NEXT)) {
                playNext();
            }
            sendNotificationButtonPressedBroadcast();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);;
        notificationManager.cancelAll();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stop();
        if(getApplicationContext() == null) {
            this.stopSelf();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        setPlayerState(State.PREPARED);
        start();
        sendPreparedBroadcast();
        notifyPlaying();
    }

    /** Sends a broadcast to notify the ui that the song has been prepared. */
    public void sendPreparedBroadcast() {
        Intent intent = new Intent(getString(R.string.broadcast_prepared));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /** Sends a broadcast to notify the ui that playback has been controlled from the notification bar. */
    public void sendNotificationButtonPressedBroadcast() {
        Intent intent = new Intent(getString(R.string.broadcast_notification_button_pressed));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /** Loads an image in the background for the notification bar. */
    public static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            return Utility.getBitmapFromURL(params[0]);
        }
    }

    /** Displays the current song and playback controls with a notification. */
    public void notifyPlaying() {
        Context context = getApplicationContext();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.prefs_notification_key);
        boolean displayNotifications = sharedPreferences.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.prefs_notification_default)));

        if ( displayNotifications ) {

            MyTrack currentTrack = mMyTracks.get(mPosition);
            String trackName = currentTrack.getTrackName();
            String albumArtUrl = currentTrack.getTrackAlbumImageUrl();
            String title = context.getString(R.string.app_name);

            LoadImageTask loadImageTask = new LoadImageTask();
            loadImageTask.execute(albumArtUrl);
            Bitmap albumArtBitmap = null;
            try {
                albumArtBitmap = loadImageTask.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(context, MainActivity.class);
            intent.setAction(RETURN);
            Intent prevIntent = new Intent(context, MediaPlayerService.class);
            prevIntent.setAction(PREVIOUS);
            Intent playIntent = new Intent(context, MediaPlayerService.class);
            playIntent.setAction(PLAY);
            Intent nextIntent = new Intent(context, MediaPlayerService.class);
            nextIntent.setAction(NEXT);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent prevPendingIntent = PendingIntent.getService(this, 1, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent pausePendingIntent = PendingIntent.getService(this, 2, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent nextPendingIntent = PendingIntent.getService(this, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_music_note_white_48dp)
                    .addAction(android.R.drawable.ic_media_previous, "", prevPendingIntent);
            if(mPlayerState.equals(State.STARTED)) {
                notification.addAction(android.R.drawable.ic_media_pause, "", pausePendingIntent);
            } else {
                notification.addAction(android.R.drawable.ic_media_play, "", pausePendingIntent);
            }
            notification.addAction(android.R.drawable.ic_media_next, "", nextPendingIntent);
            notification.setContentTitle(title)
                    .setContentText(trackName)
                    .setContentIntent(pendingIntent)
                    .setLargeIcon(albumArtBitmap);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, notification.build());
        }
    }
}
