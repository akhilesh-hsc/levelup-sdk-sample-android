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

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.levelup.core.app.net.RequestLoader;
import com.example.levelup.core.app.net.RequestLoader.RequestResult;
import com.scvngr.levelup.core.model.CreditCard;
import com.scvngr.levelup.core.model.Error;
import com.scvngr.levelup.core.model.factory.json.CreditCardJsonFactory;
import com.scvngr.levelup.core.net.AbstractRequest;
import com.scvngr.levelup.core.net.LevelUpStatus;
import com.scvngr.levelup.core.net.request.factory.CreditCardRequestFactory;

import java.util.List;

/**
 * A fragment which presents a form for users to add a credit card. This demonstrates creating the
 * request using {@link CreditCardRequestFactory} and processing the result. When a card has been
 * successfully added, it finishes the activity it's attached to.
 */
public class CreditCardAddFragment extends Fragment implements OnClickListener {

    public static final String TAG_PROGRESS_DIALOG = AddCardLoaderCallbacks.class.getName()
            + ".progress";

    /**
     * Loader ID for {@link #AddCardLoaderCallbacks}.
     */
    private static final int LOADER_ADD_CARD = 300;

    /**
     * A helper to map error message responses to form fields.
     */
    private ErrorResponseVisualizer mErrorResponseVisualizer = new ErrorResponseVisualizer(
            R.id.error_general);

    private AddCardLoaderCallbacks mLoaderCallbacks;

    public CreditCardAddFragment() {
        mErrorResponseVisualizer.addMapping(CreditCardRequestFactory.OUTER_PARAM_CARD,
                CreditCardRequestFactory.PARAM_ENCRYPTED_NUMBER, R.id.credit_card_number);

        mErrorResponseVisualizer.addMapping(CreditCardRequestFactory.OUTER_PARAM_CARD,
                CreditCardRequestFactory.PARAM_ENCRYPTED_CVV, R.id.cvv);

        mErrorResponseVisualizer.addMapping(CreditCardRequestFactory.OUTER_PARAM_CARD,
                CreditCardRequestFactory.PARAM_ENCRYPTED_EXPIRATION_MONTH, R.id.expiration_month);

        mErrorResponseVisualizer.addMapping(CreditCardRequestFactory.OUTER_PARAM_CARD,
                CreditCardRequestFactory.PARAM_ENCRYPTED_EXPIRATION_YEAR, R.id.expiration_year);

        mErrorResponseVisualizer.addMapping(CreditCardRequestFactory.OUTER_PARAM_CARD,
                CreditCardRequestFactory.PARAM_POSTAL_CODE, R.id.postal_code);
    }

