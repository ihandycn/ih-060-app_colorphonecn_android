package com.honeycomb.colorphone.util;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.honeycomb.colorphone.ColorPhoneApplication;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.utils.HSLog;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by songliu on 30/06/2017.
 */

public class LauncherAnalytics {

    public static void logEvent(String eventID) {
        logEvent(eventID, (Map) (new HashMap()));
    }

    public static void logEvent(String eventID, String... vars) {
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

        logEvent(eventID, (Map) item);
    }

    public static void logEvent(final String eventID, final Map<String, String> eventValue) {
        if (ColorPhoneApplication.isFabricInitted()) {
            CustomEvent event = new CustomEvent(eventID);
            for (String key : eventValue.keySet()) {
                event.putCustomAttribute(key, eventValue.get(key));
            }
            HSLog.d("FlurryWithAnswers", eventID);
            Answers.getInstance().logCustom(event);
            HSAnalytics.logEvent(eventID, eventValue);
        }
    }

}
