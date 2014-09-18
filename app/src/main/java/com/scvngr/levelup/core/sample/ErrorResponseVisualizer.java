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

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.scvngr.levelup.core.model.Error;

import java.util.HashMap;
import java.util.List;

/**
 * A helper to map error responses to EditText form fields. To use, call
 * {@link #addMapping(String, String, int)} to create new mappings between request fields and form
 * fields.
 */
public class ErrorResponseVisualizer {
    private final HashMap<String, Integer> mParameterToFieldMapping =
            new HashMap<String, Integer>();
    private final int mTextViewErrorGeneralId;

    /**
     * @param textViewErrorGeneralId the resource ID of the {@link TextView} that displays error
     *        messages when no matching mapping was found.
     */
    public ErrorResponseVisualizer(int textViewErrorGeneralId) {
        mTextViewErrorGeneralId = textViewErrorGeneralId;
    }

    /**
     * Creates a new mapping between the given request field and the form field that the user used
     * to input the specified request. This is keyed on the composite of the requestObject and
     * requestParameter, so every pair of them must be unique; any duplicates will be overwritten.
     * 
     * @param requestObject the object (eg.
     *        {@link com.scvngr.levelup.core.net.request.factory.UserRequestFactory#OUTER_PARAM_USER}
     *        ).
     * @param requestParameter the request parameter (eg.
     *        {@link com.scvngr.levelup.core.net.request.factory.UserRequestFactory#PARAM_EMAIL}).
     * @param editTextResourceId the resource ID of the {@link EditText} field used to enter the
     *        given parameter.
     */
    public void addMapping(String requestObject, String requestParameter, int editTextResourceId) {
        mParameterToFieldMapping.put(requestObject + "." + requestParameter, editTextResourceId);
    }

    /**
     * Shows the errors using {@link EditText#setError(CharSequence)} for all the errors that match
     * the mapped fields. If no field matches, the errors will be displayed in a newline-separated
     * list using {@link #showGeneralError(View, CharSequence)}.
     * 
     * @param container the view group containing all the associated views. This should probably
     *        just be your content view.
     * @param errors the list of errors to show.
     */
    public void showErrors(View container, List<Error> errors) {

        // Clear any previously-set error messages.
        for (int errorableFields : mParameterToFieldMapping.values()) {
            ((EditText) container.findViewById(errorableFields)).setError(null);
        }

        StringBuilder extraErrors = new StringBuilder();

        // Set the error message on any field.
        for (Error error : errors) {
            String key = error.getObject() + "." + error.getProperty();
            Integer fieldId = mParameterToFieldMapping.get(key);

            if (fieldId != null) {
                EditText erroringField = (EditText) container.findViewById(fieldId);
                CharSequence existingError = erroringField.getError();

                if (!TextUtils.isEmpty(existingError)) {
                    erroringField.setError(existingError + "\n" + error.getMessage());
                } else {
                    erroringField.setError(error.getMessage());
                }

                erroringField.requestFocus();
            } else {
                if (extraErrors.length() > 0) {
                    extraErrors.append("\n");
                }

                extraErrors.append(error.getMessage());
            }
        }

        showGeneralError(container, extraErrors);
    }

    /**
     * Displays an error message in the {@link TextView} whose ID was passed in the constructor.
     * 
     * @param container the container within which to find the given {@link TextView}.
     * @param error the error message to display or {@code null} to hide it.
     */
    public void showGeneralError(View container, CharSequence error) {
        /*
         * For any extra error messages that don't map to a field, show them in an error overflow
         * view.
         */
        TextView extraErrorView = (TextView) container.findViewById(mTextViewErrorGeneralId);
        extraErrorView.setText(error);

        if (!TextUtils.isEmpty(error)) {
            extraErrorView.setVisibility(View.VISIBLE);
        } else {
            extraErrorView.setVisibility(View.GONE);
        }
    }
}
