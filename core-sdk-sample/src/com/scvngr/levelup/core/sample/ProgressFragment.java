package com.scvngr.levelup.core.sample;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.Loader;

/**
 * A simple indeterminate progress dialog for displaying a progress message.
 */
public class ProgressFragment extends DialogFragment {

    public static final String ARG_LOADER_ID_INT = "loader_id";
    private static final String ARG_MESSAGE_STRING_INT = "message";
    private static final String ARG_TITLE_STRING_INT = "title";

    /**
     * When a loader ID isn't specified, this ID is used instead to signal that the dialog is not
     * cancelable.
     */
    private static final int LOADER_ID_NOT_SPECIFIED = -1;

    /**
     * @param titleStringId the string resource ID of the title.
     * @param messageStringId the string resource ID of the message.
     * @param loaderId the {@link Loader} ID of the request that is showing this dialog. This allows
     *        the dialog to cancel the request.
     * @return a new instance of {@link ProgressFragment}.
     */
    public static ProgressFragment
            newInstance(int titleStringId, int messageStringId, int loaderId) {
        ProgressFragment progressFragment = new ProgressFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TITLE_STRING_INT, titleStringId);
        args.putInt(ARG_MESSAGE_STRING_INT, messageStringId);
        args.putInt(ARG_LOADER_ID_INT, loaderId);
        progressFragment.setArguments(args);

        return progressFragment;
    }

    /**
     * @param titleStringId the string resource ID of the title.
     * @param messageStringId the string resource ID of the message.
     * @return a new instance of {@link ProgressFragment}.
     */
    public static ProgressFragment newInstance(int titleStringId, int messageStringId) {
        ProgressFragment progressFragment = new ProgressFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TITLE_STRING_INT, titleStringId);
        args.putInt(ARG_MESSAGE_STRING_INT, messageStringId);
        args.putInt(ARG_LOADER_ID_INT, LOADER_ID_NOT_SPECIFIED);
        progressFragment.setArguments(args);

        return progressFragment;
    }

    private int mLoaderId;
    private int mMessage;
    private int mTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        mLoaderId = args.getInt(ARG_LOADER_ID_INT);
        mTitle = args.getInt(ARG_TITLE_STRING_INT);
        mMessage = args.getInt(ARG_MESSAGE_STRING_INT);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());

        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(mLoaderId != LOADER_ID_NOT_SPECIFIED);
        progressDialog.setTitle(mTitle);
        progressDialog.setMessage(getText(mMessage));

        if (mLoaderId != LOADER_ID_NOT_SPECIFIED) {
            progressDialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    getLoaderManager().destroyLoader(mLoaderId);
                }
            });
        }

        return progressDialog;
    }

    /**
     * Dismisses any progress dialogs that are currently showing which match the given tag.
     * 
     * @param fragmentManager the fragment manager that possibly showed a progress fragment.
     * @param tag the tag on the fragment.
     */
    public static void dismissAnyShowing(FragmentManager fragmentManager, String tag) {
        /*
         * Hide any progress dialog that may be showing. This is only done when no loader was found,
         * as the loader itself should dismiss the dialog otherwise.
         */
        ProgressFragment progressFragment =
                (ProgressFragment) fragmentManager.findFragmentByTag(tag);

        if (progressFragment != null && progressFragment.isAdded()) {
            progressFragment.dismissAllowingStateLoss();
        }
    }
}
