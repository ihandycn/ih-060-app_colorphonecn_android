package com.honeycomb.colorphone.activity;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Analytics;
import com.superapps.util.Threads;
import com.superapps.util.Toasts;

public class DoubleBackHandler {
    // 2s
    private long CLICK_BACK_INTERVAL = 2 * 1000;
    private long mLastClickTimeMills = 0;
    private Runnable toastDismissCallback = new Runnable() {
        @Override
        public void run() {
            Analytics.logEvent("Quit_Toast_Show", "Result", "False");
            isToastShow = false;
        }
    };
    private boolean isToastShow;

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
        Threads.removeOnMainThread(toastDismissCallback);
        if (isToastShow) {
            Analytics.logEvent("Quit_Toast_Show", "Result", "True");
            isToastShow = false;
        }

        return false;
    }

    public void toast() {
        isToastShow = true;
        Threads.postOnMainThreadDelayed(toastDismissCallback,2000);
        Toasts.showToast(R.string.click_again_to_exit);
    }
}
