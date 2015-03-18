package com.example.levelup.core.app.util;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.example.levelup.core.app.Constants;
import com.example.levelup.core.app.R;

/**
 * Utility for checking if the API values are setup correctly.
 */
public final class ApiSetupUtil {

    public static void checkApiKey(@NonNull Activity activity) {
        // Warn if there is no API key setup.
        final String apiKey = activity.getString(R.string.levelup_api_key);
        if (Constants.PLACEHOLDER.equals(apiKey)) {
            Toast.makeText(activity,
                    activity.getString(R.string.api_key_or_app_id_not_configured_message),
                    Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }
}
