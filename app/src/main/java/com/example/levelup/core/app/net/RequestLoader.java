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
package com.example.levelup.core.app.net;

import android.content.Context;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;

import com.example.levelup.core.app.Constants;
import com.example.levelup.core.app.net.RequestLoader.RequestResult;
import com.scvngr.levelup.core.model.Error;
import com.scvngr.levelup.core.model.factory.json.AbstractJsonModelFactory;
import com.scvngr.levelup.core.model.factory.json.ErrorJsonFactory;
import com.scvngr.levelup.core.net.AbstractRequest;
import com.scvngr.levelup.core.net.LevelUpConnection;
import com.scvngr.levelup.core.net.LevelUpResponse;
import com.scvngr.levelup.core.net.LevelUpStatus;
import com.scvngr.levelup.core.util.LogManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * <p>
 * A loader that makes network requests and returns LevelUp Web Service objects (
 * {@link com.scvngr.levelup.core.model.User}, {@link com.scvngr.levelup.core.model.AccessToken},
 * {@link com.scvngr.levelup.core.model.PaymentToken}, etc.).
 * </p>
 * <p>
 * Note: currently this only supports endpoints that return a single object.
 * </p>
 * 
 * @param <T> the type of model to load.
 */
public class RequestLoader<T extends Parcelable> extends AsyncTaskLoader<RequestResult<T>> {

    private final AbstractRequest mRequest;
    private final AbstractJsonModelFactory<T> mModelFactory;

    private RequestResult<T> mResult;

    /**
     * Create a new loader to load the given {@link AbstractRequest}.
     * 
     * @param context application context.
     * @param request the request to load.
     * @param modelFactory the model factory to use to process the result.
     */
    public RequestLoader(Context context, AbstractRequest request,
            AbstractJsonModelFactory<T> modelFactory) {
        super(context);
        mRequest = request;
        mModelFactory = modelFactory;
    }

    @Override
    public RequestResult<T> loadInBackground() {
        T result = null;

        LevelUpConnection connection =
                LevelUpConnection.newInstance(getContext());

        LogManager.v("Sending request %s...", mRequest);
        LevelUpResponse response = connection.send(mRequest);

        // A helpful delay for debugging. See Constants.
        if (Constants.ASYNC_BACKGROUND_TASK_DELAY_ENABLED) {
            SystemClock.sleep(Constants.ASYNC_BACKGROUND_TASK_DELAY_MS);
        }

        LogManager.v("Got response %s", response);

        String data = response.getData();
        List<Error> errors = null;

        try {
            if (response.getStatus().equals(LevelUpStatus.OK)) {
                LogManager.v("Parsing response...");
                result = mModelFactory.from(new JSONObject(data));
            } else {
                /*
                 * The LevelUp web service returns JSON arrays of errors in its responses. In a
                 * real-world application, this should probably check the Content-Type returned
                 * before attempting to parse it as JSON.
                 */
                if (!TextUtils.isEmpty(data.trim())) {
                    errors = new ErrorJsonFactory().fromList(new JSONArray(data));
                }
            }
        } catch (JSONException e) {
            // Don't mask other errors with a parsing error.
            if (response.getStatus().equals(LevelUpStatus.OK)) {
                LogManager.e("JSONException while parsing model", e);
                response = new LevelUpResponse(data, LevelUpStatus.ERROR_PARSING);
            }
        }

        return new RequestResult<T>(response, result, errors);
    }

    @Override
    public void deliverResult(RequestResult<T> data) {
        mResult = data;

        if (isStarted() && !isAbandoned()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        mResult = null;
    }

    @Override
    protected void onStartLoading() {
        /*
         * When starting the loader, if there's a result already, deliver it instead of loading it
         * again from the network.
         */
        if (mResult != null) {
            deliverResult(mResult);
        } else {
            forceLoad();
        }
    }

    /**
     * A simple composite object returning both the response and the parsed result. If
     * {@link #getResult()} returns null, {@link #getErrors()} should be checked.
     * 
     * @param <T2> the type of model to return.
     */
    public static final class RequestResult<T2 extends Parcelable> {
        private final LevelUpResponse mResponse;
        private final T2 mResult;
        private final List<Error> mErrors;

        /**
         * @param response the full network response.
         * @param result the result of the request, if successful.
         * @param errors an optional list of errors generated by web service.
         */
        public RequestResult(LevelUpResponse response, T2 result, List<Error> errors) {
            mResponse = response;
            mResult = result;
            mErrors = errors;
        }

        /**
         * @return the full network response.
         */
        public LevelUpResponse getResponse() {
            return mResponse;
        }

        /**
         * @return the result of the request, if successful.
         */
        public T2 getResult() {
            return mResult;
        }

        /**
         * @return one or more errors from the web service, or null if the request was successful.
         */
        public List<Error> getErrors() {
            return mErrors;
        }
    }
}
