package com.scvngr.levelup.core.sample;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.scvngr.levelup.core.model.PaymentToken;
import com.scvngr.levelup.core.model.factory.json.PaymentTokenJsonFactory;
import com.scvngr.levelup.core.model.qr.LevelUpCode;
import com.scvngr.levelup.core.net.ApiStatus;
import com.scvngr.levelup.core.net.request.factory.PaymentTokenRequestFactory;
import com.scvngr.levelup.core.sample.net.RequestLoader;
import com.scvngr.levelup.core.sample.net.RequestLoader.RequestResult;
import com.scvngr.levelup.core.sample.net.RequestResultUtil;
import com.scvngr.levelup.core.sample.qr.ZXingCodeGenerator;
import com.scvngr.levelup.core.ui.view.AsyncTaskCodeLoader;
import com.scvngr.levelup.core.ui.view.HashMapCache;
import com.scvngr.levelup.core.ui.view.LevelUpCodeLoader;
import com.scvngr.levelup.core.ui.view.LevelUpCodeView;
import com.scvngr.levelup.core.ui.view.LevelUpCodeView.OnCodeLoadListener;

/**
 * A fragment that displays the user's LevelUp payment code. This also refreshes the cached payment
 * token or loads one if one hasn't been cached yet. If the user is no longer payment eligible, it
 * displays a message instead of the QR code.
 */
public final class PaymentCodeFragment extends Fragment {

    private static final int LOADER_PAYMENT_TOKEN = 100;
    private static final String STATE_COLOR_INT = PaymentCodeFragment.class.getName()
            + ".STATE_COLOR_INT";
    private static final String STATE_TIP_INT = PaymentCodeFragment.class.getName()
            + ".STATE_TIP_INT";

    /**
     * Loader of the QR code images.
     */
    private LevelUpCodeLoader mCodeLoader;

    /**
     * Callback for when the QR code is loading.
     */
    private OnCodeLoadListener mCodeLoadingListener = new OnCodeLoadListener() {

        @Override
        public void onCodeLoad(boolean isLoading) {
            if (isLoading) {
                setUiState(UI_STATE_LOADING);
            } else {
                setUiState(UI_STATE_SHOWING_CODE);
            }
        }
    };

    /**
     * The view that displays the QR codes.
     */
    private LevelUpCodeView mCodeView;

    /**
     * The user's chosen color.
     */
    private int mColor = 0;

    /**
     * In-memory cache of the payment token.
     */
    private String mPaymentToken;

    /**
     * Callbacks to be called when the payment token is loaded from the network.
     */
    private PaymentTokenLoaderCallbacks mPaymentTokenCallbacks = new PaymentTokenLoaderCallbacks();

    /**
     * The pre-cached tips to load. This is set if the payment token hasn't been loaded yet and
     * {@link #preCacheCodesForTips(int[])} gets called.
     */
    private int[] mPreCacheTips;

    private boolean mPreCacheTipsWasDeferred = false;

    /**
     * The user's chosen tip value.
     */
    private int mTip = 0;

    /**
     * The UI shows a progress indicator.
     */
    private static final int UI_STATE_LOADING = 100;

    /**
     * The UI is showing the LevelUp payment code.
     */
    private static final int UI_STATE_SHOWING_CODE = 101;

