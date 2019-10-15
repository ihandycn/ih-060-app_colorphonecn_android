package com.acb.libwallpaper.live;

import java.util.HashMap;
import java.util.Map;

/**
 * For v1.4.5 (78) release: to cut down number of events reported to Flurry, we log most events only to Fabric.
 * Give {@code true} for {@code alsoLogToFlurry} to log an event also to Flurry.
 */
public class LauncherAnalytics {

    private static final String TAG = LauncherAnalytics.class.getSimpleName();

    public static void logEvent(String eventID) {
        logEvent(eventID, false);
    }

    public static void logEvent(String eventID, boolean alsoLogToFlurry) {
        logEvent(eventID, alsoLogToFlurry, new HashMap<>());
    }

    public static void logEvent(String eventID, String... vars) {
    }

    public static void logEvent(String eventID, boolean alsoLogToFlurry, String... vars) {
    }

    public static void logEvent(final String eventID, final Map<String, String> eventValues) {
    }

    public static void logEvent(final String eventID, boolean alsoLogToFlurry, final Map<String, String> eventValues) {

    }

    private static void onLogEvent(String eventID, boolean alsoLogToFlurry, Map<String, String> eventValues) {

    }

    private static String getEventInfoDescription(String eventID, boolean alsoLogToFlurry, Map<String, String> eventValues) {
        String scope = (alsoLogToFlurry ? "F" : " ") + "|A";
        StringBuilder values = new StringBuilder();
        for (Map.Entry<String, String> valueEntry : eventValues.entrySet()) {
            values.append(valueEntry).append(", ");
        }
        if (values.length() > 0) {
            values = new StringBuilder(": " + values.substring(0, values.length() - 2)); // At ": " at front and remove ", " in the end
        }
        return "(" + scope + ") " + eventID + values;
    }
}
