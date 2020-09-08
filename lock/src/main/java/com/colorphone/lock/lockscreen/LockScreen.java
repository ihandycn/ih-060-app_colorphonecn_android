package com.colorphone.lock.lockscreen;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;

import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.smartlocker.SmartLockerFeedsActivity;
import com.colorphone.smartlocker.utils.AutoPilotUtils;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.superapps.util.Threads;

import net.appcloudbox.ads.expressad.AcbExpressAdManager;

/**
 * Abstraction for "locker screen"s.
 * <p>
 * Terminology: lock screen = locker & charging screen.
 * <p>
 * BE CONSISTENT with class and method names on this. Don't use non-standard terms like
 * "locker screen" or "charge screen". Don't use the term "locker" when referring to both
 * locker" and "charging screen". Use "lock screen" please.
 */
public abstract class LockScreen {
    protected ViewGroup mRootView;
    protected KeyguardHandler mKeyguardHandler;
    protected boolean mActivityMode;

    /**
     * Initialization.
     */
    public void setup(ViewGroup root, Bundle extra) {
        mRootView = root;
        mKeyguardHandler = new KeyguardHandler(getContext());
        mKeyguardHandler.onInit();

        AutoPilotUtils.logNewsShow();
    }

    public ViewGroup getRootView() {
        return mRootView;
    }

    protected Context getContext() {
        return mRootView.getContext();
    }

    public void dismiss(Context context, boolean dismissKeyguard) {
        if (context instanceof BaseKeyguardActivity || context instanceof SmartLockerFeedsActivity) {
            if (dismissKeyguard) {
                mKeyguardHandler.tryDismissKeyguard(true, (Activity) getContext());
            } else {
                ((Activity) context).finish();
                ((Activity) context).overridePendingTransition(0, 0);
            }
        } else {
            onPause();
            onStop();
            onDestroy();
            if (dismissKeyguard) {
                mKeyguardHandler.tryDismissKeyguard();
            }
            int hideType = (dismissKeyguard ? 0 : FloatWindowController.HIDE_LOCK_WINDOW_NO_ANIMATION);
            FloatWindowController.getInstance().hideLockScreen(hideType);
        }

        AcbExpressAdManager.getInstance().deactivePlacementInProcess(LockerCustomConfig.get().getLockerAndChargingAdName());

        Threads.postOnMainThreadDelayed(new Runnable() {
            @Override
            public void run() {
                HSGlobalNotificationCenter.sendNotification(FloatWindowController.NOTIFY_KEY_LOCKER_DISMISS);
            }
        }, 1000);

    }

    public void onPause() {
    }

    public void onStop() {
    }

    public void onDestroy() {
        mKeyguardHandler.onViewDestroy();
    }

    abstract public boolean isActivityHost();

    public void setActivityMode(boolean activityMode) {
        mActivityMode = activityMode;
    }
}
