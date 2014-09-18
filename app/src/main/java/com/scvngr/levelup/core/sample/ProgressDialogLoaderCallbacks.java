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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

import java.lang.ref.WeakReference;

/**
 * An implementation of {@link LoaderCallbacks} which shows a progress dialog while the loader is
 * running. To use, extend and override {@link #onCreateLoader(int, Bundle)} and
 * {@link #onLoadFinished(Loader, Object)} per usual, making sure to call through to the super
 * class. It's recommended to call {@link #dismissProgressDialog()} from your fragment's
 * onViewStateRestored().
 * 
 * @param <D> the data type.
 */
public abstract class ProgressDialogLoaderCallbacks<D> implements LoaderCallbacks<D> {
    private final Context mApplicationContext;

    /*
     * By only holding onto weak references here, we can make sure we don't accidentally prevent an
     * fragment from being garbage collected.
     */
    private final WeakReference<Fragment> mFragment;

    private final String mFragmentTag;

    private int mLoaderId;

    private final int mMessageId;

    private final int mTitleId;

    /**
     * @param fragment the fragment displaying the progress dialog. This will be kept using a weak
     *        reference.
     * @param loaderId the ID of the loader that will be used with this dialog.
     * @param titleId the resource ID of the title string.
     * @param messageId the resource ID of the message string.
     * @param fragmentTag the tag of the progress dialog fragment.
     */
    public ProgressDialogLoaderCallbacks(Fragment fragment, int loaderId, int titleId,
            int messageId, String fragmentTag) {
        mFragment = new WeakReference<Fragment>(fragment);
        mApplicationContext = fragment.getActivity().getApplicationContext();
        mTitleId = titleId;
        mMessageId = messageId;
        mFragmentTag = fragmentTag;
        mLoaderId = loaderId;
    }

    /**
     * Dismisses the progress dialog.
     */
    public void dismissProgressDialog() {
        Fragment fragment = getFragment();

        if (isFinishingOrGone()) {
            return;
        }

        ProgressFragment progressFragment =
                (ProgressFragment) fragment.getFragmentManager().findFragmentByTag(mFragmentTag);

        if (progressFragment != null && progressFragment.isAdded()) {
            progressFragment.dismissAllowingStateLoss();
        }
    }

    @Override
    public Loader<D> onCreateLoader(int id, Bundle args) {
        if (id == mLoaderId) {
            Fragment fragment = getFragment();

            // If the activity is finishing or gone, bail.
            if (isFinishingOrGone()) {
                return null;
            }

            ProgressFragment.newInstance(mTitleId, mMessageId).show(fragment.getFragmentManager(),
                    mFragmentTag);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<D> loader, D result) {
        if (loader.getId() == mLoaderId) {
            dismissProgressDialog();
        }
    }

    /**
     * If there's an existing loader, reconnect to it so the callbacks are delivered. This would
     * happen if the user made the request and wandered away from the activity (say, by an incoming
     * call or to check their email) before the loader finished. When they returned, the result
     * would be delivered here. This also dismisses any progress dialogs that may be showing. This
     * method should be called on the start of the activity.
     */
    public void reconnectOrDismiss() {
        Fragment fragment = getFragment();

        if (fragment != null) {
            LoaderManager lm = fragment.getLoaderManager();
            Loader<D> existingLoader = lm.getLoader(mLoaderId);

            /*
             * Only call initLoader if there's an existing loader and it's got content waiting to be
             * delivered. This reconnects the loader to the callbacks.
             */
            if (existingLoader != null) {
                lm.initLoader(mLoaderId, null, this);
            } else {
                // This can happen if the result won't be delivered.
                dismissProgressDialog();
            }
        }
    }

    /**
     * @return the application context.
     */
    protected Context getApplicationContext() {
        return mApplicationContext;
    }

    /**
     * Returns the fragment or null if it's no longer around.
     * 
     * @return the fragment passed into the constructor or null if it has been garbage collected.
     */
    protected Fragment getFragment() {
        return mFragment.get();
    }

    /**
     * @return true if the activity is finishing or the fragment this is attached to is gone.
     */
    protected boolean isFinishingOrGone() {
        Fragment fragment = getFragment();

        return fragment == null || fragment.getActivity().isFinishing();
    }
}
