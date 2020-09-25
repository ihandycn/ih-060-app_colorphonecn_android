package com.honeycomb.colorphone.wechatincall;

import com.ihs.commons.utils.HSLog;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class WeChatInCallAutopilot {

    private static final String TAG = "WeChatInCallAutopilot";
    private static final String TOPIC_ID = "topic-7o3oh4k7t";

    public static boolean isEnable(){
        boolean result = AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "switch", false);
        HSLog.d(TAG, "switch = " + result);
        return result;
    }

    public static boolean isSetDefault(){
        boolean result = AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "defaultswitch", false);
        HSLog.d(TAG, "defaultswitch = " + result);
        return result;
    }
    public static boolean isHasButton(){
        boolean result = AutopilotConfig.getBooleanToTestNow(TOPIC_ID, "wechatbuttonenable", false);
        HSLog.d(TAG, "wechatbuttonenable = " + result);
        return result;
    }

    public static void logEvent(String eventName){
        AutopilotEvent.logTopicEvent(TOPIC_ID,eventName);
    }

    public static void logCollectionNews(){
        AutopilotEvent.logAppEvent("cpm_collection_news");
    }
    public static void logCollectionLumen(){
        AutopilotEvent.logAppEvent("cpm_collection_lumen");
    }
    public static void logCollectionTexture(){
        AutopilotEvent.logAppEvent("cpm_collection_texture");
    }
    public static void logCollectionOther(){
        AutopilotEvent.logAppEvent("cpm_collection_other");
    }
    public static void logMainViewOpened(){
        AutopilotEvent.logAppEvent("colorphone_mainview_opened");
    }

}
