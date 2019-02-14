package com.honeycomb.colorphone.util;

import android.text.format.DateUtils;

import com.colorphone.lock.util.ConfigUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;

import java.util.List;

public class CallFinishUtils {

    private static final String TOPIC_ID = "topic-1536215679114-660";

    public static boolean isCallFinishFullScreenAdEnabled() {
        boolean configEnable = HSConfig.optBoolean(false, "Application", "CallFinishWire", "Enable")
                || enabledThisVersion();
        boolean limitUser = userLimitByInstallTimes();

        HSLog.d("CallFinish", "config enable : " + configEnable );
        return configEnable && !limitUser;
    }

    private static boolean userLimitByInstallTimes() {
        int disableDays = HSConfig.optInteger(-1, "Application", "CallFinishWire", "DisableAfterInstallDays");
        if (disableDays > 0) {
            return System.currentTimeMillis() - ConfigUtils.getAppFirstInstallTime() > disableDays * DateUtils.DAY_IN_MILLIS;
        }
        return false;
    }

    private static boolean enabledThisVersion() {
        List<Integer> enableList = (List<Integer>) HSConfig.getList("Application", "CallFinishWire", "EnableVersionList");
        int versionCode = HSApplication.getFirstLaunchInfo().appVersionCode;
        if (enableList != null && enableList.contains(versionCode)) {
            return true;
        }
        return false;
    }

//    public static void logCallFinishWiredShow() {
//        AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_call_finished_wire_show");
//    }
//
//    public static void logCallFinish() {
//        AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_call_finished");
//    }
//
//    public static void logCallFinishCallAssistantShow() {
//        AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_call_finished_call_assistant_show");
//    }
//
//    public static void logCallFinishWiredShouldShow() {
//        AutopilotEvent.logTopicEvent(TOPIC_ID, "colorphone_call_finished_wire_should_show");
//    }
}
