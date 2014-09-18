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
package com.scvngr.levelup.core.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;

import com.scvngr.levelup.core.sample.example1.LoginAndPaymentActivity;
import com.scvngr.levelup.core.sample.example2.PaymentActivity;
import com.scvngr.levelup.core.sample.example3.PaymentWithTipActivity;
import com.scvngr.levelup.core.sample.example4.CreditCardAddActivity;

/**
 * Launcher for the various demos.
 */
public class EnterpriseDemoActivity extends FragmentActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        setContentView(R.layout.activity_enterprise_demo);

        findViewById(R.id.button_combined).setOnClickListener(this);
        findViewById(R.id.button_individual).setOnClickListener(this);
        findViewById(R.id.button_register).setOnClickListener(this);
        findViewById(R.id.button_payment_with_tip).setOnClickListener(this);
        findViewById(R.id.button_credit_card_add).setOnClickListener(this);
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
            case R.id.button_credit_card_add:
                startActivity(new Intent(this, CreditCardAddActivity.class));
                break;
            default:
                // Do nothing.
        }
    }
}
