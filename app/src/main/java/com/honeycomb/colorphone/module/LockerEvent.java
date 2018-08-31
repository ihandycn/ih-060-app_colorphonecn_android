package com.honeycomb.colorphone.module;

import com.colorphone.lock.LockerCustomConfig;

import net.appcloudbox.autopilot.AutopilotEvent;

/**
 * Created by sundxing on 2018/1/5.
 */
public class LockerEvent extends LockerCustomConfig.Event {
    @Override
    public void onEventLockerAdShow() {
        super.onEventChargingAdClick();
        AutopilotEvent.onAdShow();
    }

    @Override
    public void onEventLockerShow() {
        super.onEventChargingAdClick();
    }

    @Override
    public void onEventLockerAdClick() {
        super.onEventChargingAdClick();
    }

    @Override
    public void onEventChargingAdShow() {
        super.onEventChargingAdClick();
        AutopilotEvent.onAdShow();
    }

    @Override
    public void onEventChargingAdClick() {
        super.onEventChargingAdClick();

    }

    @Override
    public void onEventChargingViewShow() {
        super.onEventChargingAdClick();
    }
}
