package com.honeycomb.colorphone;

import com.ihs.app.analytics.HSAnalytics;

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
            HSAnalytics.logEvent("ColorPhone_MainView_Opened");
        }

        @Override
        public void onThemePreviewOpen(String name) {
            HSAnalytics.logEvent("ColorPhone_ThemeDetail_View", "ThemeName", name);
        }

        @Override
        public void onChooseTheme(String name, String from) {
            HSAnalytics.logEvent("ColorPhone_ChooseTheme", "ThemeName", name, "from", from);
        }

        @Override
        public void onThemeDownloadStart(String name, String from) {
            HSAnalytics.logEvent("ColorPhone_Theme_Download_Started", "ThemeName", name, "from", from);
        }

        @Override
        public void onThemeDownloadFinish(String name) {
            boolean firstDownload = downloadThemes.add(name);
            if (firstDownload) {
                HSAnalytics.logEvent("ColorPhone_Theme_Download_Finished", "ThemeName", name);
            }
        }

        @Override
        public void onFeedBackClick() {
            HSAnalytics.logEvent("ColorPhone_Feedback_Clicked");
        }

        @Override
        public void onColorPhoneEnableFromSetting(boolean enabled) {
            HSAnalytics.logEvent(enabled ? "ColorPhone_Enabled_FromSettings" : "ColorPhone_Disabled_FromSettings");
        }

        @Override
        public void onCallAssistantEnableFromSetting(boolean enabled) {
            HSAnalytics.logEvent(enabled ? "CallAssistant_Enabled_FromSettings" : "CallAssistant_Disabled_FromSettings");
        }
    }

}
