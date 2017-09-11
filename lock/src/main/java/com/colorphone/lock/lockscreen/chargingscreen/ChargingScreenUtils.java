package com.colorphone.lock.lockscreen.chargingscreen;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.TelephonyManager;

import com.colorphone.lock.lockscreen.FloatWindowController;
import com.colorphone.lock.lockscreen.locker.LockerActivity;
import com.colorphone.lock.util.PreferenceHelper;
import com.ihs.app.analytics.HSAnalytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.charging.HSChargingManager;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;

import static com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings.LOCKER_PREFS;

public class ChargingScreenUtils {

    private static final long MIN_INTERVAL_VALID_CLICK = 500;
    private static final boolean MODE_ACTIVITY = true;

    private static volatile long lastClickTime;
    /**
     * 原生5.0.x系统上，系统锁屏和同时设置了FLAG_FULLSCREEN、FLAG_SHOW_WHEN_LOCKED的Activity闪烁冲突,
     * 初步判断是5.0.x上WMS在处理Window优先级上存在bug。为避免冲突，取消冲突Activity的FLAG_FULLSCREEN。
     * <p>
     * 该方法判断是否为原生5.0.x系统
     *
     * @return is native 5.0.x
     */
    public static boolean isNativeLollipop() {
        return Build.VERSION_CODES.LOLLIPOP == Build.VERSION.SDK_INT
                && ("Google".equals(Build.BRAND) || "google".equals(Build.BRAND));
    }

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

        Bundle bundle = new Bundle();
        bundle.putBoolean(ChargingScreen.EXTRA_BOOLEAN_IS_CHARGING, HSChargingManager.getInstance().isCharging());
        bundle.putInt(ChargingScreen.EXTRA_INT_BATTERY_LEVEL_PERCENT,
                HSChargingManager.getInstance().getBatteryRemainingPercent());
        bundle.putBoolean(ChargingScreen.EXTRA_BOOLEAN_IS_CHARGING_FULL,
                HSChargingManager.getInstance().getChargingState() == HSChargingManager.HSChargingState.STATE_CHARGING_FULL);
        bundle.putInt(ChargingScreen.EXTRA_INT_CHARGING_LEFT_MINUTES,
                HSChargingManager.getInstance().getChargingLeftMinutes());
        bundle.putBoolean(ChargingScreen.EXTRA_BOOLEAN_IS_CHARGING_STATE_CHANGED, chargingStateChanged);

        if (MODE_ACTIVITY) {
            Intent intent = new Intent(HSApplication.getContext(), ChargingScreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.putExtras(bundle);
            HSGlobalNotificationCenter.sendNotification(LockerActivity.EVENT_FINISH_SELF);
            HSApplication.getContext().startActivity(intent);
        } else {
            FloatWindowController.getInstance().showChargingScreen(bundle);
        }
    }

    public static void startLockerActivity() {
        if (isCalling()) {
            return;
        }
        if (MODE_ACTIVITY) {
            Intent intent = new Intent(HSApplication.getContext(), LockerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            HSApplication.getContext().startActivity(intent);
        } else {
            FloatWindowController.getInstance().showLockScreen();
        }
    }

    private static boolean isCalling() {
        TelephonyManager telephonyMgr = (TelephonyManager) HSApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyMgr.getCallState() != TelephonyManager.CALL_STATE_IDLE;
    }
}