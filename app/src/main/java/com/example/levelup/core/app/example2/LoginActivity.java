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
package com.example.levelup.core.app.example2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.example.levelup.core.app.LoginFragment;
import com.example.levelup.core.app.R;
import com.example.levelup.core.app.net.AccessTokenLoaderCallbacks.OnLoginListener;
import com.example.levelup.core.app.net.RequestLoader.RequestResult;
import com.scvngr.levelup.core.model.AccessToken;

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
