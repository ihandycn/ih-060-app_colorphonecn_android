package com.honeycomb.colorphone.activity;

import com.honeycomb.colorphone.R;
import com.superapps.util.Toasts;

public class DoubleBackHandler {
    // 2s
    private long CLICK_BACK_INTERVAL = 2 * 1000;
    long mLastClickTimeMills = 0;

    /**
     * If handle back press event
     * @return true intercept this back press event
     */
    public boolean interceptBackPressed() {
        long interval = System.currentTimeMillis() - mLastClickTimeMills;
        mLastClickTimeMills = System.currentTimeMillis();
        if (interval > CLICK_BACK_INTERVAL) {
            return true;
        }
        return false;
    }

    public void toast() {
        Toasts.showToast(R.string.click_again_to_exit);
    }
}
