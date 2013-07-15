package com.scvngr.levelup.core.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;

import com.scvngr.levelup.core.sample.example1.LoginAndPaymentActivity;
import com.scvngr.levelup.core.sample.example2.PaymentActivity;
import com.scvngr.levelup.core.sample.example3.PaymentWithTipActivity;

/**
 * Launcher for the various demos.
 */
public class DemoActivity extends FragmentActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        setContentView(R.layout.activity_demo);

        findViewById(R.id.button_combined).setOnClickListener(this);
        findViewById(R.id.button_individual).setOnClickListener(this);
        findViewById(R.id.button_register).setOnClickListener(this);
        findViewById(R.id.button_payment_with_tip).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_combined:
                startActivity(new Intent(this, LoginAndPaymentActivity.class));
                break;
            case R.id.button_individual:
                startActivity(new Intent(this, PaymentActivity.class));
                break;
            case R.id.button_register:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
            case R.id.button_payment_with_tip:
                startActivity(new Intent(this, PaymentWithTipActivity.class));
                break;
            default:
                // Do nothing.
        }
    }
}
