package com.honeycomb.colorphone;

import android.os.Build;

import com.honeycomb.colorphone.autopermission.AutoRequestManager;
import com.honeycomb.colorphone.util.Analytics;
import com.honeycomb.colorphone.util.NetUtils;

import net.appcloudbox.autopilot.AutopilotEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by sundxing on 17/7/7.
 */

public class ConfigLogDefault implements ConfigLog {


    @Override
    public Event getEvent() {
        return new FlurryEvent();
    }


    public static class FlurryEvent implements Event {
        private static boolean enabled = false;
        Set<String> downloadThemes = new HashSet<>(3);
        @Override
        public void onMainViewOpen() {
            Analytics.logEvent("ColorPhone_MainView_Opened",
                    "Brand", Build.BRAND.toLowerCase(),
                    "Permission", AutoRequestManager.getMainOpenGrantPermissionString());
            AutopilotEvent.logAppEvent("mainview_opened");
        }

        @Override
        public void onThemePreviewOpen(String name) {
            Analytics.logEvent("ColorPhone_ThemeDetail_View", "ThemeName", name, "From", "MainView");
        }

        @Override
        public void onChooseTheme(String name, String from) {
            Analytics.logEvent("ColorPhone_ChooseTheme", "ThemeName", name, "from", from);
        }

        @Override
        public void onThemeDownloadStart(String name, String from) {
            Analytics.logEvent("ColorPhone_Theme_Download_Started", "ThemeName", name, "from", from,
             "Network", NetUtils.getNetWorkStateName());
        }

        @Override
        public void onThemeDownloadFinish(String name) {
            boolean firstDownload = downloadThemes.add(name);
            if (firstDownload) {
                Analytics.logEvent("ColorPhone_Theme_Download_Finished", "ThemeName", name,
                        "Network",  NetUtils.getNetWorkStateName());

            }
        }

        @Override
        public void onFeedBackClick() {
            Analytics.logEvent("ColorPhone_Feedback_Clicked");
        }

        @Override
        public void onColorPhoneEnableFromSetting(boolean enabled) {
            Analytics.logEvent(enabled ? "ColorPhone_Enabled_FromSettings" : "ColorPhone_Disabled_FromSettings");
        }

        @Override
        public void onCallAssistantEnableFromSetting(boolean enabled) {
            Analytics.logEvent(enabled ? "CallAssistant_Enabled_FromSettings" : "CallAssistant_Disabled_FromSettings");
        }
    }

}
