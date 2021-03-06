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
package com.example.levelup.core.app.example4;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.example.levelup.core.app.R;

/**
 * A simple activity that holds a {@link com.example.levelup.core.app.CreditCardAddFragment}.
 */
public class CreditCardAddActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_credit_card_add);
    }
}
