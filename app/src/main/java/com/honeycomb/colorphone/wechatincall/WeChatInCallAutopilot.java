package com.honeycomb.colorphone.wechatincall;

import com.ihs.commons.utils.HSLog;

import net.appcloudbox.autopilot.AutopilotConfig;

public class WeChatInCallAutopilot {

    private static final String TAG = "WeChatInCallAutopilot";
    private static final String TOPIC_ID = "WeChat Show Test";

    public static boolean isEnable(){
        boolean result = AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "switch", false);
        HSLog.d(TAG, "switch = " + result);
        return result;
    }

    public static boolean isSetDefault(){
        boolean result = AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "defaultswitch", false);
        HSLog.d(TAG, "switch = " + result);
        return result;
    }
    public static boolean isHasButton(){
        boolean result = AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "wechatbuttonenable", false);
        HSLog.d(TAG, "switch = " + result);
        return result;
    }

}
