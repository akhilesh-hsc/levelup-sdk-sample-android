package com.scvngr.levelup.core.sample.example4;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.scvngr.levelup.core.sample.R;

/**
 * A simple activity that holds a {@link com.scvngr.levelup.core.sample.CreditCardAddFragment}.
 */
public class CreditCardAddActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_credit_card_add);
    }
}
