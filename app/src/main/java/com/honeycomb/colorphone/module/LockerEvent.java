package com.honeycomb.colorphone.module;

import android.os.Build;

import com.colorphone.lock.LockerCustomConfig;
import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenUtils;
import com.colorphone.lock.lockscreen.locker.Locker;
import com.honeycomb.colorphone.util.Analytics;

import net.appcloudbox.autopilot.AutopilotEvent;

/**
 * Created by sundxing on 2018/1/5.
 */
public class LockerEvent extends LockerCustomConfig.Event {
    @Override
    public void onEventLockerAdShow() {
        super.onEventChargingAdClick();
        AutopilotEvent.onAdShow();
        String suffix = ChargingScreenUtils.isFromPush ? "_Push" : "";
        Analytics.logEvent("ColorPhone_LockScreenAd_Show" + suffix,
                "Brand", Build.BRAND.toLowerCase(), "DeviceVersion", Locker.getDeviceInfo());
    }

    @Override
    public void onEventLockerShow() {
        super.onEventChargingAdClick();
    }

    @Override
    public void onEventLockerAdClick() {
        Analytics.logEvent("ColorPhone_LockScreenAd_Click");
    }

    @Override
    public void onEventChargingAdShow() {
        super.onEventChargingAdClick();
        AutopilotEvent.onAdShow();
        String suffix = ChargingScreenUtils.isFromPush ? "_Push" : "";

        Analytics.logEvent("ChargingScreen_AdShow" + suffix,
                "Brand", Build.BRAND.toLowerCase(), "DeviceVersion", Locker.getDeviceInfo());

    }

    @Override
    public void onEventChargingAdClick() {
        Analytics.logEvent("ColorPhone_ChargingScreenAd_Click");
    }

    @Override
    public void onEventChargingViewShow() {
        super.onEventChargingAdClick();
    }
}
