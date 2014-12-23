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
package com.example.levelup.core.app.example1;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.example.levelup.core.app.LoginFragment;
import com.example.levelup.core.app.PaymentCodeFragment;
import com.example.levelup.core.app.R;
import com.example.levelup.core.app.SharedPreferencesUtils;
import com.example.levelup.core.app.net.AccessTokenLoaderCallbacks.OnLoginListener;
import com.example.levelup.core.app.net.RequestLoader.RequestResult;
import com.scvngr.levelup.core.model.AccessToken;
import com.scvngr.levelup.core.util.EnvironmentUtil;

/**
 * <p>
 * A simple payment Activity. This shows either a login screen or a payment fragment depending on if
 * the user is logged in or not. This is slightly better than using separate activities as it's less
 * likely to confuse the back stack if logged-in state changes.
 * </p>
 */
public class LoginAndPaymentActivity extends FragmentActivity {
    /**
     * When the user logs in, replace the login fragment with the payment code fragment.
     */
    private OnLoginListener mOnLoginListener = new OnLoginListener() {

        @Override
        public void onLogin(AccessToken accessToken) {
            showPaymentCodeFragment();
        }

        @Override
        public void onError(RequestResult<AccessToken> result) {
            // Errors are shown by the fragment.
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.payment, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean handled = super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.logout).setVisible(SharedPreferencesUtils.isLoggedIn(this));

        return handled;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                logout();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        showLoginOrPaymentCode();
    }

    /**
     * Log the user out by clearing the access token and resetting the UI.
     */
    private void logout() {
        SharedPreferencesUtils.logout(this);

        showLoginOrPaymentCode();
    }

    /**
     * Shows the login fragment. This also hooks in the {@link OnLoginListener} so that this
     * activity can be notified of when the user successfully logs in.
     */
    private void showLoginFragment() {
        FragmentManager fm = getSupportFragmentManager();
        LoginFragment loginFragment =
                (LoginFragment) fm.findFragmentByTag(LoginFragment.class.getName());

        FragmentTransaction transaction = fm.beginTransaction();

        if (loginFragment == null) {
            loginFragment = LoginFragment.newInstance();
        }

        if (!loginFragment.isAdded()) {
            transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
            transaction.replace(android.R.id.content, loginFragment, LoginFragment.class.getName());
        }

        loginFragment.setOnLoginListener(mOnLoginListener);

        commitUiChange(transaction);
    }

    /**
     * Commits the fragment transaction, updating the other parts of the UI too.
     * 
     * @param transaction the fragment transaction to commit.
     */
    private void commitUiChange(FragmentTransaction transaction) {
        compatInvalidateOptionsMenu();

        /*
         * This is safe here, because showLoginOrPaymentCode() is called in onResume(), so there
         * never should be an instance where the fragment layout state is inconsistent with the
         * stored state. If the restored fragment state is incorrect, showLoginOrPaymentCode() will
         * fix it.
         */
        transaction.commitAllowingStateLoss();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void compatInvalidateOptionsMenu() {
        if (EnvironmentUtil.isSdk11OrGreater()) {
            invalidateOptionsMenu();
        }
    }

    /**
     * Shows either the login screen or the payment code depending on the result of
     * {@link #isLoggedIn()}.
     */
    private void showLoginOrPaymentCode() {
        if (SharedPreferencesUtils.isLoggedIn(this)) {
            showPaymentCodeFragment();
        } else {
            showLoginFragment();
        }
    }

    /**
     * Shows the payment code fragment. This will replace any existing fragments loaded into the
     * destination frames (in this case, that would be the {@link LoginFragment}).
     */
    private void showPaymentCodeFragment() {
        FragmentManager fm = getSupportFragmentManager();

        /*
         * For easy management of fragment tags, the full classname is used to identify a given
         * fragment.
         */
        PaymentCodeFragment paymentCodeFragment =
                (PaymentCodeFragment) fm.findFragmentByTag(PaymentCodeFragment.class.getName());

        FragmentTransaction transaction = fm.beginTransaction();

        if (paymentCodeFragment == null) {
            paymentCodeFragment = new PaymentCodeFragment();
        }

        /* At this point, paymentCodeFragment will be a valid, possibly-unadded fragment. */

        if (!paymentCodeFragment.isAdded()) {
            transaction.replace(android.R.id.content, paymentCodeFragment,
                    PaymentCodeFragment.class.getName());
        }

        commitUiChange(transaction);
    }
}
