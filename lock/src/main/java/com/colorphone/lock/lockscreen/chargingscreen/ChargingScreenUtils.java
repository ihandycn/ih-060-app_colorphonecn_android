package com.colorphone.lock.lockscreen.chargingscreen;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;

import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.ScreenStatusReceiver;
import com.colorphone.lock.lockscreen.FloatWindowController;
import com.colorphone.lock.lockscreen.locker.Locker;
import com.colorphone.smartlocker.SmartLockerManager;
import com.colorphone.smartlocker.utils.AutoPilotUtils;
import com.colorphone.smartlocker.utils.NetworkStatusUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.libcharging.HSChargingManager;
import com.superapps.util.Preferences;
import com.superapps.util.rom.RomUtils;

import colorphone.acb.com.libweather.WeatherClockManager;

import static com.colorphone.smartlocker.SmartLockerManager.EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF;
import static com.colorphone.smartlocker.SmartLockerManager.EXTRA_VALUE_START_BY_LOCKER;

public class ChargingScreenUtils {

    private static final long MIN_INTERVAL_VALID_CLICK = 500;
    private static final boolean MODE_ACTIVITY = !RomUtils.checkIsOppoRom();

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

        if (ScreenStatusReceiver.isScreenOn()) {
            int intervalMins = HSConfig.optInteger(3,
                    "Application", "Charging", "ChargingLockScreen", "MinDisplayIntervalMinutes");
            long lastTime = Preferences.get(ChargingScreenSettings.LOCKER_PREFS).getLong("charging_display_datetime", 0);
            if (System.currentTimeMillis() - lastTime < intervalMins * DateUtils.MINUTE_IN_MILLIS) {
                LockerCustomConfig.getLogger().logEvent("ColorPhone_LockScreen_Forbidden");
                return;
            }
        }
        Preferences.get(ChargingScreenSettings.LOCKER_PREFS).putLong("charging_display_datetime", System.currentTimeMillis());

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
            SmartLockerManager.getInstance().tryToStartChargingScreenOrLockerActivity(EXTRA_VALUE_START_BY_CHARGING_SCREEN_OFF);
        } else {
            if (AutoPilotUtils.isH5LockerMode()) {
                LockerCustomConfig.getLogger().logEvent("ChargingScreen_News_Should_Show", "reason", "Network");
                if (NetworkStatusUtils.isNetworkConnected(HSApplication.getContext())) {
                    FloatWindowController.getInstance().showChargingScreen(bundle);
                }
            } else {
                LockerCustomConfig.getLogger().logEvent("ChargingScreen_Should_Show");
                FloatWindowController.getInstance().showChargingScreen(bundle);
            }
        }

        AutoPilotUtils.logNewsChance();
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
        WeatherClockManager.getInstance().updateWeatherIfNeeded();
        isFromPush = fromPush;

        if (MODE_ACTIVITY) {
            SmartLockerManager.getInstance().tryToStartChargingScreenOrLockerActivity(EXTRA_VALUE_START_BY_LOCKER);
        } else {

            if (AutoPilotUtils.isH5LockerMode()) {
                LockerCustomConfig.getLogger().logEvent("LockScreen_News_Should_Show", "reason", "Network");
                if (NetworkStatusUtils.isNetworkConnected(HSApplication.getContext())) {
                    FloatWindowController.getInstance().showLockScreen();
                }
            } else {
                String suffix = ChargingScreenUtils.isFromPush ? "_Push" : "";
                LockerCustomConfig.getLogger().logEvent("ColorPhone_LockScreen_Should_Show" + suffix,
                        "Brand", Build.BRAND.toLowerCase(),
                        "DeviceVersion", Locker.getDeviceInfo());
                FloatWindowController.getInstance().showLockScreen();
            }
        }

        AutoPilotUtils.logNewsChance();
    }

    private static boolean isCalling() {
        TelephonyManager telephonyMgr = (TelephonyManager) HSApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyMgr.getCallState() != TelephonyManager.CALL_STATE_IDLE;
    }
}