/*
 * Copyright (C) 2014 SCVNGR, Inc. d/b/a LevelUp
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.example.levelup.core.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.scvngr.levelup.core.model.AccessToken;
import com.scvngr.levelup.core.net.AccessTokenRetriever;

/**
 * An implementation of {@link AccessTokenRetriever} that reads from the default SharedPreferences.
 * The access token is stored in {@link SharedPreferencesKeys#ACCESS_TOKEN} and the user ID is
 * stored in {@link SharedPreferencesKeys#USER_ID}.
 */
public class SharedPreferencesAccessTokenRetriever implements AccessTokenRetriever {

    /**
     * The parcelable creator. This class doesn't actually store anything in the parcel, so
     * nothing's read from it.
     */
    public static final Creator<SharedPreferencesAccessTokenRetriever> CREATOR =
            new Creator<SharedPreferencesAccessTokenRetriever>() {
                @Override
                public SharedPreferencesAccessTokenRetriever[] newArray(int size) {
                    return new SharedPreferencesAccessTokenRetriever[size];
                }

                @Override
                public SharedPreferencesAccessTokenRetriever createFromParcel(Parcel source) {
                    return new SharedPreferencesAccessTokenRetriever();
                }
            };

    /**
     * A constant for to use for the default value of the user ID, to check if it's missing from the
     * shared preferences.
     */
    private static final int USER_ID_MISSING = -1;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Do nothing, as this class doesn't have any instance fields.
    }

    @Override
    @Nullable
    public AccessToken getAccessToken(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Load the token and user info from the database.
        String accessTokenString = preferences.getString(SharedPreferencesKeys.ACCESS_TOKEN, null);
        long userId = preferences.getLong(SharedPreferencesKeys.USER_ID, USER_ID_MISSING);

        AccessToken accessToken = null;

        // Check to make sure we have them stored properly.
        if (!TextUtils.isEmpty(accessTokenString)) {
            if (userId == USER_ID_MISSING) {
                accessToken = new AccessToken(accessTokenString);
            } else {
                accessToken = new AccessToken(accessTokenString, userId);
            }
        }

        return accessToken;
    }
}
