package com.scvngr.levelup.core.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

import com.scvngr.levelup.core.model.AccessToken;
import com.scvngr.levelup.core.model.User;
import com.scvngr.levelup.core.model.factory.json.UserJsonFactory;
import com.scvngr.levelup.core.net.AbstractRequest;
import com.scvngr.levelup.core.net.LevelUpStatus;
import com.scvngr.levelup.core.net.request.factory.UserRequestFactory;
import com.scvngr.levelup.core.sample.net.AccessTokenLoaderCallbacks;
import com.scvngr.levelup.core.sample.net.AccessTokenLoaderCallbacks.OnLoginListener;
import com.scvngr.levelup.core.sample.net.RequestLoader;
import com.scvngr.levelup.core.sample.net.RequestLoader.RequestResult;

import java.util.List;

/**
 * <p>
 * A fragment that demonstrates a simple registration form to create a new LevelUp account. Most
 * importantly, this demonstrates how to handle the field-specific errors returned by the web
 * service.
 * </p>
 * <p>
 * After registration completes, if the account was successfully completed this uses the filled out
 * registration form and {@link AccessTokenLoaderCallbacks} to login. This then finishes this
 * fragment's host activity.
 * </p>
 */
public class RegisterFragment extends Fragment {
    private static final int LOADER_REGISTER = 200;
    private static final String FRAGMENT_TAG_REGISTER_PROGRESS = ProgressFragment.class.getName()
            + ".register";
    private static final int LOADER_LOGIN = 201;
    private LoaderCallbacks<RequestResult<User>> mRegisterLoaderCallbacks =
            new RegisterLoaderCallbacks();

    public RegisterFragment() {
        mErrorResponseVisualizer.addMapping(UserRequestFactory.OUTER_PARAM_USER,
                UserRequestFactory.PARAM_FIRST_NAME, R.id.first_name);

        mErrorResponseVisualizer.addMapping(UserRequestFactory.OUTER_PARAM_USER,
                UserRequestFactory.PARAM_LAST_NAME, R.id.last_name);

        mErrorResponseVisualizer.addMapping(UserRequestFactory.OUTER_PARAM_USER,
                UserRequestFactory.PARAM_EMAIL, R.id.email);

        mErrorResponseVisualizer.addMapping(UserRequestFactory.OUTER_PARAM_USER,
                UserRequestFactory.PARAM_PASSWORD, R.id.password);
    }

    private ErrorResponseVisualizer mErrorResponseVisualizer = new ErrorResponseVisualizer(
            R.id.error_general);

    private OnLoginListener mOnLoginListener = new OnLoginListener() {

        @Override
        public void onLogin(AccessToken accessToken) {
            // After the user registers and is auto-logged in successfully, dismiss the registration
            // activity.
            getActivity().finish();
        }

        @Override
        public void onError(RequestResult<AccessToken> result) {
            mErrorResponseVisualizer.showErrors(getView(), result.getErrors());
        }
    };

