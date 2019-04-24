package com.honeycomb.colorphone.dialer;

import android.os.Build;

import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.Constants;
import com.honeycomb.colorphone.dialer.util.DefaultPhoneUtils;
import com.honeycomb.colorphone.util.Analytics;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.superapps.util.Commons;
import com.superapps.util.Compats;
import com.superapps.util.Preferences;
import com.superapps.util.Threads;

import java.util.List;

/**
 * @author sundxing
 */
public class ConfigEvent {

    public static boolean dialerEnable() {
        if (BuildConfig.DEBUG) {
            return true;
        }
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
        return true;
    }

    public static void guideShow() {
        Preferences.get(Constants.PREF_FILE_DEFAULT).incrementAndGetInt("guide_show_time");
        boolean dialerEnable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ConfigEvent.dialerEnable();
        if (!dialerEnable) {
            return;
        }
        Analytics.logEvent("Dialer_Guide_Show", "Time", String.valueOf(getGuideShowTime()));
    }

    private static int getGuideShowTime() {
        return Preferences.get(Constants.PREF_FILE_DEFAULT).getInt("guide_show_time", 0);
    }

    private static int getSystemGuideShowTime() {
        return Preferences.get(Constants.PREF_FILE_DEFAULT).getInt("guide_sys_show_time", 0);
    }

    public static void guideClose() {
        Analytics.logEvent("Dialer_Guide_Cancel_Click", "Time", String.valueOf(getGuideShowTime()));
    }

    public static void guideConfirmed() {
        Threads.postOnMainThreadDelayed(sCheckPermissionResultRunnable, 5000);
        Preferences.get(Constants.PREF_FILE_DEFAULT).incrementAndGetInt("guide_sys_show_time");
        Analytics.logEvent("Dialer_Guide_Btn_Click", "Time", String.valueOf(getGuideShowTime()));
    }

    public static void monitorResult() {
        Threads.postOnMainThreadDelayed(sCheckPermissionResultRunnable2, 5000);
    }

    private static Runnable sCheckPermissionResultRunnable2 = new Runnable() {
        @Override
        public void run() {
            if (DefaultPhoneUtils.isDefaultPhone()) {
                Analytics.logEvent("Dialer_Set_Default_From_Manual", "Time", String.valueOf(getSystemGuideShowTime()));
            }
        }
    };

    private static Runnable sCheckPermissionResultRunnable = new Runnable() {
        @Override
        public void run() {
            if (DefaultPhoneUtils.isDefaultPhone()) {
                Analytics.logEvent("Dialer_Set_Default_Success", "Time", String.valueOf(getSystemGuideShowTime()));
            }
        }
    };

    public static void successSetAsDefault() {
//        Analytics.logEvent(Analytics.upperFirstCh("ColorPhone_" + "set_default_success"));
    }

    public static void dialerShow() {
        Analytics.logEvent(Analytics.upperFirstCh("dialer_page_show"), "Type",
                Commons.isKeyguardLocked(HSApplication.getContext(), false) ?
                        "Withlock" : "Withoutlock");
    }

}
