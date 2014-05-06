package com.scvngr.levelup.core.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.scvngr.levelup.core.sample.example3.PaymentWithTipActivity;
import com.scvngr.levelup.core.sample.example5.PermissionsRequestActivity;
import com.scvngr.levelup.core.sample.example5.VerifyingPermissionsRequestActivity;

/**
 * Launcher for the various demos.
 */
public class DemoActivity extends FragmentActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        setContentView(R.layout.activity_demo);

        findViewById(R.id.button_payment_with_tip).setOnClickListener(this);
        findViewById(R.id.button_request_permissions).setOnClickListener(this);
        findViewById(R.id.button_verifying_request_permissions).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_payment_with_tip:
                startActivity(new Intent(this, PaymentWithTipActivity.class));
                break;
            case R.id.button_request_permissions:
                startActivity(new Intent(this, PermissionsRequestActivity.class));
                break;
            case R.id.button_verifying_request_permissions:
                startActivity(new Intent(this, VerifyingPermissionsRequestActivity.class));
                break;
            default:
                // Do nothing.
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                SharedPreferencesUtils.logout(this);
                finish();
                return true;
            case R.id.enterprise:
                startActivity(new Intent(this, EnterpriseDemoActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
