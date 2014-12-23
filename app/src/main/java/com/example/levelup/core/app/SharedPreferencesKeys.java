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
package com.example.levelup.core.app;

/**
 * A simple class for storing all the keys for the shared preferences.
 */
public class SharedPreferencesKeys {
    /**
     * The user's access token. This is stored when the user logs in successfully.
     */
    public static final String ACCESS_TOKEN = "access_token";

    /**
     * The user's email address. This is stored when the user logs in.
     */
    public static final String EMAIL_ADDRESS = "email_address";

    /**
     * The user's unique LevelUp ID. This is stored when the user successfully logs in.
     */
    public static final String USER_ID = "user_id";

    /**
     * The token used for making a payment. This is shown in the QR Code.
     */
    public static final String PAYMENT_TOKEN = "payment_token";
}
