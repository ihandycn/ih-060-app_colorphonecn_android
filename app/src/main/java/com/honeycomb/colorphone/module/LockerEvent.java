package com.honeycomb.colorphone.module;

import com.acb.autopilot.AutopilotEvent;
import com.colorphone.lock.LockerCustomConfig;

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
        AutopilotEvent.logTopicEvent("topic-1505294061097", "color_screensaver_show");
    }

    @Override
    public void onEventLockerAdClick() {
        super.onEventChargingAdClick();
        AutopilotEvent.onAdClick();
    }

    @Override
    public void onEventChargingAdShow() {
        super.onEventChargingAdClick();
        AutopilotEvent.onAdShow();
    }

    @Override
    public void onEventChargingAdClick() {
        super.onEventChargingAdClick();
        AutopilotEvent.onAdClick();
    }

    @Override
    public void onEventChargingViewShow() {
        super.onEventChargingAdClick();
    }
}
