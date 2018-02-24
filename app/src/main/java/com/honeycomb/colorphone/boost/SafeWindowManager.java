package com.honeycomb.colorphone.boost;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.crashlytics.android.core.CrashlyticsCore;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Permissions;


/**
 * A wrapper for {@link android.view.WindowManager} which checks permission on Marshmallow devices to avoid
 * {@link SecurityException}.
 */
public class SafeWindowManager {

    private WindowManager mWindowManager;
    private boolean useActivityManager;

    public SafeWindowManager() {
        mWindowManager = (WindowManager) HSApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
    }

    public void addView(View view, WindowManager.LayoutParams params) {
        if (Permissions.isFloatWindowAllowed(HSApplication.getContext())) {
            try {
                mWindowManager.addView(view, params);
                useActivityManager = false;
            } catch (SecurityException e) {
                CrashlyticsCore.getInstance().logException(e);
                e.printStackTrace();
            }
        } else {
            // 6.0 + , apk not from Google Play can not draw overlay
            // so wo use 'TYPE_APPLICATION' to display
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION;
            if (view.getContext() instanceof Activity) {
                useActivityManager = true;
                ((Activity) view.getContext()).getWindowManager().addView(view, params);
            } else {
                HSLog.w("WindowManager", "Failed to add float window because SYSTEM_ALERT_WINDOW permission is not granted");
            }
        }
    }

    private WindowManager getInnerManager(View v) {
        if (useActivityManager && v.getContext() instanceof Activity) {
            return ((Activity) v.getContext()).getWindowManager();
        }
        return mWindowManager;
    }

    public void removeView(View view) {
        try {
            getInnerManager(view).removeView(view);
        }catch (Exception e){
            CrashlyticsCore.getInstance().logException(e);
            e.printStackTrace();
        }
    }

    public void removeViewImmediate(View view) {
        getInnerManager(view).removeViewImmediate(view);
    }

    public void updateViewLayout(View view, WindowManager.LayoutParams params) {
        getInnerManager(view).updateViewLayout(view, params);
    }

    public Display getDefaultDisplay() {
        return mWindowManager.getDefaultDisplay();
    }
}
