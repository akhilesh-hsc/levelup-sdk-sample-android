package com.example.levelup.core.app.example.oauth2;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.levelup.core.app.R;
import com.example.levelup.core.app.util.ApiSetupUtil;
import com.scvngr.levelup.core.net.Permissions;
import com.scvngr.levelup.core.ui.view.LevelUpOAuth2View;

import java.util.Collections;

/**
 * Simple example of using the {@code LevelUpOAuth2View} to retrieve an access token.
 */
public class LevelUpOAuth2Activity extends FragmentActivity {

    private LevelUpOAuth2View mWebView;
    private TextView mAccessTokenLabelTextView;
    private TextView mAccessTokenTextView;
    private EditText mEmailView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth2_example);

        ApiSetupUtil.checkApiKey(this);

        Button button = (Button) findViewById(R.id.button);
        mEmailView = (EditText) findViewById(R.id.email_text);
        mAccessTokenTextView = (TextView) findViewById(R.id.text1);
        mAccessTokenLabelTextView = (TextView) findViewById(R.id.text2);
        mWebView = (LevelUpOAuth2View) findViewById(R.id.webView);

        /*
         * Set the callback the view will use to report data back. Use the correct callback upon
         * life cycle changes.
         */
        mWebView.setCallback(mCallback);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                mWebView.setVisibility(View.VISIBLE);
                /*
                 * Begin an OAuth2 request to obtain an access token for the given email.
                 * {@link Permissions#PERMISSION_READ_QR_CODE} will be requested.
                 */
                mWebView.loadOAuthPage(mEmailView.getText().toString(),
                        Collections.singletonList(Permissions.PERMISSION_READ_QR_CODE));
            }
        });
    }

    /**
     * Call {@link LevelUpOAuth2View#saveState(Bundle)} to save the view's transient state.
     */
    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
    }

    /**
     * Call {@link LevelUpOAuth2View#restoreState(Bundle)} to restore the view's transient state.
     */
    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mWebView.restoreState(savedInstanceState);
    }

    /**
     * The callback which will receive the access token upon a successful request or data about
     * errors that occurred or an error if the request was not successful.
     */
    private LevelUpOAuth2View.OAuth2Callback mCallback = new LevelUpOAuth2View.OAuth2Callback() {

        @Override
        public void onAccessTokenReceived(@NonNull final String accessToken) {
            mAccessTokenTextView.setText(accessToken);
            mWebView.setVisibility(View.GONE);
            mAccessTokenLabelTextView.setVisibility(View.VISIBLE);
            mAccessTokenTextView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onError(@NonNull final String errorCode, @NonNull final String errorMessage) {
            mAccessTokenTextView.setText(errorCode + ":" + errorMessage);
            mWebView.setVisibility(View.GONE);
            mAccessTokenLabelTextView.setVisibility(View.GONE);
            mAccessTokenTextView.setVisibility(View.VISIBLE);
        }
    };
}
