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
package com.example.levelup.core.app.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;

import com.example.levelup.core.app.ProgressDialogLoaderCallbacks;
import com.example.levelup.core.app.ProgressFragment;
import com.example.levelup.core.app.R;
import com.example.levelup.core.app.SharedPreferencesKeys;
import com.example.levelup.core.app.net.RequestLoader.RequestResult;
import com.scvngr.levelup.core.model.AccessToken;
import com.scvngr.levelup.core.model.factory.json.AccessTokenJsonFactory;
import com.scvngr.levelup.core.net.LevelUpStatus;
import com.scvngr.levelup.core.net.request.factory.AccessTokenRequestFactory;
import com.scvngr.levelup.core.util.LogManager;

/**
 * Callbacks for loading the access token from the web service (aka logging in). This takes two
 * String arguments: {@link #ARG_EMAIL} and {@link #ARG_PASSWORD}. Access tokens are given to
 * clients and are intended to be stored indefinitely.
 */
public class AccessTokenLoaderCallbacks extends
        ProgressDialogLoaderCallbacks<RequestResult<AccessToken>> {
    public static final String ARG_EMAIL = "email";

    public static final String ARG_PASSWORD = "password";
    public static final String FRAGMENT_TAG_LOGIN_PROGRESS = ProgressFragment.class.getName()
            + AccessTokenLoaderCallbacks.class.getName();

    private OnLoginListener mOnLoginListener;

    /**
     * @param fragment the fragment hosting these callbacks. This class only holds onto a weak
     *        reference of this.
     * @param loaderId the loader ID that will be used to load this access token.
     */
    public AccessTokenLoaderCallbacks(Fragment fragment, int loaderId) {
        super(fragment, loaderId, R.string.login_with_level_up, R.string.login_progress_message,
                FRAGMENT_TAG_LOGIN_PROGRESS);
    }

    @Override
    public Loader<RequestResult<AccessToken>> onCreateLoader(int id, Bundle args) {
        super.onCreateLoader(id, args);

        String email = args.getString(ARG_EMAIL);
        String password = args.getString(ARG_PASSWORD);

        // If the activity is finishing or gone, bail.
        if (isFinishingOrGone()) {
            return null;
        }

        Context context = getApplicationContext();
        return new RequestLoader<AccessToken>(context,
                new AccessTokenRequestFactory(context).buildLoginRequest(email, password),
                new AccessTokenJsonFactory());
    }

    @Override
    public void onLoaderReset(Loader<RequestResult<AccessToken>> loader) {
        // Do nothing.
    }

    @Override
    public void onLoadFinished(Loader<RequestResult<AccessToken>> loader,
            RequestResult<AccessToken> result) {
        super.onLoadFinished(loader, result);

        // When the server responds with a 200 OK, that signals a successful login.
        if (result.getResponse().getStatus().equals(LevelUpStatus.OK)) {
            AccessToken accessToken = result.getResult();

            /*
             * Make sure that even though the response was 200 OK, it actually returned an access
             * token. This could happen if the server is broken or there was a proxy server
             * returning weird data.
             */
            if (accessToken != null) {
                SharedPreferences preferences =
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                // Store the result in the preferences.
                preferences
                        .edit()
                        .putString(SharedPreferencesKeys.ACCESS_TOKEN, accessToken.getAccessToken())
                        .putLong(SharedPreferencesKeys.USER_ID, accessToken.getUserId()).apply();

                LogManager.d("Access token saved in preferences.");
                dispatchOnLogin(accessToken);
            }
        } else {
            dispatchOnError(result);
        }
    }

    /**
     * @param onLoginListener the listener to be called when login events occur. This can be null.
     */
    public void setOnLoginListener(OnLoginListener onLoginListener) {
        mOnLoginListener = onLoginListener;
    }

    private void dispatchOnError(RequestResult<AccessToken> result) {
        // If the activity is finishing or gone, bail.
        if (isFinishingOrGone()) {
            return;
        }

        if (mOnLoginListener != null) {
            mOnLoginListener.onError(result);
        }
    }

    private void dispatchOnLogin(AccessToken accessToken) {
        // If the activity is finishing or gone, bail.
        if (isFinishingOrGone()) {
            return;
        }

        if (mOnLoginListener != null) {
            mOnLoginListener.onLogin(accessToken);
        }
    }

    /**
     * Called when the user logs in.
     */
    public interface OnLoginListener {
        /**
         * Called when an error occurs.
         * 
         * @param result
         */
        public void onError(RequestResult<AccessToken> result);

        /**
         * Called when the a login successfully occurs.
         * 
         * @param accessToken the new access token.
         */
        public void onLogin(AccessToken accessToken);
    }
}
