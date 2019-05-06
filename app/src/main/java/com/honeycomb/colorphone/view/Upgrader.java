package com.honeycomb.colorphone.view;

import com.ihs.app.framework.HSApplication;

public class Upgrader {

    private static final int VERSION_1_0_7 = 11;

    public static void upgrade() {
        HSApplication.HSLaunchInfo lastLaunch = HSApplication.getLastLaunchInfo();
        int oldVersion = lastLaunch == null ? 1 : lastLaunch.appVersionCode;
        int newVersion = HSApplication.getCurrentLaunchInfo().appVersionCode;

        if (oldVersion <= VERSION_1_0_7 && newVersion > VERSION_1_0_7) {
//            SmartChargingSettings.setChargingReportUserEnabled(ChargingScreenSettings.isChargingScreenUserEnabled());
        }
    }
}
