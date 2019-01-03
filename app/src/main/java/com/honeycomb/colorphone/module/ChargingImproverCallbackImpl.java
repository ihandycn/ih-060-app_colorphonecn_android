package com.honeycomb.colorphone.module;

import com.ihs.chargingimprover.ChargingImproverCallBack;
import com.ihs.device.clean.memory.HSAppMemory;

import java.util.List;

public class ChargingImproverCallbackImpl implements ChargingImproverCallBack {
    @Override
    public void onClickWhenBatteryScanning() {

    }

    @Override
    public void onClickWhenBatteryScanFinished(boolean hasDrainingApps, List<HSAppMemory> appList) {

    }

    @Override
    public boolean isCleanTimeFrozen() {
        return false;
    }

    @Override
    public boolean isImproverNewUser() {
        return true;
    }

    @Override
    public boolean isImproverEnabled() {
        return true;
    }

    @Override
    public void onImproverShow() {

    }

    @Override
    public void onImproveButtonClick() {

    }

    @Override
    public void onLaterButtonClick() {

    }

    @Override
    public boolean allowAlertBack() {
        return false;
    }

    @Override
    public int intervalInMinutes() {
        return 0;
    }

    @Override
    public int afterInstallInHour() {
        return 0;
    }

    @Override
    public int minAlertShowRate() {
        return 0;
    }

    @Override
    public void logEvent(String eventID, String... vars) {

    }

    @Override
    public void onChargingImproverDismiss() {

    }
}
