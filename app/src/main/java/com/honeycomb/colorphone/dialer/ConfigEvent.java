package com.honeycomb.colorphone.dialer;

import android.os.Build;

import com.honeycomb.colorphone.util.Analytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Commons;
import com.superapps.util.Compats;

import java.util.List;

/**
 * @author sundxing
 */
public class ConfigEvent {

    public static boolean dialerEnable() {
        String path = null;
        if (Compats.IS_XIAOMI_DEVICE) {
            path = "Xiaomi";
        } else if (Compats.IS_HUAWEI_DEVICE) {
            path = "Huawei";
        } else {
            return false;
        }
        boolean masterConfig = HSConfig.optBoolean(false,"Application", "Dialer", path, "Enable");

        return masterConfig || enabledOnOSVersion("Application", "Dialer", path, "EnabledOsVersion");
    }


    private static boolean enabledOnOSVersion(String... path) {
        List<Integer> enableList = (List<Integer>) HSConfig.getList(path);
        int versionCode = Build.VERSION.SDK_INT;
        if (enableList != null && enableList.contains(versionCode)) {
            return true;
        }
        return false;
    }

    public static boolean setDefaultGuideShow() {
        return false;
    }

    public static void guideShow() {
        boolean dialerEnable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ConfigEvent.dialerEnable();
        if (!dialerEnable) {
            return;
        }
        Analytics.logEvent("Dialer_Guide_Show", "Time", String.valueOf(getShowTime()));
    }

    private static int getShowTime() {
        // TODO
        return 0;
    }

    public static void guideConfirmed() {
        boolean dialerEnable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ConfigEvent.dialerEnable();
        if (!dialerEnable) {
            return;
        }
        Analytics.logEvent(Analytics.upperFirstCh("ColorPhone_" + "set_default_guide_set_clicked"));
    }

    public static void successSetAsDefault() {
        Analytics.logEvent(Analytics.upperFirstCh("ColorPhone_" + "set_default_success"));
    }

    public static void dialerShow() {
        Analytics.logEvent(Analytics.upperFirstCh("dialer_page_show"), "Type",
                Commons.isKeyguardLocked(HSApplication.getContext(), false) ?
                        "Withlock" : "Withoutlock");
    }

}
