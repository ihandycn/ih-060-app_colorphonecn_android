package com.honeycomb.colorphone;

import android.app.Activity;

import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.activity.ThemePreviewActivity;

public class ActivitySwitchUtil {
    public static void onActivityChange(Class<? extends Activity> exitActivityClazz, Activity activity) {
        if (exitActivityClazz != null &&
                exitActivityClazz.getName().equals(ThemePreviewActivity.class.getName())
                && activity instanceof ColorPhoneActivity) {
        }

    }

    public static void onActivityExit(Activity activity) {

    }

    public static void onMainViewCreate() {

    }
}
