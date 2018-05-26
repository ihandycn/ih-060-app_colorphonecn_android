package com.honeycomb.colorphone.gdpr;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.honeycomb.colorphone.R;

public class RestartingAppProgressDialog extends DialogFragment {
    private static final String TAG = "RestartingAppProgressDialog";

    public static RestartingAppProgressDialog show(FragmentManager manager) {
        RestartingAppProgressDialog dialog = new RestartingAppProgressDialog();
        dialog.showAllowingStateLoss(manager, TAG);
        return dialog;
    }

    public void showAllowingStateLoss(FragmentManager manager, String tag) {
        // This prevents us from hitting FragmentManager.checkStateLoss() which
        // throws a runtime exception if state has already been saved.
        if (manager.isStateSaved()) {
            return;
        }

        show(manager, tag);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = View.inflate(getContext(), R.layout.layout_restarting_progress_dialog, null);

        return new AlertDialog.Builder(getContext()).setView(rootView).create();
    }
}
