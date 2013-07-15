package com.scvngr.levelup.core.sample.net;

import java.lang.ref.WeakReference;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

import com.scvngr.levelup.core.model.AccessToken;
import com.scvngr.levelup.core.model.factory.json.AccessTokenJsonFactory;
import com.scvngr.levelup.core.net.ApiStatus;
import com.scvngr.levelup.core.net.request.factory.AccessTokenRequestFactory;
import com.scvngr.levelup.core.sample.ProgressFragment;
import com.scvngr.levelup.core.sample.R;
import com.scvngr.levelup.core.sample.SharedPreferencesKeys;
import com.scvngr.levelup.core.sample.net.RequestLoader.RequestResult;
import com.scvngr.levelup.core.util.LogManager;

/**
 * Callbacks for loading the access token from the web service (aka logging in). This takes two
 * String arguments: {@link #ARG_EMAIL} and {@link #ARG_PASSWORD}. Access tokens are given to
 * clients and are intended to be stored indefinitely.
 */
public class AccessTokenLoaderCallbacks implements LoaderCallbacks<RequestResult<AccessToken>> {
    public static final String FRAGMENT_TAG_LOGIN_PROGRESS = ProgressFragment.class.getName()
            + AccessTokenLoaderCallbacks.class.getName();

    public static final String ARG_EMAIL = "email";
    public static final String ARG_PASSWORD = "password";

    private OnLoginListener mOnLoginListener;

    /*
     * By only holding onto weak references here, we can make sure we don't accidentally prevent an
     * activity from being garbage collected.
     */
    private WeakReference<FragmentActivity> mFragmentActivity;

    /**
     * @param activity the activity hosting these callbacks. This class only holds onto a weak
     *        reference of this.
     */
    public AccessTokenLoaderCallbacks(FragmentActivity activity) {
        mFragmentActivity = new WeakReference<FragmentActivity>(activity);
    }

    /**
     * @param onLoginListener the listener to be called when login events occur. This can be null.
     */
    public void setOnLoginListener(OnLoginListener onLoginListener) {
        mOnLoginListener = onLoginListener;
    }

    @Override
    public Loader<RequestResult<AccessToken>> onCreateLoader(int id, Bundle args) {
        String email = args.getString(ARG_EMAIL);
        String password = args.getString(ARG_PASSWORD);

        FragmentActivity activity = mFragmentActivity.get();

        // If the activity is finishing or gone, bail.
        if (activity == null || activity.isFinishing()) {
            return null;
        }

        ProgressFragment.newInstance(R.string.login_with_level_up, R.string.login_progress_message)
                .show(activity.getSupportFragmentManager(), FRAGMENT_TAG_LOGIN_PROGRESS);

        return new RequestLoader<AccessToken>(activity,
                new AccessTokenRequestFactory(activity).buildLoginRequest(email, password),
                new AccessTokenJsonFactory());
    }

    @Override
    public void onLoaderReset(Loader<RequestResult<AccessToken>> loader) {
        // Do nothing.
    }

    @Override
    public void onLoadFinished(Loader<RequestResult<AccessToken>> loader,
            RequestResult<AccessToken> result) {

        FragmentActivity activity = mFragmentActivity.get();

        // Context is needed for everything, including saving the result.
        if (activity == null) {
            LogManager.d("Activity has gone away. Not handling result just yet.");
            return;
        }

        dismissProgressDialog();

        // When the server responds with a 200 OK, that signals a successful login.
        if (result.getResponse().getStatus().equals(ApiStatus.OK)) {
            AccessToken accessToken = result.getResult();

            /*
             * Make sure that even though the response was 200 OK, it actually returned an access
             * token. This could happen if the server is broken or there was a proxy server
             * returning weird data.
             */
            if (accessToken != null) {
                SharedPreferences preferences =
                        PreferenceManager.getDefaultSharedPreferences(activity);

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

    private void dispatchOnLogin(AccessToken accessToken) {
        FragmentActivity activity = mFragmentActivity.get();

        // If the activity is finishing or gone, bail.
        if (activity == null || activity.isFinishing()) {
            return;
        }

        if (mOnLoginListener != null) {
            mOnLoginListener.onLogin(accessToken);
        }
    }

    private void dispatchOnError(RequestResult<AccessToken> result) {
        FragmentActivity activity = mFragmentActivity.get();

        // If the activity is finishing or gone, bail.
        if (activity == null || activity.isFinishing()) {
            return;
        }

        if (mOnLoginListener != null) {
            mOnLoginListener.onError(result);
        }
    }

    private void dismissProgressDialog() {
        FragmentActivity activity = mFragmentActivity.get();

        if (activity == null || activity.isFinishing()) {
            return;
        }

        ProgressFragment progressFragment =
                (ProgressFragment) activity.getSupportFragmentManager().findFragmentByTag(
                        FRAGMENT_TAG_LOGIN_PROGRESS);

        if (progressFragment != null && progressFragment.isAdded()) {
            progressFragment.dismissAllowingStateLoss();
        }
    }

    /**
     * Called when the user logs in.
     */
    public interface OnLoginListener {
        /**
         * Called when the a login successfully occurs.
         * 
         * @param accessToken the new access token.
         */
        public void onLogin(AccessToken accessToken);

        /**
         * Called when an error occurs.
         * 
         * @param result
         */
        public void onError(RequestResult<AccessToken> result);
    }
}
