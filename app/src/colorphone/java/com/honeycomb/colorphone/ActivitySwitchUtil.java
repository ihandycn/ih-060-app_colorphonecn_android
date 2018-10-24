package com.honeycomb.colorphone;

import android.app.Activity;

import com.call.assistant.ui.CallIdleAlertActivity;
import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.activity.ThemePreviewActivity;
import com.honeycomb.colorphone.cashcenter.CashUtils;

class ActivitySwitchUtil {
    public static void onActivityChange(Class<? extends Activity> exitActivityClazz, Activity activity) {
        if (exitActivityClazz != null &&
                exitActivityClazz.getName().equals(ThemePreviewActivity.class.getName())
                && activity instanceof ColorPhoneActivity) {
            // Exit ThemePreviewActivity, enter ColorPhoneActivity
           CashUtils.showGuideIfNeeded(activity, CashUtils.Source.Inner);
        }
    }

    public static void onActivityExit(Activity activity) {
        if (activity instanceof CallIdleAlertActivity) {
            CashUtils.showGuideIfNeeded(activity, CashUtils.Source.CallAlertClose);
        }
    }
}
