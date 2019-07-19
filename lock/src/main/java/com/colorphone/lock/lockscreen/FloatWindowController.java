package com.colorphone.lock.lockscreen;

import android.content.Context;
import android.os.Bundle;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSLog;

public class FloatWindowController {

    public static final String NOTIFY_KEY_LOCKER_DISMISS = "key_locker_dismiss";

    public final static int HIDE_LOCK_WINDOW_NONE = 0;
    public final static int HIDE_LOCK_WINDOW_AUTO_LOCK = 1;
    public final static int HIDE_LOCK_WINDOW_DISMISS_ACTIVITY = 2;
    public final static int HIDE_LOCK_WINDOW_NO_ANIMATION = 4; // == !dismissKeyguard, TODO: consider refactor this?

    private static FloatWindowController instance;

    private Context context;

    private FloatWindowControllerImpl floatWindowControllerImpl;

    public static void init(Context context) {
        if (instance == null) {
            instance = new FloatWindowController(context);
        }
    }

    public static FloatWindowController getInstance() {
        init(HSApplication.getContext());
        return instance;
    }

    private FloatWindowController(Context context) {
        this.context = context.getApplicationContext();
    }

    public synchronized void start() {
        if (null == floatWindowControllerImpl) {
            floatWindowControllerImpl = new FloatWindowControllerImpl(context);
        }
    }

    public synchronized void stop() {
        if (null != floatWindowControllerImpl) {
            floatWindowControllerImpl.release();
            floatWindowControllerImpl = null;
        }
    }

    public void showLockScreen() {
        if (null != floatWindowControllerImpl) {
            floatWindowControllerImpl.showLockScreen();

        }
    }

    public boolean showChargingScreen(Bundle bundle) {
        if (null != floatWindowControllerImpl) {
            return floatWindowControllerImpl.showChargingScreen(bundle);
        }
        return false;
    }

    public void hideLockScreen(int closeType) {
        if (null != floatWindowControllerImpl) {
            floatWindowControllerImpl.hideLockScreen(closeType);
            HSLog.i("NotificationDisplayManager", "sendNotification s: " + NOTIFY_KEY_LOCKER_DISMISS);
            HSGlobalNotificationCenter.sendNotification(NOTIFY_KEY_LOCKER_DISMISS);
        }
    }

    public boolean isLockScreenShown() {
        if (null == floatWindowControllerImpl) {
            return false;
        }
        return floatWindowControllerImpl.isLockScreenShown();
    }

    public boolean isCalling() {
        if (null == floatWindowControllerImpl) {
            return false;
        }
        return floatWindowControllerImpl.isCalling();
    }
}
