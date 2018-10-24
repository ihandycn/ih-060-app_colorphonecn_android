package com.honeycomb.colorphone;

import android.app.Activity;

import com.honeycomb.colorphone.activity.ColorPhoneActivity;
import com.honeycomb.colorphone.activity.ThemePreviewActivity;
import com.honeycomb.colorphone.cashcenter.CashUtils;
import com.honeycomb.colorphone.cashcenter.InnerCashGuideActivity;
import com.honeycomb.colorphone.trigger.CashCenterTriggerList;

class ActivitySwitchUtil {
    public static void onActivityChange(Class<? extends Activity> exitActivityClazz, Activity activity) {
        if (exitActivityClazz != null &&
                exitActivityClazz.getName().equals(ThemePreviewActivity.class.getName())
                && activity instanceof ColorPhoneActivity) {
            // Exit ThemePreviewActivity, enter ColorPhoneActivity

            boolean active = CashCenterTriggerList.getInstance().checkAt(CashUtils.Source.Inner, true);
            if (active) {
                InnerCashGuideActivity.start(activity);
            }
        }
    }
}
