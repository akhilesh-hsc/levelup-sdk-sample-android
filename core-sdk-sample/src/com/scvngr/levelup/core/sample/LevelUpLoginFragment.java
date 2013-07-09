package com.scvngr.levelup.core.sample;

import java.lang.ref.WeakReference;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.scvngr.levelup.core.model.AccessToken;
import com.scvngr.levelup.core.model.factory.json.AccessTokenJsonFactory;
import com.scvngr.levelup.core.net.ApiStatus;
import com.scvngr.levelup.core.net.request.factory.AccessTokenRequestFactory;
import com.scvngr.levelup.core.sample.net.RequestLoader;
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
 * {@link SharedPreferencesKeys#ACCESS_TOKEN} key.
 * </p>
 */
public class LevelUpLoginFragment extends Fragment implements OnClickListener {
    private static final String FRAGMENT_TAG_LOGIN_PROGRESS = ProgressFragment.class.getName()
            + ".login";

    private static final int LOADER_ACCESS_TOKEN = 100;

    /**
     * @return a new instance of this fragment.
     */
    public static LevelUpLoginFragment newInstance() {
        return new LevelUpLoginFragment();
    }

    /**
     * Loader callbacks for loading the access token from the web service.
     */
    private AccessTokenLoaderCallbacks mAccessTokenLoaderCallbacks =
            new AccessTokenLoaderCallbacks(this);

    private OnLoginListener mOnLoginListener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // If there's an existing loader, reconnect to it so the callbacks are delivered.
        Loader<Object> existingLoader = getLoaderManager().getLoader(LOADER_ACCESS_TOKEN);

        if (existingLoader != null) {
            getLoaderManager().initLoader(LOADER_ACCESS_TOKEN, null, mAccessTokenLoaderCallbacks);
        }
    };

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.sign_in).setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mOnLoginListener = null;
    }

    /**
     * Set the login listener.
     * 
     * @param onLoginListener the listener to be called upon login.
     */
    public void setOnLoginListener(OnLoginListener onLoginListener) {
        mOnLoginListener = onLoginListener;
    }

    /**
     * Called when the login loader finishes, successfully or otherwise.
     */
    protected void onLoginLoaderFinished() {
        ProgressFragment progressFragment =
                (ProgressFragment) getFragmentManager().findFragmentByTag(
                        FRAGMENT_TAG_LOGIN_PROGRESS);

        if (progressFragment != null) {
            progressFragment.dismissAllowingStateLoss();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in:
                login();
                break;
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
     * Called when the user logs in.
     */
    public interface OnLoginListener {
        /**
         * Called when the a login successfully occurs.
         * 
         * @param accessToken the new access token.
         */
        public void onLogin(AccessToken accessToken);
    }

    /**
     * A simple indeterminate progress dialog for displaying the login message.
     */
    public static class ProgressFragment extends DialogFragment {
        /**
         * @return a new instance of {@link ProgressFragment}.
         */
        public static ProgressFragment newInstance() {
            ProgressFragment progressFragment = new ProgressFragment();

            return progressFragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            ProgressDialog progressDialog = new ProgressDialog(getActivity());

            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(R.string.login_button_sign_in);
            progressDialog.setMessage(getText(R.string.login_progress_message));

            return progressDialog;
        }
    }

    /**
     * Callbacks for loading the access token from the web service. This takes two String arguments:
     * {@link #ARG_EMAIL} and {@link #ARG_PASSWORD}.
     */
    private static class AccessTokenLoaderCallbacks implements
            LoaderCallbacks<RequestResult<AccessToken>> {
        public static final String ARG_EMAIL = "email";
        public static final String ARG_PASSWORD = "password";

        private WeakReference<LevelUpLoginFragment> mLoginFragment;

        public AccessTokenLoaderCallbacks(LevelUpLoginFragment fragment) {
            mLoginFragment = new WeakReference<LevelUpLoginFragment>(fragment);
        }

        @Override
        public Loader<RequestResult<AccessToken>> onCreateLoader(int id, Bundle args) {
            String email = args.getString(ARG_EMAIL);
            String password = args.getString(ARG_PASSWORD);

            LevelUpLoginFragment fragment = mLoginFragment.get();

            if (fragment == null) {
                return null;
            }

            ProgressFragment.newInstance().show(fragment.getFragmentManager(),
                    FRAGMENT_TAG_LOGIN_PROGRESS);

            return new RequestLoader<AccessToken>(fragment.getActivity(),
                    new AccessTokenRequestFactory(fragment.getActivity()).buildLoginRequest(email,
                            password), new AccessTokenJsonFactory());
        }

        @Override
        public void onLoaderReset(Loader<RequestResult<AccessToken>> loader) {
            // Do nothing.
        }

        @Override
        public void onLoadFinished(Loader<RequestResult<AccessToken>> loader,
                RequestResult<AccessToken> result) {
            LevelUpLoginFragment fragment = mLoginFragment.get();

            /*
             * If the reference to the fragment is lost, this is probably being delivered outside
             * the normal lifecycle. The result will be redelivered when the loader starts again.
             */
            if (fragment == null) {
                return;
            }

            fragment.onLoginLoaderFinished();

            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(fragment.getActivity());

            if (result.getResponse().getStatus().equals(ApiStatus.OK)) {
                AccessToken accessToken = result.getResult();

                if (accessToken != null) {
                    preferences
                            .edit()
                            .putString(SharedPreferencesKeys.ACCESS_TOKEN,
                                    accessToken.getAccessToken())
                            .putLong(SharedPreferencesKeys.USER_ID, accessToken.getUserId())
                            .apply();

                    if (fragment.mOnLoginListener != null) {
                        fragment.mOnLoginListener.onLogin(accessToken);
                    }
                } else {
                    // This shouldn't happen.
                    fragment.showErrorMessage("Access token was null, despite a successful result from the web service.");
                }
            } else {
                fragment.showErrorMessage(RequestResultUtil.errorsToString(result));
            }
        }
    }
}
