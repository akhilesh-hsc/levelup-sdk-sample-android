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
package com.scvngr.levelup.core.sample.example3;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.scvngr.levelup.core.sample.PaymentCodeFragment;
import com.scvngr.levelup.core.sample.R;
import com.scvngr.levelup.core.sample.SharedPreferencesUtils;
import com.scvngr.levelup.core.sample.example3.TipSelectorFragment.OnTipChangedListener;

/**
 * <p>
 * A simple payment Activity which shows a {@link PaymentCodeFragment} and a
 * {@link TipSelectorFragment}, linking them together.
 * </p>
 */
public class PaymentWithTipActivity extends FragmentActivity {

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
         * This layout has two fragments: the payment fragment and the tip selector fragment.
         */
        setContentView(R.layout.activity_payment_with_tip);

        // Normally, you'd want to bounce the user to the login screen here.
        if (!SharedPreferencesUtils.isLoggedIn(this)) {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        linkTipFragmentToPaymentCode();
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

    /**
     * Log the user out and finishes the activity.
     */
    private void logout() {
        SharedPreferencesUtils.logout(this);

        finish();
    }

    /**
     * Hooks the tip selector fragment to the payment code fragment. This causes changes in the tip
     * selector to be reflected in the payment code. This also pre-caches the payment codes so they
     * display quickly.
     */
    private void linkTipFragmentToPaymentCode() {
        FragmentManager fm = getSupportFragmentManager();

        PaymentCodeFragment paymentCodeFragment =
                (PaymentCodeFragment) fm.findFragmentById(R.id.payment_code_fragment);

        TipSelectorFragment tipSelectorFragment =
                (TipSelectorFragment) fm.findFragmentById(R.id.tip_selector_fragment);

        /*
         * This is where the tip selector changes gets hooked to the PaymentCodeFragment. This
         * listener will be removed in onPause to break the linkage between the fragments.
         */
        tipSelectorFragment.setOnTipChangedListener(new PaymentCodeTipSelectorListener(
                paymentCodeFragment));

        /*
         * Pre-load the LevelUp code images so that they're ready to display before the user scrolls
         * the tip selector slider to them. Otherwise, it'd briefly show the progress spinner.
         */
        paymentCodeFragment.preCacheCodesForTips(TipSelectorFragment.ALLOWED_TIPS);
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
