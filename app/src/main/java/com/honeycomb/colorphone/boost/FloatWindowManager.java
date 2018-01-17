package com.honeycomb.colorphone.boost;

import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

import com.ihs.commons.utils.HSLog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FloatWindowManager {

    private static final String TAG = FloatWindowManager.class.getSimpleName();
    private static final int MSG_DISMISS_DIALOG = 1;

    private volatile static FloatWindowManager sInstance;

    public static FloatWindowManager getInstance() {
        if (sInstance == null) {
            sInstance = new FloatWindowManager();
        }

        return sInstance;
    }

    protected Map<Class, FloatWindowDialog> mDialogs = new HashMap<>(6);

    protected SafeWindowManager sWindowManager;

    protected final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DISMISS_DIALOG:
                    FloatWindowDialog dialog = (FloatWindowDialog) msg.obj;
                    dialog.dismiss();
                    break;
            }
        }
    };

    public void showDialog(FloatWindowDialog dialog) {
        HSLog.d("FloatWindowManager", "show dialog : " + dialog.getClass().getSimpleName());
        try {
            String token = dialog.getClass().getSimpleName();
            if (dialog.hasNoNeedToShow()) {
                HSLog.i(TAG, "Dialog " + token + " has no need to show");
                return;
            }
           FloatWindowDialog oDialog = mDialogs.put(dialog.getClass(), dialog);
            if (oDialog != null) {
                removeDialog(oDialog);
                if (HSLog.isDebugging()) {
                    throw new IllegalStateException("Dialog exists already, please remove it before");
                }
                HSLog.e("");
            }
            final WindowManager.LayoutParams windowParams = dialog.getLayoutParams();

            getWindowManager().addView(dialog, windowParams);
            dialog.onAddedToWindow(getWindowManager());

        } catch (Exception e) {
            e.printStackTrace();
            HSLog.e("Error show dialog: " + e.getMessage());
        }
    }

    public FloatWindowDialog getDialog(Class<? extends FloatWindowDialog> cls) {
        return mDialogs.get(cls);
    }

    public void removeDialog(FloatWindowDialog dialog) {
        HSLog.d("BoostPlusCleanDialog", "removeDialog == " + dialog);
        if (dialog != null) {
            try {
                dialog.setSystemUiVisibility(0);
                getWindowManager().removeView(dialog);
                HSLog.d("BoostPlusCleanDialog", "removeDialog success");
            } catch (Exception ignored) {
                HSLog.d("BoostPlusCleanDialog", "removeDialog Exception");
            }
            mDialogs.remove(dialog.getClass());
        }
    }

    public void removeAllDialogs() {
        if (!mDialogs.isEmpty()) {
            Iterator<Map.Entry<Class, FloatWindowDialog>> iter = mDialogs.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Class, FloatWindowDialog> cur = iter.next();
                removeDialog(cur.getValue());
            }
        }
    }

    private void removeDialogView(FloatWindowDialog dialog) {
        try {
            dialog.setSystemUiVisibility(0);
            getWindowManager().removeView(dialog);
        } catch (Exception ignored) {
        }
    }

    public boolean isDialogShowing(Class<? extends FloatWindowDialog> cls) {
        return getDialog(cls) != null;
    }

    public boolean isShowingModalTip() {
        return !mDialogs.isEmpty();
    }

    public void dismissAnyModalTip() {
        Iterator<FloatWindowDialog> dialogIterator = mDialogs.values().iterator();
        while (dialogIterator.hasNext()) {
            FloatWindowDialog windowDialog = dialogIterator.next();
            if (windowDialog.isModalTip()) {
                removeDialogView(windowDialog);
                dialogIterator.remove();
            }
        }
    }

    public void onLauncherStop() {
        for (FloatWindowDialog dialog : mDialogs.values()) {
            if (dialog != null && dialog.shouldDismissOnLauncherStop()) {
                mHandler.sendMessage(getDismissMsg(dialog));
            }
        }
    }

    private Message getDismissMsg(FloatWindowDialog dialog) {
        Message message = Message.obtain();
        message.what = MSG_DISMISS_DIALOG;
        message.obj = dialog;
        return message;
    }

    protected SafeWindowManager getWindowManager() {
        if (sWindowManager == null) {
            sWindowManager = new SafeWindowManager();
        }
        return sWindowManager;
    }
}
