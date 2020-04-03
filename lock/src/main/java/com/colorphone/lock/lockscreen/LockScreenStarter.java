package com.colorphone.lock.lockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenActivity;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenUtils;
import com.colorphone.lock.lockscreen.chargingscreen.SmartChargingSettings;
import com.colorphone.lock.lockscreen.locker.LockerActivity;
import com.colorphone.lock.lockscreen.locker.LockerSettings;
import com.colorphone.smartlocker.SmartLockerFeedsActivity;
import com.colorphone.smartlocker.SmartLockerManager;
import com.colorphone.smartlocker.utils.AutoPilotUtils;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSLog;
import com.ihs.libcharging.HSChargingManager;
import com.superapps.util.Commons;

/**
 * Receives screen on/off events and start lock screens.
 * <p>
 * Works in ":work" process (with the exception of method {@link #handleStart(Intent)}),
 * which is kept alive.
 */
public class LockScreenStarter {

    private static final String EXTRA_LAUNCHER_ACTIVITY = "launch_activity";
    private static final String EXTRA_VALUE_CHARGING = "charging";
    private static final String EXTRA_VALUE_LOCKER = "locker";
    private static final String TAG = "LockManager";
    private static LockScreenStarter instance;

    private BatteryChangeReceiver mBatteryChangeReceiver = new BatteryChangeReceiver();
    private HSChargingManager.IChargingListener mChargingListener = new HSChargingManager.IChargingListener() {
        @Override
        public void onBatteryLevelChanged(int i, int i1) {
        }

        @Override
        public void onChargingStateChanged(HSChargingManager.HSChargingState preChargingState, HSChargingManager.HSChargingState curChargingState) {

            if (SmartChargingSettings.isChargingScreenEnabled() && HSChargingManager.getInstance().isCharging()
                    && preChargingState == HSChargingManager.HSChargingState.STATE_DISCHARGING) {

                if (AutoPilotUtils.getLockerMode().equals("fuse")) {
                    return;
                }

                if (!ChargingScreenActivity.exist && !SmartLockerFeedsActivity.exist) {

                    ChargingScreenSettings.increaseChargingCount();
                    if (AutoPilotUtils.getLockerMode().equals("cableandfuse") || AutoPilotUtils.getLockerMode().equals("cable")) {
                        SmartLockerManager.getInstance().tryToPreLoadBaiduNews();
                    }
                    ChargingScreenUtils.startChargingScreenActivity(true, false);
                }
            }
        }

        @Override
        public void onChargingRemainingTimeChanged(int i) {

        }

        @Override
        public void onBatteryTemperatureChanged(float v, float v1) {

        }
    };

    private boolean blockWhenHasKeyGuard = true;

    public static void init() {
        if (instance == null) {
            instance = new LockScreenStarter();
            instance.registerScreenOnOff();
            instance.registerChargingListener();
        }
    }

    public static LockScreenStarter getInstance() {
        init();
        return instance;
    }

    private void registerChargingListener() {
        HSChargingManager.getInstance().addChargingListener(mChargingListener);
    }

    private void registerScreenOnOff() {
        final IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_USER_PRESENT);
        HSLog.d(TAG, "init register Screen Off");
        HSApplication.getContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    HSLog.d(TAG, "Screen OFF: " + Commons.isKeyguardLocked(context, false));
                    onScreenOff();
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    HSLog.d(TAG, "Screen ON: " + Commons.isKeyguardLocked(context, false));
                    tryShowChargingScreen();
                } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                    HSLog.d(TAG, "Screen USER_PRESENT");
                    tryShowChargingScreen();
                }
            }
        }, screenFilter);
    }

    private void tryShowChargingScreen() {
        if (((!isChargingScreenExist() && AutoPilotUtils.getLockerMode().equals("normal")) ||
                (!SmartLockerFeedsActivity.exist && AutoPilotUtils.getLockerMode().equals("cableandfuse")) ||
                (!SmartLockerFeedsActivity.exist && AutoPilotUtils.getLockerMode().equals("cable")))
                && blockWhenHasKeyGuard
                && SmartChargingSettings.isChargingScreenEnabled()
                && isCharging()) {
            LockerCustomConfig.getLogger().logEvent("ChargingScreen_Show_OnPresent",
                    "Brand", Build.BRAND.toLowerCase());
            if (AutoPilotUtils.getLockerMode().equals("cableandfuse") || AutoPilotUtils.getLockerMode().equals("cable")) {
                SmartLockerManager.getInstance().tryToPreLoadBaiduNews();
            }
            ChargingScreenUtils.startChargingScreenActivity(false, false);
        }
    }

    private void onScreenOff() {
        launchScreenOnLockers();
    }

    private void launchScreenOnLockers() {
        if (isCharging() && SmartChargingSettings.isChargingScreenEnabled()) {
            notifyToStart(EXTRA_VALUE_CHARGING);
        } else if (LockerSettings.isLockerEnabled()) {
            notifyToStart(EXTRA_VALUE_LOCKER);
        }
    }

    private void notifyToStart(String target) {
        HSLog.d(TAG, "notify : " + target);
        Context context = HSApplication.getContext();
        Intent intent = new Intent();
        intent.setPackage(context.getPackageName());
        intent.putExtra(EXTRA_LAUNCHER_ACTIVITY, target);
        handleStart(intent);
    }

    /**
     * Called in main process.
     */
    private void handleStart(Intent intent) {
        String extraValue = intent.getStringExtra(EXTRA_LAUNCHER_ACTIVITY);

        if (EXTRA_VALUE_CHARGING.equals(extraValue)) {
            if ((AutoPilotUtils.getLockerMode().equals("cableandfuse") || AutoPilotUtils.getLockerMode().equals("cable"))
                    ? !SmartLockerFeedsActivity.exist : !isChargingScreenExist()) {
                blockWhenHasKeyGuard = true;
                if (AutoPilotUtils.getLockerMode().equals("cableandfuse") || AutoPilotUtils.getLockerMode().equals("cable")) {
                    SmartLockerManager.getInstance().tryToPreLoadBaiduNews();
                }
                ChargingScreenUtils.startChargingScreenActivity(false, false);
            }
        } else if (EXTRA_VALUE_LOCKER.equals(extraValue)) {
            if ((AutoPilotUtils.getLockerMode().equals("cableandfuse") || AutoPilotUtils.getLockerMode().equals("fuse"))
                    ? !SmartLockerFeedsActivity.exist : !isLockScreenExist()) {
                if (AutoPilotUtils.getLockerMode().equals("cableandfuse") || AutoPilotUtils.getLockerMode().equals("fuse")) {
                    SmartLockerManager.getInstance().tryToPreLoadBaiduNews();
                }
                ChargingScreenUtils.startLockerActivity(false);
            }
        }
    }

    private boolean isLockScreenExist() {
        return LockerActivity.exist || FloatWindowController.getInstance().isLockScreenShown();
    }

    private boolean isChargingScreenExist() {
        return ChargingScreenActivity.exist || FloatWindowController.getInstance().isLockScreenShown();
    }

    private boolean isCharging() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        Intent intent;
        try {
            intent = HSApplication.getContext().registerReceiver(null, intentFilter);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (intent == null) {
            HSApplication.getContext().unregisterReceiver(mBatteryChangeReceiver);
            return false;
        }

        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
    }

    public void onScreenDisplayed() {
        blockWhenHasKeyGuard = false;
    }

    private class BatteryChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // do nothing
            // add this for HuaWei device: register too many Broadcast Receivers
        }
    }
}
