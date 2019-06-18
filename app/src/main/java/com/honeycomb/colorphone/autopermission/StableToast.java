package com.honeycomb.colorphone.autopermission;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.honeycomb.colorphone.R;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.BackgroundDrawables;
import com.superapps.util.Dimensions;
import com.superapps.util.HomeKeyWatcher;

/**
 * @author sundxing
 */
public class StableToast {

    private static Toast toast;
    private static Handler sHandler = new Handler();
    private static HomeKeyWatcher homeKeyWatcher = new HomeKeyWatcher(HSApplication.getContext());

    private static long timeMills;
    public static void showHuaweiAccToast() {
        timeMills = System.currentTimeMillis();
        doShowAccToast();
        long duration = HSConfig.optInteger(18, "Application", "AutoPermission", "ToastDurationSeconds")
                * DateUtils.SECOND_IN_MILLIS;
        sHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cancelToastInner();
            }
        }, duration);

        // Watch home key pressed
        homeKeyWatcher.setOnHomePressedListener(new HomeKeyWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                cancelToast();
            }

            @Override
            public void onRecentsPressed() {

            }
        });
        homeKeyWatcher.startWatch();

        // Watch activity life callbacks
        Context context = HSApplication.getContext().getApplicationContext();
        if (context instanceof Application) {
            ((Application) context).registerActivityLifecycleCallbacks(sActivityLifecycleCallbacks);
        }
    }

    public static Application.ActivityLifecycleCallbacks sActivityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (activity.getPackageName().equals(HSApplication.getContext().getPackageName())) {
                cancelToast();
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };

    private static void doShowAccToast() {
        toast = new Toast(HSApplication.getContext().getApplicationContext());
        int layoutId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ?
                R.layout.toast_huawei_acc
                : R.layout.toast_huawei_acc_8;
        final View contentView = LayoutInflater.from(HSApplication.getContext()).inflate(layoutId, null);
        contentView.setAlpha(0.9f);
        contentView.setBackground(BackgroundDrawables.createBackgroundDrawable(Color.parseColor("#000000"),
                Dimensions.pxFromDp(6), false));

        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.setView(contentView);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
        sHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doShowAccToast();
            }
        }, 5000);

    }

    private static void cancelToastInner() {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        sHandler.removeCallbacksAndMessages(null);
        Context context = HSApplication.getContext().getApplicationContext();
        if (context instanceof Application) {
            ((Application) context).unregisterActivityLifecycleCallbacks(sActivityLifecycleCallbacks);
        }
        homeKeyWatcher.stopWatch();
    }

    public static void cancelToast() {
        long curTimeMills = System.currentTimeMillis();
        long intervalMills = timeMills - curTimeMills;
        long secondsInTen = intervalMills / 10000 + 1;

        Analytics.logEvent("AccessibilityPageDuration", String.valueOf(secondsInTen * 10));
        cancelToastInner();
    }
}
