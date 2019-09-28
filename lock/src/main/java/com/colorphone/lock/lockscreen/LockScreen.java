package com.colorphone.lock.lockscreen;

import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;

import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;

/**
 * Abstraction for "locker screen"s.
 *
 * Terminology: lock screen = locker & charging screen.
 *
 * BE CONSISTENT with class and method names on this. Don't use non-standard terms like
 * "locker screen" or "charge screen". Don't use the term "locker" when referring to both
 * locker" and "charging screen". Use "lock screen" please.
 */
public abstract class LockScreen {
    protected ViewGroup mRootView;
    protected KeyguardHandler mKeyguardHandler;
    /**
     * Initialization.
     */
    public void setup(ViewGroup root, Bundle extra) {
        mRootView = root;
        mKeyguardHandler = new KeyguardHandler(getContext());
        mKeyguardHandler.onInit();
    }

    public KeyguardHandler getKeyguardHandler() {
        return mKeyguardHandler;
    }

    public ViewGroup getRootView() {
        return mRootView;
    }

    protected Context getContext() {
        return mRootView.getContext();
    }

    /**
     * @param dismissKeyguard Whether to remove system keyguard.
     */
    public void dismiss(Context context, boolean dismissKeyguard) {
        HSGlobalNotificationCenter.sendNotification(FloatWindowController.NOTIFY_KEY_LOCKER_DISMISS);
    }

    public void onDestroy(){
        mKeyguardHandler.onViewDestroy();
    }

    abstract public boolean isActivityHost();
}
