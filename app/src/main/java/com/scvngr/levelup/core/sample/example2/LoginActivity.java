package com.scvngr.levelup.core.sample.example2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.scvngr.levelup.core.model.AccessToken;
import com.scvngr.levelup.core.sample.LoginFragment;
import com.scvngr.levelup.core.sample.R;
import com.scvngr.levelup.core.sample.net.AccessTokenLoaderCallbacks.OnLoginListener;
import com.scvngr.levelup.core.sample.net.RequestLoader.RequestResult;

/**
 * A simple login activity to hold a {@link LoginFragment}. This starts {@link PaymentActivity} when
 * login succeeds.
 */
public class LoginActivity extends FragmentActivity implements OnLoginListener {
    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        setContentView(R.layout.activity_login);

        LoginFragment loginFragment =
                (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_login);

        loginFragment.setOnLoginListener(this);
    }

    @Override
    public void onLogin(AccessToken accessToken) {
        // When a successful login occurs, show the PaymentActivity instead.
        startActivity(new Intent(this, PaymentActivity.class));
        finish();
    }

    @Override
    public void onError(RequestResult<AccessToken> result) {
        // Do nothing. The fragment will display the error.
    }
}
