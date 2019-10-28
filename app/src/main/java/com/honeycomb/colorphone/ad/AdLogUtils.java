package com.honeycomb.colorphone.ad;

import com.ihs.commons.utils.HSLog;

public class AdLogUtils {
    private static final String TAG = "AdLogUtils";

    public static void log(String adPlacements, int count) {
        HSLog.d(TAG, "preload ad: " + adPlacements + ", count = " + count);
    }
}
