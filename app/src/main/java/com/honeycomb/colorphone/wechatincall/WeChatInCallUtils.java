package com.honeycomb.colorphone.wechatincall;

import android.content.Context;

import com.acb.call.constant.ScreenFlashConst;
import com.acb.call.customize.ScreenFlashSettings;
import com.acb.call.wechat.WeChatInCallManager;
import com.honeycomb.colorphone.Theme;
import com.honeycomb.colorphone.download.TasksManager;
import com.honeycomb.colorphone.download.TasksManagerModel;
import com.ihs.commons.notificationcenter.HSGlobalNotificationCenter;
import com.ihs.commons.utils.HSBundle;
import com.superapps.util.Preferences;

import com.acb.call.utils.PermissionHelper;
import com.honeycomb.colorphone.util.Analytics;
import com.superapps.util.Permissions;

public class WeChatInCallUtils {
    public static final String PREFS_SCREEN_FLASH_WE_CHAT_THEME_VIDEO_FILE_NAME = "prefs_screen_flash_we_chat_theme_video_file_name";
    private static final String PREFS_WE_CHAT_THEME_SWITCH = "prefs_we_chat_theme_switch";

    public static final String NOTIFICATION_REFRESH_WE_CHAT_UI = "notification_refresh_we_chat_ui";
    public static final String HS_BUNDLE_WE_CHAT_THEME_ENABLE = "hs_bundle_we_chat_theme_enable";

    public static void applyWeChatInCallTheme(Theme theme, boolean isVideoSound) {
        TasksManagerModel tasksManagerModel = TasksManager.getImpl().requestRingtoneTask(theme);
        if (isVideoSound && tasksManagerModel != null) {
            String path = tasksManagerModel.getPath();
            Preferences.getDefault().putString(WeChatInCallManager.PREFS_SCREEN_FLASH_WE_CHAT_RING_TONE_PATH, path);
        } else {
            Preferences.getDefault().putString(WeChatInCallManager.PREFS_SCREEN_FLASH_WE_CHAT_RING_TONE_PATH, "");
        }

        Preferences.getDefault().putString(PREFS_SCREEN_FLASH_WE_CHAT_THEME_VIDEO_FILE_NAME, theme.getFileName());
        ScreenFlashSettings.putInt(ScreenFlashConst.PREFS_SCREEN_FLASH_WE_CHAT_THEME_ID, theme.getId());
    }

    public static String getWeChatInCallThemeName() {
        return Preferences.getDefault().getString(PREFS_SCREEN_FLASH_WE_CHAT_THEME_VIDEO_FILE_NAME, "");
    }

    public static void setWeChatThemeSwitch(boolean isEnable) {
        Preferences.getDefault().putBoolean(PREFS_WE_CHAT_THEME_SWITCH, isEnable);
        sendNotificationRefreshWeChatUi(isEnable);
    }

    public static void sendNotificationRefreshWeChatUi(boolean isEnable) {
        HSBundle bundle = new HSBundle();
        bundle.putBoolean(HS_BUNDLE_WE_CHAT_THEME_ENABLE, isEnable);
        HSGlobalNotificationCenter.sendNotification(WeChatInCallUtils.NOTIFICATION_REFRESH_WE_CHAT_UI, bundle);
    }

    public static boolean isWeChatThemeEnable() {
        return Preferences.getDefault().getBoolean(PREFS_WE_CHAT_THEME_SWITCH, true);
    }

    public static void checkPermissionAndRequest(Context context, Runnable afterSuccess) {
        if(!Permissions.isFloatWindowAllowed(context)){
            Permissions.requestFloatWindowPermission(context);
            return;
        }

        if(Permissions.isNotificationAccessGranted()){
            if(afterSuccess !=  null){
                afterSuccess.run();
            }
        }else {
            PermissionHelper.requestNotificationPermission(() -> {
                if(afterSuccess !=  null){
                    afterSuccess.run();
                }
            });
        }
    }

    public static class Event{

        public static final String WE_CHAT_IN_CALL_BUTTON_SHOW = "WechatFlash_Button_Show";
        public static final String WE_CHAT_IN_CALL_BUTTON_CLICK = "WechatFlash_Button_Click";
        public static final String WE_CHAT_IN_CALL_SET_SUCCESS = "WechatFlash_Set_Success";

        public static void log(String eventName){
            log(eventName, (String) null);
        }

        public static void log(String eventName,String...vars){
            if(vars == null){
                Analytics.logEvent(eventName);
            }else {
                Analytics.logEvent(eventName,vars);
            }
            eventName = eventName.toLowerCase();
            WeChatInCallAutopilot.logEvent(eventName);
        }
    }
}