    /**
     * Called when the loader successfully adds a card.
     */
    public void onCardAdded() {
        Toast.makeText(getActivity(), "Card successfully added.", Toast.LENGTH_SHORT).show();

        getActivity().finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_credit_card_add:
                addCard();
                break;
            default:
                // Do nothing.
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Request that this window be secure. This prevents credit card information being leaked by
         * way of screenshots (in the case of Android 4.x, the task switcher). It's good practice to
         * have this on the QR code screen, too, if it's normally behind a PIN. This was broken in
         * 2.x.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        mLoaderCallbacks = new AddCardLoaderCallbacks(this, LOADER_ADD_CARD);
    }

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_credit_card_add, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_credit_card_add).setOnClickListener(this);
    }

    /**
     * Submits an add card request using the form data.
     */
    protected void addCard() {
        addCard(getFormFieldOrError(R.id.credit_card_number), getFormFieldOrError(R.id.cvv),
                getFormFieldOrError(R.id.expiration_month),
                getFormFieldOrError(R.id.expiration_year), getFormFieldOrError(R.id.postal_code));
    }

    /**
     * Submits an add card request using the given parameters.
     * 
     * @param number card number.
     * @param cvv the CVV or security code.
     * @param expirationMonth expiration date month. 1 or 2 digits.
     * @param expirationYear expiration date year. 4 digits.
     * @param postalCode postal code.
     */
    protected void addCard(String number, String cvv, String expirationMonth,
            String expirationYear, String postalCode) {
        if (!TextUtils.isEmpty(number) && !TextUtils.isEmpty(expirationMonth)
                && !TextUtils.isEmpty(expirationYear) && !TextUtils.isEmpty(cvv)
                && !TextUtils.isEmpty(number) && !TextUtils.isEmpty(postalCode)) {

            Bundle args =
                    createLoaderArgs(number, cvv, expirationMonth, expirationYear, postalCode);
            getLoaderManager().restartLoader(LOADER_ADD_CARD, args, mLoaderCallbacks);
        }
    }

    /**
     * Creates a loader argument bundle based on the specified arguments.
     * 
     * @param number card number.
     * @param cvv the CVV or security code.
     * @param expirationMonth expiration date month. 1 or 2 digits.
     * @param expirationYear expiration date year. 4 digits.
     * @param postalCode postal code.
     * @return a bundle with the arguments stored in it.
     */
    protected Bundle createLoaderArgs(String number, String cvv, String expirationMonth,
            String expirationYear, String postalCode) {
        Bundle args = new Bundle();

        args.putString(AddCardLoaderCallbacks.ARG_CARD_NUMBER, number);
        args.putString(AddCardLoaderCallbacks.ARG_CVV, cvv);
        args.putString(AddCardLoaderCallbacks.ARG_EXPIRATION_MONTH, expirationMonth);
        args.putString(AddCardLoaderCallbacks.ARG_EXPIRATION_YEAR, expirationYear);
        args.putString(AddCardLoaderCallbacks.ARG_POSTAL_CODE, postalCode);

        return args;
    }

    /**
     * Gets the contents of the given {@link EditText}, as a {@link String}. If it is blank, it also
     * sets the error text on the text field (and returns the blank string).
     * 
     * @param editTextId the ID of the text field to retrieve.
     * @return the contents of the form field.
     */
    private String getFormFieldOrError(int editTextId) {
        EditText field = ((EditText) getView().findViewById(editTextId));
        String value = field.getText().toString();
        if (TextUtils.isEmpty(value)) {
            field.setError("Can't be blank.");
        } else {
            field.setError(null);
        }

        return value;
    }

    private void showAnyErrors(RequestResult<CreditCard> result) {
        List<Error> errors = result.getErrors();
        if (errors != null) {
            mErrorResponseVisualizer.showErrors(getView(), errors);
        } else {
            Exception responseError = result.getResponse().getError();
            if (responseError != null) {
                mErrorResponseVisualizer.showGeneralError(getView(),
                        responseError.getLocalizedMessage());

            } else {

                /*
                 * As a last resort, just show the response status. This shouldn't be done in real
                 * life.
                 */
                mErrorResponseVisualizer.showGeneralError(getView(), result.getResponse()
                        .getStatus().toString());
            }
        }
    }

    /**
     * Callbacks to create a credit card request. To use, provide the needed arguments. This can be
     * simplified by calling {@link #createLoaderArgs(String , String , String , String , String )}
     */
    private class AddCardLoaderCallbacks extends
            ProgressDialogLoaderCallbacks<RequestResult<CreditCard>> {

        public static final String ARG_CARD_NUMBER = "number";

        public static final String ARG_CVV = "cvv";
        public static final String ARG_EXPIRATION_MONTH = "expiration_month";
        public static final String ARG_EXPIRATION_YEAR = "expiration_year";
        public static final String ARG_POSTAL_CODE = "postal_code";

        public AddCardLoaderCallbacks(Fragment fragment, int loaderId) {
            super(fragment, loaderId, R.string.title_credit_card_add, R.string.card_add_progress,
                    TAG_PROGRESS_DIALOG);
        }

        @Override
        public Loader<RequestResult<CreditCard>> onCreateLoader(int id, Bundle args) {
            super.onCreateLoader(id, args);

            String number = args.getString(ARG_CARD_NUMBER);
            String cvv = args.getString(ARG_CVV);
            String expirationYear = args.getString(ARG_EXPIRATION_YEAR);
            String expirationMonth = args.getString(ARG_EXPIRATION_MONTH);
            String postalCode = args.getString(ARG_POSTAL_CODE);

            AbstractRequest cardRequest =
                    new CreditCardRequestFactory(getActivity(),
                            new SharedPreferencesAccessTokenRetriever()).buildCreateCardRequest(
                            number, cvv, expirationMonth, expirationYear, postalCode);

            return new RequestLoader<CreditCard>(getActivity(), cardRequest,
                    new CreditCardJsonFactory());
        }

        @Override
        public void onLoaderReset(Loader<RequestResult<CreditCard>> loader) {
            // Do nothing.
        }

        @Override
        public void onLoadFinished(Loader<RequestResult<CreditCard>> loader,
                RequestResult<CreditCard> result) {
            super.onLoadFinished(loader, result);

            if (result.getResponse().getStatus() == LevelUpStatus.OK) {
                onCardAdded();
            } else {
                showAnyErrors(result);
            }
        }
    }
}
