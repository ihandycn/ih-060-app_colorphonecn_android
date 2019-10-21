package com.honeycomb.colorphone.wallpaper.update;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;

import com.honeycomb.colorphone.wallpaper.customize.view.ProgressDialog;


/**
 * Created by sundxing on 16/11/15.
 * Dialog will be dismiss after short times.
 */

public class SplashProgressDialog {
    private ProgressDialog mDialog;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private long mTimeMills;
    private Dialog.OnDismissListener mDelegateDismissListener;
    public SplashProgressDialog(Context context, long timeMills) {
        mTimeMills = timeMills;
        mDialog = ProgressDialog.createDialog(context, "");
    }

    public void show() {
        mDialog.show();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss(true);
                    if (mDelegateDismissListener != null) {
                        mDelegateDismissListener.onDismiss(mDialog);
                    }
                }
            }
        }, mTimeMills);

        // User may close window by other way , so we should clean all callbacks to prevent mem leaks.
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mHandler.removeCallbacksAndMessages(null);
                mDelegateDismissListener = null;
            }
        });
    }

    public Dialog.OnDismissListener getDelegateDismissListener() {
        return mDelegateDismissListener;
    }

    public void setDelegateDismissListener(Dialog.OnDismissListener delegateDismissListener) {
        mDelegateDismissListener = delegateDismissListener;
    }
}
