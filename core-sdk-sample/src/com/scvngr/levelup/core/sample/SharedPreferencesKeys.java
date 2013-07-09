package com.scvngr.levelup.core.sample;

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
