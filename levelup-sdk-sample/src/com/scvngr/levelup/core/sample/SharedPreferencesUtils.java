package com.scvngr.levelup.core.sample;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Utilities to work with this application's {@link SharedPreferences}. See
 * {@link SharedPreferencesKeys} for more info.
 */
public class SharedPreferencesUtils {
    /**
     * Check to see if the user has a cached access token. Note: this doesn't check to make sure the
     * access token is still valid. This also has the possibility of doing a disk read when the
     * Context's default shared preferences hasn't been loaded yet.
     * 
     * @param context the application context.
     * @return true if the user has an access token.
     */
    public static boolean isLoggedIn(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        String accessToken = preferences.getString(SharedPreferencesKeys.ACCESS_TOKEN, null);
        boolean loggedIn = accessToken != null;

        return loggedIn;
    }

    /**
     * Log the user out by clearing the access token, payment token, email address, and user ID.
     * Note: this has the possibility of doing a disk read when the Context's default shared
     * preferences hasn't been loaded yet.
     * 
     * @param context the application context.
     */
    public static void logout(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().remove(SharedPreferencesKeys.ACCESS_TOKEN)
                .remove(SharedPreferencesKeys.PAYMENT_TOKEN).remove(SharedPreferencesKeys.USER_ID)
                .remove(SharedPreferencesKeys.EMAIL_ADDRESS).apply();
    }
}
