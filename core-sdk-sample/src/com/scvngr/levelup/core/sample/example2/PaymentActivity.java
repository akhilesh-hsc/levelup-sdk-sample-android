package com.scvngr.levelup.core.sample.example2;

import com.scvngr.levelup.core.sample.PaymentCodeFragment;
import com.scvngr.levelup.core.sample.R;
import com.scvngr.levelup.core.sample.SharedPreferencesUtils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * A simple activity to hold a {@link PaymentCodeFragment}.
 */
public class PaymentActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        if (!SharedPreferencesUtils.isLoggedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            setContentView(R.layout.activity_payment);
        }
    }

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

    private void logout() {
        SharedPreferencesUtils.logout(this);
        finish();
    }
}
