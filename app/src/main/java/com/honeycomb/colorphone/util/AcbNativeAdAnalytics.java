package com.honeycomb.colorphone.util;

import net.appcloudbox.ads.common.analytics.AcbAnalytics;

public class AcbNativeAdAnalytics {
    public AcbNativeAdAnalytics() {
    }

    public static void logAppViewEvent(String placementName, boolean success) {
        Analytics.logEvent("AcbAdNative_Viewed_In_App", new String[]{placementName, String.valueOf(success)});
    }
}