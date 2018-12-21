package com.honeycomb.colorphone.ad;

import com.honeycomb.colorphone.Ap;
import com.ihs.app.framework.HSApplication;
import com.ihs.commons.config.HSConfig;
import com.ihs.commons.utils.HSLog;

import java.util.List;

public class ConfigSettings {
    public static boolean showAdOnApplyTheme() {
        boolean configEnable = HSConfig.optBoolean(false,"Application", "FullScreen", "ThemeApply");
        boolean autopilotEnable = Ap.DetailAd.enableAdOnApply();
        HSLog.d("ConfigSettings", "AdOnApplyTheme:" + configEnable + "," + autopilotEnable);
        return (configEnable || enabledThisVersion()) && autopilotEnable;
    }

    public static boolean showAdOnDetailView() {
        boolean configEnable = HSConfig.optBoolean(false,"Application", "FullScreen", "DetailView");
        boolean autopilotEnable = Ap.DetailAd.enableAdOnDetailView();
        HSLog.d("ConfigSettings", "AdOnDetailView:" + configEnable + "," + autopilotEnable);
        return (configEnable || enabledThisVersion()) && autopilotEnable;
    }

    private static boolean enabledThisVersion() {
        List<Integer> enableList = (List<Integer>) HSConfig.getList("Application", "FullScreen", "EnableVersionList");
        int versionCode = HSApplication.getFirstLaunchInfo().appVersionCode;
        if (enableList != null && enableList.contains(versionCode)) {
            return true;
        }
        return false;
    }
}
