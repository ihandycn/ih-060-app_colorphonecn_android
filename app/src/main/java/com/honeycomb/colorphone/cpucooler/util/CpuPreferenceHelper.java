package com.honeycomb.colorphone.cpucooler.util;

import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.cpucooler.CpuCoolDownActivity;
import com.honeycomb.colorphone.resultpage.data.ResultConstants;
import com.ihs.commons.utils.HSLog;
import com.superapps.util.Preferences;

import java.util.Calendar;

public class CpuPreferenceHelper {

    private static final String PREF_KEY_LAST_SCAN_FINISH_TIME = "PREF_KEY_LAST_SCAN_FINISH_TIME";
    private static final String PREF_KEY_LAST_CLEAN_START_TIME = "PREF_KEY_LAST_CLEAN_START_TIME";

    private static final String PREF_SCAN_CANCELED = "PREF_SCAN_CANCELED";

    private static final String PREF_KEY_CPU_COOLER_ICON_CHANGED_TIME = "cpu_cooler_icon_changed_time";

    private static final String PREF_KEY_RESULT_SHOULD_DISPLAY_TEMPERATURE = "PREF_KEY_RESULT_SHOULD_DISPLAY_TEMPERATURE";

    public static void putBoolean(String key, boolean value) {
        Preferences.get(ResultConstants.CPU_COOLER_PREFS).putBoolean(key, value);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return Preferences.get(ResultConstants.CPU_COOLER_PREFS).getBoolean(key, defaultValue);
    }

    public static void setLastCpuCoolerScanFinishTime() {
        HSLog.d(CpuCoolDownActivity.TAG, "setLastCpuCoolerScanFinishTime ============= **************** lastCpuCoolerScanFinishTime = " + System.currentTimeMillis());
        Preferences.get(ResultConstants.CPU_COOLER_PREFS).putLong(PREF_KEY_LAST_SCAN_FINISH_TIME, System.currentTimeMillis());
    }

    static long getLastCpuCoolerScanFinishTime() {
        return Preferences.get(ResultConstants.CPU_COOLER_PREFS).getLong(PREF_KEY_LAST_SCAN_FINISH_TIME, 0);
    }

    public static void setLastCpuCoolerCleanStartTime() {
        HSLog.d(CpuCoolDownActivity.TAG, "setLastCpuCoolerCleanStartTime ============= **************** lastCpuCoolerCleanStartTime = " + System.currentTimeMillis());
        Preferences.get(ResultConstants.CPU_COOLER_PREFS).putLong(PREF_KEY_LAST_CLEAN_START_TIME, System.currentTimeMillis());
    }

    static long getLastCpuCoolerCleanStartTime() {
        return Preferences.get(ResultConstants.CPU_COOLER_PREFS).getLong(PREF_KEY_LAST_CLEAN_START_TIME, 0);
    }

    static boolean isScanCanceled() {
        return Preferences.get(ResultConstants.CPU_COOLER_PREFS).getBoolean(PREF_SCAN_CANCELED, false);
    }

    public static void setIsScanCanceled(boolean isScanCanceled) {
        Preferences.get(ResultConstants.CPU_COOLER_PREFS).putBoolean(PREF_SCAN_CANCELED, isScanCanceled);
    }

    public static boolean hasUserUsedCpuCoolerRecently(long timeInMs) {
        long lastTime = Preferences.get(ResultConstants.CPU_COOLER_PREFS)
                .getLong(ResultConstants.PREF_KEY_LAST_CPU_COOLER_USED_TIME, -1);
        return (System.currentTimeMillis() - lastTime) < timeInMs;
    }

    public static boolean canChangeToBoostCpuIcon() {
        // use {date * 100 + count (2017050101)} record date and count
        int data = Preferences.get(ResultConstants.CPU_COOLER_PREFS).getInt(PREF_KEY_CPU_COOLER_ICON_CHANGED_TIME, 0);
        int date = data / 100;
        int count = data % 100;
        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.YEAR) * 10000 + cal.get(Calendar.MONTH) * 100 + cal.get(Calendar.DAY_OF_MONTH);
        if (BuildConfig.DEBUG) {
            HSLog.i("BoostNotification", "get CpuCooler 日期：" + date + "  today：" + today + "  变换次数：" + count);
        }
        if (today == date) {
            return count < CpuCoolerConstant.CPU_ICON_CHANGED_LIMIT;
        }
        return true;
    }

    public static void setChangeToBoostCpuIcon() {
        // use {date * 100 + count (2017050101)} record date and count
        int data = Preferences.get(ResultConstants.CPU_COOLER_PREFS).getInt(PREF_KEY_CPU_COOLER_ICON_CHANGED_TIME, 0);
        int date = data / 100;
        int count = data % 100;
        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.YEAR) * 10000 + cal.get(Calendar.MONTH) * 100 + cal.get(Calendar.DAY_OF_MONTH);
        if (today == date) {
            count++;
        } else {
            count = 1;
        }
        data = today * 100 + count;
        if (BuildConfig.DEBUG) {
            HSLog.i("BoostNotification", "set CpuCooler 日期：" + date + "  today：" + today + "  变换次数：" + count);
        }
        Preferences.get(ResultConstants.CPU_COOLER_PREFS).putInt(PREF_KEY_CPU_COOLER_ICON_CHANGED_TIME, data);
    }

    public static void setShouldResultDisplayTemperature(boolean newState) {
        Preferences.get(ResultConstants.CPU_COOLER_PREFS).putBoolean(PREF_KEY_RESULT_SHOULD_DISPLAY_TEMPERATURE, newState);
    }

    public static boolean getShouldResultDisplayTemperature() {
        return Preferences.get(ResultConstants.CPU_COOLER_PREFS).getBoolean(PREF_KEY_RESULT_SHOULD_DISPLAY_TEMPERATURE, false);
    }
}
