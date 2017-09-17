package com.honeycomb.colorphone.util;

import android.text.format.DateUtils;

import com.acb.autopilot.AutopilotConfig;
import com.acb.call.CPSettings;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings;
import com.colorphone.lock.lockscreen.locker.LockerSettings;
import com.colorphone.lock.util.PreferenceHelper;

/**
 * Created by sundxing on 17/9/13.
 */

public class ModuleUtils {
    private static final String PREFS_FILE_NAME = "pref_file_colorphone";

    public static boolean isNeedGuideAfterApply() {

        long guideInterval = System.currentTimeMillis() - PreferenceHelper.get(PREFS_FILE_NAME).getLong("apply_guide_time", 0);
        int interval = (int) AutopilotConfig.getDoubleToTestNow("topic-1505294061097", "apply_finish_guide_show_interval", 6);
        if (guideInterval < interval * DateUtils.HOUR_IN_MILLIS) {
            return false;
        }

        int guideCount = PreferenceHelper.get(PREFS_FILE_NAME).getInt("apply_guide_count", 0);
        int max = (int) AutopilotConfig.getDoubleToTestNow("topic-1505294061097", "apply_finish_guide_max_show_time", 1);

        if (guideCount >= max) {
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
