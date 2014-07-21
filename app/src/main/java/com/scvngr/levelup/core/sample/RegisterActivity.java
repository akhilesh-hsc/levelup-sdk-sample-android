package com.scvngr.levelup.core.sample;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * A simple activity to hold a {@link RegisterFragment}.
 */
public class RegisterActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        setContentView(R.layout.activity_register);
    }
}
