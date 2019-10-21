package com.acb.libwallpaper.live.customize.theme.data;

import com.acb.libwallpaper.live.customize.CustomizeConfig;

import net.appcloudbox.common.utils.AcbMapUtils;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ShareDataProvider {

    // todo:
    private static final String DEFAULT_LAUNCHER_URL = "https://play.google.com/store/apps/details?id=com.honeycomb.launcher";
    private static final String DEFAUTL_SUBJECT = "Recommend an amazing wallpaper";

    public static List<String> getApps() {
        return (List<String>) CustomizeConfig.getList("Share", "Apps");
    }

    public static String getLauncherUrl() {
        try {
            Map<String, ?> map = CustomizeConfig.getMap("Share");
            return AcbMapUtils.getString(map, "LauncherUrl");
        } catch (Exception e) {
            return DEFAULT_LAUNCHER_URL;
        }
    }

    public static String getSubject() {
        return CustomizeConfig.getString(DEFAUTL_SUBJECT, "Share", "Subject");
    }
}
