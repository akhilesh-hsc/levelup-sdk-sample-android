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
package com.scvngr.levelup.core.sample;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.scvngr.levelup.core.model.AccessToken;
import com.scvngr.levelup.core.sample.net.AccessTokenLoaderCallbacks;
import com.scvngr.levelup.core.sample.net.AccessTokenLoaderCallbacks.OnLoginListener;
import com.scvngr.levelup.core.sample.net.RequestLoader.RequestResult;
import com.scvngr.levelup.core.sample.net.RequestResultUtil;

/**
 * <p>
 * A fragment that provides a login form (email address and password) to allow users to access their
 * LevelUp account.
 * </p>
 * <p>
 * To use, instantiate this fragment using {@link #newInstance()} and register an
 * {@link OnLoginListener} by calling {@link #setOnLoginListener(OnLoginListener)}.
 * </p>
 * <p>
 * This retrieves an {@link AccessToken} and stores it in {@link SharedPreferences} under the
 * {@link SharedPreferencesKeys#ACCESS_TOKEN} key. See {@link AccessTokenLoaderCallbacks} for more
 * information.
 * </p>
 */
public class LoginFragment extends Fragment implements OnClickListener {

    /**
     * Used with {@link #mAccessTokenLoaderCallbacks}.
     */
    private static final int LOADER_ACCESS_TOKEN = 100;

    private OnLoginListener mWrappedOnLoginListener;

    /**
     * @return a new instance of this fragment.
     */
    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    /**
     * Loader callbacks for loading the access token from the web service.
     */
    private AccessTokenLoaderCallbacks mAccessTokenLoaderCallbacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccessTokenLoaderCallbacks = new AccessTokenLoaderCallbacks(this, LOADER_ACCESS_TOKEN);
        mAccessTokenLoaderCallbacks.setOnLoginListener(new WrappingOnLoginListener());
    }

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.sign_in).setOnClickListener(this);
        view.findViewById(R.id.sign_up).setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAccessTokenLoaderCallbacks.reconnectOrDismiss();
    }

    @Override
    public void onPause() {
        super.onPause();

        // This should be set again when the fragment is re-attached.
        mWrappedOnLoginListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mAccessTokenLoaderCallbacks.setOnLoginListener(null);
    }

    /**
     * Set the login listener.
     * 
     * @param onLoginListener the listener to be called upon login.
     */
    public void setOnLoginListener(OnLoginListener onLoginListener) {
        mWrappedOnLoginListener = onLoginListener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in:
                login();
                break;

            case R.id.sign_up:
                startActivity(new Intent(getActivity(), RegisterActivity.class));
                break;
            default:
                // Do nothing.
        }
    }

    /**
     * Retrieve the login information from the UI state and call login.
     */
    private void login() {
        EditText emailView = (EditText) getView().findViewById(R.id.email);
        EditText passwordView = (EditText) getView().findViewById(R.id.password);

        // Hide the soft keyboard.
        InputMethodManager im =
                (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(getView().getApplicationWindowToken(), 0);

        login(emailView.getText().toString(), passwordView.getText().toString());
    }

    /**
     * Call the {@link #LOADER_ACCESS_TOKEN} loader with the given credentials.
     * 
     * @param email the user's email address.
     * @param password the password for the account.
     */
    private void login(String email, String password) {
        final Bundle args = new Bundle();
        args.putString(AccessTokenLoaderCallbacks.ARG_EMAIL, email);
        args.putString(AccessTokenLoaderCallbacks.ARG_PASSWORD, password);

        getLoaderManager().restartLoader(LOADER_ACCESS_TOKEN, args, mAccessTokenLoaderCallbacks);
    }

    /**
     * Shows an error message in the fragment. This is for general errors relating to logging in.
     * 
     * @param message the error message to display or null to hide the message.
     */
    private void showErrorMessage(CharSequence message) {
        TextView errorView = (TextView) getView().findViewById(R.id.error_message);
        errorView.setText(message);

        if (TextUtils.isEmpty(message)) {
            errorView.setVisibility(View.GONE);
        } else {
            errorView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * A login listener that calls through to the wrapped login listener. This also displays errors
     * using {@link LoginFragment#showErrorMessage(CharSequence)}. This is needed so that an error
     * message can be shown and the result can also be used by the activity to handle a successful
     * login.
     */
    private class WrappingOnLoginListener implements OnLoginListener {
        @Override
        public void onLogin(AccessToken accessToken) {
            if (mWrappedOnLoginListener != null) {
                mWrappedOnLoginListener.onLogin(accessToken);
            }
        }

        @Override
        public void onError(RequestResult<AccessToken> result) {
            showErrorMessage(RequestResultUtil.errorsToString(result));

            if (mWrappedOnLoginListener != null) {
                mWrappedOnLoginListener.onError(result);
            }
        }
    };

}
