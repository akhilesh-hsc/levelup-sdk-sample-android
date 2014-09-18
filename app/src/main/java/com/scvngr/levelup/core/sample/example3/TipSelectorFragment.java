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
package com.scvngr.levelup.core.sample.example3;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.scvngr.levelup.core.sample.R;

/**
 * <p>
 * A basic tip selector fragment. This displays a {@link SeekBar} and a {@link TextView} to show the
 * current tip value.
 * </p>
 */
public class TipSelectorFragment extends Fragment {

    /**
     * Although LevelUp codes can contain any tip percentage, it's simpler (in the UI) to limit them
     * to a set of fixed values. These are percentage values.
     */
    public static final int[] ALLOWED_TIPS = new int[] { 0, 5, 10, 15, 20, 25 };

    public static final TipSelectorFragment newInstance() {
        return new TipSelectorFragment();
    }

    /**
     * When the tip selector changes (the SeekBar), the tip values are updated in both the text
     * displayed to the user and in the QR code.
     */
    private OnSeekBarChangeListener mOnSeekBarChange = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mTipValueView.setText(Integer.toString(ALLOWED_TIPS[progress]) + "% tip");

            if (mOnTipChangedListener != null) {
                mOnTipChangedListener.onTipChanged(ALLOWED_TIPS[progress]);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // Do nothing.
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // Do nothing.
        }
    };

    /**
     * The listener that will receive tip changed updates. This can be null.
     */
    private OnTipChangedListener mOnTipChangedListener;

    private TextView mTipValueView;

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_tip_selector, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SeekBar tipSelector = (SeekBar) view.findViewById(R.id.tip_selector);

        mTipValueView = (TextView) view.findViewById(R.id.tip_value);

        // The progress on the SeekBar represents the indices of valid tip values.
        tipSelector.setMax(ALLOWED_TIPS.length - 1);
        tipSelector.setOnSeekBarChangeListener(mOnSeekBarChange);

        mOnSeekBarChange.onProgressChanged(tipSelector, 0, false);
    }

    /**
     * Sets the {@link OnTipChangedListener}.
     * 
     * @param onTipChangedListener the listener that will receive tip changed updates or
     *        {@code null}.
     */
    public void setOnTipChangedListener(OnTipChangedListener onTipChangedListener) {
        this.mOnTipChangedListener = onTipChangedListener;
    }

    /**
     * A listener for when the tip is changed. This will be called when the user changes the tip
     * value.
     */
    public interface OnTipChangedListener {

        /**
         * Called when the user changes the tip value. This will be called on the UI thread.
         * 
         * @param tipPercentage the tip value, in percentage.
         */
        public void onTipChanged(int tipPercentage);
    }

}
