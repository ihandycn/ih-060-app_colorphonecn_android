package com.honeycomb.colorphone;

import com.honeycomb.colorphone.util.LauncherAnalytics;

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
            LauncherAnalytics.logEvent("ColorPhone_MainView_Opened");
        }

        @Override
        public void onThemePreviewOpen(String name) {
            LauncherAnalytics.logEvent("ColorPhone_ThemeDetail_View", "ThemeName", name);
        }

        @Override
        public void onChooseTheme(String name, String from) {
            LauncherAnalytics.logEvent("ColorPhone_ChooseTheme", "ThemeName", name, "from", from);
        }

        @Override
        public void onThemeDownloadStart(String name, String from) {
            LauncherAnalytics.logEvent("ColorPhone_Theme_Download_Started", "ThemeName", name, "from", from);
        }

        @Override
        public void onThemeDownloadFinish(String name) {
            boolean firstDownload = downloadThemes.add(name);
            if (firstDownload) {
                LauncherAnalytics.logEvent("ColorPhone_Theme_Download_Finished", "ThemeName", name);
            }
        }

        @Override
        public void onFeedBackClick() {
            LauncherAnalytics.logEvent("ColorPhone_Feedback_Clicked");
        }

        @Override
        public void onColorPhoneEnableFromSetting(boolean enabled) {
            LauncherAnalytics.logEvent(enabled ? "ColorPhone_Enabled_FromSettings" : "ColorPhone_Disabled_FromSettings");
        }

        @Override
        public void onCallAssistantEnableFromSetting(boolean enabled) {
            LauncherAnalytics.logEvent(enabled ? "CallAssistant_Enabled_FromSettings" : "CallAssistant_Disabled_FromSettings");
        }
    }

}
