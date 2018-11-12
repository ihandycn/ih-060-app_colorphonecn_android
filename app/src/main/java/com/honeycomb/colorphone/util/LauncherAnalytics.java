package com.honeycomb.colorphone.util;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.core.CrashlyticsCore;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.ihs.commons.utils.HSLog;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by songliu on 30/06/2017.
 */

public class LauncherAnalytics {

    public static int FLAG_LOG_FLURRY = 0x1;
    public static int FLAG_LOG_FABRIC = 0x2;

    public static void logEvent(String eventID) {
        logEvent(eventID, FLAG_LOG_FABRIC | FLAG_LOG_FLURRY, (Map) (new HashMap()));
    }

    public static void logEvent(String eventID, int flag) {
        logEvent(eventID, flag, (Map) (new HashMap()));
    }

    public static void logEvent(String eventID, String... vars) {
        logEvent(eventID, FLAG_LOG_FABRIC | FLAG_LOG_FLURRY, vars);
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
        logEvent(eventID, FLAG_LOG_FABRIC | FLAG_LOG_FLURRY, eventValue);
    }

    public static void logEvent(final String eventID, int flag, final Map<String, String> eventValue) {
        if (ColorPhoneApplication.isFabricInitted()) {
            CustomEvent event = new CustomEvent(eventID);
            for (String key : eventValue.keySet()) {
                event.putCustomAttribute(key, eventValue.get(key));
            }
            HSLog.d("FlurryWithAnswers", eventID);
            if ((flag & FLAG_LOG_FABRIC) == FLAG_LOG_FABRIC) {
                Answers.getInstance().logCustom(event);
            }
            if ((flag & FLAG_LOG_FLURRY) == FLAG_LOG_FLURRY) {
                com.ihs.app.analytics.HSAnalytics.logEvent(eventID, eventValue);
            }
        } else {
            HSLog.i("FlurryWithAnswers", "not init fabric event: " + eventID);
        }
    }

    public static void logException(Exception e) {
        if (ColorPhoneApplication.isFabricInitted()) {
            try {
                CrashlyticsCore.getInstance().logException(e);
            } catch (Exception ignore) {
            }
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
}
