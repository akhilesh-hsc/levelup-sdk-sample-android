package com.scvngr.levelup.core.sample.example5;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.scvngr.levelup.core.net.Permissions;
import com.scvngr.levelup.core.sample.R;
import com.scvngr.levelup.core.sample.SharedPreferencesKeys;
import com.scvngr.levelup.core.util.LogManager;
import com.scvngr.levelup.deeplinkauth.DeepLinkAuthUtil;
import com.scvngr.levelup.deeplinkauth.LevelUpDeepLinkIntegrator;
import com.scvngr.levelup.deeplinkauth.LevelUpDeepLinkIntegrator.PermissionsRequestResult;

/**
 * A basic example of how to get a LevelUp access token using the {@link LevelUpDeepLinkIntegrator}.
 * This requires the LevelUp app to be installed on the device. If LevelUp is not installed, the
 * default behavior is to pop-up a dialog box prompting the user to download and install it.
 */
public class PermissionsRequestActivity extends FragmentActivity {

    // Just a little view to show what's going on.
    private TextView mStatusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_permissions_request);

        findViewById(R.id.button_request_permissions).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new LevelUpDeepLinkIntegrator(PermissionsRequestActivity.this, Integer
                                .parseInt(getString(R.string.levelup_app_id)))
                                .requestPermissions(Permissions.PERMISSION_CREATE_ORDERS,
                                        Permissions.PERMISSION_READ_QR_CODE);
                    }
                });
        mStatusView = (TextView) findViewById(R.id.status);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PermissionsRequestResult result =
                LevelUpDeepLinkIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (null != result) {
            if (result.isSuccessful()) {
                final String accessToken = result.getAccessToken();

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
