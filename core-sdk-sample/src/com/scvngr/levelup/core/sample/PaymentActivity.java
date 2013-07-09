package com.scvngr.levelup.core.sample;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.scvngr.levelup.core.model.AccessToken;
import com.scvngr.levelup.core.sample.LevelUpLoginFragment.OnLoginListener;
import com.scvngr.levelup.core.sample.TipSelectorFragment.OnTipChangedListener;

/**
 * <p>
 * A simple Payment Activity. This shows either a login screen or a payment screen depending on if
 * the user is logged in or not. This is generally considered the main screen.
 * </p>
 * <p>
 * LevelUp works by providing a QR code to the user which can be scanned at participating merchants
 * using a LevelUp-provided code scanner. The QR code (also known as the LevelUp Code) contains the
 * Payment Token as well as some user preferences that the scanner can understand.
 * </p>
 * <p>
 * The two preferences currently in use are a color and a tip value. The color value determines what
 * color the scanner will show (it lights up) when the LevelUp code is successfully scanned. The tip
 * value is a percentage of the transaction amount (working like a normal tip).
 * </p>
 * <p>
 * Both the tip and color are optional and it's safe to leave them out of your application by simply
 * setting them to 0.
 * </p>
 */
public class PaymentActivity extends FragmentActivity {

    /**
     * Although LevelUp codes can contain any tip value, it's simpler to limit them to a set of
     * fixed values. These are percentage values.
     */
    public static final int[] ALLOWED_TIPS = new int[] { 0, 5, 10, 15, 20, 25 };

    /**
     * When the user logs in, replace the login fragment with the payment code fragment.
     */
    private OnLoginListener mOnLoginListener = new OnLoginListener() {

        @Override
        public void onLogin(AccessToken accessToken) {
            showPaymentCodeFragment();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.payment, menu);
        return true;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
         * This layout has two frames for fragments: one for either the login screen or the payment
         * QR code, the other to put the tip selector into. The fragments are added using the
         * methods below.
         */
        setContentView(R.layout.activity_payment);
    }

    @Override
    protected void onPause() {
        super.onPause();

        /*
         * Break the link between the payment code fragment and the tip selector fragment. It will
         * be reset on the next call to {@link #showPaymentCodeFragment()}.
         */
        unregisterOnTipChangedListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        showLoginOrPaymentCode();
    }

    /**
     * Check to see if the user has a cached access token. Note: this doesn't check to make sure the
     * access token is still valid.
     * 
     * @return true if the user has an access token.
     */
    private boolean isLoggedIn() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String accessToken = preferences.getString(SharedPreferencesKeys.ACCESS_TOKEN, null);

        boolean loggedIn = accessToken != null;

        return loggedIn;
    }

    /**
     * Log the user out by clearing the access token and resetting the UI.
     */
    private void logout() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().remove(SharedPreferencesKeys.ACCESS_TOKEN)
                .remove(SharedPreferencesKeys.PAYMENT_TOKEN).apply();

        showLoginOrPaymentCode();
    }

    /**
     * Shows the login fragment. This also hooks in the {@link OnLoginListener} so that this
     * activity can be notified of when the user successfully logs in.
     */
    private void showLoginFragment() {
        FragmentManager fm = getSupportFragmentManager();
        LevelUpLoginFragment loginFragment =
                (LevelUpLoginFragment) fm.findFragmentByTag(LevelUpLoginFragment.class.getName());

        FragmentTransaction transaction = fm.beginTransaction();

        if (loginFragment == null) {
            loginFragment = LevelUpLoginFragment.newInstance();
        }

        if (!loginFragment.isAdded()) {
            transaction.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
            transaction.replace(R.id.payment_code_fragment, loginFragment,
                    LevelUpLoginFragment.class.getName());
        }

        loginFragment.setOnLoginListener(mOnLoginListener);

        /* Make sure to remove the tip selector fragment. */
        Fragment tipSelector = fm.findFragmentByTag(TipSelectorFragment.class.getName());

        if (tipSelector != null && tipSelector.isAdded()) {
            transaction.remove(tipSelector);
        }

        transaction.commitAllowingStateLoss();
    }

    /**
     * Shows either the login screen or the payment code depending on the result of
     * {@link #isLoggedIn()}.
     */
    private void showLoginOrPaymentCode() {
        if (isLoggedIn()) {
            showPaymentCodeFragment();
        } else {
            showLoginFragment();
        }
    }

    /**
     * Shows the payment code fragment, also loading in the tip selector fragment and hooking them
     * together. This will replace any existing fragments loaded into the destination frames (in
     * this case, that would be the {@link LevelUpLoginFragment}).
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
            transaction.replace(R.id.payment_code_fragment, paymentCodeFragment,
                    PaymentCodeFragment.class.getName());
        }

        TipSelectorFragment tipSelectorFragment =
                (TipSelectorFragment) fm.findFragmentByTag(TipSelectorFragment.class.getName());

        if (tipSelectorFragment == null) {
            tipSelectorFragment = TipSelectorFragment.newInstance(ALLOWED_TIPS);
        }

        /* At this point, tipSelectorFragment will be a valid, possibly-unadded fragment. */

        if (!tipSelectorFragment.isAdded()) {
            transaction.add(R.id.tip_selector_fragment, tipSelectorFragment,
                    TipSelectorFragment.class.getName());
        }

        /*
         * This is where the tip selector changes gets hooked to the PaymentCodeFragment. This
         * listener will be removed in onPause to break the linkage between the fragments.
         */
        tipSelectorFragment.setOnTipChangedListener(new PaymentCodeTipSelectorListener(
                paymentCodeFragment));

        transaction.commitAllowingStateLoss();

        /*
         * Pre-load the LevelUp code images so that they're ready to display before the user scrolls
         * the tip selector slider to them. Otherwise, it'd briefly show the progress spinner.
         */
        paymentCodeFragment.preCacheCodesForTips(ALLOWED_TIPS);
    }

    /**
     * Clear the {@link OnTipChangedListener} so that it breaks the link between the payment code
     * fragment and the tip selector fragment.
     */
    private void unregisterOnTipChangedListener() {
        TipSelectorFragment tipSelectorFragment =
                (TipSelectorFragment) getSupportFragmentManager().findFragmentByTag(
                        TipSelectorFragment.class.getName());

        if (tipSelectorFragment != null) {
            tipSelectorFragment.setOnTipChangedListener(null);
        }
    }

    /**
     * This is the glue between the tip selector and the payment code fragments. This calls
     * {@link PaymentCodeFragment#setTip(int)} when {@link #onTipChanged(int)} is called.
     */
    private static class PaymentCodeTipSelectorListener implements OnTipChangedListener {

        private PaymentCodeFragment mPaymentCodeFragment;

        public PaymentCodeTipSelectorListener(PaymentCodeFragment paymentCodeFragment) {
            mPaymentCodeFragment = paymentCodeFragment;
        }

        @Override
        public void onTipChanged(int tipValue) {
            mPaymentCodeFragment.setTip(tipValue);
        }
    }
}
