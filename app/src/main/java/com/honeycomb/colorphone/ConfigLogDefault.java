package com.honeycomb.colorphone;

import com.ihs.app.analytics.HSAnalytics;
import com.ihs.commons.config.HSConfig;

import java.util.List;

/**
 * Created by sundxing on 17/7/7.
 */

public class ConfigLogDefault implements ConfigLog {

    @Override
    public List<String> getHotThemeList() {
        return (List<String>) HSConfig.getList("Application", "Theme", "HotTheme");
    }

    @Override
    public boolean isAssistantEnabledDefault() {
        return HSConfig.optBoolean(false, "Application", "CallAssistant", "DefaultEnabled");
    }

    @Override
    public Event getEvent() {
        return new FlurryEvent();
    }


    class FlurryEvent implements Event {
        @Override
        public void onMainViewOpen() {
            HSAnalytics.logEvent("ColorPhone_MainView_Opened");
        }

        @Override
        public void onThemePreviewOpen(String name) {
            HSAnalytics.logEvent("ColorPhone_ThemeDetail_View", "type", name);
        }

        @Override
        public void onChooseTheme(String name) {
            HSAnalytics.logEvent("ColorPhone_ChooseTheme", "type", name);
        }

        @Override
        public void onThemeDownloadStart(String name) {
            HSAnalytics.logEvent("ColorPhone_Theme_Download_Started", "type", name);
        }

        @Override
        public void onThemeDownloadFinish(String name) {
            HSAnalytics.logEvent("ColorPhone_Theme_Download_Finished", "type", name);
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
