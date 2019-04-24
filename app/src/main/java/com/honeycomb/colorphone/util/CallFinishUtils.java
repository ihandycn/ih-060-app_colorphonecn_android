package com.honeycomb.colorphone.util;

import android.os.Build;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;

import java.util.List;

public class CallFinishUtils {

    public static boolean isCallFinishFullScreenAdEnabled() {
        boolean configEnable = HSConfig.optBoolean(false, "Application", "CallFinishWire", "Enable")
                || enabledThisVersion();

        HSLog.d("CallFinish", "config enable : " + configEnable );
        return configEnable && enabledThisBrand();
    }

    private static boolean enabledThisVersion() {
        List<Integer> enableList = (List<Integer>) HSConfig.getList("Application", "CallFinishWire", "EnableVersionList");
        int versionCode = HSApplication.getFirstLaunchInfo().appVersionCode;
        if (enableList != null && enableList.contains(versionCode)) {
            return true;
        }
        return false;
    }

    private static boolean enabledThisBrand() {
        List<String> enableList = (List<String>) HSConfig.getList("Application", "CallFinishWire", "EnableBrandList");
        String brand = Build.BRAND;
        if (enableList != null) {
            for (String b : enableList) {
                if (brand.equalsIgnoreCase(b)) {
                    return true;
                }
            }
        }
        return false;
    }

}
