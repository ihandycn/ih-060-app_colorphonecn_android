package com.honeycomb.colorphone.dialer;

import android.os.Build;

import com.honeycomb.colorphone.util.LauncherAnalytics;
import com.ihs.app.framework.HSApplication;
import com.superapps.util.Commons;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class AP {

    public static boolean dialerEnable() {
        boolean dialerEnableBoolean = AutopilotConfig.getBooleanToTestNow("topic-1536215679114-660", "dialer enable", false);
        return dialerEnableBoolean;
    }

    public static boolean setDefaultGuideShow() {
        return false;
    }

    public static void guideShow() {
        boolean dialerEnable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && AP.dialerEnable();
        if (!dialerEnable) {
            return;
        }
        AutopilotEvent.logTopicEvent("topic-1536215679114-660", "set_default_guide_show");
        LauncherAnalytics.logEvent(LauncherAnalytics.upperFirstCh("ColorPhone_" + "set_default_guide_show"));
    }

    public static void guideConfirmed() {
        boolean dialerEnable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && AP.dialerEnable();
        if (!dialerEnable) {
            return;
        }
        AutopilotEvent.logTopicEvent("topic-1536215679114-660", "set_default_guide_set_clicked");
        LauncherAnalytics.logEvent(LauncherAnalytics.upperFirstCh("ColorPhone_" + "set_default_guide_set_clicked"));
    }

    public static void successSetAsDefault() {
        AutopilotEvent.logTopicEvent("topic-1536215679114-660", "set_default_success");
        LauncherAnalytics.logEvent(LauncherAnalytics.upperFirstCh("ColorPhone_" + "set_default_success"));
    }

    public static void dialerShow() {
        AutopilotEvent.logTopicEvent("topic-1536215679114-660", "dailer_page_show");
        LauncherAnalytics.logEvent(LauncherAnalytics.upperFirstCh("dialer_page_show"), "Type",
                Commons.isKeyguardLocked(HSApplication.getContext(), false) ?
                        "Withlock" : "Withoutlock");
    }

}
