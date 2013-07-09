package com.scvngr.levelup.core.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * <p>
 * A basic tip selector fragment. This displays a {@link SeekBar} and a {@link TextView} to show the
 * current tip value.
 * </p>
 * <p>
 * To use, call {@link #newInstance(int[])} with a list of allowed tip values.
 * </p>
 */
public class TipSelectorFragment extends Fragment {

    /**
     * The fragment argument, storing the allowed tips.
     */
    private static final String ARG_ALLOWED_TIPS_INT_ARRAY = TipSelectorFragment.class.getName()
            + ".ALLOWED_TIPS";

    /**
     * Creates a new {@link TipSelectorFragment} with the specified allowed tips.
     * 
     * @param allowedTips a list of percentage tips that are user-selectable.
     * @return a new instance of this fragment.
     */
    public static final TipSelectorFragment newInstance(int[] allowedTips) {
        Bundle args = new Bundle(1);
        args.putIntArray(ARG_ALLOWED_TIPS_INT_ARRAY, allowedTips);

        TipSelectorFragment fragment = new TipSelectorFragment();
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * The list of allowed tip values. This is passed in by the {@link #ARG_ALLOWED_TIPS_INT_ARRAY}
     * fragment argument or by {@link #newInstance(int[])}.
     */
    private int[] mAllowedTips;

    /**
     * When the tip selector changes (the SeekBar), the tip values are updated in both the text
     * displayed to the user and in the QR code.
     */
    private OnSeekBarChangeListener mOnSeekBarChange = new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mTipValueView.setText(Integer.toString(mAllowedTips[progress]) + "% tip");

            if (mOnTipChangedListener != null) {
                mOnTipChangedListener.onTipChanged(mAllowedTips[progress]);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAllowedTips = getArguments().getIntArray(ARG_ALLOWED_TIPS_INT_ARRAY);
    }

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
        tipSelector.setMax(mAllowedTips.length - 1);
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
