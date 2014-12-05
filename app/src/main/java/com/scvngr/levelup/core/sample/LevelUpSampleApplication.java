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

import android.app.Application;

import com.scvngr.levelup.core.util.CoreLibConstants;
import com.scvngr.levelup.core.util.NullUtils;
import com.scvngr.levelup.deeplinkauth.util.DeeplinkAuthLibConstants;

/**
 * Application subclass to assert that the environment is correctly configured.
 */
public class LevelUpSampleApplication extends Application {
    private static final String PROGUARDED_ASSERTION_ERROR_FORMAT =
            "Release builds must apply ProGuard but %s was not ProGuarded.";

    @Override
    public void onCreate() {
        super.onCreate();

        assertApplicationProperlyConfigured();
    }

    private void assertApplicationProperlyConfigured() {
        assertProguardedInReleaseMode();
    }

    /**
     * Workaround for https://code.google.com/p/android/issues/detail?id=52962.
     * @return whether the application is the release flavor.
     */
    private boolean isReleaseApplication() {
        return !BuildConfig.DEBUG && getPackageName().endsWith(".app");
    }

    /**
     * Asserts that library constants that depend on ProGuard are set to expected values in release
     * builds.
     */
    private void assertProguardedInReleaseMode() {
        if (isReleaseApplication()) {
            if (!CoreLibConstants.PROGUARDED) {
                throw new AssertionError(
                        NullUtils.format(PROGUARDED_ASSERTION_ERROR_FORMAT, "levelUpCoreLib"));
            }

            if (!DeeplinkAuthLibConstants.PROGUARDED) {
                throw new AssertionError(
                        NullUtils.format(PROGUARDED_ASSERTION_ERROR_FORMAT,
                                "levelUpDeeplinkAuthLib"));
            }
        }
    }

}
