package com.honeycomb.colorphone.themeselector;

import android.text.format.DateUtils;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

public class ThemeGuideTest {
    private static final String THEME_TEST_TOPICID = "topic-6yb8007c4";

    public static boolean isApplyButtonShow() {
        return AutopilotConfig.getBooleanToTestNow(THEME_TEST_TOPICID, "apply_button_show", false);
    }

    public static boolean isThemeGuideShow() {
        return AutopilotConfig.getBooleanToTestNow(THEME_TEST_TOPICID, "themeguideshow", false);
    }

    public static int getMaxTime() {
        return (int) AutopilotConfig.getDoubleToTestNow(THEME_TEST_TOPICID, "themeguide_maxtime", 0);
    }

    public static long getInterval() {
        return (int) AutopilotConfig.getDoubleToTestNow(THEME_TEST_TOPICID, "themeguide_interval_hour", 0) * DateUtils.HOUR_IN_MILLIS;
    }

    public static int getIntervalAfterApplySuccess() {
        return (int) AutopilotConfig.getDoubleToTestNow(THEME_TEST_TOPICID, "themeguide_interval_applysuccess", 0);
    }

    public static void logThemewireADShow() {
        isThemeGuideShow();
        AutopilotEvent.logTopicEvent(THEME_TEST_TOPICID, "colorphone_themewiread_show");
    }

    public static void logThemeGuideShow() {
        isThemeGuideShow();
        AutopilotEvent.logTopicEvent(THEME_TEST_TOPICID, "colorphone_calassistant_themeguide_show");
    }

    public static void logThemeGuideDetailShow() {
        isThemeGuideShow();
        AutopilotEvent.logTopicEvent(THEME_TEST_TOPICID, "colorphone_themedetail_view_fromthemeguide");
    }

    public static void logThemeGuideApply() {
        isThemeGuideShow();
        AutopilotEvent.logTopicEvent(THEME_TEST_TOPICID, "colorphone_choosetheme_fromthemeguide");
    }

    public static void logThemeGuideThemeClicked() {
        isThemeGuideShow();
        AutopilotEvent.logTopicEvent(THEME_TEST_TOPICID, "colorphone_callassistant_themeguide_theme_click");
    }

    public static void logThemeGuideMoreClicked() {
        isThemeGuideShow();
        AutopilotEvent.logTopicEvent(THEME_TEST_TOPICID, "colorphone_callassistant_themeguide_button_click");
    }
}
