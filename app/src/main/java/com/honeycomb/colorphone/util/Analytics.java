package com.honeycomb.colorphone.util;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by songliu on 30/06/2017.
 */

public class Analytics {

    public static int FLAG_LOG_FLURRY = 0x1;
    public static int FLAG_LOG_UMENG = 0x4;

    public static void logEvent(String eventID) {
        logEvent(eventID, FLAG_LOG_FLURRY | FLAG_LOG_UMENG, (Map) (new HashMap()));
    }

    public static void logEvent(String eventID, int flag) {
        logEvent(eventID, flag, (Map) (new HashMap()));
    }

    public static void logEvent(String eventID, String... vars) {
        logEvent(eventID, FLAG_LOG_FLURRY | FLAG_LOG_UMENG, vars);
    }

    public static void logEvent(String eventID, boolean onlyUMENG, String... vars) {
        int flag = FLAG_LOG_UMENG;
        if (!onlyUMENG) {
            flag |= FLAG_LOG_FLURRY;
        }
        logEvent(eventID, flag, vars);
    }

    public static void logEvent(String eventID, int flag, String... vars) {
        HashMap item = new HashMap();
        if (null != vars) {
            int length = vars.length;
            if (length % 2 != 0) {
                --length;
            }

            String key = null;
            String value = null;
            int i = 0;

            while (i < length) {
                key = vars[i++];
                value = vars[i++];
                item.put(key, value);
            }
        }

        logEvent(eventID, flag, (Map) item);
    }

    public static void logEvent(final String eventID, final Map<String, String> eventValue) {
        logEvent(eventID, FLAG_LOG_FLURRY, eventValue);
    }

    private static void logEvent(final String eventID, int flag, final Map<String, String> eventValue) {
        if (UMConfigure.getInitStatus()) {
            HSLog.d("FlurryWithAnswers", eventID);
            if ((flag & FLAG_LOG_FLURRY) == FLAG_LOG_FLURRY) {
                com.ihs.app.analytics.HSAnalytics.logEvent(eventID, eventValue);
            }
            if ((flag & FLAG_LOG_UMENG) == FLAG_LOG_UMENG) {
                if (eventValue.size() == 0) {
                    MobclickAgent.onEvent(HSApplication.getContext(), eventID);
                } else {
                    MobclickAgent.onEvent(HSApplication.getContext(), eventID, eventValue);
                }
            }
        } else {
            HSLog.i("FlurryWithAnswers", "not init umeng event: " + eventID);
        }
    }

    public static String upperFirstCh(String event) {
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

    public static void logAdViewEvent(String placementName, boolean success) {
        logEvent("AcbAdNative_Viewed_In_App", new String[]{placementName, String.valueOf(success)});
    }
}
