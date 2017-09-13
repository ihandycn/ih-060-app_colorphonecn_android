package com.honeycomb.colorphone.util;

import android.text.format.DateUtils;

import com.acb.call.CPSettings;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings;
import com.colorphone.lock.lockscreen.locker.LockerSettings;
import com.colorphone.lock.util.PreferenceHelper;

/**
 * Created by sundxing on 17/9/13.
 */

public class ModuleUtils {
    private static final String PREFS_FILE_NAME = "pref_file_colorphone";
    private static final int MAX_COUNT_APPLY_GUIDE = 3;

    public static boolean isNeedGuideAfterApply() {

        long guideInterval = System.currentTimeMillis() - PreferenceHelper.get(PREFS_FILE_NAME).getLong("apply_guide_time", 0);
        if (guideInterval < DateUtils.DAY_IN_MILLIS) {
            return false;
        }
        int guideCount = PreferenceHelper.get(PREFS_FILE_NAME).getInt("apply_guide_count", 0);
        if (guideCount > MAX_COUNT_APPLY_GUIDE) {
            return false;
        }

        //TODO sms
        if (CPSettings.isCallAssistantModuleEnabled()
                && ChargingScreenSettings.isChargingScreenEverEnabled()
                && LockerSettings.isLockerEnabled()) {
            return false;
        }
        PreferenceHelper.get(PREFS_FILE_NAME).putLong("apply_guide_time", System.currentTimeMillis());
        PreferenceHelper.get(PREFS_FILE_NAME).putInt("apply_guide_count", ++guideCount);
        return true;
    }
}
