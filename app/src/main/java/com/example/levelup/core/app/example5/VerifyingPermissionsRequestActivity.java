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
package com.example.levelup.core.app.example5;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.levelup.core.app.Constants;
import com.example.levelup.core.app.R;
import com.example.levelup.core.app.SharedPreferencesKeys;
import com.scvngr.levelup.core.net.Permissions;
import com.scvngr.levelup.core.util.LogManager;
import com.scvngr.levelup.deeplinkauth.LevelUpDeepLinkIntegrator;
import com.scvngr.levelup.deeplinkauth.LevelUpDeepLinkIntegrator.PermissionsRequestResult;

/**
 * A basic example of how to get a LevelUp access token using the {@link LevelUpDeepLinkIntegrator}.
 * This requires the LevelUp app to be installed on the device. This first checks to see if LevelUp
 * is installed on the device. If it's not, this activity displays a custom message informing the
 * user that LevelUp is needed to function.
 */
public class VerifyingPermissionsRequestActivity extends FragmentActivity {

    private LevelUpDeepLinkIntegrator mIntegrator;

    // Just a little view to show what's going on.
    private TextView mStatusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_permissions_request);

        String appId = getString(R.string.levelup_app_id);

        if (!Constants.PLACEHOLDER.equals(appId)) {
            mIntegrator = new LevelUpDeepLinkIntegrator(this,
                    Integer.parseInt(getString(R.string.levelup_app_id)));
        } else {
            Toast.makeText(VerifyingPermissionsRequestActivity.this,
                    getString(R.string.api_key_or_app_id_not_configured_message),
                    Toast.LENGTH_LONG).show();
            finish();
        }

        /*
         * In production code, you could choose to either not display the option to pay with LevelUp
         * or just let the default action to take place.
         */
        boolean isInstalled = LevelUpDeepLinkIntegrator.isLevelUpAvailable(getApplicationContext());

        findViewById(R.id.permissions_levelup_available).setVisibility(
                isInstalled ? View.VISIBLE : View.GONE);
        findViewById(R.id.permissions_levelup_unavailable).setVisibility(
                isInstalled ? View.GONE : View.VISIBLE);

        findViewById(R.id.button_request_permissions).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (mIntegrator != null) {
                            mIntegrator.requestPermissions(Permissions.PERMISSION_CREATE_ORDERS,
                                    Permissions.PERMISSION_READ_QR_CODE);
                        }
                    }
                }
        );
        mStatusView = (TextView) findViewById(R.id.status);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PermissionsRequestResult result =
                LevelUpDeepLinkIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (null != result) {
            if (result.isSuccessful()) {
                String accessToken = result.getAccessToken();

                mStatusView.setText("W00t, permission granted! Check out my access token: "
                        + accessToken);

                if (accessToken != null) {
                    SharedPreferences preferences =
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    // Store the result in the preferences.
                    preferences.edit().putString(SharedPreferencesKeys.ACCESS_TOKEN, accessToken)
                            .apply();

                    LogManager.d("Access token saved in preferences.");
                }
            } else {
                mStatusView.setText("Aww man. Permission DENIED.");
            }
        } else {
            mStatusView.setText("I got nothin'");
            // You can handle your own startActivityForResult results here.
        }
    }
}
