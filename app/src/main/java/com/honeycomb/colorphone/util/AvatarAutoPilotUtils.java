package com.honeycomb.colorphone.util;

import com.honeycomb.colorphone.BuildConfig;
import com.honeycomb.colorphone.activity.AvatarVideoActivity;
import com.ihs.commons.utils.HSLog;

import net.appcloudbox.autopilot.AutopilotConfig;
import net.appcloudbox.autopilot.AutopilotEvent;

/**
 * Created by zhewang on 23/01/2018.
 */

public class AvatarAutoPilotUtils {
    public static final String HEAD_NAME = "facemoji";
    public static final String CAMERA_NAME = "live01";
    public static final String CAMERA_NAME_2 = "live02";

    public static final String ZMOJI_NAME = "zmoji";

    public static String CAMERA_PKG_NAME = "com.camera.beautycam";
    public static String HEAD_PKG_NAME = "com.jibjab.android.messages.fbmessenger";
    public static String ZMOJI_PKG_NAME = "com.futurebits.zmoji.free";

    private static final String AVATAR_TEST_TOPIC_ID = "topic-1516620266175-105";
    private static final boolean DEBUG_TEST = false && BuildConfig.DEBUG;

    public static boolean isAvatarBtnShow() {
        boolean enable = AutopilotConfig.getBooleanToTestNow(AVATAR_TEST_TOPIC_ID, "avatar_button_show", false);
        HSLog.i(AvatarVideoActivity.TAG, "Debug isAvatarBtnShow == " + enable);
        return DEBUG_TEST || enable;
    }

    public static String getAvatarType() {
        String type = AutopilotConfig.getStringToTestNow(AVATAR_TEST_TOPIC_ID, "avatar_type", AvatarAutoPilotUtils.HEAD_NAME);
        HSLog.i(AvatarVideoActivity.TAG, "Debug getAvatarType == " + type);
        return DEBUG_TEST ? AvatarAutoPilotUtils.HEAD_NAME : type;
    }

    public static void logAvatarButtonShown() {
        HSLog.i(AvatarVideoActivity.TAG, "avatar_button_shown");
        AutopilotEvent.logTopicEvent(AVATAR_TEST_TOPIC_ID, "avatar_button_shown");

        Analytics.logEvent("Colorphone_AvatarButton_Shown", "AvatarType", AvatarAutoPilotUtils.getAvatarType());
    }

    public static void logAvatarViewShown() {
        HSLog.i(AvatarVideoActivity.TAG, "avatar_view_shown");
        AutopilotEvent.logTopicEvent(AVATAR_TEST_TOPIC_ID, "avatar_view_shown");

        Analytics.logEvent("Colorphone_AvatarView_Shown", "AvatarType", AvatarAutoPilotUtils.getAvatarType());
    }

    public static void logAvatarViewBackButtonClicked() {
        HSLog.i(AvatarVideoActivity.TAG, "avatar_view_back_button_clicked");
        AutopilotEvent.logTopicEvent(AVATAR_TEST_TOPIC_ID, "avatar_view_back_button_clicked");

        Analytics.logEvent("Colorphone_AvatarView_Back_Clicked", "AvatarType", AvatarAutoPilotUtils.getAvatarType());
    }

    public static void logAvatarViewInstallButtonClicked() {
        HSLog.i(AvatarVideoActivity.TAG, "avatar_view_install_button_clicked");
        AutopilotEvent.logTopicEvent(AVATAR_TEST_TOPIC_ID, "avatar_view_install_button_clicked");

        Analytics.logEvent("Colorphone_AvatarView_Install_Clicked", "AvatarType", AvatarAutoPilotUtils.getAvatarType());
    }

    public static void dump() {
        isAvatarBtnShow();
        getAvatarType();
    }
}
