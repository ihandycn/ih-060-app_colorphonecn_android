package com.colorphone.lock.lockscreen.chargingscreen;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.TelephonyManager;

import com.colorphone.lock.lockscreen.FloatWindowController;
import com.colorphone.lock.lockscreen.LockScreensLifeCycleRegistry;
import com.colorphone.lock.util.PreferenceHelper;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.charging.HSChargingManager;

import static com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings.LOCKER_PREFS;

public class ChargingScreenUtils {

    private static final long MIN_INTERVAL_VALID_CLICK = 500;

    private static volatile long lastClickTime;

    public static boolean shouldGuideShow() {
        if (ChargingScreenSettings.isChargingScreenEverEnabled()) {
            return false;
        }

        boolean chargingCountMatched = false;
        switch (ChargingScreenSettings.getChargingScreenGuideShowCount()) {
            case 0:
                if (ChargingScreenSettings.getChargingCount() >= 1) {
                    chargingCountMatched = true;
                    HSAnalytics.logEvent("Alert_ChargingScreen_Shown", "type", "1");
                }
                break;
            case 1:
                if (ChargingScreenSettings.getChargingCount() - PreferenceHelper.get(LOCKER_PREFS).getInt(
                        ChargingScreenSettings.PREF_KEY_CHARGING_SCREEN_GUIDE_LAST_SHOW_TIME, 0) >= 5) {
                    chargingCountMatched = true;
                    HSAnalytics.logEvent("Alert_ChargingScreen_Shown", "type", "2");
                }
                break;
            case 2:
                if (ChargingScreenSettings.getChargingCount() - PreferenceHelper.get(LOCKER_PREFS).getInt(
                        ChargingScreenSettings.PREF_KEY_CHARGING_SCREEN_GUIDE_LAST_SHOW_TIME, 0) >= 9) {
                    chargingCountMatched = true;
                    HSAnalytics.logEvent("Alert_ChargingScreen_Shown", "type", "3");
                }
                break;
            default:
                break;
        }

        return chargingCountMatched;
    }

    public static boolean shouldDialogGuideShow() {
        return !ChargingScreenSettings.isChargingScreenEverEnabled() && !ChargingScreenSettings.isChargingScreenDialogGuideShown();
    }

    public static boolean isFastDoubleClick() {

        long currentClickTime = SystemClock.elapsedRealtime();
        long intervalClick = currentClickTime - lastClickTime;

        if (0 < intervalClick && intervalClick < MIN_INTERVAL_VALID_CLICK) {
            return true;
        }

        lastClickTime = currentClickTime;
        return false;
    }

    public static void startChargingScreenActivity(boolean chargingStateChanged) {
        if (isCalling()) {
            return;
        }

        // If charging screen activity already exists, do nothing.
        if (LockScreensLifeCycleRegistry.isChargingScreenActive()) {
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean(ChargingScreen.EXTRA_BOOLEAN_IS_CHARGING, HSChargingManager.getInstance().isCharging());
        bundle.putInt(ChargingScreen.EXTRA_INT_BATTERY_LEVEL_PERCENT,
                HSChargingManager.getInstance().getBatteryRemainingPercent());
        bundle.putBoolean(ChargingScreen.EXTRA_BOOLEAN_IS_CHARGING_FULL,
                HSChargingManager.getInstance().getChargingState() == HSChargingManager.HSChargingState.STATE_CHARGING_FULL);
        bundle.putInt(ChargingScreen.EXTRA_INT_CHARGING_LEFT_MINUTES,
                HSChargingManager.getInstance().getChargingLeftMinutes());
        bundle.putBoolean(ChargingScreen.EXTRA_BOOLEAN_IS_CHARGING_STATE_CHANGED, chargingStateChanged);

        FloatWindowController.getInstance().showChargingScreen(bundle);
    }

    public static void startLockerActivity() {
        if (isCalling()) {
            return;
        }

        if (LockScreensLifeCycleRegistry.isChargingScreenActive() || LockScreensLifeCycleRegistry.isLockerActive()) {
            return;
        }

        FloatWindowController.getInstance().showLockScreen();
    }

    private static boolean isCalling() {
        TelephonyManager telephonyMgr = (TelephonyManager) HSApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyMgr.getCallState() != TelephonyManager.CALL_STATE_IDLE;
    }
}