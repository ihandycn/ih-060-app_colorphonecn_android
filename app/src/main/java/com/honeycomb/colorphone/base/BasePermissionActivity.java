package com.honeycomb.colorphone.base;

import android.app.Dialog;
import android.os.Bundle;

import com.honeycomb.colorphone.util.ActivityUtils;
import com.superapps.broadcast.BroadcastCenter;
import com.superapps.broadcast.BroadcastListener;

public abstract class BasePermissionActivity extends BaseCenterActivity {

    private static final String TAG = "BasePermissionActivity";
    private BroadcastListener mCloseSystemDialogsReceiver;
    protected boolean mIsHomeKeyClicked;
    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (registerCloseSystemDialogsReceiver()) {
//            mCloseSystemDialogsReceiver = new BroadcastListener() {
//                private SystemKeyRecognizer mRecognizer = new SystemKeyRecognizer();
//
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    Runnable allKeys = () -> LauncherFloatWindowManager.getInstance().removeFloatButton();
//                    Runnable homeKey = () -> {
//                        mIsHomeKeyClicked = true;
//                        allKeys.run();
//                    };
//                    mRecognizer.onBroadcast(intent, homeKey, allKeys, allKeys, allKeys, allKeys);
//                }
//            };
//            BroadcastCenter.register(this, mCloseSystemDialogsReceiver,
//                    new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        LauncherFloatWindowManager.getInstance().removeFloatButton();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registerCloseSystemDialogsReceiver() && null != mCloseSystemDialogsReceiver) {
            BroadcastCenter.unregister(this, mCloseSystemDialogsReceiver);
            mCloseSystemDialogsReceiver = null;
        }
    }

    public boolean showDialog(Dialog dialog) {
        if (isFinishing() || ActivityUtils.isDestroyed(this)) {
            return false;
        }

        dismissDialog();

        mDialog = dialog;
        dialog.show();

        return true;
    }

    @Override
    public void dismissDialog() {
        super.dismissDialog();
        if (mDialog != null && !ActivityUtils.isDestroyed(this)) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    protected boolean registerCloseSystemDialogsReceiver() {
        return false;
    }
}
