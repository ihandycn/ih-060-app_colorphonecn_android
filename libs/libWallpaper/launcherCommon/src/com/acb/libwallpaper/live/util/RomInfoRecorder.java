package com.acb.libwallpaper.live.util;

import com.acb.libwallpaper.live.LauncherAnalytics;
import com.acb.libwallpaper.live.model.LauncherFiles;
import com.superapps.util.Compats;
import com.superapps.util.Preferences;

/**
 * Created by jelly on 2018/12/5.
 */

public class RomInfoRecorder {
    private static final String PREF_KEY_ROM_INFO_RECORD_ONCE = "PREF_KEY_ROM_INFO_RECORD_ONCE";

    public static void recordRomInfo() {
        Preferences.get(LauncherFiles.RECORD_USE_TIME_PREFS).doOnce(() -> {
            if (Compats.IS_HUAWEI_DEVICE) {
                LauncherAnalytics.logEvent("huawei_emui_version_code_new",
                        "version", String.valueOf(Compats.getHuaweiEmuiVersionCode()));
            }

            if (Compats.IS_OPPO_DEVICE) {
                LauncherAnalytics.logEvent("oppo_coloros_version_code",
                        "version", String.valueOf(Compats.getOppoVersionName()));
            }

            if (Compats.IS_VIVO_DEVICE) {
                String version = "";
                version += String.valueOf(Compats.getVivoVersionName()) + (Compats.isFuntouchLite() ? "lite" : "");
                LauncherAnalytics.logEvent("vivo_os_version_code",true,
                        "version", version);
            }

            if (Compats.IS_XIAOMI_DEVICE) {
                LauncherAnalytics.logEvent("miui_version_code",
                        "version", String.valueOf(Compats.getMiuiVersionName()));

                LauncherAnalytics.logEvent("Device_BrandCheck_Xiaomi", true);
            }
        }, PREF_KEY_ROM_INFO_RECORD_ONCE);

    }
}
