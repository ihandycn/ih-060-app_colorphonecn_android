package com.honeycomb.colorphone;

import android.text.TextUtils;

import com.ihs.commons.config.HSConfig;
import com.superapps.util.Compats;
import com.superapps.util.Threads;

public class AppflyerLogger {
    public static void logAppOpen() {
        Threads.postOnMainThreadDelayed(new Runnable() {
            @Override
            public void run() {
                if (isTargetRom()) {
//                    HSAnalytics.logEventToAppsFlyer("App_Open_New");
                    String level = HSConfig.optString("not_configured", "UserLevel");
                    if (TextUtils.isDigitsOnly(level)) {
                        boolean non = level.equals("5") || level.equals("8");
                        if (non) {
//                            HSAnalytics.logEventToAppsFlyer("App_Open_New_NonOrganic");
                        }
                    }
                }
            }
        }, 10 * 1000);
    }

    private static boolean isTargetRom() {
        return ! (Compats.IS_HUAWEI_DEVICE
                || Compats.IS_OPPO_DEVICE
                || Compats.IS_VIVO_DEVICE
                || Compats.IS_XIAOMI_DEVICE
                || Compats.IS_LENOVO_DEVICE);
    }
}