    private AccessTokenLoaderCallbacks mAccessTokenLoaderCallbacks;

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.sign_up).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LoaderManager loaderManager = getLoaderManager();

        if (mAccessTokenLoaderCallbacks != null) {
            mAccessTokenLoaderCallbacks.reconnectOrDismiss();
        }

        // Reconnect the loader callbacks.
        if (loaderManager.getLoader(LOADER_LOGIN) != null) {
            loaderManager.initLoader(LOADER_LOGIN, null, mAccessTokenLoaderCallbacks);
        }

        if (loaderManager.getLoader(LOADER_REGISTER) != null) {
            loaderManager.initLoader(LOADER_REGISTER, null, mRegisterLoaderCallbacks);
        }
    }

    /**
     * Called when the register request has finished. This calls
     * {@link #onSuccessfulRegistration(User)} or
     * {@link ErrorResponseVisualizer#showErrors(View, List)} depending on the web service result.
     * This must be called on the main thread.
     * 
     * @param result the result of registration.
     */
    protected void onRegisterLoadFinished(RequestResult<User> result) {
        if (result.getResponse().getStatus().equals(LevelUpStatus.OK)) {
            User user = result.getResult();

            onSuccessfulRegistration(user);
        } else {
            mErrorResponseVisualizer.showErrors(getView(), result.getErrors());
        }
    }

    /**
     * Called when the registration request was successful. This automatically triggers a login. The
     * username and password are retrieved from the form that's still filled out. This must be
     * called from the main thread.
     * 
     * @param user the newly-created user.
     */
    protected void onSuccessfulRegistration(User user) {
        // Store the email address.
        if (user != null) {
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                    .putString(SharedPreferencesKeys.EMAIL_ADDRESS, user.getEmail())
                    .putLong(SharedPreferencesKeys.USER_ID, user.getId()).apply();
        }

        Bundle args = new Bundle();
        args.putString(AccessTokenLoaderCallbacks.ARG_EMAIL, getTrimmedTextFromView(R.id.email));
        args.putString(AccessTokenLoaderCallbacks.ARG_PASSWORD, getPasswordFromView());

        mAccessTokenLoaderCallbacks = new AccessTokenLoaderCallbacks(this, LOADER_LOGIN);

        mAccessTokenLoaderCallbacks.setOnLoginListener(mOnLoginListener);

        getLoaderManager().restartLoader(LOADER_LOGIN, args, mAccessTokenLoaderCallbacks);
    }

    /**
     * Registers a user using the values in the form fields.
     */
    private void register() {
        String firstName = getTrimmedTextFromView(R.id.first_name);
        String lastName = getTrimmedTextFromView(R.id.last_name);
        String email = getTrimmedTextFromView(R.id.email);
        String password = getPasswordFromView();

        register(firstName, lastName, email, password);
    }

    /**
     * @return the unmodified password from the EditText view.
     */
    private String getPasswordFromView() {
        return ((EditText) getView().findViewById(R.id.password)).getText().toString();
    }

    /**
     * @param id the ID of the EditText.
     * @return the trimmed version of text from the given EditText View.
     */
    private String getTrimmedTextFromView(int id) {
        return ((EditText) getView().findViewById(id)).getText().toString().trim();
    }

    /**
     * Registers a user with the given values.
     * 
     * @param firstName the user's first (given) name.
     * @param lastName the user's last (family/surname) name.
     * @param email email address of the user. This is used to login.
     * @param password the user's password.
     */
    private void register(String firstName, String lastName, String email, String password) {
        Bundle args = new Bundle();
        args.putString(RegisterLoaderCallbacks.ARG_FIRST_NAME, firstName);
        args.putString(RegisterLoaderCallbacks.ARG_LAST_NAME, lastName);
        args.putString(RegisterLoaderCallbacks.ARG_EMAIL, email);
        args.putString(RegisterLoaderCallbacks.ARG_PASSWORD, password);

        getLoaderManager().restartLoader(LOADER_REGISTER, args, mRegisterLoaderCallbacks);
    }

    /**
     * Callbacks to handle perform and handle the actual register network request. To use, start
     * {@link #LOADER_REGISTER} with the arguments {@link #ARG_FIRST_NAME}, {@link #ARG_LAST_NAME},
     * {@link #ARG_EMAIL}, and {@link #ARG_PASSWORD}. All the arguments are {@link String}s.
     */
    public final class RegisterLoaderCallbacks implements LoaderCallbacks<RequestResult<User>> {
        /**
         * Loader argument for the user's first name (String).
         */
        public static final String ARG_FIRST_NAME = "first_name";

        /**
         * Loader argument for the user's last name (String).
         */
        public static final String ARG_LAST_NAME = "last_name";

        /**
         * Loader argument for the user's email address (String).
         */
        public static final String ARG_EMAIL = "email";

        /**
         * Loader argument for the user's password (String).
         */
        public static final String ARG_PASSWORD = "password";

        @Override
        public Loader<RequestResult<User>> onCreateLoader(int id, Bundle args) {
            String firstName = args.getString(ARG_FIRST_NAME);
            String lastName = args.getString(ARG_LAST_NAME);
            String email = args.getString(ARG_EMAIL);
            String password = args.getString(ARG_PASSWORD);

            ProgressFragment.newInstance(R.string.register_button_sign_up,
                    R.string.register_progress_message, LOADER_REGISTER).show(getFragmentManager(),
                    FRAGMENT_TAG_REGISTER_PROGRESS);

            AbstractRequest registerRequest =
                    new UserRequestFactory(getActivity(),
                            new SharedPreferencesAccessTokenRetriever()).buildRegisterRequest(
                            firstName, lastName, email, password, null /* location isn't needed */);

            return new RequestLoader<User>(getActivity(), registerRequest, new UserJsonFactory());
        }

        @Override
        public void onLoadFinished(Loader<RequestResult<User>> loader,
                RequestResult<User> registerResponse) {

            ProgressFragment
                    .dismissAnyShowing(getFragmentManager(), FRAGMENT_TAG_REGISTER_PROGRESS);

            Activity activity = RegisterFragment.this.getActivity();
            /*
             * Only send the message to the handler when the fragment and activity are alive and
             * well.
             */
            if (isAdded() && activity != null && !activity.isFinishing()) {
                new RegisterLoaderHandler(RegisterFragment.this)
                        .sendOnRegisterLoadFinished(registerResponse);

            }
        }

        @Override
        public void onLoaderReset(Loader<RequestResult<User>> loader) {
            // Do nothing.
        }
    }

    /**
     * In order to properly chain loader callbacks (in this case, the
     * {@link AccessTokenLoaderCallbacks} from the {@link RegisterLoaderCallbacks}), use a handler
     * to ensure that the next loader is started on the main thread. If this isn't done, the
     * fragment manager will complain that it's being called from onLoadFinished if
     * {@link FragmentTransaction#commit()} is used (instead of
     * {@link FragmentTransaction#commitAllowingStateLoss()}). This handler should only be called
     * when the activity is alive and not finishing.
     */
    private static final class RegisterLoaderHandler extends Handler {
        public static final int MSG_REGISTER_LOAD_FINISHED = 100;
        private RegisterFragment mRegisterFragment;

        public RegisterLoaderHandler(RegisterFragment registerFragment) {
            super(Looper.getMainLooper());
            mRegisterFragment = registerFragment;
        }

        public void sendOnRegisterLoadFinished(RequestResult<User> result) {
            obtainMessage(MSG_REGISTER_LOAD_FINISHED, result).sendToTarget();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_LOAD_FINISHED:
                    mRegisterFragment.onRegisterLoadFinished((RequestResult<User>) msg.obj);
                    break;
                default:
                    // Do nothing.
            }
        }
    }
}
