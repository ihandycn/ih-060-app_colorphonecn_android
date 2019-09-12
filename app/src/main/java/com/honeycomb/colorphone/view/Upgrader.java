package com.honeycomb.colorphone.view;

import android.content.ComponentName;
import android.content.pm.PackageManager;

import com.colorphone.lock.lockscreen.chargingscreen.ChargingScreenSettings;
import com.colorphone.lock.lockscreen.chargingscreen.SmartChargingSettings;
import com.ihs.app.framework.HSApplication;

public class Upgrader {

    private static final int VERSION_1_0_7 = 11;

    public static void upgrade() {
        HSApplication.HSLaunchInfo lastLaunch = HSApplication.getLastLaunchInfo();
        int oldVersion = lastLaunch == null ? 1 : lastLaunch.appVersionCode;
        int newVersion = HSApplication.getCurrentLaunchInfo().appVersionCode;

        if (oldVersion <= VERSION_1_0_7 && newVersion > VERSION_1_0_7) {
            SmartChargingSettings.setChargingReportUserEnabled(ChargingScreenSettings.isChargingScreenUserEnabled());
        }

        if (oldVersion <= 160 && newVersion >= 161) {
            ComponentName thisComponent = new ComponentName(HSApplication.getContext().getPackageName(), /*getClass()*/ "com.honeycomb.colorphone.notification.NotificationServiceV18");
            PackageManager pm = HSApplication.getContext().getPackageManager();
            pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }
}
