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
package com.example.levelup.core.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.example.levelup.core.app.example.oauth2.LevelUpOAuth2Activity;
import com.example.levelup.core.app.example3.PaymentWithTipActivity;
import com.example.levelup.core.app.example5.PermissionsRequestActivity;
import com.example.levelup.core.app.example5.VerifyingPermissionsRequestActivity;

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
        findViewById(R.id.button_oauth2_example).setOnClickListener(this);
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
            case R.id.button_oauth2_example:
                startActivity(new Intent(this, LevelUpOAuth2Activity.class));
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
