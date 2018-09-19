package com.honeycomb.colorphone.dialer;

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
        AutopilotEvent.logTopicEvent("topic-1536215679114-660", "set_default_guide_show");
        LauncherAnalytics.logEvent(upperFirstCh("ColorPhone_" + "set_default_guide_show"));
    }

    public static void guideConfirmed() {
        AutopilotEvent.logTopicEvent("topic-1536215679114-660", "set_default_guide_set_clicked");
        LauncherAnalytics.logEvent(upperFirstCh("ColorPhone_" + "set_default_guide_set_clicked"));
    }

    public static void successSetAsDefault() {
        AutopilotEvent.logTopicEvent("topic-1536215679114-660", "set_default_success");
        LauncherAnalytics.logEvent(upperFirstCh("ColorPhone_" + "set_default_success"));
    }

    public static void dialerShow() {
        AutopilotEvent.logTopicEvent("topic-1536215679114-660", "dailer_page_show");
        LauncherAnalytics.logEvent(upperFirstCh("dialer_page_show"), "Type",
                Commons.isKeyguardLocked(HSApplication.getContext(), false) ?
                        "Withlock" : "Withoutlock");
    }

    private static String upperFirstCh(String event) {
        StringBuilder sb = new StringBuilder();
        char aheadCh = 0;
        char spitCh = '_';
        for (int i = 0; i < event.length(); i++) {
            if (aheadCh == 0 || aheadCh == spitCh) {
                sb.append(Character.toUpperCase(event.charAt(i)));
            } else {
                sb.append(event.charAt(i));
            }
            aheadCh = event.charAt(i);
        }
        return sb.toString();
    }

}
