package com.honeycomb.colorphone.battery;

import android.text.format.DateUtils;
import android.view.View;

import com.honeycomb.colorphone.resultpage.data.ResultConstants;
import com.superapps.util.Preferences;

public class BatteryIconAnimations {

    private static final boolean ICON_SHAKE_ANIM_DISABLED = true;
    private static final float BATTERY_ICON_ANIM_TIMES_LIMIT = 3;

    private static final String PREF_KEY_BATTERY_ICON_ANIM_COUNT = "PREF_KEY_BATTERY_ICON_ANIM_COUNT";

    private static int sAnimPlayCount = 0;

    public static void startIfNeeded(View batteryIcon) {
        if (!needShowIconAnim()) {
            return;
        }
        IconAnimations.startHintAnimation(batteryIcon);
    }

    // Battery Icon Anim
    private static boolean needShowIconAnim() {
        if (ICON_SHAKE_ANIM_DISABLED) {
            return false;
        }
        if (sAnimPlayCount == 0) {
            sAnimPlayCount = Preferences.get(ResultConstants.BATTERY_PREFS)
                    .getInt(PREF_KEY_BATTERY_ICON_ANIM_COUNT, 0);
        }
        // 1 User enter battery manager out one day
        if (sAnimPlayCount >= BATTERY_ICON_ANIM_TIMES_LIMIT) {
            return false;
        }
        // 2 Anim has reach limit
        if (isUserEnterBatteryManagerRecent(DateUtils.DAY_IN_MILLIS)) {
            return false;
        }

        sAnimPlayCount++;
        Preferences.get(ResultConstants.BATTERY_PREFS)
                .putInt(PREF_KEY_BATTERY_ICON_ANIM_COUNT, sAnimPlayCount);
        return true;
    }

    private static boolean isUserEnterBatteryManagerRecent(long interval) {
        long lastTime = Preferences.get(ResultConstants.BATTERY_PREFS)
                .getLong(BatteryUtils.PREF_KEY_BATTERY_USER_VISIT_TIME, 0);
        return (System.currentTimeMillis() - lastTime) < interval;
    }
}