    /**
     * The UI is showing an error message.
     */
    private static final int UI_STATE_ERROR_MESSAGE = 102;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mTip = savedInstanceState.getInt(STATE_TIP_INT);
            mColor = savedInstanceState.getInt(STATE_COLOR_INT);
        }

        /*
         * The code loader is responsible for loading the images of the QR codes that are displayed
         * to the user using the LevelUpCodeView. In this instance, images are generated using the
         * ZXing library and cached in memory.
         */
        mCodeLoader = new AsyncTaskCodeLoader(new ZXingCodeGenerator(), new HashMapCache());
    }

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mCodeLoader = null;
    }

    @Override
    public void onPause() {
        super.onPause();

        mPaymentToken = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        showCachedPaymentCodeAndReload();
        preCacheDeferredCodes();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STATE_TIP_INT, mTip);
        outState.putInt(STATE_COLOR_INT, mColor);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCodeView = (LevelUpCodeView) getView().findViewById(R.id.payment_qr_code);
        mCodeView.setOnCodeLoadListener(mCodeLoadingListener);
    }

    /**
     * Pre-cache the QR code images for the given tip values. This makes it so that the user can
     * slide the tip slider and the images will already be loaded. If the payment token changes,
     * this will need to be run again. If the payment token hasn't loaded yet, the codes will be
     * cached upon a successful load.
     * 
     * @param tips the list of tip values to pre-cache.
     */
    public void preCacheCodesForTips(int[] tips) {
        String cachedPaymentToken = getCachedPaymentToken();
        mPreCacheTips = tips;

        if (cachedPaymentToken != null) {
            preCacheCodesForTipsInternal(cachedPaymentToken, tips);
        } else {
            // Defer the pre-caching until the payment token has been loaded.
            mPreCacheTipsWasDeferred = true;
        }
    }

    /**
     * Called once the payment token has been loaded to pre-cache the images.
     */
    private void preCacheDeferredCodes() {
        String paymentToken = getCachedPaymentToken();

        /*
         * If the payment token is null here, it's most likely due to the user being payment
         * ineligible.
         */
        if (mPreCacheTipsWasDeferred && paymentToken != null) {
            preCacheCodesForTipsInternal(paymentToken, mPreCacheTips);
            mPreCacheTipsWasDeferred = false;
        }
    }

    /**
     * Set the color that the dock turns when the code is scanned.
     * 
     * @param color the color index, between 0 and 9 inclusive.
     */
    public void setColor(int color) {
        if (mColor != color) {
            mColor = color;

            showCachedPaymentCode();
            // Refresh the pre-cached images.
            preCacheCodesForTips(mPreCacheTips);
        }
    }

    /**
     * Sets the tip percentage.
     * 
     * @param tip the tip percentage. A value between 0 and 100.
     */
    public void setTip(int tip) {
        if (mTip != tip) {
            mTip = tip;

            showCachedPaymentCode();
        }
    }

    /**
     * Called from the loader's onLoadFinished when the payment token has been loaded from the
     * network.
     * 
     * @param paymentToken the loaded payment token.
     */
    protected void onPaymentTokenLoaded(PaymentToken paymentToken) {
        setCachedPaymentToken(paymentToken);
        String paymentTokenString = paymentToken.getData();
        showPaymentCode(paymentTokenString);

        preCacheDeferredCodes();
    }

    /**
     * Clears the cached payment token, if there is one. In this case, it's stored in the
     * {@link SharedPreferences} under {@link SharedPreferencesKeys#PAYMENT_TOKEN}.
     */
    private void clearCachedPaymentToken() {
        mPaymentToken = null;
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                .remove(SharedPreferencesKeys.PAYMENT_TOKEN).apply();
    }

    /**
     * Updates the views to show a given state.
     * 
     * @param uiState one of {@link #UI_STATE_LOADING}, {@link #UI_STATE_ERROR_MESSAGE}, or
     *        {@link #UI_STATE_SHOWING_CODE}.
     */
    private void setUiState(int uiState) {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalStateException("Must be called from main thread");
        }

        int qrCodeVisibility;
        int progressVisibility;
        int errorMessageVisibility;

        switch (uiState) {
            case UI_STATE_LOADING:
                qrCodeVisibility = View.INVISIBLE;
                progressVisibility = View.VISIBLE;
                errorMessageVisibility = View.GONE;
                break;

            case UI_STATE_SHOWING_CODE:
                qrCodeVisibility = View.VISIBLE;
                progressVisibility = View.GONE;
                errorMessageVisibility = View.GONE;
                break;

            case UI_STATE_ERROR_MESSAGE:
                qrCodeVisibility = View.INVISIBLE;
                progressVisibility = View.GONE;
                errorMessageVisibility = View.VISIBLE;
                break;

            default:
                throw new IllegalArgumentException();
        }

        View fragmentView = getView();

        mCodeView.setVisibility(qrCodeVisibility);
        fragmentView.findViewById(R.id.progress).setVisibility(progressVisibility);
        fragmentView.findViewById(R.id.error_message).setVisibility(errorMessageVisibility);
    }

    /**
     * Sets the UI state to display a given error message.
     * 
     * @param errorMessage the error message to display.
     */
    private void showErrorMessage(CharSequence errorMessage) {
        setUiState(UI_STATE_ERROR_MESSAGE);
        TextView errorMessageView = (TextView) getView().findViewById(R.id.error_message);

        errorMessageView.setText(errorMessage);
    }

    /**
     * Retrieves the payment token from the cache. In this case, it's stored in the
     * {@link SharedPreferences} under {@link SharedPreferencesKeys#PAYMENT_TOKEN}. This also caches
     * the payment token in memory each time the fragment is resumed.
     * 
     * @return the cached payment token or {@code null} if none has been stored.
     */
    private String getCachedPaymentToken() {
        String paymentToken = null;

        if (mPaymentToken != null) {
            paymentToken = mPaymentToken;
        } else {
            Context context = getActivity();
            if (context != null) {
                paymentToken =
                        PreferenceManager.getDefaultSharedPreferences(context).getString(
                                SharedPreferencesKeys.PAYMENT_TOKEN, null);
                mPaymentToken = paymentToken;
            }
        }

        return paymentToken;
    }

    /**
     * This is called when the web service returns that the user is not payment eligible and any
     * cached payment tokens are invalid. To resolve this issue, the user needs to add a new credit
     * card to become payment eligible again.
     */
    private void onPaymentIneligible() {
        clearCachedPaymentToken();
        showErrorMessage(getText(R.string.payment_ineligible_notice));
    }

    /**
     * Actually performs the pre-caching of QR codes for the given list of tips. Does not block.
     * 
     * @param cachedPaymentToken payment token for code.
     * @param tips list of tips to pre-cache.
     */
    private void preCacheCodesForTipsInternal(String cachedPaymentToken, int[] tips) {
        for (int tipPercent : tips) {
            mCodeLoader.loadLevelUpCode(LevelUpCode.encodeLevelUpCode(cachedPaymentToken, mColor,
                    tipPercent));
        }
    }

    /**
     * Saves the payment token to the cache. In this case, it's stored in the
     * {@link SharedPreferences} under {@link SharedPreferencesKeys#PAYMENT_TOKEN}.
     * 
     * @param paymentToken the payment token to cache.
     */
    private void setCachedPaymentToken(PaymentToken paymentToken) {
        mPaymentToken = paymentToken.getData();

        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                .putString(SharedPreferencesKeys.PAYMENT_TOKEN, paymentToken.getData()).apply();
    }

    /**
     * Calls {@link #showPaymentCode(String)} with the cached payment code.
     */
    private void showCachedPaymentCode() {
        String paymentToken = getCachedPaymentToken();

        if (paymentToken != null) {
            showPaymentCode(paymentToken);
        } else {
            setUiState(UI_STATE_LOADING);
        }
    }

    /**
     * Shows the cached payment token if one's available and schedules a reload of the payment
     * token.
     */
    private void showCachedPaymentCodeAndReload() {
        showCachedPaymentCode();

        getLoaderManager().restartLoader(LOADER_PAYMENT_TOKEN, null, mPaymentTokenCallbacks);
    }

    /**
     * Loads the payment token into the {@link LevelUpCodeView}. This is where the code is encoded
     * as well, given the payment token, color, and tip values.
     * 
     * @param paymentTokenData the payment token.
     */
    private void showPaymentCode(String paymentTokenData) {
        mCodeView.setLevelUpCode(LevelUpCode.encodeLevelUpCode(paymentTokenData, mColor, mTip),
                mCodeLoader);
    }

    /**
     * Loader callbacks to retrieve a payment token from the network. The result will be cached and
     * displayed.
     */
    private class PaymentTokenLoaderCallbacks implements
            LoaderCallbacks<RequestResult<PaymentToken>> {

        @Override
        public Loader<RequestResult<PaymentToken>> onCreateLoader(int id, Bundle args) {
            Context context = getActivity();

            // On the first load, show a progress spinner.
            if (getCachedPaymentToken() == null) {
                setUiState(UI_STATE_LOADING);
            }

            return new RequestLoader<PaymentToken>(context, new PaymentTokenRequestFactory(context,
                    new SharedPreferencesAccessTokenRetriever()).buildGetPaymentTokenRequest(),
                    new PaymentTokenJsonFactory());
        }

        @Override
        public void onLoaderReset(Loader<RequestResult<PaymentToken>> loader) {
            // Do nothing.
        }

        @Override
        public void onLoadFinished(Loader<RequestResult<PaymentToken>> loader,
                RequestResult<PaymentToken> result) {
            ApiStatus status = result.getResponse().getStatus();

            if (status.equals(ApiStatus.OK)) {
                PaymentToken paymentToken = result.getResult();

                onPaymentTokenLoaded(paymentToken);
            } else {
                // Not found for this endpoint means "payment ineligible".
                if (status.equals(ApiStatus.ERROR_NOT_FOUND)) {
                    onPaymentIneligible();
                } else {
                    showErrorMessage(RequestResultUtil.errorsToString(result));
                }
            }
        }
    }
}
