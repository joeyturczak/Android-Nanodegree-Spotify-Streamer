package com.joeyturczak.spotifystreamer.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.joeyturczak.spotifystreamer.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by joeyturczak on 7/26/15.
 */

/**
 * Contains various functions that can be used anywhere.
 */
public class Utility {

    /** Gets the country code for the Spotify API that is stored in SharedPreferences. */
    public static String getCountryCode(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(context.getString(R.string.country_code_key), context.getString(R.string.default_country_code));
    }

    /**
     * Checks if a service is running.
     * Credit: http://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android
     */
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if there is an active network interface.
     * Credit: http://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /** Formats time from milliseconds into m:ss or 0:00 format. */
    public static String formatTime(int time) {

        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("m:ss");
        return formatter.format(date);
    }

    /**
     * Hides the keyboard
     * Credit: http://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
     */
    public static void hideKeyboard(Activity activity) {
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * Gets a bitmap from a url
     * Credit: http://stackoverflow.com/questions/11831188/how-to-get-bitmap-from-a-url-in-android
     */
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
