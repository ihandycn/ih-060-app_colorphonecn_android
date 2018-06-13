package com.colorphone.lock.lockscreen;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

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

    /**
     * Initialization.
     */
    public void setup(ViewGroup root, Bundle extra) {
        updateFullScreenFlags(root);
        mRootView = root;
    }

    private void updateFullScreenFlags(ViewGroup root) {
        int viewFlag = root.getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            viewFlag |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            viewFlag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        }
        viewFlag |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        root.setSystemUiVisibility(viewFlag);
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
        int hideType = (dismissKeyguard ? 0 : FloatWindowController.HIDE_LOCK_WINDOW_NO_ANIMATION);
        FloatWindowController.getInstance().hideLockScreen(hideType);
    }
}
