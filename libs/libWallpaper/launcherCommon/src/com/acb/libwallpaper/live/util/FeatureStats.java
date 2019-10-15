package com.acb.libwallpaper.live.util;

import android.content.SharedPreferences;

import com.acb.libwallpaper.live.model.LauncherFiles;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

public class FeatureStats {

    public static final String TAG = "FeatureStats";
    private static final String START_TIME = "_startTime";
    private static final String FULL_USE_TIME = "_fullUseTime";
    private static final String[] ITEM_KEYS = {
            "Desktop", "FA", "Clean", "Personalization", "Game",
            "UsefulFeature", "V-UsefulFeature", "Locker", "ColorPhone"};

    public static final int DATE_TODAY = 0;
    public static final int DATE_YESTERDAY = 1;

    private static Preferences sPrefs = Preferences.get(LauncherFiles.RECORD_USE_TIME_PREFS);

    public static void recordStartTime(String itemKey) {
        HSLog.i(TAG, itemKey + " switch to foreground");
        sPrefs.putLong(itemKey + START_TIME + getDateKey(DATE_TODAY), System.currentTimeMillis());
    }

    public static void recordLastTime(String itemKey) {
        long lastStartTime = sPrefs.getLong(itemKey + START_TIME + getDateKey(DATE_TODAY), 0);
        long allTime = sPrefs.getLong(itemKey + FULL_USE_TIME + getDateKey(DATE_TODAY), 0);
        if (lastStartTime != 0) {
            long lastTime = System.currentTimeMillis() - lastStartTime;
            allTime += lastTime;
            SharedPreferences.Editor editor = sPrefs.edit();
            editor.putLong(itemKey + FULL_USE_TIME + getDateKey(DATE_TODAY), allTime);
            editor.putLong(itemKey + START_TIME + getDateKey(DATE_TODAY), 0);
            editor.apply();
            HSLog.i(TAG, itemKey + " lastTime--" + lastTime / 1000);
            HSLog.i(TAG, itemKey + " fullUseTime--" + allTime / 1000);
        }
    }

    public static void readyForUpload() {
        for (String itemKey : ITEM_KEYS) {
            recordLastTime(itemKey);
        }
    }

    public static long getDesktopUseTime() {
        long desktop = sPrefs.getLong("Desktop" + FULL_USE_TIME + getDateKey(DATE_YESTERDAY), 0);
        long vFA = sPrefs.getLong("FA" + FULL_USE_TIME + getDateKey(DATE_YESTERDAY), 0);
        long vUsefulFeature = sPrefs.getLong("V-UsefulFeature" + FULL_USE_TIME + getDateKey(DATE_YESTERDAY), 0);

        desktop = desktop - vFA - vUsefulFeature;
        return desktop;
    }

    public static long getUsefulFeatureUseTime() {
        long usefulFeature = sPrefs.getLong("UsefulFeature" + FULL_USE_TIME + getDateKey(DATE_YESTERDAY), 0);
        long vUsefulFeature = sPrefs.getLong("V-UsefulFeature" + FULL_USE_TIME + getDateKey(DATE_YESTERDAY), 0);

        usefulFeature += vUsefulFeature;
        return usefulFeature;
    }

    public static long getOtherUseTime(String itemKey) {
        return sPrefs.getLong(itemKey + FULL_USE_TIME + getDateKey(DATE_YESTERDAY), 0);
    }

    public static String getFormattedTime(IntegerBuckets buckets, long rawTime) {
        float minute = rawTime / 60000f;
        return buckets.getBucket(Math.round(minute));
    }

    public static String getDateKey(int date) {
        long startOfDate = DateUtils.getStartTimeStampOfDaysAgo(date) / 1000;
        return String.valueOf(startOfDate);
    }

    public static void removeYesterdayTime() {
        SharedPreferences.Editor editor = sPrefs.edit();
        for (String itemKey : ITEM_KEYS) {
            editor.remove(itemKey + FULL_USE_TIME + getDateKey(DATE_YESTERDAY));
            editor.remove(itemKey + START_TIME + getDateKey(DATE_YESTERDAY));
        }
        editor.apply();
    }
}
