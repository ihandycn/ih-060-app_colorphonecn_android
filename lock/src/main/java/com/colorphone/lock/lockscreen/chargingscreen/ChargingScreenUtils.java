package com.colorphone.lock.lockscreen.chargingscreen;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.TelephonyManager;

import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.lockscreen.FloatWindowController;
import com.colorphone.lock.lockscreen.locker.LockerActivity;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.libcharging.HSChargingManager;
import com.superapps.util.Navigations;
import com.superapps.util.Preferences;

import static com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings.LOCKER_PREFS;

public class ChargingScreenUtils {

    private static final long MIN_INTERVAL_VALID_CLICK = 500;
    private static final boolean MODE_ACTIVITY = true;

    private static volatile long lastClickTime;
    public static boolean isFromPush;

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
                    LockerCustomConfig.getLogger().logEvent("Alert_ChargingScreen_Shown", "type", "1");
                }
                break;
            case 1:
                if (ChargingScreenSettings.getChargingCount() - Preferences.get(LOCKER_PREFS).getInt(
                        ChargingScreenSettings.PREF_KEY_CHARGING_SCREEN_GUIDE_LAST_SHOW_TIME, 0) >= 5) {
                    chargingCountMatched = true;
                    LockerCustomConfig.getLogger().logEvent("Alert_ChargingScreen_Shown", "type", "2");
                }
                break;
            case 2:
                if (ChargingScreenSettings.getChargingCount() - Preferences.get(LOCKER_PREFS).getInt(
                        ChargingScreenSettings.PREF_KEY_CHARGING_SCREEN_GUIDE_LAST_SHOW_TIME, 0) >= 9) {
                    chargingCountMatched = true;
                    LockerCustomConfig.getLogger().logEvent("Alert_ChargingScreen_Shown", "type", "3");
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

    public static void startChargingScreenActivity(boolean chargingStateChanged, boolean fromPush) {
        if (isCalling()) {
            return;
        }
        isFromPush = fromPush;
        Bundle bundle = new Bundle();
        bundle.putBoolean(ChargingScreen.EXTRA_BOOLEAN_IS_CHARGING, HSChargingManager.getInstance().isCharging());
        bundle.putInt(ChargingScreen.EXTRA_INT_BATTERY_LEVEL_PERCENT,
                getBatteryPercentage(HSApplication.getContext()));
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
            Navigations.startActivitySafely(HSApplication.getContext(),intent);
        } else {
            FloatWindowController.getInstance().showChargingScreen(bundle);
        }
    }


    public static int getBatteryPercentage(Context context) {
        int batteryPercentage = HSChargingManager.getInstance().getBatteryRemainingPercent();
        if (batteryPercentage <= 0) {
            Intent intent = context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
            if (intent != null) {
                int currentBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                batteryPercentage = currentBatteryLevel * 100 / batteryScale;
            }
        }
        return batteryPercentage;
    }

    public static void startLockerActivity(boolean fromPush) {
        if (isCalling()) {
            return;
        }
        isFromPush = fromPush;
        if (MODE_ACTIVITY) {
            try {
                Intent intent = new Intent(HSApplication.getContext(), LockerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                Navigations.startActivitySafely(HSApplication.getContext(), intent);
            } catch (ActivityNotFoundException ignore) {
                // crash #749 some device report.
            }
        } else {
            FloatWindowController.getInstance().showLockScreen();
        }
    }

    private static boolean isCalling() {
        TelephonyManager telephonyMgr = (TelephonyManager) HSApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyMgr.getCallState() != TelephonyManager.CALL_STATE_IDLE;
    }
}